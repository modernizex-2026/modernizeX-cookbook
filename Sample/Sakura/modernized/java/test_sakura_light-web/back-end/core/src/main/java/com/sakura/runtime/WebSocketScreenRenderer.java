package com.sakura.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sakura.runtime.ScreenModels.*;
import com.sakura.runtime.conventions.WebAidCodes;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket-based COBOL SCREEN SECTION renderer. Sends screen data as JSON over WebSocket; receives
 * user input via BlockingQueue. Includes per-screen ACCEPT buffering: first acceptField after
 * displayScreen sends ALL screen fields; client fills all + Enter; server buffers responses.
 */
public class WebSocketScreenRenderer implements ScreenRendererInstance {

    private static final Logger log = LoggerFactory.getLogger(WebSocketScreenRenderer.class);
    private static final long DEFAULT_TIMEOUT_MINUTES = 30;

    private final WebSocketSession session;
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long sessionTimeoutMinutes;

    // Per-screen ACCEPT buffering state
    private Map<String, String> fieldBuffer = null;
    private String estsBuffer = null;
    private ScreenDef lastDisplayedScreen = null;

    // Window state
    private WindowDef activeWindow = null;

    // Typed-record list accumulator (per session, lifetime = renderer)
    private final RecordAccumulator recordAccumulator = new RecordAccumulator(objectMapper);

    /**
     * Error-shadow guard. Legacy NEC WEBCOBOL blocked at ACCEPT-FIELD so an EI* error stayed on
     * DS-MESSAG row 24 until the user typed. In async WebSocket, a follow-up GF001 broadcast would
     * clobber the EI007 before the user sees it. Set on error frames; suppresses non-error
     * DS-MESSAG until next user input.
     */
    private String pendingErrorMessage = null;

    public WebSocketScreenRenderer(WebSocketSession session) {
        this.session = session;
        this.sessionTimeoutMinutes =
                Long.parseLong(
                        System.getProperty(
                                "screen.session.timeout", String.valueOf(DEFAULT_TIMEOUT_MINUTES)));
    }

