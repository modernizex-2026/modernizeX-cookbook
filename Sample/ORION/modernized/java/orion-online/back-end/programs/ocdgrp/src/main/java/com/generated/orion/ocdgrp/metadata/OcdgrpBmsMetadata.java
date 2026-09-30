package com.generated.orion.ocdgrp.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/** BMS screen metadata for OCDGRP. Contains button definitions, FSET fields, and field mappings. */
public class OcdgrpBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF5", "PF5", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MDGRPA", Set.of("DGGRP", "DGTYPE", "DGCAT", "DGRATE"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MDGRPA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCDGRP_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MDGRPA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("DGTYPE", "dgtypei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("DGRATE", "dgratei")
                    .addDataIn("DGCAT", "dgcati")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("DGGRP", "dggrpi")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("DGTYPE", "dgtypeo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("DGRATE", "dgrateo")
                    .addDataOut("DGCAT", "dgcato")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("DGGRP", "dggrpo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("DGTYPE", "dgtypea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("TITLE", "titlea")
                    .addAttr("DGRATE", "dgratea")
                    .addAttr("DGCAT", "dgcata")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("DGGRP", "dggrpa")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("DGTYPE", "dgtypel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("TITLE", "titlel")
                    .addLength("DGRATE", "dgratel")
                    .addLength("DGCAT", "dgcatl")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("DGGRP", "dggrpl")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
