package com.generated.orion.octranl.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCTRANL. Contains button definitions, FSET fields, and field mappings.
 */
public class OctranlBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MTRANLA", Set.of("CARDNUM"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MTRANLA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCTRANL_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MTRANLA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TYP2", "typ2i")
                    .addDataIn("TYP1", "typ1i")
                    .addDataIn("TRN5", "trn5i")
                    .addDataIn("TRN4", "trn4i")
                    .addDataIn("TRN3", "trn3i")
                    .addDataIn("TRN2", "trn2i")
                    .addDataIn("CARDNUM", "cardnumi")
                    .addDataIn("TRN1", "trn1i")
                    .addDataIn("TYP5", "typ5i")
                    .addDataIn("TYP4", "typ4i")
                    .addDataIn("TYP3", "typ3i")
                    .addDataIn("AMT4", "amt4i")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("AMT5", "amt5i")
                    .addDataIn("AMT2", "amt2i")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("AMT3", "amt3i")
                    .addDataIn("AMT1", "amt1i")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("TYP2", "typ2o")
                    .addDataOut("TYP1", "typ1o")
                    .addDataOut("TRN5", "trn5o")
                    .addDataOut("TRN4", "trn4o")
                    .addDataOut("TRN3", "trn3o")
                    .addDataOut("TRN2", "trn2o")
                    .addDataOut("CARDNUM", "cardnumo")
                    .addDataOut("TRN1", "trn1o")
                    .addDataOut("TYP5", "typ5o")
                    .addDataOut("TYP4", "typ4o")
                    .addDataOut("TYP3", "typ3o")
                    .addDataOut("AMT4", "amt4o")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("AMT5", "amt5o")
                    .addDataOut("AMT2", "amt2o")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("AMT3", "amt3o")
                    .addDataOut("AMT1", "amt1o")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("TYP2", "typ2a")
                    .addAttr("TYP1", "typ1a")
                    .addAttr("TRN5", "trn5a")
                    .addAttr("TRN4", "trn4a")
                    .addAttr("TRN3", "trn3a")
                    .addAttr("TRN2", "trn2a")
                    .addAttr("CARDNUM", "cardnuma")
                    .addAttr("TRN1", "trn1a")
                    .addAttr("TYP5", "typ5a")
                    .addAttr("TYP4", "typ4a")
                    .addAttr("TYP3", "typ3a")
                    .addAttr("AMT4", "amt4a")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("AMT5", "amt5a")
                    .addAttr("AMT2", "amt2a")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("AMT3", "amt3a")
                    .addAttr("AMT1", "amt1a")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("TYP2", "typ2l")
                    .addLength("TYP1", "typ1l")
                    .addLength("TRN5", "trn5l")
                    .addLength("TRN4", "trn4l")
                    .addLength("TRN3", "trn3l")
                    .addLength("TRN2", "trn2l")
                    .addLength("CARDNUM", "cardnuml")
                    .addLength("TRN1", "trn1l")
                    .addLength("TYP5", "typ5l")
                    .addLength("TYP4", "typ4l")
                    .addLength("TYP3", "typ3l")
                    .addLength("AMT4", "amt4l")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("AMT5", "amt5l")
                    .addLength("AMT2", "amt2l")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("AMT3", "amt3l")
                    .addLength("AMT1", "amt1l")
                    .addLength("TITLE", "titlel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
