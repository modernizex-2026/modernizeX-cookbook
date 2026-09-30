package com.generated.orion.octranv.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCTRANV. Contains button definitions, FSET fields, and field mappings.
 */
public class OctranvBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MTRANVA", Set.of("TRANID"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MTRANVA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCTRANV_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MTRANVA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("TRCARD", "trcardi")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("TRDESC", "trdesci")
                    .addDataIn("TRMERCH", "trmerchi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("TRTYPE", "trtypei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("TRANID", "tranidi")
                    .addDataIn("TRAMT", "tramti")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("TRCARD", "trcardo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("TRDESC", "trdesco")
                    .addDataOut("TRMERCH", "trmercho")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("TRTYPE", "trtypeo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("TRANID", "tranido")
                    .addDataOut("TRAMT", "tramto")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("TRCARD", "trcarda")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("TRDESC", "trdesca")
                    .addAttr("TRMERCH", "trmercha")
                    .addAttr("TITLE", "titlea")
                    .addAttr("TRTYPE", "trtypea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("TRANID", "tranida")
                    .addAttr("TRAMT", "tramta")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("TRCARD", "trcardl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("TRDESC", "trdescl")
                    .addLength("TRMERCH", "trmerchl")
                    .addLength("TITLE", "titlel")
                    .addLength("TRTYPE", "trtypel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("TRANID", "tranidl")
                    .addLength("TRAMT", "tramtl")
                    .build();
        }
        return FieldMapping.empty();
    }
}
