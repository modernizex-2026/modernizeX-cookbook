package com.appruntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core CICS runtime engine. Implements all AppService operations using ThreadLocal context for
 * per-request state management.
 *
 * <p>Autowires all {@link AppProgram} and {@link FileDao} beans by name.
 */
@Component
public class AppRunner implements AppService, UowHost {

    private static final Logger log = LoggerFactory.getLogger(AppRunner.class);

    // EIBFN function codes — matches real CICS 2-byte hex values
    private static final String EIBFN_SEND_MAP = "0E02";
    private static final String EIBFN_RECEIVE_MAP = "0E04";
    private static final String EIBFN_RETURN = "0E06";
    private static final String EIBFN_XCTL = "0E08";
    private static final String EIBFN_LINK = "0402";
    private static final String EIBFN_READ = "0602";
    private static final String EIBFN_WRITE = "0604";
    private static final String EIBFN_REWRITE = "0606";
    private static final String EIBFN_DELETE = "0608";
    private static final String EIBFN_START = "1002";
    private static final String EIBFN_RETRIEVE = "1004";
    private static final String EIBFN_STARTBR = "0C02";
    private static final String EIBFN_READNEXT = "0C04";
    private static final String EIBFN_READPREV = "0C06";
    private static final String EIBFN_ENDBR = "0C08";
    private static final String EIBFN_WRITEQ_TS = "1804";
    private static final String EIBFN_READQ_TS = "1802";
    private static final String EIBFN_DELETEQ_TS = "1806";
    private static final String EIBFN_WRITEQ_TD = "0804";
    private static final String EIBFN_READQ_TD = "0802";
    private static final String EIBFN_DELETEQ_TD = "0806";
    private static final String EIBFN_ASKTIME = "1402";
    private static final String EIBFN_FORMATTIME = "1404";
    private static final String EIBFN_ABEND = "1A02";
    private static final String EIBFN_ASSIGN = "0202";

    private final Map<String, AppProgram> programs;
    private final Map<String, FileDao> fileDaos;
    private final Map<String, Set<String>> mapFsetRegistry = new ConcurrentHashMap<>();

    // IMS DL/I service — set via DliAutoConfig when spring-jdbc is on classpath
    private volatile DliService dliRunner;
    // MQ service — set via MqAutoConfig when spring-jms is on classpath
    private volatile MqService mqRunner;

    private static final ThreadLocal<AppContext> contextHolder = new ThreadLocal<>();

    // Browse state: fileName -> browse context (list + bidirectional position)
    private static final ThreadLocal<Map<String, BrowseContext>> browseState =
            ThreadLocal.withInitial(HashMap::new);

    // UPDATE cursor: fileName -> key held after READ ... UPDATE, cleared after REWRITE/DELETE
    private static final ThreadLocal<Map<String, Object>> updateCursorKey =
            ThreadLocal.withInitial(HashMap::new);

    /**
     * Bidirectional browse context: ALL records in ASC order with cursor position. CICS semantics:
     * STARTBR positions at first record >= key. READNEXT reads record at position, then advances.
     * READPREV reads record before position, then retreats. Supports interleaved READNEXT/READPREV
     * without re-query.
     */
    private enum BrowseDir {
        NONE,
        NEXT,
        PREV
    }

    private static class BrowseContext {
        final List<Object> allRecords; // full dataset ASC (from startBrowse >= key)
        final String startKey;
        final String fileName;
        int cursor; // index of last-returned record (-1 = before first read)
        BrowseDir lastDir = BrowseDir.NONE; // tracks direction for bidirectional switch

        BrowseContext(List<Object> allRecords, String startKey, String fileName) {
            this.allRecords = allRecords;
            this.startKey = startKey;
            this.fileName = fileName;
            this.cursor = -1; // before any read
        }
    }

    // Temporary Storage (TS) queues — ordered list per queue name (CICS semantics: append/rewrite)
    // TS/TD queues: in-memory only — data lost on restart.
    // For production persistence, replace with Redis or database-backed implementation.
    private final Map<String, java.util.List<Object>> tsQueues = new ConcurrentHashMap<>();
    private final Map<String, java.util.Queue<Object>> tdQueues = new ConcurrentHashMap<>();

    // ASK TIME result
    private static final ThreadLocal<String> abstimeHolder = new ThreadLocal<>();

    // Current program (for field mapping access during receiveMap)
    private static final ThreadLocal<AppProgram> currentProgram = new ThreadLocal<>();

    /**
     * CICS program names are PIC X(8) blank-padded — trailing blanks are not part of the name
     * (XCTL/LINK/CALL pass the padded field verbatim), so keys are trimmed uppercase.
     */
    private static String programKey(String name) {
        return name == null ? null : name.trim().toUpperCase();
    }

    /**
     * CICS queue/resource names are fixed-width blank-padded values — a literal writer and a PIC X
     * field reader must resolve to the same key, so trailing blanks are trimmed. Case is preserved:
     * CICS compares these names as raw bytes.
     */
    private static String queueKey(String name) {
        return name == null ? null : name.trim();
    }

    public AppRunner(Map<String, AppProgram> programs, Map<String, FileDao> fileDaos) {
        // Index programs by canonical uppercase key for consistent lookup
        this.programs = new HashMap<>();
        for (Map.Entry<String, AppProgram> entry : programs.entrySet()) {
            AppProgram program = entry.getValue();
            String canonicalKey = programKey(program.getProgramName());
            this.programs.put(canonicalKey, program);
        }
        this.fileDaos = fileDaos;
        // Register FSET fields for each program
        for (AppProgram program : programs.values()) {
            program.registerFsetFields(this);
        }
    }

    /**
     * Canonical (uppercase) names of all registered online programs, sorted — for the SPA program
     * list / GET /api/terminal/programs. Replaces the old server-rendered index page.
     */
    public java.util.Set<String> getProgramNames() {
        return new java.util.TreeSet<>(programs.keySet());
    }

    /**
     * Transaction-ID → program-name map (CICS EXEC CICS RETURN TRANSID). Drives the SPA's entry
     * gateway screen (type a TransID like CC00 → start its program). Only programs that declare a
     * transaction id appear.
     */
    public java.util.Map<String, String> getTransactionMap() {
        java.util.Map<String, String> m = new java.util.TreeMap<>();
        for (AppProgram p : programs.values()) {
            String t = p.getTransId();
            if (t != null && !t.isBlank()) {
                m.put(t.trim().toUpperCase(), p.getProgramName().toUpperCase());
            }
        }
        return m;
    }