    /**
     * COBOL @WSNAME backing value: the per-session workstation name. Web clients pass it as a
     * `?station=` query param on the WebSocket connect URL; falls back to the `cobol.station.name`
     * system property, else empty (auth check skipped).
     */
    @Override
    public String getStationName() {
        try {
            java.net.URI uri = session != null ? session.getUri() : null;
            if (uri != null && uri.getQuery() != null) {
                for (String pair : uri.getQuery().split("&")) {
                    int eq = pair.indexOf('=');
                    if (eq > 0 && "station".equalsIgnoreCase(pair.substring(0, eq))) {
                        return java.net.URLDecoder.decode(
                                        pair.substring(eq + 1),
                                        java.nio.charset.StandardCharsets.UTF_8)
                                .trim();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("getStationName failed: {}", e.getMessage());
        }
        return System.getProperty("cobol.station.name", "");
    }

    /**
     * Internal sink for accumulator-emitted frames. Swallows IOException with a log (matches
     * existing renderer pattern in displayScreen/etc.).
     */
    private void emitFrameSafe(ObjectNode frame) {
        try {
            sendMessage(frame);
        } catch (IOException e) {
            log.error("Failed to send displayRecordList: {}", e.getMessage());
        }
    }

    /** Called by WebSocketHandler when client sends a message. */
    public void onClientMessage(String json) {
        inputQueue.offer(json);
    }

    @Override
    public void displayScreen(ScreenDef def, RuntimeFieldAccess fields) {
        if (def == null) {
            return;
        }
        // Flush any pending list records BEFORE this screen frame so FE order is preserved.
        recordAccumulator.maybeFlush(def, this::emitFrameSafe);
        // Clear per-screen ACCEPT buffer on new screen
        lastDisplayedScreen = def;
        fieldBuffer = null;
        estsBuffer = null;

        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "displayScreen");
            msg.put("screenName", def.name);
            msg.put("clearScreen", def.clearScreen);
            // COBOL `CLEAR DATA TO N` directive — emits an inclusive row-range
            // clear instruction. Frontend composable blanks charBuffer rows
            // [from..to] (1-based) before painting fields. Required to honour
            // DSP-RTN-style `DISPLAY SC-CLEAR` paragraphs that wipe the list
            // region between page transitions.
            if (def.clearLineFrom > 0 && def.clearLineTo >= def.clearLineFrom) {
                ObjectNode regionNode = msg.putObject("clearRegion");
                regionNode.put("fromLine", def.clearLineFrom);
                regionNode.put("toLine", def.clearLineTo);
            }
            // Only attach window offset when ALL rows fit inside the active window.
            // Screens like DS-MESSAG (LINE 24 absolute) are drawn outside the window
            // bounds even while a popup is open — offsetting them would push their
            // content off-screen (e.g. line 24 + lineOffset 8 = line 32).
            // Offsets are PURE DELTAS (subtract 1): COBOL's displayLine/displayCol
            // is 1-indexed terminal coords for the window's top-left, so dialog row 1
            // lands at terminal row `1 + (displayLine - 1) = displayLine`.
            if (activeWindow != null && screenFitsInWindow(def, activeWindow)) {
                ObjectNode winNode = msg.putObject("window");
                winNode.put("lineOffset", activeWindow.displayLine - 1);
                winNode.put("colOffset", activeWindow.displayCol - 1);
            }

            ArrayNode rowsNode = msg.putArray("rows");
            for (ScreenRow row : def.rows) {
                int effectiveLine = row.line;
                if (row.dynamicLineField != null) {
                    try {
                        effectiveLine =
                                Integer.parseInt(
                                        String.valueOf(fields.getString(row.dynamicLineField))
                                                .trim());
                    } catch (Exception ignored) {
                        continue; /* unresolved → skip row */
                    }
                    if (effectiveLine <= 0) {
                        continue;
                    }
                    effectiveLine += row.lineOffset; // COBOL `LINE name PLUS N` recovery
                }
                ObjectNode rowNode = rowsNode.addObject();
                rowNode.put("line", effectiveLine);
                ArrayNode fieldsNode = rowNode.putArray("fields");
                for (ScreenField field : row.fields) {
                    ObjectNode fNode = fieldsNode.addObject();
                    fNode.put("col", field.col);
                    fNode.put("type", field.type);
                    if ("literal".equals(field.type)) {
                        fNode.put("text", field.text);
                    } else if ("from".equals(field.type) && field.fieldName != null) {
                        String value = ScreenModels.resolveFieldRef(fields, field.fieldName);
                        fNode.put("text", ScreenModels.formatPicture(value, field.picture));
                        fNode.put("fieldName", field.fieldName);
                    }
                    if (field.color != null) {
                        fNode.put("color", field.color);
                    }
                    if (field.reverse) {
                        fNode.put("reverse", true);
                    }
                    if (field.blink) {
                        fNode.put("blink", true);
                    }
                    if (field.lowlight) {
                        fNode.put("lowlight", true);
                    }
                    if (field.overLineTo > 0) {
                        fNode.put("overLineTo", field.overLineTo);
                    }
                    if (field.underLineTo > 0) {
                        fNode.put("underLineTo", field.underLineTo);
                    }
                    if (field.verticalLineAt != null) {
                        ArrayNode vl = fNode.putArray("verticalLines");
                        for (int v : field.verticalLineAt) {
                            vl.add(v);
                        }
                    }
                }
            }
            // DS-MESSAG error-shadow guard: drop a non-error broadcast that would
            // otherwise clobber a still-pending EI/EE/EF message on the client.
            if ("DS-MESSAG".equals(def.name)) {
                String msgText = extractMessageText(rowsNode);
                boolean isError = msgText != null && msgText.matches("^E[A-Z]\\d{3}.*");
                if (isError) {
                    pendingErrorMessage = msgText;
                } else if (pendingErrorMessage != null) {
                    return; // skip sendMessage — keep prior error visible
                }
            }
            sendMessage(msg);
            // Accumulate one record per list-screen display call. Read after the
            // existing displayScreen frame is sent so frame ordering on the wire is:
            // displayScreen×N → displayRecordList (flushed by next non-list display or accept).
            recordAccumulator.accumulate(def, fields);
        } catch (Exception e) {
            log.error("Failed to send displayScreen: {}", e.getMessage());
        }
    }

    /**
     * Concatenate all `text` values across rows.fields in a DS-MESSAG frame for EI / EE / EF
     * error-code detection.
     */
    private String extractMessageText(ArrayNode rows) {
        StringBuilder out = new StringBuilder();
        for (JsonNode row : rows) {
            JsonNode fs = row.get("fields");
            if (fs == null) {
                continue;
            }
            for (JsonNode f : fs) {
                JsonNode t = f.get("text");
                if (t != null && !t.isNull()) {
                    out.append(t.asText());
                }
            }
        }
        return out.toString().trim();
    }

    @Override
    public String acceptField(InputFieldDef field) {
        if (field == null) {
            return "";
        }
        recordAccumulator.flush(this::emitFrameSafe);
        // 1. Check buffer — return immediately if pre-buffered
        if (fieldBuffer != null && fieldBuffer.containsKey(field.name.toUpperCase())) {
            return fieldBuffer.remove(field.name.toUpperCase());
        }

        // 2. Determine: per-screen batch mode or single-field mode
        List<String> screenFields =
                (lastDisplayedScreen != null) ? lastDisplayedScreen.inputFieldNames : List.of();
        List<InputFieldDef> screenFieldDefs =
                (lastDisplayedScreen != null) ? lastDisplayedScreen.inputFieldDefs : List.of();

        inputQueue.clear(); // discard stale messages before a fresh accept

        try {
            if (screenFields.size() > 1 && activeWindow == null) {
                // SCREEN MODE: batch all fields in 1 round-trip (full-screen only)
                sendAcceptScreen(screenFieldDefs, screenFields);
            } else {
                // SINGLE FIELD MODE (or windowed — always single to get correct absolute coords)
                sendAcceptField(field);
            }

            // 3. Block for client response
            String response = waitForInput();

            // 4. Parse and buffer (with PIC-aware coercion per InputFieldDef)
            JsonNode json = objectMapper.readTree(response);
            if (json.has("values")) {
                // Per-screen response: coerce + buffer all values
                Map<String, InputFieldDef> defMap = new HashMap<>();
                for (InputFieldDef d : screenFieldDefs) {
                    defMap.put(d.name.toUpperCase(), d);
                }
                fieldBuffer = new HashMap<>();
                json.get("values")
                        .fields()
                        .forEachRemaining(
                                e -> {
                                    String _k = e.getKey().toUpperCase();
                                    String _raw = e.getValue().asText();
                                    InputFieldDef _def = defMap.get(_k);
                                    fieldBuffer.put(
                                            _k, _def != null ? coerceToPicture(_raw, _def) : _raw);
                                });
                estsBuffer =
                        mapAidKey(
                                json.has("aidKey") ? json.get("aidKey").asText() : null,
                                field.advanceEsts);
                return fieldBuffer.remove(field.name.toUpperCase());
            }
            // Single field response — also buffer aidKey if present (PF key during acceptField)
            if (json.has("aidKey")) {
                estsBuffer = mapAidKey(json.get("aidKey").asText(), field.advanceEsts);
            }
            if (json.has("value")) {
                return coerceToPicture(json.get("value").asText(), field);
            }
            return response;
        } catch (Exception e) {
            throw new StopRunSignal("WebSocket accept failed: " + e.getMessage());
        }
    }

    @Override
    public String acceptAt(int line, int col, int width) {
        recordAccumulator.flush(this::emitFrameSafe);
        inputQueue.clear();
        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "acceptField");
            msg.put("line", line);
            msg.put("col", col);
            msg.put("width", width);
            sendMessage(msg);
            String response = waitForInput();
            JsonNode json = objectMapper.readTree(response);
            return json.has("value") ? json.get("value").asText() : response;
        } catch (Exception e) {
            throw new StopRunSignal("WebSocket acceptAt failed: " + e.getMessage());
        }
    }

    @Override
    public String readEndStatus() {
        recordAccumulator.flush(this::emitFrameSafe);
        // Check buffer first (per-screen ACCEPT already captured aidKey)
        if (estsBuffer != null) {
            String result = estsBuffer;
            estsBuffer = null;
            return result;
        }

        // No buffer: ask client for PF key
        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "readEndStatus");
            sendMessage(msg);
            String response = waitForInput();
            JsonNode json = objectMapper.readTree(response);
            return json.has("aidKey") ? json.get("aidKey").asText() : response;
        } catch (Exception e) {
            throw new StopRunSignal("WebSocket readEndStatus failed: " + e.getMessage());
        }
    }

    @Override
    public void displayField(InputFieldDef field, String value) {
        if (field == null) {
            return;
        }
        recordAccumulator.flush(this::emitFrameSafe);
        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "displayField");
            msg.put("fieldName", field.name);
            // Same window-offset rule as sendAcceptField: when a popup is open and the
            // current screen + this field fit inside the popup, the field's line/col
            // are popup-relative and must be offset to absolute terminal coords. Without
            // this, a popup field at internal (line=2, col=19) lands at absolute (2, 19)
            // (the underlying main-screen column header) instead of inside the popup.
            // Mirrors the acceptField path so render + ACCEPT overlay coords agree.
            int lineOffset = 0;
            int colOffset = 0;
            if (activeWindow != null && screenFitsInWindow(lastDisplayedScreen, activeWindow)) {
                boolean fieldInWindow =
                        field.line >= 1
                                && field.line <= activeWindow.height
                                && field.col >= 1
                                && field.col <= activeWindow.width;
                if (fieldInWindow) {
                    lineOffset = activeWindow.displayLine - 1;
                    colOffset = activeWindow.displayCol - 1;
                }
            }
            msg.put("line", field.line + lineOffset);
            msg.put("col", field.col + colOffset);
            msg.put("value", ScreenModels.formatPicture(value, field.picture));
            sendMessage(msg);
        } catch (Exception e) {
            log.error("Failed to send displayField: {}", e.getMessage());
        }
    }

    @Override
    public void showMessage(String msg) {
        recordAccumulator.flush(this::emitFrameSafe);
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "showMessage");
            node.put("text", msg);
            sendMessage(node);
        } catch (Exception e) {
            log.error("Failed to send showMessage: {}", e.getMessage());
        }
    }

    /**
     * COBOL STOP 'literal' / DISPLAY ... UPON GDD → Guide Display Window. Sends a blocking dialog
     * frame and waits for the operator to acknowledge (FE replies guideDisplayAck on OK/Esc), then
     * returns so execution continues. Mirrors the acceptField send+waitForInput pattern; the ack
     * payload is irrelevant (no operator choice — OK/Esc both just close it).
     */
    @Override
    public void guideDisplay(String text) {
        recordAccumulator.flush(this::emitFrameSafe);
        inputQueue.clear();
        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "guideDisplay");
            msg.put("text", text);
            sendMessage(msg);
        } catch (Exception e) {
            throw new StopRunSignal("WebSocket guideDisplay failed: " + e.getMessage());
        }
        waitForInput(); // BLOCK until guideDisplayAck arrives
    }

    @Override
    public void openWindow(WindowDef win) {
        if (win == null) {
            return;
        }
        recordAccumulator.flush(this::emitFrameSafe);
        activeWindow = win;
        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "openWindow");
            msg.put("name", win.name);
            msg.put("height", win.height);
            msg.put("width", win.width);
            msg.put("line", win.displayLine);
            msg.put("col", win.displayCol);
            msg.put("border", win.border);
            sendMessage(msg);
        } catch (Exception e) {
            log.error("Failed to send openWindow: {}", e.getMessage());
        }
    }

    @Override
    public void closeWindow() {
        recordAccumulator.flush(this::emitFrameSafe);
        activeWindow = null;
        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "closeWindow");
            sendMessage(msg);
        } catch (Exception e) {
            log.error("Failed to send closeWindow: {}", e.getMessage());
        }
    }

    @Override
    public int getWindowLineOffset() {
        return activeWindow != null ? activeWindow.displayLine : 0;
    }

    @Override
    public int getWindowColOffset() {
        return activeWindow != null ? activeWindow.displayCol : 0;
    }

    @Override
    public boolean hasActiveWindow() {
        return activeWindow != null;
    }

    @Override
    public void clearScreen() {
        recordAccumulator.flush(this::emitFrameSafe);
        try {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("type", "clearScreen");
            sendMessage(msg);
        } catch (Exception e) {
            log.error("Failed to send clearScreen: {}", e.getMessage());
        }
    }

    @Override
    public void cursorToBottom() {
        /* no-op in WebSocket mode */
    }

    @Override
    public String getProperty(String key) {
        return null; // WebSocket mode: no system properties, forces interactive flow
    }

    // ── Private helpers ──

    /**
     * True when every row/field of the screen fits inside the active window bounds. Screens that
     * declare absolute lines outside the window (e.g. DS-MESSAG at LINE 24 while a line-10 popup is
     * open) are global overlays and must NOT receive window offsets.
     */
    private boolean screenFitsInWindow(ScreenDef def, WindowDef win) {
        if (def == null || win == null || def.rows == null) {
            return false;
        }
        for (ScreenRow row : def.rows) {
            // Dynamic rows resolve their line from a runtime variable; assume they don't fit
            // a popup window so they render as full-screen overlay (matches COBOL semantics).
            if (row.dynamicLineField != null) {
                return false;
            }
            if (row.line > win.height) {
                return false;
            }
            for (ScreenField f : row.fields) {
                if (f.col > win.width) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendMessage(ObjectNode msg) throws IOException {
        if (session.isOpen()) {
            // ConcurrentWebSocketSessionDecorator handles thread-safety — no synchronized needed
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    private static final String DEFAULT_ENTER_AID =
            System.getProperty("cobol.aid.defaultEnter", "06");

    private static String mapAidKey(String raw, String fieldAdvanceEsts) {
        // Frontend sends WebAidCodes.ENTER ("00") for a plain Enter keypress.
        // Most COBOL EVALUATE blocks don't bind "00" → would hit WHEN OTHER and loop.
        // Resolve to the field-specific advanceEsts (set by generator post-pass) or fall back to
        // the global default.
        //
        // When raw is a generic PF code (e.g. "P4")
        // that NUMERICALLY MATCHES the field's intent code (e.g. advanceEsts="04"),
        // prefer the advanceEsts. NEC WEBCOBOL convention is that some PF keys on
        // some fields map to the "confirm" code variant — e.g. F4 on SC-OK sends
        // "04" (confirm-write) not "P4" (generic PF4). The FE's pfMap always emits
        // generic "P{digit}" because it doesn't know the per-field advanceEsts; the
        // backend has that info so it does the translation here. Without this, a
        // user pressing F4 on SC-OK looped acpZ990 forever (raw="P4" → case "04"
        // in COBOL never matched → default → continue).
        String resolved;
        if (raw == null || raw.isEmpty() || WebAidCodes.ENTER.equals(raw)) {
            resolved = fieldAdvanceEsts != null ? fieldAdvanceEsts : DEFAULT_ENTER_AID;
        } else if (fieldAdvanceEsts != null
                && fieldAdvanceEsts.length() == 2
                && raw.length() == 2
                && raw.charAt(0) == 'P'
                && fieldAdvanceEsts.charAt(0) == '0'
                && raw.charAt(1) == fieldAdvanceEsts.charAt(1)) {
            // PF code (P{n}) numerically matches field's intent code (0{n}) → use intent.
            resolved = fieldAdvanceEsts;
        } else {
            resolved = raw;
        }
        return resolved;
    }

    private String waitForInput() {
        try {
            long warningMinutes = Math.max(1, sessionTimeoutMinutes - 5);
            // First poll: up to warning threshold
            String input = inputQueue.poll(warningMinutes, TimeUnit.MINUTES);
            if (input != null) {
                pendingErrorMessage = null; // user acknowledged any prior error
                return validateInput(input);
            }
            // Send idle warning, then wait remaining time
            try {
                ObjectNode warn = objectMapper.createObjectNode();
                warn.put("type", "idleWarning");
                warn.put("minutesLeft", sessionTimeoutMinutes - warningMinutes);
                sendMessage(warn);
            } catch (IOException ignored) {
            }
            input = inputQueue.poll(sessionTimeoutMinutes - warningMinutes, TimeUnit.MINUTES);
            if (input == null) {
                throw new StopRunSignal("Session timeout");
            }
            pendingErrorMessage = null; // user acknowledged any prior error
            return validateInput(input);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StopRunSignal("Thread interrupted");
        }
    }

    private String validateInput(String input) {
        if ("__DISCONNECT__".equals(input)) {
            throw new StopRunSignal("Client disconnected");
        }
        if (!session.isOpen()) {
            throw new StopRunSignal("Session closed");
        }
        if (Thread.interrupted()) {
            throw new StopRunSignal("Thread interrupted");
        }
        return input;
    }

    private void sendAcceptScreen(List<InputFieldDef> fieldDefs, List<String> fieldNames)
            throws IOException {
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("type", "acceptScreen");
        ArrayNode fieldsArr = msg.putArray("fields");
        Map<String, InputFieldDef> defMap = new java.util.LinkedHashMap<>();
        for (InputFieldDef def : fieldDefs) {
            defMap.put(def.name.toUpperCase(), def);
        }
        for (String name : fieldNames) {
            InputFieldDef def = defMap.get(name.toUpperCase());
            if (def != null) {
                ObjectNode fNode = fieldsArr.addObject();
                fNode.put("fieldName", def.name);
                fNode.put("line", def.line);
                fNode.put("col", def.col);
                fNode.put("width", def.width);
                if (def.picture != null) {
                    fNode.put("picture", def.picture);
                }
            } else {
                ObjectNode fNode = fieldsArr.addObject();
                fNode.put("fieldName", name);
            }
        }
        sendMessage(msg);
    }

    private void sendAcceptField(InputFieldDef field) throws IOException {
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("type", "acceptField");
        msg.put("fieldName", field.name);
        // Window offset rule: only apply if BOTH (a) the last displayed screen actually
        // fits inside the active window, AND (b) the field itself fits. A full-screen
        // (clearScreen=true) like DS-AREA1 may have small line/col coords (e.g. SC-SINKIKB
        // at line=6 col=13) that look 'window-sized' but are actually absolute — adding
        // window offset would push them off their declared row. Mirrors the same logic
        // used in displayScreen (screenFitsInWindow) so render coords and input overlay
        // coords stay consistent.
        int lineOffset = 0;
        int colOffset = 0;
        if (activeWindow != null && screenFitsInWindow(lastDisplayedScreen, activeWindow)) {
            boolean fieldInWindow =
                    field.line >= 1
                            && field.line <= activeWindow.height
                            && field.col >= 1
                            && field.col <= activeWindow.width;
            if (fieldInWindow) {
                // Pure delta — displayLine/displayCol are 1-indexed terminal coords
                // for the window's top-left, so a field at row 1 col 1 within the
                // window lands at terminal (displayLine, displayCol).
                lineOffset = activeWindow.displayLine - 1;
                colOffset = activeWindow.displayCol - 1;
            }
        }
        msg.put("line", field.line + lineOffset);
        msg.put("col", field.col + colOffset);
        msg.put("width", field.width);
        if (field.picture != null) {
            msg.put("picture", field.picture);
        }
        sendMessage(msg);
    }

    private static String coerceToPicture(String value, InputFieldDef field) {
        if (field == null) {
            return value;
        }
        String[] r = ScreenModels.coerceInputToPicture(value, field.picture, field.width);
        int stripped = Integer.parseInt(r[1]);
        if (stripped > 0) {
            log.warn(
                    "acceptField '{}' (PIC {}): stripped {} non-digit char(s) from client input"
                            + " '{}' — non-conformant client?",
                    field.name,
                    field.picture,
                    stripped,
                    value);
        }
        return r[0];
    }
}
