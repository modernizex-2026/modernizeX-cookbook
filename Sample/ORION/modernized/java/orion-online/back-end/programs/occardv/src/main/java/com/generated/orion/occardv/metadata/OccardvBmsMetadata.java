package com.generated.orion.occardv.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCCARDV. Contains button definitions, FSET fields, and field mappings.
 */
public class OccardvBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF12", "PF12", ""),
                new ScreenResponse.ButtonDef("CLEAR", "Clear", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MCARDVA", Set.of("CARDNUM"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MCARDVA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCCARDV_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MCARDVA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("CDACCT", "cdaccti")
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
                    .addDataOut("CDACCT", "cdaccto")
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
                    .addAttr("CDACCT", "cdaccta")
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
                    .addLength("CDACCT", "cdacctl")
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
