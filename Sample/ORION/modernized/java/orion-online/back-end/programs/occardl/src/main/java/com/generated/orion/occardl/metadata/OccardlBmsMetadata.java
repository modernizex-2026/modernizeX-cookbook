package com.generated.orion.occardl.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCCARDL. Contains button definitions, FSET fields, and field mappings.
 */
public class OccardlBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF12", "PF12", ""),
                new ScreenResponse.ButtonDef("CLEAR", "Clear", ""),
                new ScreenResponse.ButtonDef("PF7", "PF7", ""),
                new ScreenResponse.ButtonDef("PF8", "PF8", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MCARDLA", Set.of("ACCTID"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MCARDLA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCCARDL_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MCARDLA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("STAT1", "stat1i")
                    .addDataIn("STAT2", "stat2i")
                    .addDataIn("STAT3", "stat3i")
                    .addDataIn("CARD4", "card4i")
                    .addDataIn("CARD5", "card5i")
                    .addDataIn("CARD2", "card2i")
                    .addDataIn("CARD3", "card3i")
                    .addDataIn("CARD1", "card1i")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ACCTID", "acctidi")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("STAT4", "stat4i")
                    .addDataIn("STAT5", "stat5i")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("STAT1", "stat1o")
                    .addDataOut("STAT2", "stat2o")
                    .addDataOut("STAT3", "stat3o")
                    .addDataOut("CARD4", "card4o")
                    .addDataOut("CARD5", "card5o")
                    .addDataOut("CARD2", "card2o")
                    .addDataOut("CARD3", "card3o")
                    .addDataOut("CARD1", "card1o")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ACCTID", "acctido")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("STAT4", "stat4o")
                    .addDataOut("STAT5", "stat5o")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("STAT1", "stat1a")
                    .addAttr("STAT2", "stat2a")
                    .addAttr("STAT3", "stat3a")
                    .addAttr("CARD4", "card4a")
                    .addAttr("CARD5", "card5a")
                    .addAttr("CARD2", "card2a")
                    .addAttr("CARD3", "card3a")
                    .addAttr("CARD1", "card1a")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ACCTID", "acctida")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("STAT4", "stat4a")
                    .addAttr("STAT5", "stat5a")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("STAT1", "stat1l")
                    .addLength("STAT2", "stat2l")
                    .addLength("STAT3", "stat3l")
                    .addLength("CARD4", "card4l")
                    .addLength("CARD5", "card5l")
                    .addLength("CARD2", "card2l")
                    .addLength("CARD3", "card3l")
                    .addLength("CARD1", "card1l")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ACCTID", "acctidl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("STAT4", "stat4l")
                    .addLength("STAT5", "stat5l")
                    .addLength("TITLE", "titlel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
