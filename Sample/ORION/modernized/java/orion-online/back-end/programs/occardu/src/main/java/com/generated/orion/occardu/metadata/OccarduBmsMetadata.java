package com.generated.orion.occardu.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCCARDU. Contains button definitions, FSET fields, and field mappings.
 */
public class OccarduBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF12", "PF12", ""),
                new ScreenResponse.ButtonDef("CLEAR", "Clear", ""),
                new ScreenResponse.ButtonDef("PF5", "PF5", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MCARDUA", Set.of("CARDNUM", "CDNAME", "CDEXP", "CDSTAT"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MCARDUA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCCARDU_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MCARDUA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("CARDNUM", "cardnumi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("CDNAME", "cdnamei")
                    .addDataIn("CDEXP", "cdexpi")
                    .addDataIn("CDSTAT", "cdstati")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("CARDNUM", "cardnumo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("CDNAME", "cdnameo")
                    .addDataOut("CDEXP", "cdexpo")
                    .addDataOut("CDSTAT", "cdstato")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("CARDNUM", "cardnuma")
                    .addAttr("TITLE", "titlea")
                    .addAttr("CDNAME", "cdnamea")
                    .addAttr("CDEXP", "cdexpa")
                    .addAttr("CDSTAT", "cdstata")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("CARDNUM", "cardnuml")
                    .addLength("TITLE", "titlel")
                    .addLength("CDNAME", "cdnamel")
                    .addLength("CDEXP", "cdexpl")
                    .addLength("CDSTAT", "cdstatl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
