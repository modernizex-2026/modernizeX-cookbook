package com.generated.orion.ocops.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OCOPS. Contains button definitions, FSET fields, and field mappings. */
public class OcopsBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=RUN", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=EXIT", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF12", "PF12", ""),
                new ScreenResponse.ButtonDef("CLEAR", "Clear", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MOPSA", Set.of("OPTION", "PARM"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MOPSA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCOPS_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MOPSA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("RMSG", "errmsgi")
                    .addDataOut("RMSG", "errmsgo")
                    .addAttr("RMSG", "errmsga")
                    .addLength("RMSG", "errmsgl")
                    .build();
        }
        return FieldMapping.empty();
    }
}
