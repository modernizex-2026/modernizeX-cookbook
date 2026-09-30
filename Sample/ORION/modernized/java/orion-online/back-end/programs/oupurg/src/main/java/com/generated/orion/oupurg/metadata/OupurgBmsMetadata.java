package com.generated.orion.oupurg.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OUPURG. Contains button definitions, FSET fields, and field mappings. */
public class OupurgBmsMetadata {

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
        return "layout/OUPURG_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        return FieldMapping.empty();
    }
}
