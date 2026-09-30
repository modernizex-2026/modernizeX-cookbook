package com.generated.orion.ocusrl.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OCUSRL. Contains button definitions, FSET fields, and field mappings. */
public class OcusrlBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF7", "PF7", ""),
                new ScreenResponse.ButtonDef("PF8", "PF8", ""));
    }

    public static void registerFsetFields(AppRunner runner) {}

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MUSRLA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCUSRL_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MUSRLA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("USR4", "usr4i")
                    .addDataIn("UNM4", "unm4i")
                    .addDataIn("UTY1", "uty1i")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("USR3", "usr3i")
                    .addDataIn("UNM3", "unm3i")
                    .addDataIn("USR2", "usr2i")
                    .addDataIn("UNM2", "unm2i")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("USR1", "usr1i")
                    .addDataIn("UNM1", "unm1i")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("UTY4", "uty4i")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("UTY3", "uty3i")
                    .addDataIn("UTY2", "uty2i")
                    .addDataOut("USR4", "usr4o")
                    .addDataOut("UNM4", "unm4o")
                    .addDataOut("UTY1", "uty1o")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("USR3", "usr3o")
                    .addDataOut("UNM3", "unm3o")
                    .addDataOut("USR2", "usr2o")
                    .addDataOut("UNM2", "unm2o")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("USR1", "usr1o")
                    .addDataOut("UNM1", "unm1o")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("UTY4", "uty4o")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("UTY3", "uty3o")
                    .addDataOut("UTY2", "uty2o")
                    .addAttr("USR4", "usr4a")
                    .addAttr("UNM4", "unm4a")
                    .addAttr("UTY1", "uty1a")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("USR3", "usr3a")
                    .addAttr("UNM3", "unm3a")
                    .addAttr("USR2", "usr2a")
                    .addAttr("UNM2", "unm2a")
                    .addAttr("TITLE", "titlea")
                    .addAttr("USR1", "usr1a")
                    .addAttr("UNM1", "unm1a")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("UTY4", "uty4a")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("UTY3", "uty3a")
                    .addAttr("UTY2", "uty2a")
                    .addLength("USR4", "usr4l")
                    .addLength("UNM4", "unm4l")
                    .addLength("UTY1", "uty1l")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("USR3", "usr3l")
                    .addLength("UNM3", "unm3l")
                    .addLength("USR2", "usr2l")
                    .addLength("UNM2", "unm2l")
                    .addLength("TITLE", "titlel")
                    .addLength("USR1", "usr1l")
                    .addLength("UNM1", "unm1l")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("UTY4", "uty4l")
                    .addLength("CURDATE", "curdatel")
                    .addLength("UTY3", "uty3l")
                    .addLength("UTY2", "uty2l")
                    .build();
        }
        return FieldMapping.empty();
    }
}
