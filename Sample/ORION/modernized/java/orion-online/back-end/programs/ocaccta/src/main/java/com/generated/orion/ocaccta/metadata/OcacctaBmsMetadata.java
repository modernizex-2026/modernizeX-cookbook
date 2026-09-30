package com.generated.orion.ocaccta.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCACCTA. Contains button definitions, FSET fields, and field mappings.
 */
public class OcacctaBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields(
                "MACCTAA", Set.of("ACCTID", "CUSTID", "ACCRLIM", "ACCSLIM", "ACOPEN", "ACGRP"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MACCTAA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCACCTA_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MACCTAA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("ACGRP", "acgrpi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ACCTID", "acctidi")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("CUSTID", "custidi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("ACOPEN", "acopeni")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("ACCSLIM", "accslimi")
                    .addDataIn("ACCRLIM", "accrlimi")
                    .addDataOut("ACGRP", "acgrpo")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ACCTID", "acctido")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("CUSTID", "custido")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("ACOPEN", "acopeno")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("ACCSLIM", "accslimo")
                    .addDataOut("ACCRLIM", "accrlimo")
                    .addAttr("ACGRP", "acgrpa")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ACCTID", "acctida")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("CUSTID", "custida")
                    .addAttr("TITLE", "titlea")
                    .addAttr("ACOPEN", "acopena")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("ACCSLIM", "accslima")
                    .addAttr("ACCRLIM", "accrlima")
                    .addLength("ACGRP", "acgrpl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ACCTID", "acctidl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("CUSTID", "custidl")
                    .addLength("TITLE", "titlel")
                    .addLength("ACOPEN", "acopenl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("ACCSLIM", "accsliml")
                    .addLength("ACCRLIM", "accrliml")
                    .build();
        }
        return FieldMapping.empty();
    }
}
