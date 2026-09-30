package com.appruntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IBM MQ runtime — maps MQ API calls to Spring JMS.
 *
 * <p>Supports MQOPEN, MQGET, MQPUT (pre-opened queue), MQPUT1 (one-shot), MQCLOSE. Params follow
 * IBM MQ spec layouts — BY REFERENCE write-back for output params only.
 *
 * <p><b>Hai dạng descriptor</b> mà runtime phải nhận (2026-08-21):
 *
 * <ol>
 *   <li><b>Ảnh byte của group</b> — dạng generator ĐANG sinh: {@code Utility.groupToString(MQOD)}
 *       trả về ảnh 108 byte {@code "OD " + VERSION(4B) + OBJECTTYPE(4B) + OBJECTNAME(48) +
 *       OBJECTQMGRNAME(48)}. Đọc theo offset cố định của chuẩn MQ.
 *   <li><b>POJO {@code @CobolField}</b> — dạng accessor cũ (trước Schema-v2) và dạng test tự dựng:
 *       lấy String field lớn nhất (MQOD-OBJECTNAME PIC X(48) là lớn nhất).
 * </ol>
 *
 * Trước đây nhánh String bị coi là "đã là tên queue" và chỉ {@code trim()} ⇒ với ảnh group thì tên
 * queue thu được là CẢ 108 byte. Bộ test chuẩn online (CG-01/02/04) chỉ ra chỗ này.
 *
 * <p><b>Giới hạn còn lại của dạng (1)</b>: String là bất biến nên MỌI đường ghi-trả-về vào
 * descriptor đều không thực hiện được ({@code writeBackMsgDesc} → MQMD-MSGID/CORRELID/FORMAT), và
 * các field BINARY trong ảnh (MQGMO-WAITINTERVAL, MQGMO-OPTIONS) không giải mã tin cậy được sau khi
 * đã qua charset. Runtime KHÔNG đoán — nó cảnh báo một lần để chỗ mất mát nhìn thấy được; sửa gốc
 * phải ở phía emit (truyền object group thay vì ảnh chuỗi).
 */
public class MqRunner implements MqService {

    private static final Logger log = LoggerFactory.getLogger(MqRunner.class);

    /** MQ Reason Codes — IBM MQ spec constants (in addition to MqService interface constants) */
    private static final int MQRC_HOBJ_ERROR = 2019;

    private static final int MQRC_UNKNOWN_OBJECT_NAME = 2085;

    /** MQGMO_ACCEPT_TRUNCATED_MSG — bit trong MQGMO-OPTIONS (chuẩn IBM MQ). */
    private static final int MQGMO_ACCEPT_TRUNCATED_MSG = 0x40;

    /** MQGMO_WAIT — bit "có chờ" trong MQGMO-OPTIONS. Không có bit này = MQGMO_NO_WAIT. */
    private static final int MQGMO_WAIT = 0x01;

    /** MQWI_UNLIMITED — {@code MQGMO-WAITINTERVAL = -1} nghĩa là chờ vô hạn. */
    private static final int MQWI_UNLIMITED = -1;

    /**
     * Ân hạn cho MQGET <b>không chờ</b> — KHÔNG phải "chờ message" mà là thời gian để consumer JMS
     * kịp đăng ký với broker.
     *
     * <p><b>Vì sao phải có</b>: MQGET no-wait của IBM MQ là phép kiểm ĐỒNG BỘ trên trạng thái queue
     * — có message hợp lệ thì trả về, không thì {@code MQRC 2033}. Bên JMS thì {@code
     * MessageConsumer.receive(t)} phải chờ broker <i>dispatch</i>; consumer vừa tạo cần một
     * round-trip đăng ký trước đã. Map no-wait thành {@code receive(1ms)} ⇒ message ĐANG nằm trên
     * queue vẫn cho ra 2033, và chương trình COBOL rẽ nhầm sang nhánh "không có message".
     *
     * <p><b>Vì sao 50</b>: đo trên máy phát triển, mỗi giá trị một JVM nguội, message đã nằm sẵn
     * trên queue — {@code timeout=1ms} MISS (riêng phần dựng consumer đã 13ms), từ {@code 5ms} trở
     * lên HIT. 50ms là một bậc độ lớn dự phòng cho máy chậm hơn và cho broker ở xa (dispatch đầu
     * tiên là round-trip mạng chứ không phải trong cùng JVM).
     *
     * <p><b>Đánh đổi, nói rõ</b>: message đến TRONG khoảng ân hạn sẽ được trả về, trong khi MQ thật
     * đã báo 2033. Chọn chiều này có chủ đích — bỏ sót message đang có làm ĐỔI LUỒNG chương trình,
     * còn nhận sớm một message thì vòng poll kế tiếp cũng lấy nó thôi.
     */
    private static final long NO_WAIT_CONSUMER_SETUP_MS = 50;

