package com.generated.orion.occusta.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCCUSTA. Contains button definitions, FSET fields, and field mappings.
 */
public class OccustaBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields(
                "MCUSTAA",
                Set.of("CUSTID", "CUFNAM", "CULNAM", "CUADDR", "CUCITY", "CUSSN", "CUFICO"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MCUSTAA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCCUSTA_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MCUSTAA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("CUADDR", "cuaddri")
                    .addDataIn("CUFICO", "cuficoi")
                    .addDataIn("CUSSN", "cussni")
                    .addDataIn("CUFNAM", "cufnami")
                    .addDataIn("CULNAM", "culnami")
                    .addDataIn("CUSTID", "custidi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("CUCITY", "cucityi")
                    .addDataOut("CUADDR", "cuaddro")
                    .addDataOut("CUFICO", "cuficoo")
                    .addDataOut("CUSSN", "cussno")
                    .addDataOut("CUFNAM", "cufnamo")
                    .addDataOut("CULNAM", "culnamo")
                    .addDataOut("CUSTID", "custido")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("CUCITY", "cucityo")
                    .addAttr("CUADDR", "cuaddra")
                    .addAttr("CUFICO", "cuficoa")
                    .addAttr("CUSSN", "cussna")
                    .addAttr("CUFNAM", "cufnama")
                    .addAttr("CULNAM", "culnama")
                    .addAttr("CUSTID", "custida")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("CUCITY", "cucitya")
                    .addLength("CUADDR", "cuaddrl")
                    .addLength("CUFICO", "cuficol")
                    .addLength("CUSSN", "cussnl")
                    .addLength("CUFNAM", "cufnaml")
                    .addLength("CULNAM", "culnaml")
                    .addLength("CUSTID", "custidl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("TITLE", "titlel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("CUCITY", "cucityl")
                    .build();
        }
        return FieldMapping.empty();
    }
}
