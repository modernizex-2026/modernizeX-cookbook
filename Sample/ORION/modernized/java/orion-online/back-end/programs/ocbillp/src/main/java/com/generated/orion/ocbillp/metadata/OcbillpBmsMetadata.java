package com.generated.orion.ocbillp.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCBILLP. Contains button definitions, FSET fields, and field mappings.
 */
public class OcbillpBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF1", "PF1", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MBILLPA", Set.of("ACCTID", "BLAMT", "BLCONF"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MBILLPA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCBILLP_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MBILLPA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("BLCONF", "blconfi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ACCTID", "acctidi")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("BLBAL", "blbali")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("BLAMT", "blamti")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("BLCONF", "blconfo")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ACCTID", "acctido")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("BLBAL", "blbalo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("BLAMT", "blamto")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("BLCONF", "blconfa")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ACCTID", "acctida")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("BLBAL", "blbala")
                    .addAttr("TITLE", "titlea")
                    .addAttr("BLAMT", "blamta")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("BLCONF", "blconfl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ACCTID", "acctidl")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("BLBAL", "blball")
                    .addLength("TITLE", "titlel")
                    .addLength("BLAMT", "blamtl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