    /**
     * Thân message: TextMessage lấy thẳng, còn lại nhờ MessageConverter đã cấu hình (giữ đúng hành
     * vi của receiveAndConvert trước đây).
     */
    private Object bodyOf(jakarta.jms.Message m) {
        try {
            if (m instanceof jakarta.jms.TextMessage tm) {
                return tm.getText();
            }
            return jmsTemplate.getMessageConverter().fromMessage(m);
        } catch (Exception e) {
            log.warn("MQGET: không đọc được thân message ({}) — dùng toString()", e.getMessage());
            return m.toString();
        }
    }

    /** JMSMessageID (msgId=true) hoặc JMSCorrelationID; null nếu không có/không đọc được. */
    private String jmsHeader(jakarta.jms.Message m, boolean msgId) {
        if (m == null) {
            return null;
        }
        try {
            return msgId ? m.getJMSMessageID() : m.getJMSCorrelationID();
        } catch (jakarta.jms.JMSException e) {
            return null;
        }
    }

    /** Đọc một field của descriptor khi nó là tham chiếu group; null với ảnh chuỗi/POJO. */
    private String descField(Object desc, String field) {
        return (desc instanceof GroupRef g) ? g.get(field) : null;
    }

    /**
     * Tên field CHUẨN của IBM trong copybook CMQODV/CMQMDV/CMQGMOV. Cùng hạng với MQCC/MQRC ở trên
     * — do IBM cấp, KHÔNG phải dữ liệu của khách nào. Dùng khi tham số tới dưới dạng {@link
     * GroupRef} (có layout ⇒ truy cập được theo tên, không cần offset).
     */
    private static final String F_OBJECTNAME = "MQOD-OBJECTNAME";

    private static final String F_MSGID = "MQMD-MSGID";
    private static final String F_CORRELID = "MQMD-CORRELID";
    private static final String F_FORMAT = "MQMD-FORMAT";
    private static final String F_WAITINTERVAL = "MQGMO-WAITINTERVAL";
    private static final String F_GMO_OPTIONS = "MQGMO-OPTIONS";

    /**
     * Offset cố định trong ảnh MQOD (chuẩn IBM MQ): STRUCID 0..4, VERSION 4..8, OBJECTTYPE 8..12,
     * OBJECTNAME 12..60, OBJECTQMGRNAME 60..108. CHỈ dùng cho đường tương thích (tham số còn tới
     * dưới dạng ảnh chuỗi).
     */
    private static final String MQOD_STRUCID = "OD";

    private static final int MQOD_OBJECTNAME_AT = 12;
    private static final int MQOD_OBJECTNAME_END = 60;

    /** Cảnh báo một lần cho mỗi loại mất mát — đừng spam mỗi MQGET. */
    private final java.util.Set<String> warned = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private void warnOnce(String key, String message) {
        if (warned.add(key)) {
            log.warn(message);
        }
    }

    private final JmsTemplate jmsTemplate;

    /**
     * Object handle đang mở của TASK hiện tại: handle → tên queue.
     *
     * <p>Per-task chứ không dùng chung: trong CICS, HOBJ do MQOPEN cấp thuộc về task và được trả
     * lại khi task kết thúc — task khác không với tới được. Trước 2026-08-22 đây là map toàn cục,
     * nên (a) task abend trước MQCLOSE để lại entry sống mãi, (b) một handle của task này về nguyên
     * tắc vẫn dùng được từ task kia. Nay handle của task khác cho ra {@code MQRC_HOBJ_ERROR}, đúng
     * như MQ thật.
     *
     * <p>{@code handleCounter} vẫn TOÀN CỤC (không đổi thành per-task): số handle duy nhất trên
     * toàn tiến trình làm việc dùng nhầm handle của task khác lộ ra thành lỗi thay vì âm thầm trúng
     * một queue khác.
     */
    private final ThreadLocal<Map<Integer, String>> openQueues =
            ThreadLocal.withInitial(java.util.HashMap::new);

    /** {@inheritDoc} — xoá bảng handle của task hiện tại (gọi từ khối finally của AppRunner). */
    @Override
    public void releaseTaskHandles() {
        openQueues.remove();
    }

    /** Tên queue đang gắn với handle — để test kiểm chứng extractQueueName đọc đúng field. */
    String queueNameOf(int handle) {
        return openQueues.get().get(handle);
    }

    private final AtomicInteger handleCounter = new AtomicInteger(1);

    /**
     * Tham gia syncpoint của host (CICS: MQ là resource manager trong UOW). Null (standalone/test
     * cũ, và MỌI cohort batch — không có task runner) → gửi/nhận ngay.
     */
    private UowHost appRunner;

    void setAppRunner(UowHost appRunner) {
        this.appRunner = appRunner;
    }

