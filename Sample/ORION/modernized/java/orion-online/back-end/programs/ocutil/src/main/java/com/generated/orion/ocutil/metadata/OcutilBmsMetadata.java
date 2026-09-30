package com.generated.orion.ocutil.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OCUTIL. Contains button definitions, FSET fields, and field mappings. */
public class OcutilBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=RUN", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF12", "PF12", ""),
                new ScreenResponse.ButtonDef("CLEAR", "Clear", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MUTILA", Set.of("UTOPT", "UTMODE", "UTDATE", "UTCYC", "UTNUM"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MUTILA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCUTIL_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MUTILA".equalsIgnoreCase(mapName)) {
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
