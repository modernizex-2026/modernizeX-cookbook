package com.sakura.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakura.runtime.ScreenModels.*;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared parser for per-program manifest.json files. Builds ScreenDef / InputFieldDef / WindowDef
 * instances from JSON so per-program ScreenDefs classes can be thin wrappers (Appendix A
 * consolidation).
 *
 * <p>Auto-generated — do not edit. Source:
 * JavaScreenRendererGenerator.generateScreenManifestParser.
 */
public final class ScreenManifestParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ScreenManifestParser() {
        /* static helpers only */
    }

    /** Result of parsing one program's manifest.json. */
    public static final class ParsedManifest {
        public final Map<String, ScreenDef> screens = new LinkedHashMap<>();
        public final Map<String, InputFieldDef> inputs = new LinkedHashMap<>();
        public final Map<String, WindowDef> windows = new LinkedHashMap<>();
    }

    /**
     * Load + parse the manifest.json resource at the given classpath path. Returns a fully
     * populated ParsedManifest. Throws IllegalStateException if the resource is missing or JSON is
     * malformed — these are generator bugs (manifest is auto-generated, must always be present +
     * well-formed).
     */
    public static ParsedManifest load(String classpathResourcePath) {
        try (InputStream is =
                ScreenManifestParser.class.getResourceAsStream(classpathResourcePath)) {
            if (is == null) {
                throw new IllegalStateException(
                        "manifest.json not found on classpath: " + classpathResourcePath);
            }
            JsonNode root = MAPPER.readTree(is);
            ParsedManifest pm = new ParsedManifest();
            parseScreens(root.path("screens"), pm.screens);
            parseInputs(root.path("inputFields"), pm.inputs);
            parseWindows(root.path("windows"), pm.windows);
            return pm;
        } catch (java.io.IOException e) {
            throw new IllegalStateException(
                    "Failed to parse manifest.json at " + classpathResourcePath, e);
        }
    }

    private static void parseScreens(JsonNode screensNode, Map<String, ScreenDef> out) {
        if (!screensNode.isObject()) {
            return;
        }
        screensNode
                .fields()
                .forEachRemaining(
                        entry -> {
                            String name = entry.getKey();
                            JsonNode sn = entry.getValue();
                            boolean clearScreen = sn.path("clearScreen").asBoolean(false);
                            ScreenDef def = new ScreenDef(name, clearScreen);
                            if (sn.has("clearLineFrom") && sn.has("clearLineTo")) {
                                def.setClearRegion(
                                        sn.path("clearLineFrom").asInt(),
                                        sn.path("clearLineTo").asInt());
                            }
                            // Per-screen input field names
                            JsonNode inputFieldsNode = sn.path("inputFields");
                            if (inputFieldsNode.isArray()) {
                                for (JsonNode f : inputFieldsNode) {
                                    def.withInputField(f.asText());
                                }
                            }
                            // Rows
                            JsonNode rowsNode = sn.path("rows");
                            if (rowsNode.isArray()) {
                                for (JsonNode rn : rowsNode) {
                                    ScreenRow row;
                                    if (rn.has("line")) {
                                        row = def.addRow(rn.path("line").asInt());
                                    } else if (rn.has("dynamicLineField")) {
                                        row =
                                                def.addDynamicRow(
                                                        rn.path("dynamicLineField").asText(),
                                                        rn.path("lineOffset").asInt(0));
                                    } else {
                                        continue; // row without line info — skip (matches Java
                                        // emit)
                                    }
                                    JsonNode fieldsNode = rn.path("fields");
                                    if (fieldsNode.isArray()) {
                                        for (JsonNode fn : fieldsNode) {
                                            applyField(row, fn);
                                        }
                                    }
                                }
                            }
                            // Record schema for list screens
                            JsonNode schemaNode = sn.path("recordSchema");
                            if (schemaNode.isObject()) {
                                def.setRecordSchema(parseRecordSchema(schemaNode));
                            }
                            out.put(name.toUpperCase(), def);
                        });
    }

    private static RecordSchemaDef parseRecordSchema(JsonNode sn) {
        String name = sn.path("recordName").asText("Record");
        RecordSchemaDef schema = new RecordSchemaDef(name);
        JsonNode fields = sn.path("fields");
        if (!fields.isArray()) {
            return schema;
        }
        for (JsonNode fn : fields) {
            RecordFieldDef f =
                    new RecordFieldDef(
                            fn.path("key").asText(""),
                            fn.path("sourceField").asText(""),
                            fn.path("kind").asText("scalar"));
            if (fn.has("indexVar")) {
                f.indexVar = fn.get("indexVar").asText();
            }
            if (fn.has("literalIndex")) {
                f.literalIndex = fn.get("literalIndex").asInt();
            }
            if (fn.has("indexes")) {
                JsonNode arr = fn.get("indexes");
                if (arr.isArray()) {
                    int[] out = new int[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        out[i] = arr.get(i).asInt();
                    }
                    f.indexes = out;
                }
            }
            if (fn.has("substring")) {
                JsonNode s = fn.get("substring");
                f.substring = new Substring(s.path("from").asInt(1), s.path("length").asInt(0));
            }
            if (fn.has("picture") && !fn.get("picture").isNull()) {
                f.picture = fn.get("picture").asText();
            }
            if (fn.has("type")) {
                f.type = fn.get("type").asText();
            }
            schema.fields.add(f);
        }
        return schema;
    }

    private static void applyField(ScreenRow row, JsonNode fn) {
        int col = fn.path("col").asInt(1);
        String type = fn.path("type").asText("");
        String color =
                fn.has("color") && !fn.get("color").isNull() ? fn.get("color").asText() : null;
        boolean reverse = fn.path("reverse").asBoolean(false);
        boolean blink = fn.path("blink").asBoolean(false);
        boolean lowlight = fn.path("lowlight").asBoolean(false);
        if ("literal".equals(type)) {
            String text = fn.path("text").asText("");
            if (reverse) {
                row.literalReverse(col, text);
            } else row.literal(col, text);
        } else if ("from".equals(type)) {
            String fieldName =
                    fn.has("boundField")
                            ? fn.get("boundField").asText()
                            : fn.path("name").asText("");
            String picture =
                    fn.has("picture") && !fn.get("picture").isNull()
                            ? fn.get("picture").asText()
                            : null;
            row.fromField(col, fieldName, picture, color);
        }
        // Apply line drawings + attrs to the last-added field as appropriate.
        if (fn.has("overLineTo")) {
            row.overLine(col, fn.get("overLineTo").asInt());
        }
        if (fn.has("underLineTo")) {
            row.underLine(col, fn.get("underLineTo").asInt());
        }
        if (fn.has("verticalLines")) {
            JsonNode vl = fn.get("verticalLines");
            if (vl.isArray()) {
                int[] arr = new int[vl.size()];
                for (int i = 0; i < vl.size(); i++) {
                    arr[i] = vl.get(i).asInt();
                }
                row.vLines(arr);
            }
        }
        if (blink) {
            row.withBlink();
        }
        if (lowlight) {
            row.withLowlight();
        }
    }

    private static void parseInputs(JsonNode inputsNode, Map<String, InputFieldDef> out) {
        if (!inputsNode.isObject()) {
            return;
        }
        inputsNode
                .fields()
                .forEachRemaining(
                        entry -> {
                            String name = entry.getKey();
                            JsonNode v = entry.getValue();
                            InputFieldDef def =
                                    new InputFieldDef(
                                            name,
                                            v.path("line").asInt(),
                                            v.path("col").asInt(),
                                            v.path("width").asInt(),
                                            v.has("picture") && !v.get("picture").isNull()
                                                    ? v.get("picture").asText()
                                                    : null,
                                            v.has("usingField") && !v.get("usingField").isNull()
                                                    ? v.get("usingField").asText()
                                                    : null);
                            if (v.has("advanceEsts") && !v.get("advanceEsts").isNull()) {
                                def.setAdvanceEsts(v.get("advanceEsts").asText());
                            }
                            out.put(name.toUpperCase(), def);
                        });
    }

    private static void parseWindows(JsonNode windowsNode, Map<String, WindowDef> out) {
        if (!windowsNode.isObject()) {
            return;
        }
        windowsNode
                .fields()
                .forEachRemaining(
                        entry -> {
                            String name = entry.getKey();
                            JsonNode v = entry.getValue();
                            WindowDef def =
                                    new WindowDef(
                                            name,
                                            v.path("height").asInt(),
                                            v.path("width").asInt(),
                                            v.path("line").asInt(),
                                            v.path("col").asInt(),
                                            v.path("border").asBoolean(false));
                            out.put(name.toUpperCase(), def);
                        });
    }
}