    public MqRunner(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    // --- MQOPEN: [hconn, objDesc, options, hobj(out), compCode(out), reason(out)] ---

    @Override
    public void mqOpen(Object[] params) {
        if (params.length < 6) {
            log.error("MQOPEN: insufficient params ({})", params.length);
            safeSet(params, 4, MQCC_FAILED);
            safeSet(params, 5, MQRC_Q_MGR_NOT_AVAILABLE);
            return;
        }

        String queueName = extractQueueName(params[1]);
        if (queueName == null || queueName.isBlank()) {
            log.error(
                    "MQOPEN: could not extract queue name from object descriptor (class={})",
                    params[1] != null ? params[1].getClass().getSimpleName() : "null");
            safeSet(params, 4, MQCC_FAILED);
            safeSet(params, 5, MQRC_Q_MGR_NOT_AVAILABLE);
            return;
        }

        int handle = handleCounter.getAndIncrement();
        openQueues.get().put(handle, queueName.trim());

        safeSet(params, 3, handle);
        safeSet(params, 4, MQCC_OK);
        safeSet(params, 5, MQRC_NONE);

        log.debug("MQOPEN: queue='{}' handle={}", queueName.trim(), handle);
    }

    // --- MQGET: [hconn, hobj, msgDesc, getOpts, buffLen, buffer(out), dataLen(out), compCode(out),
    // reason(out)] ---

    @Override
    public void mqGet(Object[] params) {
        if (params.length < 9) {
            log.error("MQGET: insufficient params ({})", params.length);
            safeSet(params, 7, MQCC_FAILED);
            safeSet(params, 8, MQRC_Q_MGR_NOT_AVAILABLE);
            return;
        }

        int hobj = toInt(params[1]);
        String queueName = openQueues.get().get(hobj);
        if (queueName == null) {
            log.error("MQGET: invalid handle {} (queue not open)", hobj);
            safeSet(params, 7, MQCC_FAILED);
            safeSet(params, 8, MQRC_HOBJ_ERROR);
            return;
        }

        // MQGMO (params[3]) quyết định timeout — xem receiveTimeoutFor() cho luật đầy đủ.
        long receiveTimeout = receiveTimeoutFor(params[3]);
        log.debug(
                "MQGET: queue='{}' handle={} receiveTimeout={}ms", queueName, hobj, receiveTimeout);

        try {
            // `receive` (không phải receiveAndConvert): giữ lại object Message để đọc identity
            // THẬT của nó. MQGET của IBM MQ trả MQMD-MSGID/CORRELID của message lấy được, và
            // chương trình COBOL dùng đúng cặp đó để ghép request/reply (đo ở coacct01: lưu
            // SAVE-MSGID/SAVE-CORELID rồi echo lại khi MQPUT reply). receiveAndConvert vứt mất
            // object nên trước đây runtime phải bịa giá trị tổng hợp.
            jakarta.jms.Message jmsMsg = receivingTemplate(receiveTimeout).receive(queueName);
            Object received = (jmsMsg == null) ? null : bodyOf(jmsMsg);
            String jmsMsgId = jmsHeader(jmsMsg, true);
            String jmsCorrelId = jmsHeader(jmsMsg, false);
            if (received == null) {
                safeSet(params, 7, MQCC_FAILED);
                safeSet(params, 8, MQRC_NO_MSG_AVAILABLE);
                return;
            }

            String message = received.toString();
            // MQ truncation: BUFFERLENGTH (params[4]) < độ dài message. Mặc định (không
            // MQGMO_ACCEPT_TRUNCATED_MSG) IBM MQ trả MQCC_WARNING + 2080 và KHÔNG cắt buffer;
            // có accept → 2079 + cắt buffer. DataLength (params[6]) luôn = độ dài đầy đủ.
            // Ta chọn faithful-an-toàn: KHÔNG lặng lẽ nhét message dài vào buffer khai báo ngắn.
            int buffLen = toInt(params[4]);
            int fullLen = message.length();
            if (buffLen > 0 && fullLen > buffLen) {
                boolean acceptTruncated = hasAcceptTruncated(params[3]);
                String delivered = acceptTruncated ? message.substring(0, buffLen) : "";
                safeSet(params, 5, delivered);
                safeSet(params, 6, fullLen);
                safeSet(params, 7, MQCC_WARNING);
                safeSet(
                        params,
                        8,
                        acceptTruncated ? MQRC_TRUNCATED_MSG_ACCEPTED : MQRC_TRUNCATED_MSG_FAILED);
                writeBackMsgDesc(params[2], message, jmsMsgId, jmsCorrelId);
                if (appRunner != null) {
                    final String rq = queueName;
                    final String rm = message;
                    final String rc = jmsCorrelId;
                    appRunner.registerUowCompletion(
                            null,
                            () -> {
                                if (rc == null || rc.isBlank()) jmsTemplate.convertAndSend(rq, rm);
                                else
                                    jmsTemplate.convertAndSend(
                                            rq,
                                            rm,
                                            m -> {
                                                m.setJMSCorrelationID(rc.trim());
                                                return m;
                                            });
                            });
                }
                return;
            }
            // CICS: MQGET destructive trong syncpoint — back-out trả message về queue.
            // XẤP XỈ: re-send về TAIL (JMS không hỗ trợ push-front) — khác thứ tự, documented.
            if (appRunner != null) {
                final String requeueQueue = queueName;
                final String requeueMsg = message;
                final String requeueCorrel = jmsCorrelId;
                appRunner.registerUowCompletion(
                        null,
                        () -> {
                            if (requeueCorrel == null || requeueCorrel.isBlank())
                                jmsTemplate.convertAndSend(requeueQueue, requeueMsg);
                            else
                                jmsTemplate.convertAndSend(
                                        requeueQueue,
                                        requeueMsg,
                                        m -> {
                                            m.setJMSCorrelationID(requeueCorrel.trim());
                                            return m;
                                        });
                        });
            }

            safeSet(params, 5, message);
            safeSet(params, 6, message.length());
            safeSet(params, 7, MQCC_OK);
            safeSet(params, 8, MQRC_NONE);

            writeBackMsgDesc(params[2], message, jmsMsgId, jmsCorrelId);
            log.debug("MQGET: received {} bytes from '{}'", message.length(), queueName);

        } catch (Exception e) {
            log.error("MQGET error on queue '{}': {}", queueName, e.getMessage());
            safeSet(params, 7, MQCC_FAILED);
            safeSet(params, 8, mapExceptionToMqrc(e));
        }
    }

    // --- MQPUT: [hconn, hobj, msgDesc, putOpts, buffLen, buffer, compCode(out), reason(out)] ---

    @Override
    public void mqPut(Object[] params) {
        if (params.length < 8) {
            log.error("MQPUT: insufficient params ({})", params.length);
            safeSet(params, 6, MQCC_FAILED);
            safeSet(params, 7, MQRC_Q_MGR_NOT_AVAILABLE);
            return;
        }

        int hobj = toInt(params[1]);
        String queueName = openQueues.get().get(hobj);
        if (queueName == null) {
            log.error("MQPUT: invalid handle {} (queue not open)", hobj);
            safeSet(params, 6, MQCC_FAILED);
            safeSet(params, 7, MQRC_HOBJ_ERROR);
            return;
        }

        Object messageBody = params[5];
        String message = (messageBody != null) ? messageBody.toString() : "";

        log.debug(
                "MQPUT: queue='{}' handle={} message={} bytes", queueName, hobj, message.length());

        try {
            // CICS: MQPUT trong syncpoint scope — message chỉ visible sau COMMIT;
            // back-out → không gửi. Defer qua UOW-completion của AppRunner.
            sendWithinUow(queueName, message, descField(params[2], F_CORRELID));
            safeSet(params, 6, MQCC_OK);
            safeSet(params, 7, MQRC_NONE);
        } catch (Exception e) {
            log.error("MQPUT error on queue '{}': {}", queueName, e.getMessage());
            safeSet(params, 6, MQCC_FAILED);
            safeSet(params, 7, mapExceptionToMqrc(e));
        }
    }

    // --- MQPUT1: [hconn, objDesc, msgDesc, putOpts, buffLen, buffer, compCode(out), reason(out)]
    // ---

    @Override
    public void mqPut1(Object[] params) {
        if (params.length < 8) {
            log.error("MQPUT1: insufficient params ({})", params.length);
            safeSet(params, 6, MQCC_FAILED);
            safeSet(params, 7, MQRC_Q_MGR_NOT_AVAILABLE);
            return;
        }

        String queueName = extractQueueName(params[1]);
        if (queueName == null || queueName.isBlank()) {
            log.error("MQPUT1: could not extract queue name from object descriptor");
            safeSet(params, 6, MQCC_FAILED);
            safeSet(params, 7, MQRC_Q_MGR_NOT_AVAILABLE);
            return;
        }

        Object messageBody = params[5];
        String message = (messageBody != null) ? messageBody.toString() : "";

        log.debug("MQPUT1: queue='{}' message={} bytes", queueName.trim(), message.length());

        try {
            sendWithinUow(queueName.trim(), message, descField(params[2], F_CORRELID));
            safeSet(params, 6, MQCC_OK);
            safeSet(params, 7, MQRC_NONE);
        } catch (Exception e) {
            log.error("MQPUT1 error on queue '{}': {}", queueName.trim(), e.getMessage());
            safeSet(params, 6, MQCC_FAILED);
            safeSet(params, 7, mapExceptionToMqrc(e));
        }
    }

    // --- MQCLOSE: [hconn, hobj, closeOpts, compCode(out), reason(out)] ---

    /**
     * Gửi message theo semantics syncpoint CICS: có UOW → defer tới COMMIT (back-out → bỏ); không
     * có AppRunner/UOW → gửi ngay (hành vi cũ, đường standalone/test).
     */
    /**
     * MQGMO có MQGMO_ACCEPT_TRUNCATED_MSG (0x40)? Best-effort đọc field 'options' của
     * msg-get-options; không đọc được → false (mặc định MQ: truncation là failure).
     */
    private boolean hasAcceptTruncated(Object mqgmo) {
        Integer options = gmoOptions(mqgmo);
        return options != null && (options & MQGMO_ACCEPT_TRUNCATED_MSG) != 0;
    }

    /**
     * MQGMO-OPTIONS nếu đọc được; {@code null} khi không đọc được (ảnh chuỗi: field BINARY không
     * sống qua charset — xem warnOnce ở {@link #extractWaitInterval}).
     */
    private Integer gmoOptions(Object mqgmo) {
        if (mqgmo == null || mqgmo instanceof String) {
            return null;
        }
        if (mqgmo instanceof GroupRef g) {
            return g.has(F_GMO_OPTIONS) ? g.getInt(F_GMO_OPTIONS, 0) : null;
        }
        try {
            for (java.lang.reflect.Field f : mqgmo.getClass().getDeclaredFields()) {
                if (f.getName().toLowerCase().contains("option")) {
                    f.setAccessible(true);
                    Object v = f.get(mqgmo);
                    if (v instanceof Number n) {
                        return n.intValue();
                    }
                }
            }
        } catch (Exception ignore) {
            /* không đọc được → null */
        }
        return null;
    }

    /**
     * Timeout JMS (ms) tương ứng với MQGMO, theo đúng luật MQGET của IBM MQ.
     *
     * <p>Hai field cùng quyết định, không phải một: bit {@code MQGMO_WAIT} trong {@code
     * MQGMO-OPTIONS} nói CÓ CHỜ HAY KHÔNG, {@code MQGMO-WAITINTERVAL} nói CHỜ BAO LÂU. Trước
     * 2026-08-22 runtime chỉ đọc WAITINTERVAL ⇒ chương trình set interval mà không set bit WAIT thì
     * MQ thật trả về ngay còn ta chờ đủ chừng ấy; và {@code MQWI_UNLIMITED} rơi qua cả hai nhánh
     * nên rốt cuộc dùng timeout đang có sẵn của bean.
     *
     * <p>Đọc được OPTIONS thì theo OPTIONS; KHÔNG đọc được thì giữ nguyên luật cũ ({@code interval
     * > 0} coi như có chờ) — không đoán thêm.
     */
    /**
     * {@link JmsTemplate} RIÊNG cho một lời gọi MQGET, mang timeout của CHÍNH task đó.
     *
     * <p><b>Vì sao không đặt timeout lên bean dùng chung</b>: {@code MQGMO} nằm trong
     * WORKING-STORAGE (đo ở `COACCT01`), mà trong CICS WORKING-STORAGE là bản RIÊNG của từng task ⇒
     * wait interval là giá trị per-task. Đường online chạy thread-per-request ({@code
     * TerminalController} là {@code @RestController}, không khoá gì) và {@code AppRunner} đã giữ
     * MỌI state per-task bằng {@code ThreadLocal} (contextHolder, browseState, uow, heldEnqueues…).
     * Ghi wait interval vào field của một singleton là chỗ DUY NHẤT trong runtime đi ngược hợp đồng
     * đó: hai task song song có thể cướp timeout của nhau ⇒ chương trình xin chờ 5 giây nhận {@code
     * MQRC 2033} giả rồi rẽ nhánh "không có message".
     *
     * <p><b>Vì sao copy cả JmsTemplate chứ không tự gọi {@code consumer.receive(timeout)}</b>:
     * {@code JmsTemplate.doReceive} còn clamp timeout theo {@code JmsResourceHolder}, gọi {@code
     * commitIfNecessary} khi session transacted và {@code message.acknowledge()} khi
     * CLIENT_ACKNOWLEDGE. Khách tự cấp broker starter và hoàn toàn có thể bật {@code
     * session.transacted=true} (rất thường gặp với IBM MQ, đúng tinh thần syncpoint của CICS), nên
     * KHÔNG được giả định non-transacted/auto-ack. Đi qua đúng {@code receive()} của Spring thì giữ
     * nguyên mọi ngữ nghĩa đó, chỉ khác là trên một instance không dùng chung.
     *
     * <p>Tạo mới mỗi lời gọi thay vì cache theo {@code ThreadLocal}: {@code JmsTemplate} không giữ
     * connection (caching nằm ở {@code ConnectionFactory}, được copy theo tham chiếu) nên không mất
     * cache, còn {@code BeanUtils.copyProperties} dùng introspection đã cache — chi phí không đáng
     * kể so với một vòng receive qua broker, và đổi lại không có vòng đời ThreadLocal nào trên
     * thread pool phải lo.
     */
    /* package-private để test gọi thẳng — xem MqSharedTemplateIsolationTest. */
    JmsTemplate receivingTemplate(long receiveTimeout) {
        JmsTemplate t = new JmsTemplate();
        org.springframework.beans.BeanUtils.copyProperties(jmsTemplate, t);
        t.afterPropertiesSet();
        t.setReceiveTimeout(receiveTimeout);
        return t;
    }

    private long receiveTimeoutFor(Object mqgmo) {
        Integer options = gmoOptions(mqgmo);
        long interval = extractWaitInterval(mqgmo);
        boolean waits = (options != null) ? (options & MQGMO_WAIT) != 0 : interval > 0;
        if (!waits) {
            return NO_WAIT_CONSUMER_SETUP_MS;
        }
        if (interval == MQWI_UNLIMITED) {
            return org.springframework.jms.core.JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT;
        }
        // MQGMO_WAIT kèm WaitInterval 0 vẫn là "trả về ngay" theo spec.
        return interval > 0 ? interval : NO_WAIT_CONSUMER_SETUP_MS;
    }

    private void sendWithinUow(String queueName, String message) {
        sendWithinUow(queueName, message, null);
    }

    /**
     * Gửi kèm JMSCorrelationID lấy từ MQMD-CORRELID của chương trình — đó là cách COBOL ghép reply
     * với request (đo ở coacct01: MQPUT reply sau khi MOVE SAVE-CORELID vào MQMD-CORRELID). Không
     * có correlId thì gửi như cũ.
     */
    private void sendWithinUow(String queueName, String message, String correlId) {
        org.springframework.jms.core.MessagePostProcessor pp =
                (correlId == null || correlId.isBlank())
                        ? null
                        : m -> {
                            m.setJMSCorrelationID(correlId.trim());
                            return m;
                        };
        Runnable send =
                (pp == null)
                        ? () -> jmsTemplate.convertAndSend(queueName, message)
                        : () -> jmsTemplate.convertAndSend(queueName, message, pp);
        if (appRunner != null) {
            appRunner.registerUowCompletion(send::run, null);
        } else {
            send.run();
        }
    }

    @Override
    public void mqClose(Object[] params) {
        if (params.length < 5) {
            log.error("MQCLOSE: insufficient params ({})", params.length);
            safeSet(params, 3, MQCC_FAILED);
            safeSet(params, 4, MQRC_Q_MGR_NOT_AVAILABLE);
            return;
        }

        int hobj = toInt(params[1]);
        String queueName = openQueues.get().remove(hobj);

        if (queueName == null) {
            log.warn("MQCLOSE: handle {} not found (already closed?)", hobj);
        } else {
            log.debug("MQCLOSE: queue='{}' handle={}", queueName, hobj);
        }

        safeSet(params, 3, MQCC_OK);
        safeSet(params, 4, MQRC_NONE);
    }

    // --- Queue Name Extraction ---

    /**
     * Extract queue name from MQOD Object Descriptor POJO.
     *
     * <p>Strategy: find the largest {@code @CobolField}-annotated String field in the POJO. In
     * MQOD, OBJECTNAME is PIC X(48) — the largest String field. This avoids hardcoding field name
     * patterns like "objectname" or "qname".
     *
     * <p>If param is a plain String, returns it directly (e.g., from stub mode).
     */
    private String extractQueueName(Object objDesc) {
        if (objDesc == null) {
            return null;
        }
        if (objDesc instanceof GroupRef g) {
            String v = g.get(F_OBJECTNAME);
            String t = v == null ? "" : v.trim();
            if (!t.isEmpty()) {
                return t;
            }
            warnOnce(
                    "mqod-ref-empty-name",
                    "MQOD: "
                            + g.groupName()
                            + "."
                            + F_OBJECTNAME
                            + " rỗng — chương trình chưa MOVE "
                            + "tên queue vào MQOD trước khi CALL?");
            return null;
        }
        if (objDesc instanceof String s) {
            if (isMqodImage(s)) {
                return queueNameFromMqodImage(s);
            }
            // Không phải ảnh MQOD → coi như đã là tên queue (đường stub/test).
            String trimmed = s.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
        return extractLargestStringField(objDesc);
    }

    /**
     * Chuỗi này là ảnh group MQOD, hay vốn đã là tên queue?
     *
     * <p>Nhận diện bằng STRUCID {@code "OD"} ở đầu + đủ độ dài tới hết OBJECTNAME. Nhờ tách riêng
     * bước nhận diện, đường stub (truyền thẳng {@code "DEV.AUTH.REQUEST"}) vẫn chạy y như trước,
     * còn ảnh MQOD có OBJECTNAME rỗng thì FAIL rõ ràng chứ không biến thành queue tên {@code "OD"}.
     */
    private boolean isMqodImage(String s) {
        return s.length() >= MQOD_OBJECTNAME_END && s.startsWith(MQOD_STRUCID);
    }

    /**
     * Tên queue trong ảnh MQOD; {@code null} nếu OBJECTNAME rỗng (caller phải FAIL, không đoán).
     */
    private String queueNameFromMqodImage(String s) {
        String name = s.substring(MQOD_OBJECTNAME_AT, MQOD_OBJECTNAME_END).trim();
        if (name.isEmpty()) {
            warnOnce(
                    "mqod-image-empty-name",
                    "MQOD: ảnh group có STRUCID 'OD' nhưng OBJECTNAME (offset 12..60) rỗng — "
                            + "chương trình chưa MOVE tên queue vào MQOD-OBJECTNAME?");
            return null;
        }
        return name;
    }

    /**
     * Find the largest @CobolField-annotated String field value in a POJO. Uses @CobolField(size=N)
     * annotation to determine field size — no name pattern matching.
     */
    private String extractLargestStringField(Object pojo) {
        String bestValue = null;
        int bestSize = 0;

        try {
            for (java.lang.reflect.Field f : pojo.getClass().getDeclaredFields()) {
                if (f.getType() != String.class) {
                    continue;
                }

                // Read @CobolField annotation size
                int fieldSize = getCobolFieldSize(f);
                if (fieldSize <= 0) {
                    continue;
                }

                if (fieldSize > bestSize) {
                    f.setAccessible(true);
                    Object val = f.get(pojo);
                    if (val != null) {
                        String trimmed = val.toString().trim();
                        if (!trimmed.isEmpty()) {
                            bestValue = trimmed;
                            bestSize = fieldSize;
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            log.debug("Cannot access field in MQOD POJO: {}", e.getMessage());
        }

        return bestValue;
    }

    /**
     * Extract MQGMO-WAITINTERVAL from MQGMO POJO.
     *
     * <p>MQGMO structure: STRUCID(str,4) + VERSION(int,9) + OPTIONS(int,9) + WAITINTERVAL(int,9).
     * WAITINTERVAL is the LAST @CobolField-annotated int field in the POJO. Returns 0 if not found
     * or POJO is null/String.
     */
    private long extractWaitInterval(Object mqgmo) {
        if (mqgmo == null) {
            return 0;
        }
        if (mqgmo instanceof GroupRef g) {
            return g.getInt(F_WAITINTERVAL, 0);
        }
        if (mqgmo instanceof String) {
            // MQGMO-WAITINTERVAL là 4 byte BINARY trong ảnh group; sau khi ảnh đã qua charset thì
            // giải mã lại không tin cậy (byte > 0x7F bị biến dạng tuỳ charset). KHÔNG đoán.
            warnOnce(
                    "mqgmo-image-wait",
                    "MQGET: MQGMO tới dưới dạng ảnh chuỗi ⇒ không đọc được WAITINTERVAL/OPTIONS "
                            + "(field BINARY). Đang dùng WAITINTERVAL=0 (không chờ) và "
                            + "ACCEPT_TRUNCATED_MSG=false. Muốn đúng thì phía emit phải truyền "
                            + "object group thay vì groupToString().");
            return 0;
        }
        // WAITINTERVAL is the last int field in MQGMO — iterate all, keep last non-zero
        long lastIntValue = 0;
        try {
            for (java.lang.reflect.Field f : mqgmo.getClass().getDeclaredFields()) {
                if (f.getType() != int.class && f.getType() != long.class) {
                    continue;
                }
                int fieldSize = getCobolFieldSize(f);
                if (fieldSize <= 0) {
                    continue;
                }
                f.setAccessible(true);
                long val = ((Number) f.get(mqgmo)).longValue();
                lastIntValue = val; // keep updating — last @CobolField int field
            }
        } catch (IllegalAccessException e) {
            log.debug("Cannot read MQGMO wait interval: {}", e.getMessage());
        }
        return lastIntValue;
    }

    /** Read @CobolField(size=N) annotation value from a field. Returns 0 if not annotated. */
    private int getCobolFieldSize(java.lang.reflect.Field f) {
        for (java.lang.annotation.Annotation ann : f.getAnnotations()) {
            if (ann.annotationType().getSimpleName().equals("CobolField")) {
                try {
                    java.lang.reflect.Method sizeMethod = ann.annotationType().getMethod("size");
                    return (int) sizeMethod.invoke(ann);
                } catch (ReflectiveOperationException e) {
                    log.trace("Cannot read CobolField.size: {}", e.getMessage());
                }
            }
        }
        return 0;
    }

    // --- MQMD Write-back ---

    /**
     * Write message metadata back to MQMD POJO after MQGET. Uses @CobolField annotation to identify
     * fields by size: - PIC X(24) = MSGID/CORRELID → generated nanoTime ID - PIC X(8) = FORMAT →
     * "MQSTR " (string format) - PIC S9(9) int fields → set default values (persistence=0,
     * expiry=-1, priority=5)
     */
    private void writeBackMsgDesc(
            Object msgDesc, String message, String jmsMsgId, String jmsCorrelId) {
        if (msgDesc == null) {
            return;
        }
        if (msgDesc instanceof GroupRef g) {
            // Ghi-trả-về THẬT vào buffer của chương trình, nên `MOVE MQMD-MSGID TO …` ngay sau
            // MQGET đọc được giá trị mới. Nguồn giá trị là identity THẬT của message JMS —
            // trước 2026-08-21 runtime bịa `System.nanoTime()` cho CẢ HAI field, làm hỏng ghép
            // request/reply (COBOL echo MSGID+CORRELID của request sang reply).
            g.set(
                    F_MSGID,
                    jmsMsgId != null ? jmsMsgId : String.format("%-24.24s", System.nanoTime()));
            // CORRELID chỉ ghi khi message THẬT SỰ có: message request thường không có
            // JMSCorrelationID, và COBOL đã đặt sẵn MQCI-NONE trước MQGET — ghi đè bằng giá trị
            // bịa sẽ khiến reply mang correlation sai.
            if (jmsCorrelId != null && !jmsCorrelId.isBlank()) {
                g.set(F_CORRELID, jmsCorrelId);
            }
            g.set(F_FORMAT, "MQSTR   ");
            return;
        }
        if (msgDesc instanceof String) {
            // String bất biến ⇒ không ghi lại được. Chương trình COBOL đọc MQMD-MSGID/CORRELID/
            // REPLYTOQ NGAY SAU MQGET (đo được ở coacct01/codate01/copaua0c) nên nó sẽ thấy giá trị
            // cũ. Đây là mất mát THẬT, phải nói ra chứ không im lặng.
            warnOnce(
                    "mqmd-image-writeback",
                    "MQGET/MQPUT: MQMD tới dưới dạng ảnh chuỗi ⇒ KHÔNG ghi trả về được "
                            + "MSGID/CORRELID/FORMAT. Chương trình đọc MQMD ngay sau MQGET sẽ thấy "
                            + "giá trị cũ. Sửa gốc ở phía emit (truyền object group).");
            return;
        }
        try {
            for (java.lang.reflect.Field f : msgDesc.getClass().getDeclaredFields()) {
                int size = getCobolFieldSize(f);
                if (size <= 0) {
                    continue;
                }
                f.setAccessible(true);

                if (f.getType() == String.class && size == 24) {
                    // MQMD-MSGID / MQMD-CORRELID — PIC X(24). Reflection không phân biệt được hai
                    // field này (cùng kiểu, cùng size) nên đường POJO vẫn ghi CÙNG một giá trị;
                    // đường GroupRef ở trên mới ghi đúng từng field.
                    f.set(
                            msgDesc,
                            String.format(
                                    "%-24.24s",
                                    jmsMsgId != null
                                            ? jmsMsgId
                                            : String.valueOf(System.nanoTime())));
                } else if (f.getType() == String.class && size == 8) {
                    // MQMD-FORMAT — PIC X(8) → "MQSTR   "
                    f.set(msgDesc, "MQSTR   ");
                }
            }
        } catch (IllegalAccessException e) {
            log.debug("Could not write back MQMD metadata: {}", e.getMessage());
        }
    }

    /**
     * Map JMS exception to specific IBM MQ reason code. Distinguishes connection errors,
     * authorization, and message-level errors.
     */
    private int mapExceptionToMqrc(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("timeout")
                || msg.contains("no message")
                || msg.contains("receive timed out")) {
            return MQRC_NO_MSG_AVAILABLE;
        }
        if (msg.contains("security") || msg.contains("denied") || msg.contains("auth")) {
            return MQRC_NOT_AUTHORIZED;
        }
        if (msg.contains("connection") || msg.contains("refused") || msg.contains("broken")) {
            return MQRC_CONNECTION_BROKEN;
        }
        if (msg.contains("not found")
                || msg.contains("does not exist")
                || msg.contains("unknown")) {
            return MQRC_UNKNOWN_OBJECT_NAME;
        }
        // Default: no message available (most common JMS exception for empty queue)
        return MQRC_NO_MSG_AVAILABLE;
    }

    // --- Utilities ---

    private void safeSet(Object[] params, int index, Object value) {
        if (index < params.length) {
            params[index] = value;
        }
    }

    private int toInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
