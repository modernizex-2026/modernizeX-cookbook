package com.generated.orion.octsrch.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCTSRCH. Contains button definitions, FSET fields, and field mappings.
 */
public class OctsrchBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MTSRCHA", Set.of("CARDNUM", "FRAMT", "TOAMT"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MTSRCHA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCTSRCH_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MTSRCHA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("CARDNUM", "cardnumi")
                    .addDataIn("TOAMT", "toamti")
                    .addDataIn("SR2", "sr2i")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("SR1", "sr1i")
                    .addDataIn("SR4", "sr4i")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("SR3", "sr3i")
                    .addDataIn("SR5", "sr5i")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("FRAMT", "framti")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("CARDNUM", "cardnumo")
                    .addDataOut("TOAMT", "toamto")
                    .addDataOut("SR2", "sr2o")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("SR1", "sr1o")
                    .addDataOut("SR4", "sr4o")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("SR3", "sr3o")
                    .addDataOut("SR5", "sr5o")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("FRAMT", "framto")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("CARDNUM", "cardnuma")
                    .addAttr("TOAMT", "toamta")
                    .addAttr("SR2", "sr2a")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("SR1", "sr1a")
                    .addAttr("SR4", "sr4a")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("SR3", "sr3a")
                    .addAttr("SR5", "sr5a")
                    .addAttr("TITLE", "titlea")
                    .addAttr("FRAMT", "framta")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("CARDNUM", "cardnuml")
                    .addLength("TOAMT", "toamtl")
                    .addLength("SR2", "sr2l")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("SR1", "sr1l")
                    .addLength("SR4", "sr4l")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("SR3", "sr3l")
                    .addLength("SR5", "sr5l")
                    .addLength("TITLE", "titlel")
                    .addLength("FRAMT", "framtl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
