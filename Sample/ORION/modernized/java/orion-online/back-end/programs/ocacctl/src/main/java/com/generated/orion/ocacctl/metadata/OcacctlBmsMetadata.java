package com.generated.orion.ocacctl.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCACCTL. Contains button definitions, FSET fields, and field mappings.
 */
public class OcacctlBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF8", "PF8", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MACCTLA", Set.of("FRACCT"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MACCTLA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCACCTL_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MACCTLA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("FRACCT", "fraccti")
                    .addDataIn("ACB4", "acb4i")
                    .addDataIn("ACB5", "acb5i")
                    .addDataIn("ACB2", "acb2i")
                    .addDataIn("ACB3", "acb3i")
                    .addDataIn("ACB1", "acb1i")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ACL4", "acl4i")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("ACL5", "acl5i")
                    .addDataIn("ACL2", "acl2i")
                    .addDataIn("ACL3", "acl3i")
                    .addDataIn("ACL1", "acl1i")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("ACS5", "acs5i")
                    .addDataIn("ACS3", "acs3i")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("ACS4", "acs4i")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("ACS1", "acs1i")
                    .addDataIn("ACS2", "acs2i")
                    .addDataOut("FRACCT", "fraccto")
                    .addDataOut("ACB4", "acb4o")
                    .addDataOut("ACB5", "acb5o")
                    .addDataOut("ACB2", "acb2o")
                    .addDataOut("ACB3", "acb3o")
                    .addDataOut("ACB1", "acb1o")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ACL4", "acl4o")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("ACL5", "acl5o")
                    .addDataOut("ACL2", "acl2o")
                    .addDataOut("ACL3", "acl3o")
                    .addDataOut("ACL1", "acl1o")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("ACS5", "acs5o")
                    .addDataOut("ACS3", "acs3o")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("ACS4", "acs4o")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("ACS1", "acs1o")
                    .addDataOut("ACS2", "acs2o")
                    .addAttr("FRACCT", "fraccta")
                    .addAttr("ACB4", "acb4a")
                    .addAttr("ACB5", "acb5a")
                    .addAttr("ACB2", "acb2a")
                    .addAttr("ACB3", "acb3a")
                    .addAttr("ACB1", "acb1a")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ACL4", "acl4a")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("ACL5", "acl5a")
                    .addAttr("ACL2", "acl2a")
                    .addAttr("ACL3", "acl3a")
                    .addAttr("ACL1", "acl1a")
                    .addAttr("TITLE", "titlea")
                    .addAttr("ACS5", "acs5a")
                    .addAttr("ACS3", "acs3a")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("ACS4", "acs4a")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("ACS1", "acs1a")
                    .addAttr("ACS2", "acs2a")
                    .addLength("FRACCT", "fracctl")
                    .addLength("ACB4", "acb4l")
                    .addLength("ACB5", "acb5l")
                    .addLength("ACB2", "acb2l")
                    .addLength("ACB3", "acb3l")
                    .addLength("ACB1", "acb1l")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ACL4", "acl4l")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("ACL5", "acl5l")
                    .addLength("ACL2", "acl2l")
                    .addLength("ACL3", "acl3l")
                    .addLength("ACL1", "acl1l")
                    .addLength("TITLE", "titlel")
                    .addLength("ACS5", "acs5l")
                    .addLength("ACS3", "acs3l")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("ACS4", "acs4l")
                    .addLength("CURDATE", "curdatel")
                    .addLength("ACS1", "acs1l")
                    .addLength("ACS2", "acs2l")
                    .build();
        }
        return FieldMapping.empty();
    }
}
