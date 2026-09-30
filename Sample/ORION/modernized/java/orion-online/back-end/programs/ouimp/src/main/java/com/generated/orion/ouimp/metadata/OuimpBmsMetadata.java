package com.generated.orion.ouimp.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OUIMP. Contains button definitions, FSET fields, and field mappings. */
public class OuimpBmsMetadata {

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
        return "layout/OUIMP_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        return FieldMapping.empty();
    }
}
