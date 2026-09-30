package com.generated.orion.occustv.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCCUSTV. Contains button definitions, FSET fields, and field mappings.
 */
public class OccustvBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MCUSTVA", Set.of("CUSTID"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MCUSTVA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCCUSTV_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MCUSTVA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("CUADDR", "cuaddri")
                    .addDataIn("CUFICO", "cuficoi")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("CUSTID", "custidi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("CUPHONE", "cuphonei")
                    .addDataIn("CUNAME", "cunamei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("CUCITY", "cucityi")
                    .addDataOut("CUADDR", "cuaddro")
                    .addDataOut("CUFICO", "cuficoo")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("CUSTID", "custido")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("CUPHONE", "cuphoneo")
                    .addDataOut("CUNAME", "cunameo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("CUCITY", "cucityo")
                    .addAttr("CUADDR", "cuaddra")
                    .addAttr("CUFICO", "cuficoa")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("CUSTID", "custida")
                    .addAttr("TITLE", "titlea")
                    .addAttr("CUPHONE", "cuphonea")
                    .addAttr("CUNAME", "cunamea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("CUCITY", "cucitya")
                    .addLength("CUADDR", "cuaddrl")
                    .addLength("CUFICO", "cuficol")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("CUSTID", "custidl")
                    .addLength("TITLE", "titlel")
                    .addLength("CUPHONE", "cuphonel")
                    .addLength("CUNAME", "cunamel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("CUCITY", "cucityl")
                    .build();
        }
        return FieldMapping.empty();
    }
}
