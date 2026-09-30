package com.generated.orion.oumqreq.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OUMQREQ. Contains button definitions, FSET fields, and field mappings.
 */
public class OumqreqBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return Collections.emptyList();
    }

    public static void registerFsetFields(AppRunner runner) {}

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return Collections.emptyList();
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OUMQREQ_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        return FieldMapping.empty();
    }
}
