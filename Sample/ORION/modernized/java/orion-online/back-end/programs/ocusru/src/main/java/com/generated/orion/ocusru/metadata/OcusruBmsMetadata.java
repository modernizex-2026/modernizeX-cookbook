package com.generated.orion.ocusru.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OCUSRU. Contains button definitions, FSET fields, and field mappings. */
public class OcusruBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF1", "PF1", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields(
                "MUSRUA", Set.of("USERID", "USFNAM", "USLNAM", "USPWD", "USTYPE"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MUSRUA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCUSRU_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MUSRUA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("USERID", "useridi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("USPWD", "uspwdi")
                    .addDataIn("USFNAM", "usfnami")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("USLNAM", "uslnami")
                    .addDataIn("USTYPE", "ustypei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("USERID", "userido")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("USPWD", "uspwdo")
                    .addDataOut("USFNAM", "usfnamo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("USLNAM", "uslnamo")
                    .addDataOut("USTYPE", "ustypeo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("USERID", "userida")
                    .addAttr("TITLE", "titlea")
                    .addAttr("USPWD", "uspwda")
                    .addAttr("USFNAM", "usfnama")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("USLNAM", "uslnama")
                    .addAttr("USTYPE", "ustypea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("USERID", "useridl")
                    .addLength("TITLE", "titlel")
                    .addLength("USPWD", "uspwdl")
                    .addLength("USFNAM", "usfnaml")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("USLNAM", "uslnaml")
                    .addLength("USTYPE", "ustypel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
