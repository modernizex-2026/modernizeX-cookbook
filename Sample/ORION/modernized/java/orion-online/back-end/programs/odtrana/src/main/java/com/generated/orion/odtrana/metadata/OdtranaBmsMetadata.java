package com.generated.orion.odtrana.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for ODTRANA. Contains button definitions, FSET fields, and field mappings.
 */
public class OdtranaBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields(
                "MTRANAA", Set.of("CARDNUM", "TRTYPE", "TRCAT", "TRAMT", "TRMERCH", "TRDESC"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MTRANAA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/ODTRANA_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MTRANAA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("TRDESC", "trdesci")
                    .addDataIn("CARDNUM", "cardnumi")
                    .addDataIn("TRMERCH", "trmerchi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("TRTYPE", "trtypei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("TRCAT", "trcati")
                    .addDataIn("TRAMT", "tramti")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("TRDESC", "trdesco")
                    .addDataOut("CARDNUM", "cardnumo")
                    .addDataOut("TRMERCH", "trmercho")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("TRTYPE", "trtypeo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("TRCAT", "trcato")
                    .addDataOut("TRAMT", "tramto")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("TRDESC", "trdesca")
                    .addAttr("CARDNUM", "cardnuma")
                    .addAttr("TRMERCH", "trmercha")
                    .addAttr("TITLE", "titlea")
                    .addAttr("TRTYPE", "trtypea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("TRCAT", "trcata")
                    .addAttr("TRAMT", "tramta")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("TRDESC", "trdescl")
                    .addLength("CARDNUM", "cardnuml")
                    .addLength("TRMERCH", "trmerchl")
                    .addLength("TITLE", "titlel")
                    .addLength("TRTYPE", "trtypel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("TRCAT", "trcatl")
                    .addLength("TRAMT", "tramtl")
                    .build();
        }
        return FieldMapping.empty();
    }
}
