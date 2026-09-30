package com.sakura.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sakura.runtime.ScreenModels.*;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Per-session list-record accumulator. Auto-generated; do not edit. */
public final class RecordAccumulator {

    private static final Logger log = LoggerFactory.getLogger(RecordAccumulator.class);

    private final ObjectMapper mapper;
    private ScreenDef currentScreen;
    private final List<ObjectNode> pending = new ArrayList<>();

    public RecordAccumulator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Hook from WebSocketScreenRenderer BEFORE every WS frame (incl. displayScreen). Flushes
     * accumulated records when context switches away from current list screen.
     */
    public void maybeFlush(ScreenDef nextDef, Consumer<ObjectNode> frameSink) {
        if (currentScreen == null) {
            return;
        }
        boolean keepAccumulating =
                (nextDef != null)
                        && nextDef.recordSchema != null
                        && nextDef.name.equals(currentScreen.name);
        if (!keepAccumulating) {
            flush(frameSink);
        }
    }

    /**
     * Hook from WebSocketScreenRenderer AFTER emitting a displayScreen frame for a list-screen —
     * extracts and appends one record. No-op when def.recordSchema is null.
     */
    public void accumulate(ScreenDef def, RuntimeFieldAccess fields) {
        if (def == null || def.recordSchema == null || fields == null) {
            return;
        }
        currentScreen = def;
        pending.add(extractRecord(def.recordSchema, fields));
    }

    /** Force flush — called from the renderer's accept/window/clear methods. */
    public void flush(Consumer<ObjectNode> frameSink) {
        if (currentScreen == null) {
            return;
        }
        ObjectNode frame = mapper.createObjectNode();
        frame.put("type", "displayRecordList");
        frame.put("screenName", currentScreen.name);
        frame.put("recordName", currentScreen.recordSchema.recordName);
        ArrayNode arr = frame.putArray("records");
        for (ObjectNode r : pending) {
            arr.add(r);
        }
        frameSink.accept(frame);
        pending.clear();
        currentScreen = null;
    }

    /** Extract one record from working storage using the schema. */
    private ObjectNode extractRecord(RecordSchemaDef schema, RuntimeFieldAccess fields) {
        ObjectNode rec = mapper.createObjectNode();
        for (RecordFieldDef f : schema.fields) {
            try {
                switch (f.kind) {
                    case "rowIndex":
                        {
                            rec.put(f.key, safeInt(fields, f.sourceField));
                            break;
                        }
                    case "scalar":
                        {
                            rec.put(
                                    f.key,
                                    applySubstring(safeString(fields, f.sourceField), f.substring));
                            break;
                        }
                    case "indexed":
                        {
                            int idx = safeInt(fields, f.indexVar);
                            if (f.literalIndex != null) {
                                // 2D OCCURS (e.g. KB-HANBAICD(2 IDX1)): literalIndex=2 selects
                                // edit-area copy,
                                // indexVar resolves the runtime row counter.
                                rec.put(
                                        f.key,
                                        applySubstring(
                                                safeStringAt(
                                                        fields, f.sourceField, f.literalIndex, idx),
                                                f.substring));
                            } else {
                                rec.put(
                                        f.key,
                                        applySubstring(
                                                safeStringAt(fields, f.sourceField, idx),
                                                f.substring));
                            }
                            break;
                        }
                    case "indexedLiteral":
                        {
                            int idx = f.literalIndex != null ? f.literalIndex : 1;
                            rec.put(
                                    f.key,
                                    applySubstring(
                                            safeStringAt(fields, f.sourceField, idx), f.substring));
                            break;
                        }
                    case "fixedArray":
                        {
                            ArrayNode arr = rec.putArray(f.key);
                            if (f.indexes != null) {
                                for (int idx : f.indexes) {
                                    arr.add(safeStringAt(fields, f.sourceField, idx));
                                }
                            }
                            break;
                        }
                    default:
                        rec.putNull(f.key);
                }
            } catch (Exception ex) {
                log.warn(
                        "[RECORD-LIST] field extract failed key={} src={}: {}",
                        f.key,
                        f.sourceField,
                        ex.getMessage());
                rec.putNull(f.key);
            }
        }
        return rec;
    }

    private static int safeInt(RuntimeFieldAccess f, String name) {
        if (name == null || name.isEmpty()) {
            return 0;
        }
        try {
            return f.getInt(name);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String safeString(RuntimeFieldAccess f, String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        try {
            return String.valueOf(f.getString(name));
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeStringAt(RuntimeFieldAccess f, String name, int... subs) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        try {
            return String.valueOf(f.getString(name, subs));
        } catch (Exception e) {
            return "";
        }
    }

    /** Apply COBOL `(start:length)` reference modification. 1-based start. */
    private static String applySubstring(String value, Substring s) {
        if (s == null || value == null) {
            return value == null ? "" : value;
        }
        int start = Math.max(1, s.from) - 1;
        int length = Math.max(0, s.length);
        if (start >= value.length()) {
            return "";
        }
        int end = Math.min(value.length(), start + length);
        return value.substring(start, end);
    }
}
