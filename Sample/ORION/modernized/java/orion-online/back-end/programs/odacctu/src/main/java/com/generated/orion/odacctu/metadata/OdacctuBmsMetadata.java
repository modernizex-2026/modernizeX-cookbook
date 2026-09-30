package com.generated.orion.odacctu.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for ODACCTU. Contains button definitions, FSET fields, and field mappings.
 */
public class OdacctuBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF5", "PF5", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields(
                "MACCTUA", Set.of("ACCTID", "ACSTAT", "ACCRLIM", "ACCSLIM", "ACEXP", "ACGRP"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MACCTUA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/ODACCTU_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MACCTUA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("ACGRP", "acgrpi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ACCTID", "acctidi")
                    .addDataIn("ACSTAT", "acstati")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("ACEXP", "acexpi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("ACCSLIM", "accslimi")
                    .addDataIn("ACCRLIM", "accrlimi")
                    .addDataOut("ACGRP", "acgrpo")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ACCTID", "acctido")
                    .addDataOut("ACSTAT", "acstato")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("ACEXP", "acexpo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("ACCSLIM", "accslimo")
                    .addDataOut("ACCRLIM", "accrlimo")
                    .addAttr("ACGRP", "acgrpa")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ACCTID", "acctida")
                    .addAttr("ACSTAT", "acstata")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("ACEXP", "acexpa")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("ACCSLIM", "accslima")
                    .addAttr("ACCRLIM", "accrlima")
                    .addLength("ACGRP", "acgrpl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ACCTID", "acctidl")
                    .addLength("ACSTAT", "acstatl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("ACEXP", "acexpl")
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
