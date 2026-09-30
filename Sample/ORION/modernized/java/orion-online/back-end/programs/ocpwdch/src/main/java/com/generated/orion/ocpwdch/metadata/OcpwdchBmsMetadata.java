package com.generated.orion.ocpwdch.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCPWDCH. Contains button definitions, FSET fields, and field mappings.
 */
public class OcpwdchBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MPWDCHA", Set.of("USERID", "OLDPWD", "NEWPWD", "CFMPWD"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MPWDCHA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCPWDCH_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MPWDCHA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("CFMPWD", "cfmpwdi")
                    .addDataIn("NEWPWD", "newpwdi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("USERID", "useridi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("OLDPWD", "oldpwdi")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("CFMPWD", "cfmpwdo")
                    .addDataOut("NEWPWD", "newpwdo")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("USERID", "userido")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("OLDPWD", "oldpwdo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("CFMPWD", "cfmpwda")
                    .addAttr("NEWPWD", "newpwda")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("USERID", "userida")
                    .addAttr("TITLE", "titlea")
                    .addAttr("OLDPWD", "oldpwda")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("CFMPWD", "cfmpwdl")
                    .addLength("NEWPWD", "newpwdl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("USERID", "useridl")
                    .addLength("TITLE", "titlel")
                    .addLength("OLDPWD", "oldpwdl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
