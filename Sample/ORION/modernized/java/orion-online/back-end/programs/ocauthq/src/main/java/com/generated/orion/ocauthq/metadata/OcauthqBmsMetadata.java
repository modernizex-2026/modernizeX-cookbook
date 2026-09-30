package com.generated.orion.ocauthq.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCAUTHQ. Contains button definitions, FSET fields, and field mappings.
 */
public class OcauthqBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MAUTHQA", Set.of("CARDNUM", "AMOUNT", "MERCH"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MAUTHQA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCAUTHQ_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MAUTHQA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("MERCH", "merchi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("AVAIL", "availi")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("AMOUNT", "amounti")
                    .addDataIn("CARDNUM", "cardnumi")
                    .addDataIn("DECISN", "decisni")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("REASON", "reasoni")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("MERCH", "mercho")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("AVAIL", "availo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("AMOUNT", "amounto")
                    .addDataOut("CARDNUM", "cardnumo")
                    .addDataOut("DECISN", "decisno")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("REASON", "reasono")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("MERCH", "mercha")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("AVAIL", "availa")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("AMOUNT", "amounta")
                    .addAttr("CARDNUM", "cardnuma")
                    .addAttr("DECISN", "decisna")
                    .addAttr("TITLE", "titlea")
                    .addAttr("REASON", "reasona")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("MERCH", "merchl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("AVAIL", "availl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("AMOUNT", "amountl")
                    .addLength("CARDNUM", "cardnuml")
                    .addLength("DECISN", "decisnl")
                    .addLength("TITLE", "titlel")
                    .addLength("REASON", "reasonl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
