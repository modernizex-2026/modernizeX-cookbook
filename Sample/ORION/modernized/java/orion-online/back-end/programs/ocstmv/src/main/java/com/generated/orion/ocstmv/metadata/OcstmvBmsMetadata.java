package com.generated.orion.ocstmv.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OCSTMV. Contains button definitions, FSET fields, and field mappings. */
public class OcstmvBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MSTMVA", Set.of("ACCTID", "STCYC"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MSTMVA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCSTMV_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MSTMVA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ACCTID", "acctidi")
                    .addDataIn("STCYC", "stcyci")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("STCLOSE", "stclosei")
                    .addDataIn("STMIN", "stmini")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("STDUE", "stduei")
                    .addDataIn("STOPEN", "stopeni")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ACCTID", "acctido")
                    .addDataOut("STCYC", "stcyco")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("STCLOSE", "stcloseo")
                    .addDataOut("STMIN", "stmino")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("STDUE", "stdueo")
                    .addDataOut("STOPEN", "stopeno")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ACCTID", "acctida")
                    .addAttr("STCYC", "stcyca")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("STCLOSE", "stclosea")
                    .addAttr("STMIN", "stmina")
                    .addAttr("TITLE", "titlea")
                    .addAttr("STDUE", "stduea")
                    .addAttr("STOPEN", "stopena")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ACCTID", "acctidl")
                    .addLength("STCYC", "stcycl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("STCLOSE", "stclosel")
                    .addLength("STMIN", "stminl")
                    .addLength("TITLE", "titlel")
                    .addLength("STDUE", "stduel")
                    .addLength("STOPEN", "stopenl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
