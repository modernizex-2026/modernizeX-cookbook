package com.generated.orion.ocpauin.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCPAUIN. Contains button definitions, FSET fields, and field mappings.
 */
public class OcpauinBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=INQUIRE", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF8", "PF8=NEXT", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MPAUINA", Set.of("AUTHID"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MPAUINA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCPAUIN_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MPAUINA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("AUTHID", "authidi")
                    .addDataIn("PAREQTS", "pareqtsi")
                    .addDataIn("PAACCT", "paaccti")
                    .addDataIn("PAAMT", "paamti")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("PADEC", "padeci")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("PAMERCH", "pamerchi")
                    .addDataIn("PACARD", "pacardi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PASTAT", "pastati")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("AUTHID", "authido")
                    .addDataOut("PAREQTS", "pareqtso")
                    .addDataOut("PAACCT", "paaccto")
                    .addDataOut("PAAMT", "paamto")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("PADEC", "padeco")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("PAMERCH", "pamercho")
                    .addDataOut("PACARD", "pacardo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PASTAT", "pastato")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("AUTHID", "authida")
                    .addAttr("PAREQTS", "pareqtsa")
                    .addAttr("PAACCT", "paaccta")
                    .addAttr("PAAMT", "paamta")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("PADEC", "padeca")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("PAMERCH", "pamercha")
                    .addAttr("PACARD", "pacarda")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PASTAT", "pastata")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("AUTHID", "authidl")
                    .addLength("PAREQTS", "pareqtsl")
                    .addLength("PAACCT", "paacctl")
                    .addLength("PAAMT", "paamtl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("PADEC", "padecl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("PAMERCH", "pamerchl")
                    .addLength("PACARD", "pacardl")
                    .addLength("TITLE", "titlel")
                    .addLength("PASTAT", "pastatl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
