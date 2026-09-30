package com.generated.orion.outrnin.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OUTRNIN. Contains button definitions, FSET fields, and field mappings.
 */
public class OutrninBmsMetadata {

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
        return "layout/OUTRNIN_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        return FieldMapping.empty();
    }
}
