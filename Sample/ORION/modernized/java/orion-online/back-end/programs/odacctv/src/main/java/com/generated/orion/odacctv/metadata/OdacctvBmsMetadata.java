package com.generated.orion.odacctv.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for ODACCTV. Contains button definitions, FSET fields, and field mappings.
 */
public class OdacctvBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MACCTVA", Set.of("ACCTID"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MACCTVA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/ODACCTV_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MACCTVA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("ACEXP", "acexpi")
                    .addDataIn("ACOPEN", "acopeni")
                    .addDataIn("ACGRP", "acgrpi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ACCTID", "acctidi")
                    .addDataIn("ACSTAT", "acstati")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("ACBAL", "acbali")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("ACCSLIM", "accslimi")
                    .addDataIn("ACCRLIM", "accrlimi")
                    .addDataOut("ACEXP", "acexpo")
                    .addDataOut("ACOPEN", "acopeno")
                    .addDataOut("ACGRP", "acgrpo")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ACCTID", "acctido")
                    .addDataOut("ACSTAT", "acstato")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("ACBAL", "acbalo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("ACCSLIM", "accslimo")
                    .addDataOut("ACCRLIM", "accrlimo")
                    .addAttr("ACEXP", "acexpa")
                    .addAttr("ACOPEN", "acopena")
                    .addAttr("ACGRP", "acgrpa")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ACCTID", "acctida")
                    .addAttr("ACSTAT", "acstata")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("ACBAL", "acbala")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("ACCSLIM", "accslima")
                    .addAttr("ACCRLIM", "accrlima")
                    .addLength("ACEXP", "acexpl")
                    .addLength("ACOPEN", "acopenl")
                    .addLength("ACGRP", "acgrpl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ACCTID", "acctidl")
                    .addLength("ACSTAT", "acstatl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("ACBAL", "acball")
                    .addLength("TITLE", "titlel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("ACCSLIM", "accsliml")
                    .addLength("ACCRLIM", "accrliml")
                    .build();
        }
        return FieldMapping.empty();
    }
}
