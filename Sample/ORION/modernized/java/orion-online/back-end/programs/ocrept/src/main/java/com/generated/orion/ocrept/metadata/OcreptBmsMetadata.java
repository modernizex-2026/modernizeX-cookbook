package com.generated.orion.ocrept.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OCREPT. Contains button definitions, FSET fields, and field mappings. */
public class OcreptBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MREPTA", Set.of("RPTYPE", "RPFROM", "RPTO"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MREPTA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCREPT_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MREPTA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("RPTYPE", "rptypei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("RPFROM", "rpfromi")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("RPTO", "rptoi")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("RPTYPE", "rptypeo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("RPFROM", "rpfromo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("RPTO", "rptoo")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("RPTYPE", "rptypea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("RPFROM", "rpfroma")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("RPTO", "rptoa")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("RPTYPE", "rptypel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("TITLE", "titlel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("RPFROM", "rpfroml")
                    .addLength("CURDATE", "curdatel")
                    .addLength("RPTO", "rptol")
                    .build();
        }
        return FieldMapping.empty();
    }
}
