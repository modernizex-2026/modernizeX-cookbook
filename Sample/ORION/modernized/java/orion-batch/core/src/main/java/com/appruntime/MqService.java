package com.appruntime;

/**
 * IBM MQ service abstraction — maps MQ API calls to Java messaging operations.
 *
 * <p>Supports all 5 MQ operations per IBM MQI spec:
 *
 * <ul>
 *   <li>MQOPEN: [hconn, objDesc, options, hobj(out), compCode(out), reason(out)]
 *   <li>MQGET: [hconn, hobj, msgDesc, getOpts, buffLen, buffer(out), dataLen(out), compCode(out),
 *       reason(out)]
 *   <li>MQPUT: [hconn, hobj, msgDesc, putOpts, buffLen, buffer, compCode(out), reason(out)]
 *   <li>MQPUT1: [hconn, objDesc, msgDesc, putOpts, buffLen, buffer, compCode(out), reason(out)]
 *   <li>MQCLOSE: [hconn, hobj, closeOpts, compCode(out), reason(out)]
 * </ul>
 */
public interface MqService {

    /** MQ Completion Codes */
    int MQCC_OK = 0;

    int MQCC_WARNING = 1;
    int MQCC_FAILED = 2;

    /** MQ Reason Codes */
    int MQRC_NONE = 0;

    int MQRC_CONNECTION_BROKEN = 2009;
    int MQRC_NO_MSG_AVAILABLE = 2033;
    int MQRC_NOT_AUTHORIZED = 2035;
    int MQRC_Q_MGR_NOT_AVAILABLE = 2059;
    int MQRC_TRUNCATED_MSG_ACCEPTED = 2079;
    int MQRC_TRUNCATED_MSG_FAILED = 2080;

    void mqOpen(Object[] params);

    void mqGet(Object[] params);

    /** MQPUT — put message to pre-opened queue (params[1] = hobj handle). */
    void mqPut(Object[] params);

    /** MQPUT1 — atomic open+put+close (params[1] = objDesc POJO). */
    void mqPut1(Object[] params);

    void mqClose(Object[] params);

    /**
     * Giải phóng mọi object handle mà TASK hiện tại đã MQOPEN nhưng chưa MQCLOSE.
     *
     * <p>CICS trả lại handle của task khi task kết thúc, kể cả khi chương trình abend giữa chừng.
     * Không làm vậy thì một chương trình quên MQCLOSE (hoặc abend trước khi tới đó) để lại entry
     * sống mãi trong bảng handle — rò rỉ chậm, và handle chết vẫn dùng được từ task khác. Cùng một
     * lý do với {@code releaseTaskEnqueues()} của {@code AppRunner}.
     *
     * <p>Mặc định no-op để implementer khác (stub/test) không phải quan tâm.
     */
    default void releaseTaskHandles() {}
}