    /**
     * Execute a CICS program based on terminal input.
     *
     * @param input the terminal input (program name, AID key, fields, commarea)
     * @return the screen response to send back to the terminal
     */
    /**
     * CICS unit-of-work: mỗi request mở UOW; SYNCPOINT commit-giữa-chừng và SYNCPOINT ROLLBACK
     * back-out-về-syncpoint-trước được map 1-1 (SSD_SyncpointTransaction). Không có
     * PlatformTransactionManager (app không DB) → chạy không tx như trước.
     */
    public ScreenResponse execute(TerminalInput input) {
        if (txManager == null) {
            return doExecute(input);
        }
        beginUow();
        boolean ok = false;
        try {
            ScreenResponse r = doExecute(input);
            ok = true;
            return r;
        } finally {
            endUow(ok);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setTransactionManager(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }

    private PlatformTransactionManager txManager;

    /** UOW hiện hành theo thread — AppRunner là singleton phục vụ concurrent request. */
    private final ThreadLocal<TransactionStatus> uow = new ThreadLocal<>();

    /**
     * Hành động chạy khi UOW kết thúc — CICS resource managers (MQ) tham gia syncpoint qua đây:
     * commit → thực thi (vd gửi MQPUT đã defer), back-out → hành động bù (vd trả lại message
     * MQGET). Không có UOW active → onCommit chạy NGAY (hành vi cũ).
     */
    private final ThreadLocal<java.util.List<Runnable>> uowCommitActions =
            ThreadLocal.withInitial(java.util.ArrayList::new);

    private final ThreadLocal<java.util.List<Runnable>> uowRollbackActions =
            ThreadLocal.withInitial(java.util.ArrayList::new);

    @Override
    public void registerUowCompletion(Runnable onCommit, Runnable onRollback) {
        if (txManager == null || uow.get() == null) {
            if (onCommit != null) {
                onCommit.run();
            }
            return;
        }
        if (onCommit != null) {
            uowCommitActions.get().add(onCommit);
        }
        if (onRollback != null) {
            uowRollbackActions.get().add(onRollback);
        }
    }

    private void runUowCompletions(boolean committed) {
        java.util.List<Runnable> actions =
                committed ? uowCommitActions.get() : uowRollbackActions.get();
        uowCommitActions.remove();
        uowRollbackActions.remove();
        for (Runnable action : actions) {
            try {
                action.run();
            } catch (Exception e) {
                log.error("UOW completion action failed: {}", e.getMessage());
            }
        }
    }

    void beginUow() {
        uow.set(txManager.getTransaction(new DefaultTransactionDefinition()));
    }

    void endUow(boolean ok) {
        TransactionStatus st = uow.get();
        uow.remove();
        boolean committed = false;
        if (st != null && !st.isCompleted()) {
            if (ok && !st.isRollbackOnly()) {
                txManager.commit(st);
                committed = true;
            } else {
                txManager.rollback(st);
            }
        }
        runUowCompletions(committed);
    }

    private ScreenResponse doExecute(TerminalInput input) {
        // Normalize to canonical uppercase key — all lookups use this form
        String programName = programKey(input.getProgramName());
        AppProgram program = programs.get(programName);
        if (program == null) {
            throw new IllegalArgumentException("Unknown program: " + input.getProgramName());
        }

        // Set up context
        AppContext ctx = new AppContext();
        String httpAidKey = input.getAidKey() != null ? input.getAidKey() : "ENTER";
        ctx.setEibaid(httpAidKey);
        ctx.setCommarea(input.getCommarea());
        // EIBCALEN = declared LENGTH from previous RETURN TRANSID (stored in session).
        // In CICS, EIBCALEN reflects the LENGTH parameter, not the content size.
        // If no declared length, fall back to content-based calculation.
        ctx.setEibcalen(
                input.getCommareaLength() > 0
                        ? input.getCommareaLength()
                        : calculateCommareaLength(input.getCommarea()));
        ctx.setInputFields(input.getFields());

        // E-1: Initialize EIB date/time fields at transaction start
        LocalDate today = LocalDate.now();
        // CICS EIBDATE format: 0CYYDDD (packed decimal) where C=century(0=1900,1=2000), YY=year,
        // DDD=day-of-year
        int century = (today.getYear() >= 2000) ? 1 : 0;
        int yy = today.getYear() % 100;
        int ddd = today.getDayOfYear();
        ctx.setEibdate(century * 100000 + yy * 1000 + ddd);
        // CICS EIBTIME format: 0HHMMSS (packed decimal)
        LocalTime now = LocalTime.now();
        ctx.setEibtime(now.getHour() * 10000 + now.getMinute() * 100 + now.getSecond());
        // EIBTRMID: derive from session or use default terminal identifier
        ctx.setEibtrmid("WEB0");

        contextHolder.set(ctx);
        currentProgram.set(program);

        try {
            program.mainLine(this);
            // If mainLine returns normally (no RETURN TRANSID), build response from context
            ScreenResponse response = buildResponse(ctx, program);
            response.setProgramName(program.getProgramName());
            return response;
        } catch (ReturnException e) {
            // Normal flow: RETURN TRANSID causes this exception
            // After XCTL, currentProgram points to the target program (not the original caller)
            AppProgram activeProgram = currentProgram.get();
            if (activeProgram == null) {
                activeProgram = program;
            }
            ScreenResponse response = buildResponse(ctx, activeProgram);
            response.setProgramName(activeProgram.getProgramName());
            // RETURN without TRANSID = CICS session end (e.g., PF3 exit)
            if (e.getTransid() == null) {
                response.setSessionEnd(true);
            }
            // Preserve commarea + declared length for session persistence (pseudo-conversational)
            response.setReturnCommarea(e.getCommarea());
            response.setReturnCommareaLength(e.getLength());
            return response;
        } catch (StopRunException e) {
            // COBOL STOP RUN — terminate the run unit. In the pseudo-conversational HTTP model
            // this is normal program completion (control returns to the terminal), analogous to a
            // top-level GOBACK: build the response from the current program's context.
            AppProgram activeProgram = currentProgram.get();
            if (activeProgram == null) {
                activeProgram = program;
            }
            ScreenResponse response = buildResponse(ctx, activeProgram);
            response.setProgramName(activeProgram.getProgramName());
            return response;
        } catch (AbendException e) {
            // EXEC CICS HANDLE ABEND LABEL — dispatch to registered handler if active
            Runnable abendHandler = ctx.getAbendHandler();
            if (abendHandler != null) {
                // Clear before invoking to prevent re-entry if the handler itself abends
                ctx.setAbendHandler(null);
                try {
                    abendHandler.run();
                    // Handler finished normally — treat as successful program completion
                    AppProgram activeProgram = currentProgram.get();
                    if (activeProgram == null) {
                        activeProgram = program;
                    }
                    ScreenResponse response = buildResponse(ctx, activeProgram);
                    response.setProgramName(activeProgram.getProgramName());
                    return response;
                } catch (ReturnException re) {
                    // Handler issued RETURN / RETURN TRANSID — normal transaction exit
                    AppProgram activeProgram = currentProgram.get();
                    if (activeProgram == null) {
                        activeProgram = program;
                    }
                    ScreenResponse response = buildResponse(ctx, activeProgram);
                    response.setProgramName(activeProgram.getProgramName());
                    if (re.getTransid() == null) {
                        response.setSessionEnd(true);
                    }
                    response.setReturnCommarea(re.getCommarea());
                    response.setReturnCommareaLength(re.getLength());
                    return response;
                } catch (AbendException nested) {
                    // Nested abend inside handler — abnormal task termination: CICS dynamic
                    // transaction backout (DTB) backs out the current UOW (audit round-A).
                    rollback();
                    ScreenResponse response = new ScreenResponse();
                    response.setMessage("ABEND: " + nested.getAbcode());
                    return response;
                }
            }
            // No handler registered — abnormal task termination: DTB backs out the current
            // UOW before the error screen is returned (writes since last SYNCPOINT are lost,
            // exactly like CICS). A recovered HANDLE ABEND above keeps the task running and
            // does NOT back out — also CICS semantics.
            rollback();
            ScreenResponse response = new ScreenResponse();
            response.setMessage("ABEND: " + e.getAbcode());
            return response;
        } finally {
            // CICS releases all task-owned ENQs at task termination — do the same so a program that
            // ENQs without an explicit DEQ does not leave a pooled thread holding the lock forever.
            releaseTaskEnqueues();
            // Cùng lý do: CICS trả lại object handle của task khi task kết thúc, kể cả khi abend.
            if (mqRunner != null) {
                mqRunner.releaseTaskHandles();
            }
            contextHolder.remove();
            currentProgram.remove();
            browseState.remove();
            abstimeHolder.remove();
            updateCursorKey.remove();
            heldEnqueues.remove();
        }
    }

    private ScreenResponse buildResponse(AppContext ctx, AppProgram program) {
        ScreenResponse response = new ScreenResponse();
        Object mapOutput = ctx.getLastMapOutput();
        Object mapInput = ctx.getLastMapInput();
        if (mapOutput != null) {
            // Use explicit field mapping from the program (metadata-driven, not name heuristics)
            FieldMapping fieldMapping = program.getFieldMapping(ctx.getMapName());
            Map<String, Object> fields = MapBinder.extract(mapOutput, fieldMapping);
            // COBOL BMS REDEFINES overlays I↔O memory — writing to *I fields is visible in *O.
            // Java has no REDEFINES, so generated code may write data to input (*I) model
            // while SEND MAP passes the output (*O) model. Merge input data for missing fields.
            if (mapInput != null) {
                Map<String, Object> inputFields =
                        MapBinder.extractFromInput(mapInput, fieldMapping);
                for (Map.Entry<String, Object> e : inputFields.entrySet()) {
                    fields.putIfAbsent(e.getKey(), e.getValue());
                }
            }
            // Trim space-only values to empty (COBOL PIC X default = spaces, display as empty)
            fields.replaceAll((k, v) -> (v instanceof String s && s.isBlank()) ? "" : v);
            response.setFields(fields);
            // Attr/length fields live on the INPUT symbolic map.
            Object attrSource = (mapInput != null) ? mapInput : mapOutput;
            response.setAttrs(MapBinder.extractAttrs(attrSource, fieldMapping));
            response.setCursors(MapBinder.extractCursors(attrSource, fieldMapping));
        }
        if (ctx.getMapName() != null) {
            response.setTemplateName(ctx.getMapName().toLowerCase());
        }
        // SEND TEXT / SEND FROM — raw text content (non-BMS)
        if (ctx.getSendTextContent() != null) {
            response.setMessage(ctx.getSendTextContent().trim());
        }
        // D-1: Propagate SEND MAP options to response
        response.setEraseScreen(ctx.isEraseScreen());
        response.setFreekb(ctx.isFreekb());
        // Resolve cursor field: COBOL MOVE -1 TO fieldL sets cursor on that field.
        // The cursors map contains {BmsFieldName: lengthValue}. Find field with -1.
        if (response.getCursors() != null) {
            for (var entry : response.getCursors().entrySet()) {
                if (entry.getValue() != null && entry.getValue() == -1) {
                    response.setCursorField(entry.getKey());
                    break;
                }
            }
        }
        response.setButtons(program.getButtonDefs());
        return response;
    }

    /**
     * Test hook (package-private) — cho unit test ngữ nghĩa CICS gọi lệnh trực tiếp không qua
     * execute(); prod không dùng.
     */
    static AppContext beginContextForTest() {
        AppContext ctx = new AppContext();
        contextHolder.set(ctx);
        return ctx;
    }

    private AppContext context() {
        AppContext ctx = contextHolder.get();
        if (ctx == null) {
            throw new IllegalStateException("No AppContext available — not inside execute()");
        }
        return ctx;
    }

    /**
     * Returns programs indexed by declared COBOL name (deduplicated). The internal map has multiple
     * keys per program (uppercase, lowercase, bean name) for lookup flexibility, but this method
     * returns only the canonical entries.
     */
    public Map<String, AppProgram> getPrograms() {
        Map<String, AppProgram> result = new java.util.LinkedHashMap<>();
        for (AppProgram p : programs.values()) {
            result.putIfAbsent(p.getProgramName(), p);
        }
        return result;
    }

    // --- Registration ---

    public void registerFsetFields(String mapName, Set<String> fields) {
        mapFsetRegistry.put(mapName, fields);
    }

    public Set<String> getFsetFields(String mapName) {
        return mapFsetRegistry.getOrDefault(mapName, Set.of());
    }

    // --- AppService implementation ---

    @Override
    public void sendMap(
            String mapName,
            Object mapOutput,
            Object mapInput,
            boolean eraseScreen,
            boolean freekb,
            boolean cursor) {
        AppContext ctx = context();
        ctx.setEibfn(EIBFN_SEND_MAP);
        ctx.setMapName(mapName);
        ctx.setLastMapOutput(mapOutput);
        ctx.setLastMapInput(mapInput);
        ctx.setEraseScreen(eraseScreen);
        ctx.setFreekb(freekb);
        // CURSOR option: actual cursor field resolved from cursors map in buildResponse()
        // (COBOL sets cursor via MOVE -1 TO fieldL, not via SEND MAP CURSOR option directly)
        ctx.setEibresp(AppResp.NORMAL);
    }

    @Override
    public void sendMapOnly(String mapName, Object mapOutput, boolean eraseScreen) {
        sendMap(mapName, mapOutput, null, eraseScreen, false, false);
    }

    @Override
    public void rollback() {
        // EXEC CICS SYNCPOINT ROLLBACK: back out the CURRENT unit-of-work (mọi ghi từ
        // syncpoint gần nhất) rồi mở UOW mới — chương trình chạy tiếp, ghi sau đó vẫn giữ.
        if (txManager == null) {
            return;
        }
        TransactionStatus st = uow.get();
        if (st != null && !st.isCompleted()) {
            txManager.rollback(st);
        }
        runUowCompletions(false);
        beginUow();
    }

    @Override
    public void syncpoint() {
        // EXEC CICS SYNCPOINT: commit unit-of-work hiện hành NGAY (CICS-faithful — trước đây
        // no-op nên fail-sau-syncpoint làm mất cả phần đã syncpoint), rồi mở UOW mới.
        if (txManager == null) {
            return;
        }
        TransactionStatus st = uow.get();
        if (st != null && !st.isCompleted()) {
            txManager.commit(st);
        }
        runUowCompletions(true);
        beginUow();
    }

    @Override
    public void sendText(String text, boolean eraseScreen, boolean freekb) {
        AppContext ctx = context();
        ctx.setSendTextContent(text);
        ctx.setEraseScreen(eraseScreen);
        ctx.setFreekb(freekb);
        ctx.setEibresp(AppResp.NORMAL);
    }

    @Override
    public void sendFrom(Object data, boolean eraseScreen, boolean freekb) {
        AppContext ctx = context();
        String text;
        if (data instanceof String) {
            text = (String) data;
        } else {
            // Serialize POJO to string via Utility.groupToString (reflection)
            try {
                Class<?> helper = findHelperClass();
                java.lang.reflect.Method m = helper.getMethod("groupToString", Object.class);
                text = (String) m.invoke(null, data);
            } catch (Exception e) {
                text = data.toString();
            }
        }
        ctx.setSendTextContent(text);
        ctx.setEraseScreen(eraseScreen);
        ctx.setFreekb(freekb);
        ctx.setEibresp(AppResp.NORMAL);
    }

    @Override
    public void receiveMap(String mapName, Object mapInput) {
        AppContext ctx = context();
        ctx.setEibfn(EIBFN_RECEIVE_MAP);
        ctx.setMapName(mapName);

        // B-1: Detect MAPFAIL condition — CLEAR key or no input data
        String aidKey = ctx.getEibaid();
        if ("CLEAR".equalsIgnoreCase(aidKey)) {
            ctx.setEibresp(AppResp.MAPFAIL);
            return;
        }
        java.util.Map<String, String> inputFields = ctx.getInputFields();
        if (inputFields == null || inputFields.isEmpty()) {
            ctx.setEibresp(AppResp.MAPFAIL);
            return;
        }

        // Bind terminal input fields into the map input object using explicit field mapping
        if (mapInput != null) {
            AppProgram program = currentProgram.get();
            FieldMapping fieldMapping =
                    (program != null) ? program.getFieldMapping(mapName) : FieldMapping.empty();
            MapBinder.bind(inputFields, mapInput, fieldMapping);
        } else {
            log.warn("RECEIVE MAP '{}' — no mapInput provided, terminal fields not bound", mapName);
        }
        ctx.setEibresp(AppResp.NORMAL);
    }

    @Override
    public void setCommarea(Object commarea) {
        context().setCommarea(commarea);
    }

    @Override
    public void returnTransid(String transid, Object commarea, int length) {
        context().setEibfn(EIBFN_RETURN);
        // When LENGTH=0 but commarea is present, CICS uses the actual commarea size.
        // This happens when COBOL omits LENGTH in EXEC CICS RETURN TRANSID COMMAREA(x).
        int effectiveLength = length;
        if (effectiveLength == 0 && commarea != null) {
            effectiveLength = calculateCommareaLength(commarea);
        }
        throw new ReturnException(transid, commarea, effectiveLength);
    }

    @Override
    public void returnProgram() {
        context().setEibfn(EIBFN_RETURN);
        throw new ReturnException(null, null, 0);
    }

    @Override
    public void xctl(String program, Object commarea, int length) {
        context().setEibfn(EIBFN_XCTL);
        AppProgram target = programs.get(programKey(program));
        if (target == null) {
            log.warn("XCTL target program '{}' not found — setting EIBRESP=ERROR", program);
            context().setEibresp(AppResp.ERROR);
            return;
        }
        // XCTL = transfer control to another program.
        // Real CICS preserves commarea LENGTH on XCTL — target sees EIBCALEN = LENGTH.
        // This is critical for programs that check EIBCALEN > 0 to distinguish
        // first-time entry (no commarea) vs transfer with data.
        // When LENGTH=0 but commarea present, use actual size (COBOL omits LENGTH)
        int effectiveLength = length;
        if (effectiveLength == 0 && commarea != null) {
            effectiveLength = calculateCommareaLength(commarea);
        }
        context().setCommarea(commarea);
        context().setEibcalen(effectiveLength);
        currentProgram.set(target);
        try {
            target.mainLine(this);
        } catch (ReturnException e) {
            // Target did RETURN TRANSID — propagate to terminate original caller too
            throw e;
        }
        // XCTL = permanent transfer: original program must NOT continue
        throw new ReturnException(null, commarea, length);
    }

    @Override
    public void callProgram(String program, Object[] params) {
        context().setEibfn(EIBFN_LINK);

        // Route MQ calls to MqRunner (if configured) or stub
        String upper = programKey(program);
        if (mqRunner != null) {
            switch (upper) {
                case "MQOPEN":
                    mqRunner.mqOpen(params);
                    setEibrespFromMq(params, 4);
                    return;
                case "MQGET":
                    mqRunner.mqGet(params);
                    setEibrespFromMq(params, 7);
                    return;
                case "MQPUT":
                    mqRunner.mqPut(params);
                    setEibrespFromMq(params, 6);
                    return;
                case "MQPUT1":
                    mqRunner.mqPut1(params);
                    setEibrespFromMq(params, 6);
                    return;
                case "MQCLOSE":
                    mqRunner.mqClose(params);
                    setEibrespFromMq(params, 3);
                    return;
                default:
                    break;
            }
        } else if (upper.startsWith("MQ")) {
            // MQ call but no broker configured — stub mode returns MQ-level failure codes
            // Programs handle these gracefully (MQGET→no message→exit loop, MQPUT→error handler)
            log.warn("MQ CALL '{}' — no JMS broker configured, stub mode", program);
            stubMqCall(upper, params);
            context().setEibresp(AppResp.NORMAL); // stub always OK
            return;
        }

        AppProgram target = programs.get(upper);
        if (target == null) {
            log.warn("CALL target program '{}' not found", program);
            context().setEibresp(AppResp.ERROR);
            return;
        }
        // COBOL CALL USING: direct subroutine call.
        // Pass Object[] as commarea so callee can read/write BY REFERENCE params.
        // Save/restore caller's context — called program may modify commarea/eibcalen.
        Object savedCommarea = context().getCommarea();
        int savedCalen = context().getEibcalen();
        context().setCommarea(params);
        context().setEibcalen(params.length);
        try {
            target.mainLine(this);
        } catch (ReturnException e) {
            // GOBACK in called program — absorb and continue
        } finally {
            context().setCommarea(savedCommarea);
            context().setEibcalen(savedCalen);
        }
    }

    @Override
    public void link(String program, Object commarea, int length) {
        context().setEibfn(EIBFN_LINK);
        AppProgram target = programs.get(programKey(program));
        if (target == null) {
            context().setEibresp(AppResp.ERROR);
            return;
        }
        Object savedCommarea = context().getCommarea();
        int savedCalen = context().getEibcalen();
        // Save caller's browse state — LINK target may STARTBR on same files
        Map<String, BrowseContext> savedBrowse = new java.util.HashMap<>(browseState.get());
        context().setCommarea(commarea);
        context().setEibcalen(length);
        try {
            target.mainLine(this);
        } catch (ReturnException e) {
            // LINK target did RETURN — absorb and continue
        } finally {
            // LINK is in/out: the callee published its COMMAREA region back into the context (via
            // its
            // mainLine finally). Copy those bytes into the caller's array so the caller sees
            // changes.
            Object result = context().getCommarea();
            if (commarea instanceof byte[] && result instanceof byte[] && result != commarea) {
                byte[] dst = (byte[]) commarea;
                byte[] src = (byte[]) result;
                System.arraycopy(src, 0, dst, 0, Math.min(src.length, dst.length));
            }
            context().setCommarea(savedCommarea);
            context().setEibcalen(savedCalen);
            browseState.get().clear();
            browseState.get().putAll(savedBrowse);
        }
    }

    // --- File I/O ---

    private FileDao getDao(String fileName) {
        // COBOL file names are PIC X(08) — always padded with spaces. Trim for lookup.
        String key = fileName != null ? fileName.trim() : "";
        FileDao dao = fileDaos.get(key);
        if (dao == null) {
            log.warn(
                    "getDao('{}') — no FileDao registered (available: {})", key, fileDaos.keySet());
            context().setEibresp(AppResp.NOTOPEN);
            return null;
        }
        return dao;
    }

    @Override
    public void readFile(String fileName, Object into, String ridfld, int keyLength) {
        context().setEibfn(EIBFN_READ);
        FileDao dao = getDao(fileName);
        if (dao == null) {
            // CICS: dataset không được cài đặt → FILENOTFOUND(12). Return im lặng để
            // EIBRESP STALE của lệnh trước là bug (audit middleware round-1: chương trình
            // check RESP thấy NORMAL giả dù INTO không được ghi).
            context().setEibresp(AppResp.FILENOTFOUND);
            return;
        }
        try {
            Object result = dao.readByKey(ridfld);
            if (result == null) {
                context().setEibresp(AppResp.NOTFND);
            } else {
                copyFields(result, into);
                context().setEibresp(AppResp.NORMAL);
            }
        } catch (FileException e) {
            context().setEibresp(e.getResp());
            context().setEibresp2(e.getResp2());
        }
    }

    @Override
    public void readFileForUpdate(String fileName, Object into, String ridfld, int keyLength) {
        context().setEibfn(EIBFN_READ);
        FileDao dao = getDao(fileName);
        if (dao == null) {
            // CICS: dataset không được cài đặt → FILENOTFOUND(12). Return im lặng để
            // EIBRESP STALE của lệnh trước là bug (audit middleware round-1: chương trình
            // check RESP thấy NORMAL giả dù INTO không được ghi).
            context().setEibresp(AppResp.FILENOTFOUND);
            return;
        }
        try {
            Object result = dao.readByKeyForUpdate(ridfld);
            if (result == null) {
                context().setEibresp(AppResp.NOTFND);
            } else {
                copyFields(result, into);
                // Remember key so a subsequent DELETE/REWRITE without RIDFLD can use it
                updateCursorKey.get().put(fileName, ridfld);
                context().setEibresp(AppResp.NORMAL);
            }
        } catch (FileException e) {
            context().setEibresp(e.getResp());
            context().setEibresp2(e.getResp2());
        }
    }

    @Override
    public void writeFile(String fileName, Object from, String ridfld, int keyLength) {
        context().setEibfn(EIBFN_WRITE);
        FileDao dao = getDao(fileName);
        if (dao == null) {
            // CICS: dataset không được cài đặt → FILENOTFOUND(12). Return im lặng để
            // EIBRESP STALE của lệnh trước là bug (audit middleware round-1: chương trình
            // check RESP thấy NORMAL giả dù INTO không được ghi).
            context().setEibresp(AppResp.FILENOTFOUND);
            return;
        }
        try {
            dao.write(from, ridfld);
            context().setEibresp(AppResp.NORMAL);
        } catch (FileException e) {
            context().setEibresp(e.getResp());
            context().setEibresp2(e.getResp2());
        }
    }

    @Override
    public void rewriteFile(String fileName, Object from) {
        context().setEibfn(EIBFN_REWRITE);
        // CICS: REWRITE chỉ hợp lệ sau READ UPDATE trong cùng UOW → INVREQ(16) nếu không
        if (!updateCursorKey.get().containsKey(fileName)) {
            context().setEibresp(AppResp.INVREQ);
            return;
        }
        FileDao dao = getDao(fileName);
        if (dao == null) {
            // CICS: dataset không được cài đặt → FILENOTFOUND(12). Return im lặng để
            // EIBRESP STALE của lệnh trước là bug (audit middleware round-1: chương trình
            // check RESP thấy NORMAL giả dù INTO không được ghi).
            context().setEibresp(AppResp.FILENOTFOUND);
            return;
        }
        try {
            dao.rewrite(from);
            updateCursorKey.get().remove(fileName);
            context().setEibresp(AppResp.NORMAL);
        } catch (FileException e) {
            context().setEibresp(e.getResp());
            context().setEibresp2(e.getResp2());
        }
    }

    @Override
    public void deleteFile(String fileName, String ridfld, int keyLength) {
        context().setEibfn(EIBFN_DELETE);
        FileDao dao = getDao(fileName);
        if (dao == null) {
            // CICS: dataset không được cài đặt → FILENOTFOUND(12). Return im lặng để
            // EIBRESP STALE của lệnh trước là bug (audit middleware round-1: chương trình
            // check RESP thấy NORMAL giả dù INTO không được ghi).
            context().setEibresp(AppResp.FILENOTFOUND);
            return;
        }
        // CICS cursor delete: no RIDFLD supplied → use key from preceding READ UPDATE
        Object effectiveKey = (ridfld != null) ? ridfld : updateCursorKey.get().get(fileName);
        if (effectiveKey == null) {
            // CICS: DELETE không RIDFLD yêu cầu READ UPDATE trước đó → INVREQ(16)
            context().setEibresp(AppResp.INVREQ);
            return;
        }
        try {
            dao.delete(effectiveKey);
            updateCursorKey.get().remove(fileName);
            context().setEibresp(AppResp.NORMAL);
        } catch (FileException e) {
            context().setEibresp(e.getResp());
            context().setEibresp2(e.getResp2());
        }
    }

    // --- Browse ---

    @Override
    public void startBrowse(String fileName, String ridfld, int keyLength) {
        context().setEibfn(EIBFN_STARTBR);
        FileDao dao = getDao(fileName);
        if (dao == null) {
            // CICS: dataset không được cài đặt → FILENOTFOUND(12). Return im lặng để
            // EIBRESP STALE của lệnh trước là bug (audit middleware round-1: chương trình
            // check RESP thấy NORMAL giả dù INTO không được ghi).
            context().setEibresp(AppResp.FILENOTFOUND);
            return;
        }
        try {
            String normalizedKey = ridfld != null ? ridfld.replace('\u0000', ' ') : "";
            // Load records around the key for bidirectional browse:
            // Forward: records >= key (for READNEXT)
            // Backward: records < key via startBrowsePrev (for READPREV)
            // Combine into single ASC list, find key position.
            List<Object> forward = dao.startBrowse(normalizedKey);
            List<Object> backward = dao.startBrowsePrev(normalizedKey);
            // backward is DESC — reverse to ASC, then remove the key record if duplicated.
            java.util.Collections.reverse(backward);
            // Remove backward's last record only when it actually duplicates forward[0] (the key
            // exists in the file). With GTEQ positioning on an absent key, backward's last record
            // is the largest record BELOW the key — a distinct record READPREV must still return.
            if (!backward.isEmpty()
                    && !forward.isEmpty()
                    && backward.get(backward.size() - 1).equals(forward.get(0))) {
                backward.remove(backward.size() - 1);
            }
            // Combine: backward (< key, ASC) + forward (>= key, ASC)
            List<Object> combined = new java.util.ArrayList<>(backward.size() + forward.size());
            combined.addAll(backward);
            combined.addAll(forward);
            int startPos = backward.size(); // key is at this index
            log.debug(
                    "STARTBR {} key='{}' → {} records ({}+{}), position={}",
                    fileName,
                    normalizedKey,
                    combined.size(),
                    backward.size(),
                    forward.size(),
                    startPos);
            BrowseContext ctx = new BrowseContext(combined, normalizedKey, fileName);
            ctx.cursor = startPos - 1; // before key (first READNEXT/READPREV reads AT key)
            browseState.get().put(fileName, ctx);
            context().setEibresp(AppResp.NORMAL);
        } catch (FileException e) {
            context().setEibresp(e.getResp());
            context().setEibresp2(e.getResp2());
        }
    }

    @Override
    public void readNext(String fileName, Object into) {
        context().setEibfn(EIBFN_READNEXT);
        BrowseContext ctx = browseState.get().get(fileName);
        if (ctx == null) {
            context().setEibresp(AppResp.INVREQ);
            return;
        }

        // Advance cursor: first read after STARTBR reads AT key position
        int nextIdx;
        if (ctx.lastDir == BrowseDir.NONE) {
            // First read after STARTBR: read AT key position (cursor+1)
            nextIdx = ctx.cursor + 1;
        } else if (ctx.lastDir == BrowseDir.NEXT) {
            // Continuing forward: next record after last returned
            nextIdx = ctx.cursor + 1;
        } else {
            // Switching from PREV to NEXT: skip the record already returned by last READPREV,
            // then advance one more (CICS skips current on direction change)
            nextIdx = ctx.cursor + 2;
        }

        if (nextIdx >= ctx.allRecords.size() || nextIdx < 0) {
            log.debug(
                    "READNEXT {} — ENDFILE (idx={}, size={})",
                    fileName,
                    nextIdx,
                    ctx.allRecords.size());
            context().setEibresp(AppResp.ENDFILE);
            return;
        }
        ctx.cursor = nextIdx;
        ctx.lastDir = BrowseDir.NEXT;
        copyFields(ctx.allRecords.get(nextIdx), into);
        context().setEibresp(AppResp.NORMAL);
    }

    @Override
    public void readPrev(String fileName, Object into) {
        context().setEibfn(EIBFN_READPREV);
        BrowseContext ctx = browseState.get().get(fileName);
        if (ctx == null) {
            context().setEibresp(AppResp.INVREQ);
            return;
        }

        int prevIdx;
        if (ctx.lastDir == BrowseDir.NONE) {
            // First read after STARTBR: read AT key position (cursor+1)
            prevIdx = ctx.cursor + 1;
        } else if (ctx.lastDir == BrowseDir.PREV) {
            // Continuing backward: previous record before last returned
            prevIdx = ctx.cursor - 1;
        } else {
            // Switching from NEXT to PREV: skip the record already returned by last READNEXT,
            // then retreat one more
            prevIdx = ctx.cursor - 2;
        }

        if (prevIdx < 0 || prevIdx >= ctx.allRecords.size()) {
            log.debug("READPREV {} — ENDFILE (idx={})", fileName, prevIdx);
            context().setEibresp(AppResp.ENDFILE);
            return;
        }
        ctx.cursor = prevIdx;
        ctx.lastDir = BrowseDir.PREV;
        copyFields(ctx.allRecords.get(prevIdx), into);
        context().setEibresp(AppResp.NORMAL);
    }

    @Override
    public void resetBrowse(String fileName, String ridfld, int keyLength) {
        context().setEibfn(EIBFN_STARTBR);
        BrowseContext existing = browseState.get().get(fileName);
        if (existing == null) {
            context().setEibresp(AppResp.INVREQ);
            return;
        }
        FileDao dao = getDao(fileName);
        if (dao == null) {
            context().setEibresp(AppResp.FILENOTFOUND);
            return;
        }
        // CICS: RESETBR ≡ reposition với semantics như STARTBR — phải bidirectional
        // (bản cũ chỉ load forward → READPREV sau RESETBR bị ENDFILE sớm).
        browseState.get().remove(fileName);
        startBrowse(fileName, ridfld, keyLength);
    }

    @Override
    public void endBrowse(String fileName) {
        context().setEibfn(EIBFN_ENDBR);
        browseState.get().remove(fileName);
        context().setEibresp(AppResp.NORMAL);
    }

    // --- Interval Control (START/RETRIEVE) ---

    /**
     * Start-data theo TRANSID (region-wide như CICS) — STARTed task RETRIEVE đọc theo EIBTRNID.
     * Web-runtime KHÔNG tự chạy async task: task đích chạy khi được invoke qua terminal; MQ-trigger
     * pattern (MQTM) của corpus đi qua đường này.
     */
    private final Map<String, java.util.Queue<Object>> startData = new ConcurrentHashMap<>();

    @Override
    public void startTransaction(String transid, Object fromData, int length) {
        context().setEibfn(EIBFN_START);
        if (transid == null || transid.isBlank()) {
            context().setEibresp(AppResp.TRANSIDERR);
            return;
        }
        startData
                .computeIfAbsent(
                        transid.trim().toUpperCase(),
                        k -> new java.util.concurrent.ConcurrentLinkedQueue<>())
                .add(deepCopy(fromData));
        context().setEibresp(AppResp.NORMAL);
    }

    @Override
    public void retrieve(Object into) {
        context().setEibfn(EIBFN_RETRIEVE);
        String transid = context().getEibtrnid();
        java.util.Queue<Object> q =
                (transid == null || transid.isBlank())
                        ? null
                        : startData.get(transid.trim().toUpperCase());
        Object data = (q != null) ? q.poll() : null;
        if (data == null) {
            // CICS: task không có (hoặc đã đọc hết) start-data → ENDDATA. Trước đây RETRIEVE
            // bị no-op comment → RESP-var giữ default 0, chương trình trigger-monitor tưởng
            // đọc được MQTM rỗng (audit gap-inventory).
            context().setEibresp(AppResp.ENDDATA);
            return;
        }
        copyFields(data, into);
        context().setEibresp(AppResp.NORMAL);
    }

    // --- Temporary Storage Queues ---

    @Override
    public void writeQueue(String queueName, Object from, boolean rewrite) {
        writeQueue(queueName, from, rewrite, 0);
    }

    @Override
    public void writeQueue(String queueName, Object from, boolean rewrite, int item) {
        context().setEibfn(EIBFN_WRITEQ_TS);
        queueName = queueKey(queueName);
        Object copy = deepCopy(from);
        java.util.List<Object> items =
                tsQueues.computeIfAbsent(
                        queueName,
                        k -> java.util.Collections.synchronizedList(new java.util.ArrayList<>()));
        if (rewrite) {
            // REWRITE: ITEM(n) 1-based thay đúng item n; item==0 (không ITEM) → thay item cuối.
            int idx = (item > 0) ? item - 1 : items.size() - 1;
            if (idx < 0 || idx >= items.size()) {
                context().setEibresp(AppResp.ITEMERR);
                return;
            }
            items.set(idx, copy);
        } else {
            // WRITEQ mới: append. CICS trả item-number được cấp qua data-area ITEM — caller
            // đọc lại field ITEM; ở đây item là input nên không ghi ngược (COBOL byte-model
            // đọc số item từ RESP path riêng nếu cần).
            items.add(copy);
        }
        context().setEibresp(AppResp.NORMAL);
    }

    /** Per-queue read position for READQ TS NEXT semantics */
    private final Map<String, java.util.concurrent.atomic.AtomicInteger> tsQueuePositions =
            new ConcurrentHashMap<>();

    public void readQueue(String queueName, Object into) {
        readQueue(queueName, into, 0);
    }

    /**
     * READQ TS with ITEM support. item=0 means NEXT (sequential read). item>0 reads specific item
     * (1-based).
     */
    public void readQueue(String queueName, Object into, int item) {
        context().setEibfn(EIBFN_READQ_TS);
        queueName = queueKey(queueName);
        java.util.List<Object> items = tsQueues.get(queueName);
        if (items == null || items.isEmpty()) {
            context().setEibresp(AppResp.QIDERR);
            return;
        }
        int idx;
        if (item > 0) {
            // ITEM(n): read specific item (1-based)
            idx = item - 1;
        } else {
            // NEXT: sequential read from current position
            var pos =
                    tsQueuePositions.computeIfAbsent(
                            queueName, k -> new java.util.concurrent.atomic.AtomicInteger(0));
            idx = pos.getAndIncrement();
        }
        if (idx < 0 || idx >= items.size()) {
            context().setEibresp(AppResp.ITEMERR);
            return;
        }
        copyFields(items.get(idx), into);
        context().setEibresp(AppResp.NORMAL);
    }

    @Override
    public void deleteQueue(String queueName) {
        context().setEibfn(EIBFN_DELETEQ_TS);
        queueName = queueKey(queueName);
        tsQueues.remove(queueName);
        // Queue tạo lại sau DELETEQ phải đọc NEXT từ item 1 — vị trí cũ là bug ITEMERR giả
        tsQueuePositions.remove(queueName);
        context().setEibresp(AppResp.NORMAL);
    }

    // --- Transient Data (TD) Queues ---

    @Override
    public void writeQueueTd(String queueName, Object from, int length) {
        context().setEibfn(EIBFN_WRITEQ_TD);
        queueName = queueKey(queueName);
        Object copy = deepCopy(from);
        tdQueues.computeIfAbsent(queueName, k -> new java.util.concurrent.ConcurrentLinkedQueue<>())
                .add(copy);
        log.info("WRITEQ TD: destination={}, record written (length={})", queueName, length);
        context().setEibresp(AppResp.NORMAL);
    }

    @Override
    public void readQueueTd(String queueName, Object into, int length) {
        context().setEibfn(EIBFN_READQ_TD);
        queueName = queueKey(queueName);
        java.util.Queue<Object> queue = tdQueues.get(queueName);
        if (queue == null || queue.isEmpty()) {
            context().setEibresp(AppResp.QIDERR);
            return;
        }
        Object data = queue.poll();
        copyFields(data, into);
        context().setEibresp(AppResp.NORMAL);
    }

    @Override
    public void deleteQueueTd(String queueName) {
        context().setEibfn(EIBFN_DELETEQ_TD);
        queueName = queueKey(queueName);
        tdQueues.remove(queueName);
        context().setEibresp(AppResp.NORMAL);
    }

    // --- Time ---

    @Override
    public void askTime() {
        context().setEibfn(EIBFN_ASKTIME);
        abstimeHolder.set(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        context().setEibresp(AppResp.NORMAL);
    }

    @Override
    public FormatTimeResult formatTime(
            String abstime, String dateForm, String dateSep, String timeSep) {
        context().setEibfn(EIBFN_FORMATTIME);
        // ABSTIME channel: passed value → askTime() ThreadLocal → now.
        // A valid channel value is always yyyyMMddHHmmss (14 chars); anything shorter is
        // not a formattable timestamp (e.g. a never-written COBOL field in legacy mode).
        String time = abstime;
        if (time == null || time.length() < 14) {
            time = abstimeHolder.get();
        }
        if (time == null || time.length() < 14) {
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        context().setEibresp(AppResp.NORMAL);
        String yyyy = time.substring(0, 4);
        String mm = time.substring(4, 6);
        String dd = time.substring(6, 8);
        String ds = dateSep == null ? "" : dateSep;
        String date =
                switch (dateForm == null ? "YYYYMMDD" : dateForm) {
                    case "DDMMYY" -> dd + ds + mm + ds + yyyy.substring(2);
                    case "MMDDYY" -> mm + ds + dd + ds + yyyy.substring(2);
                    case "YYMMDD" -> yyyy.substring(2) + ds + mm + ds + dd;
                    case "YYDDMM" -> yyyy.substring(2) + ds + dd + ds + mm;
                    case "DDMMYYYY" -> dd + ds + mm + ds + yyyy;
                    case "MMDDYYYY" -> mm + ds + dd + ds + yyyy;
                    default -> yyyy + ds + mm + ds + dd;
                };
        String ts = timeSep == null ? "" : timeSep;
        String timeStr =
                time.substring(8, 10) + ts + time.substring(10, 12) + ts + time.substring(12, 14);
        return new FormatTimeResult(date, timeStr);
    }

    // --- Miscellaneous ---

    @Override
    public void abend(String abcode) {
        context().setEibfn(EIBFN_ABEND);
        throw new AbendException(abcode);
    }

    @Override
    public void registerAbendHandler(Runnable handler) {
        context().setAbendHandler(handler);
    }

    @Override
    public void assign(Object target, String field) {
        context().setEibfn(EIBFN_ASSIGN);
        // Route ASSIGN fields to EIB/system values
        if (field != null && target != null) {
            String upper = field.toUpperCase();
            Object value =
                    switch (upper) {
                        case "APPLID" -> "CICS";
                        case "USERID" -> "";
                        case "SYSID" -> "SYS1";
                        case "TRMIDNT", "TERMID" -> getEibtrmid();
                        case "EIBDATE" -> getEibdate();
                        case "EIBTIME" -> getEibtime();
                        case "EIBCALEN" -> getEibcalen();
                        case "EIBTRNID" -> getEibtrnid();
                        default -> null;
                    };
            if (value != null) {
                if (target instanceof FieldStore fs) {
                    // Byte model: write by COBOL name. ASSIGN to a field absent from the
                    // program's storage is a CICS no-op (the option simply isn't captured).
                    if (fs.has(field)) {
                        fs.setString(field, String.valueOf(value));
                    }
                } else {
                    try {
                        var f =
                                target.getClass()
                                        .getDeclaredField(field.toLowerCase().replace("-", ""));
                        f.setAccessible(true);
                        if (f.getType() == String.class) {
                            f.set(target, String.valueOf(value));
                        } else if (f.getType() == int.class)
                            f.setInt(target, ((Number) value).intValue());
                    } catch (ReflectiveOperationException ignored) {
                        // Field not found in target — ASSIGN to non-existent field is no-op
                    }
                }
            }
        }
        context().setEibresp(AppResp.NORMAL);
    }

    // --- Resource Locking (ENQ/DEQ) ---

    private static final java.util.concurrent.ConcurrentHashMap<
                    String, java.util.concurrent.locks.ReentrantLock>
            resourceLocks = new java.util.concurrent.ConcurrentHashMap<>();

    // Resources ENQ'd by the CURRENT task (request). CICS releases all task-owned ENQs at task end
    // (syncpoint / task termination) — a COBOL program may EXEC CICS ENQ without an explicit DEQ
    // and
    // rely on this. Tracked per-request so execute()'s finally can auto-release them; otherwise the
    // ReentrantLock stays owned by a pooled request thread and the next task that ENQs the same
    // resource on a different thread blocks forever.
    private static final ThreadLocal<java.util.Set<String>> heldEnqueues =
            ThreadLocal.withInitial(java.util.LinkedHashSet::new);

    @Override
    public void enqueue(String resource) {
        context().setEibresp(AppResp.NORMAL);
        if (resource == null) {
            return;
        }
        String key = queueKey(resource).toUpperCase();
        resourceLocks
                .computeIfAbsent(key, k -> new java.util.concurrent.locks.ReentrantLock())
                .lock();
        heldEnqueues.get().add(key);
    }

    @Override
    public void dequeue(String resource) {
        context().setEibresp(AppResp.NORMAL);
        if (resource == null) {
            return;
        }
        String key = queueKey(resource).toUpperCase();
        var lock = resourceLocks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            if (!lock.isHeldByCurrentThread()) {
                heldEnqueues.get().remove(key);
            }
        }
    }

    /** CICS task-end ENQ release: fully unlock every resource the current task still holds. */
    private void releaseTaskEnqueues() {
        for (String key : heldEnqueues.get()) {
            var lock = resourceLocks.get(key);
            if (lock != null) {
                while (lock.isHeldByCurrentThread()) lock.unlock();
            }
        }
    }

    @Override
    public int getEibresp() {
        return context().getEibresp();
    }

    @Override
    public int getEibcalen() {
        return context().getEibcalen();
    }

    @Override
    public String getEibaid() {
        // Return DFHAID-compatible single-char value for COBOL EVALUATE EIBAID comparisons
        return aidKeyToDfhaid(context().getEibaid());
    }

    /** Map HTTP AidKey string to DFHAID single-character value used by COBOL programs. */
    private static String aidKeyToDfhaid(String httpAidKey) {
        if (httpAidKey == null) return "'"; // DFHENTER
        switch (httpAidKey.toUpperCase()) {
            case "ENTER":
                return "'"; // DFHENTER X'7D'
            case "CLEAR":
                return "_"; // DFHCLEAR X'6D'
            case "PA1":
                return "%"; // DFHPA1 X'6C'
            case "PA2":
                return ">"; // DFHPA2 X'6E'
            case "PA3":
                return ","; // DFHPA3 X'6B'
            case "PF1":
                return "1"; // DFHPF1 X'F1'
            case "PF2":
                return "2";
            case "PF3":
                return "3";
            case "PF4":
                return "4";
            case "PF5":
                return "5";
            case "PF6":
                return "6";
            case "PF7":
                return "7";
            case "PF8":
                return "8";
            case "PF9":
                return "9";
            case "PF10":
                return ":"; // DFHPF10 X'7A'
            case "PF11":
                return "#"; // DFHPF11 X'7B'
            case "PF12":
                return "@"; // DFHPF12 X'7C'
            case "PF13":
                return "A"; // DFHPF13 X'C1'
            case "PF14":
                return "B";
            case "PF15":
                return "C";
            case "PF16":
                return "D";
            case "PF17":
                return "E";
            case "PF18":
                return "F";
            case "PF19":
                return "G";
            case "PF20":
                return "H";
            case "PF21":
                return "I";
            case "PF22":
                return "J"; // DFHPF22 X'D1'
            case "PF23":
                return "K";
            case "PF24":
                return "L";
            default:
                return httpAidKey;
        }
    }

    @Override
    public int getEibresp2() {
        return context().getEibresp2();
    }

    @Override
    public String getEibfn() {
        return context().getEibfn();
    }

    @Override
    public String getEibtrmid() {
        return context().getEibtrmid();
    }

    @Override
    public int getEibdate() {
        return context().getEibdate();
    }

    @Override
    public int getEibtime() {
        return context().getEibtime();
    }

    @Override
    public String getEibtrnid() {
        return context().getEibtrnid();
    }

    @Override
    public Object getCommarea() {
        return context().getCommarea();
    }

    // Minimum padding to prevent substring(n, n+k) out-of-bounds when caller's commarea
    // is smaller than the target program's commarea layout (XCTL commarea size mismatch).
    /**
     * Minimum commarea pad size — CICS allows up to 32767 bytes. Dynamically expanded as needed.
     */
    private static final int COMMAREA_DEFAULT_PAD = 4096;

    private static final int COMMAREA_MAX_PAD = 32767;

    @Override
    public String getSerializedCommarea() {
        Object commarea = context().getCommarea();
        // Use declared EIBCALEN for padding size, fall back to default
        int padSize = Math.max(context().getEibcalen(), COMMAREA_DEFAULT_PAD);
        padSize = Math.min(padSize, COMMAREA_MAX_PAD);
        if (commarea == null) {
            return " ".repeat(padSize);
        }
        if (commarea instanceof String s) {
            return s.length() >= padSize ? s : s + " ".repeat(padSize - s.length());
        }
        // Byte-model COMMAREA: the bytes of the COBOL region (from f.sliceBytes), encoded in the
        // field charset. Decode back to a String so the receiving program's MOVE DFHCOMMAREA(1:n)
        // TO xxx-COMMAREA repopulates its byte-backed fields (e.g. CDEMO-PGM-CONTEXT).
        if (commarea instanceof byte[] b) {
            String s = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            return s.length() >= padSize ? s : s + " ".repeat(padSize - s.length());
        }
        // Serialize POJO commarea to string via Utility.groupToString()
        try {
            Class<?> helperClass = findHelperClass();
            java.lang.reflect.Method groupToString =
                    helperClass.getMethod("groupToString", Object.class);
            String serialized = (String) groupToString.invoke(null, commarea);
            log.debug(
                    "getSerializedCommarea: class={} len={}",
                    commarea.getClass().getSimpleName(),
                    serialized.length());
            return serialized.length() >= COMMAREA_DEFAULT_PAD
                    ? serialized
                    : serialized + " ".repeat(COMMAREA_DEFAULT_PAD - serialized.length());
        } catch (Exception e) {
            log.warn("getSerializedCommarea: cannot serialize commarea — {}", e.getMessage());
            return " ".repeat(COMMAREA_DEFAULT_PAD);
        }
    }

    // --- DL/I and MQ service accessors ---

    @Override
    public DliService getDliService() {
        return dliRunner;
    }

    /** Set the MQ service implementation. Called by MqAutoConfig. */
    public void setMqRunner(MqService mqRunner) {
        this.mqRunner = mqRunner;
    }

    /**
     * Set the DL/I service implementation. Called by DliAutoConfig when spring-jdbc is on
     * classpath.
     */
    public void setDliRunner(DliService dliRunner) {
        this.dliRunner = dliRunner;
    }

    /**
     * Set EIBRESP from MQ completion code at given param index. MQCC_OK (0) → NORMAL, MQCC_FAILED
     * (2) → ERROR.
     */
    private void setEibrespFromMq(Object[] params, int compCodeIndex) {
        if (compCodeIndex < params.length && params[compCodeIndex] instanceof Number n) {
            context().setEibresp(n.intValue() == 0 ? AppResp.NORMAL : AppResp.ERROR);
        } else {
            context().setEibresp(AppResp.NORMAL);
        }
    }

    /**
     * Stub MQ call — set completion code to OK in output params without actual messaging. Allows
     * programs with MQ calls to run without a JMS broker configured.
     */
    private void stubMqCall(String command, Object[] params) {
        switch (command) {
            case "MQOPEN":
                // params: [hconn, objDesc, options, hobj(out), compCode(out), reason(out)]
                if (params.length > 3) params[3] = 1; // stub handle
                if (params.length > 4) params[4] = 0; // MQCC_OK
                if (params.length > 5) params[5] = 0; // MQRC_NONE
                break;
            case "MQGET":
                // params: [hconn, hobj, msgDesc, getOpts, buffLen, buffer(out), dataLen(out),
                // compCode(out), reason(out)]
                if (params.length > 5) params[5] = ""; // empty buffer
                if (params.length > 6) params[6] = 0; // dataLen = 0
                if (params.length > 7) params[7] = 2; // MQCC_FAILED
                if (params.length > 8) params[8] = 2033; // MQRC_NO_MSG_AVAILABLE
                break;
            case "MQPUT":
            case "MQPUT1":
                // params: [hconn, objDesc, msgDesc, putOpts, buffLen, buffer, compCode(out),
                // reason(out)]
                if (params.length > 6) params[6] = 0; // MQCC_OK
                if (params.length > 7) params[7] = 0; // MQRC_NONE
                break;
            case "MQCLOSE":
                // params: [hconn, hobj, closeOpts, compCode(out)]
                if (params.length > 3) params[3] = 0; // MQCC_OK
                break;
            default:
                break;
        }
    }

    // --- Internal helpers ---

    /**
     * Create a deep copy of a POJO by instantiating a new object and recursively copying all
     * fields. Handles nested generated model classes (group items). Primitive types, Strings, and
     * boxed numbers are immutable — safe to share. Returns the original object as fallback if copy
     * fails.
     */
    /**
     * Independent byte snapshot of a {@link FieldStore} held in a TS/TD queue (see
     * deepCopy/copyFields).
     */
    private record FieldSnapshot(byte[] bytes) {}

    private static Object deepCopy(Object source) {
        if (source == null) {
            return null;
        }
        Class<?> clazz = source.getClass();
        // Immutable types — no copy needed
        if (clazz.isPrimitive()
                || source instanceof String
                || source instanceof Number
                || source instanceof Boolean
                || source instanceof Character) {
            return source;
        }
        // FieldStore (byte-model accessor): has no no-arg constructor and its state lives in byte
        // buffers, not reflectable POJO fields — the reflection path below would fail and fall back
        // to returning the live reference (queued items would then mutate with the program).
        // Snapshot
        // the bytes so the queue holds an independent point-in-time copy.
        if (source instanceof FieldStore fs) {
            return new FieldSnapshot(fs.snapshot());
        }
        // Arrays: deep copy each element
        if (clazz.isArray()) {
            int len = java.lang.reflect.Array.getLength(source);
            Object arr = java.lang.reflect.Array.newInstance(clazz.getComponentType(), len);
            for (int i = 0; i < len; i++) {
                java.lang.reflect.Array.set(
                        arr, i, deepCopy(java.lang.reflect.Array.get(source, i)));
            }
            return arr;
        }
        // Lists: deep copy each element
        if (source instanceof java.util.List<?> srcList) {
            java.util.List<Object> copyList = new java.util.ArrayList<>(srcList.size());
            for (Object item : srcList) {
                copyList.add(deepCopy(item));
            }
            return copyList;
        }
        // Maps: deep copy values
        if (source instanceof java.util.Map<?, ?> srcMap) {
            java.util.Map<Object, Object> copyMap = new java.util.LinkedHashMap<>(srcMap.size());
            for (var entry : srcMap.entrySet()) {
                copyMap.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return copyMap;
        }
        try {
            Object copy = clazz.getDeclaredConstructor().newInstance();
            Class<?> current = clazz;
            while (current != null && !current.getPackageName().startsWith("java.")) {
                for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(source);
                    if (value != null
                            && !f.getType().isPrimitive()
                            && !(value instanceof String)
                            && !(value instanceof Number)
                            && !(value instanceof Boolean)
                            && !(value instanceof Character)) {
                        // Nested POJO (group item) — recurse
                        value = deepCopy(value);
                    }
                    f.set(copy, value);
                }
                current = current.getSuperclass();
            }
            return copy;
        } catch (ReflectiveOperationException e) {
            log.debug(
                    "Deep copy failed for {}, using reference: {}",
                    clazz.getSimpleName(),
                    e.getMessage());
            return source;
        }
    }

    private static void copyFields(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        // A FieldStore snapshot (taken by deepCopy on a TS/TD queue write) restores by writing its
        // bytes straight back into the target store — the byte-exact inverse of the snapshot.
        if (source instanceof FieldSnapshot snap && target instanceof FieldStore fst) {
            fst.restore(snap.bytes());
            return;
        }
        // Byte model (Option C): the target is a FieldStore (byte record buffer). Write each
        // record field by COBOL name instead of reflecting on POJO fields (which it has none of).
        // VSAM DAO returns queryForMap → snake_case column keys (cust_name → CUST-NAME);
        // a POJO record uses camelCase getters (custName → CUST-NAME).
        if (target instanceof FieldStore fs) {
            if (source instanceof java.util.Map<?, ?> map) {
                for (java.util.Map.Entry<?, ?> e : map.entrySet()) {
                    if (e.getKey() == null || e.getValue() == null) {
                        continue;
                    }
                    String cobol = e.getKey().toString().toUpperCase().replace('_', '-');
                    if (fs.has(cobol)) {
                        fs.setString(cobol, String.valueOf(e.getValue()));
                    }
                }
            } else {
                Class<?> c = source.getClass();
                while (c != null && !c.getPackageName().startsWith("java.")) {
                    for (java.lang.reflect.Field sf : c.getDeclaredFields()) {
                        sf.setAccessible(true);
                        try {
                            Object v = sf.get(source);
                            if (v == null) {
                                continue;
                            }
                            String cobol = camelToCobolName(sf.getName());
                            if (fs.has(cobol)) {
                                fs.setString(cobol, String.valueOf(v));
                            }
                        } catch (IllegalAccessException ignored) {
                            // skip inaccessible field
                        }
                    }
                    c = c.getSuperclass();
                }
            }
            return;
        }
        // Handle Map source (from JdbcTemplate.queryForMap — keys are snake_case column names)
        if (source instanceof java.util.Map) {
            copyFromMap((java.util.Map<?, ?>) source, target);
            return;
        }
        // Traverse the full class hierarchy of the source to include inherited fields
        Class<?> sourceClass = source.getClass();
        while (sourceClass != null && !sourceClass.getPackageName().startsWith("java.")) {
            for (java.lang.reflect.Field sf : sourceClass.getDeclaredFields()) {
                sf.setAccessible(true);
                try {
                    java.lang.reflect.Field tf = findDeclaredField(target.getClass(), sf.getName());
                    if (tf != null) {
                        tf.setAccessible(true);
                        tf.set(target, sf.get(source));
                    }
                } catch (IllegalAccessException e) {
                    log.debug("Cannot copy field '{}': {}", sf.getName(), e.getMessage());
                }
            }
            sourceClass = sourceClass.getSuperclass();
        }
    }

    /**
     * Copy data from a Map (typically from JdbcTemplate.queryForMap) to a POJO target. Map keys are
     * snake_case column names (e.g. "ten_tokuicd"), POJO fields are camelCase (e.g. "tenTokuicd").
     * Converts snake_case → camelCase for field matching. Also tries exact key match for cases
     * where Map keys already match field names.
     */
    private static void copyFromMap(java.util.Map<?, ?> source, Object target) {
        for (java.util.Map.Entry<?, ?> entry : source.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            // Try exact key match first
            java.lang.reflect.Field tf = findDeclaredField(target.getClass(), key);
            // Then try snake_case → camelCase conversion
            if (tf == null) {
                tf = findDeclaredField(target.getClass(), snakeToCamel(key));
            }
            if (tf != null) {
                tf.setAccessible(true);
                try {
                    tf.set(target, convertMapValue(value, tf.getType()));
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    log.debug("Cannot copy map key '{}': {}", key, e.getMessage());
                }
            }
        }
    }

    /** Convert snake_case to camelCase: "ten_tokuicd" → "tenTokuicd" */
    private static String snakeToCamel(String snake) {
        if (snake == null || snake.isEmpty()) {
            return snake;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : snake.toLowerCase().toCharArray()) {
            if (c == '_' || c == '-') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    /** camelCase POJO field name → COBOL field name: custName → CUST-NAME. */
    private static String camelToCobolName(String camel) {
        if (camel == null || camel.isEmpty()) {
            return camel;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('-');
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

    /**
     * Convert JDBC result value to the expected POJO field type. JdbcTemplate may return Long for
     * INTEGER columns, BigDecimal for DECIMAL, etc.
     */
    private static Object convertMapValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Common JDBC → Java type conversions
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }
        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        if (targetType == java.math.BigDecimal.class) {
            if (value instanceof Number) {
                return new java.math.BigDecimal(value.toString());
            }
        }
        return value;
    }

    private static java.lang.reflect.Field findDeclaredField(Class<?> clazz, String name) {
        // Traverse target class hierarchy to find the field
        Class<?> current = clazz;
        while (current != null && !current.getPackageName().startsWith("java.")) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * A-1: Calculate the actual byte length of a COMMAREA object. In CICS, EIBCALEN holds the
     * length of the communication area passed to the program. For JDK types (String, Number, Map),
     * use their natural size. For user-defined COMMAREA classes, estimate length from declared
     * fields.
     */
    private String getSerializedCommareaFor(Object commarea) {
        if (commarea == null) {
            return null;
        }
        if (commarea instanceof String) {
            return (String) commarea;
        }
        try {
            Class<?> helperClass = findHelperClass();
            java.lang.reflect.Method groupToString =
                    helperClass.getMethod("groupToString", Object.class);
            return (String) groupToString.invoke(null, commarea);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Cached helper (Utility) class reference — discovered dynamically from program classloader.
     */
    private volatile Class<?> helperClassCache;

    /**
     * Find the Utility helper class dynamically by scanning known program packages (R7a: tên hợp
     * nhất; các path cũ *.support.Utility chưa từng match layout thật — giờ trỏ đúng
     * {base}.common.infrastructure nơi common module đặt helper).
     */
    private Class<?> findHelperClass() throws ClassNotFoundException {
        if (helperClassCache != null) {
            return helperClassCache;
        }
        // Try common patterns: derive base package from any registered program
        for (AppProgram program : programs.values()) {
            String progPkg =
                    program.getClass().getPackageName(); // e.g., com.generated.cocrdlic.service
            // Navigate up to base: com.generated.cocrdlic.service → com.generated
            String[] parts = progPkg.split("\\.");
            if (parts.length >= 3) {
                // Try: {base}.common.infrastructure.Utility (multi-module pattern)
                String base = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 2));
                try {
                    helperClassCache = Class.forName(base + ".common.infrastructure.Utility");
                    return helperClassCache;
                } catch (ClassNotFoundException ignored) {
                }
                // Try: {base}.infrastructure.Utility (single-module pattern)
                try {
                    helperClassCache = Class.forName(base + ".infrastructure.Utility");
                    return helperClassCache;
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        throw new ClassNotFoundException(
                "Utility helper class not found — check generated common module");
    }

    private int calculateCommareaLength(Object commarea) {
        if (commarea == null) {
            return 0;
        }
        // Byte model: COMMAREA flows as the raw bytes of the COBOL region (f.sliceBytes).
        // EIBCALEN = the byte length directly (lossless; avoids String round-trip on binary
        // fields).
        if (commarea instanceof byte[] b) {
            return b.length;
        }
        // Best approach: use serialized length (consistent with how COBOL counts bytes)
        String serialized = getSerializedCommareaFor(commarea);
        if (serialized != null && !serialized.isEmpty()) {
            return serialized.length();
        }
        // Fallback for JDK types
        if (commarea instanceof String s) {
            return s.length();
        }
        if (commarea instanceof Number) {
            return 4;
        }
        if (commarea instanceof Map<?, ?> map) {
            int total = 0;
            for (Object v : map.values()) {
                if (v instanceof String s) {
                    total += s.length();
                } else total += 4;
            }
            return total > 0 ? total : 1;
        }
        // User-defined COMMAREA class — reflect on declared fields
        Class<?> clazz = commarea.getClass();
        if (clazz.getPackageName().startsWith("java.")) {
            // Safety: don't reflect on JDK internal classes — log for debugging
            log.warn("COMMAREA is JDK type {} — cannot calculate length, using 1", clazz.getName());
            return 1;
        }
        int totalLength = 0;
        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            Class<?> type = f.getType();
            if (type == String.class) {
                try {
                    // Use @CobolField(size=N) annotation for PIC-based size (not content length)
                    java.lang.annotation.Annotation[] anns = f.getAnnotations();
                    int picSize = -1;
                    for (var ann : anns) {
                        try {
                            var sizeMethod = ann.annotationType().getMethod("size");
                            picSize = (int) sizeMethod.invoke(ann);
                        } catch (Exception ignored) {
                        }
                    }
                    if (picSize > 0) {
                        totalLength += picSize;
                    } else {
                        String val = (String) f.get(commarea);
                        totalLength += (val != null) ? val.length() : 0;
                    }
                } catch (IllegalAccessException e) {
                    log.debug("Cannot access commarea field '{}': {}", f.getName(), e.getMessage());
                }
            } else if (type == int.class || type == Integer.class) {
                totalLength += 4;
            } else if (type == long.class || type == Long.class) {
                totalLength += 8;
            } else if (type == short.class || type == Short.class) {
                totalLength += 2;
            } else if (type == byte.class || type == Byte.class) {
                totalLength += 1;
            } else {
                totalLength += 4;
            }
        }
        // In CICS, eibcalen > 0 means "commarea was passed".
        // Even if all fields are empty/zero, a non-null commarea must return > 0.
        return Math.max(totalLength, 1);
    }
}
