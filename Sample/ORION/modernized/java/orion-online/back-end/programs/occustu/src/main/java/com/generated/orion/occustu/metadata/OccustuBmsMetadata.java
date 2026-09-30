package com.generated.orion.occustu.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCCUSTU. Contains button definitions, FSET fields, and field mappings.
 */
public class OccustuBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields(
                "MCUSTUA", Set.of("CUSTID", "CUFNAM", "CULNAM", "CUADDR", "CUCITY", "CUPHONE"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MCUSTUA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCCUSTU_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MCUSTUA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("CUADDR", "cuaddri")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("CUFNAM", "cufnami")
                    .addDataIn("CULNAM", "culnami")
                    .addDataIn("CUSTID", "custidi")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("CUPHONE", "cuphonei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("CUCITY", "cucityi")
                    .addDataOut("CUADDR", "cuaddro")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("CUFNAM", "cufnamo")
                    .addDataOut("CULNAM", "culnamo")
                    .addDataOut("CUSTID", "custido")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("CUPHONE", "cuphoneo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("CUCITY", "cucityo")
                    .addAttr("CUADDR", "cuaddra")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("CUFNAM", "cufnama")
                    .addAttr("CULNAM", "culnama")
                    .addAttr("CUSTID", "custida")
                    .addAttr("TITLE", "titlea")
                    .addAttr("CUPHONE", "cuphonea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("CUCITY", "cucitya")
                    .addLength("CUADDR", "cuaddrl")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("CUFNAM", "cufnaml")
                    .addLength("CULNAM", "culnaml")
                    .addLength("CUSTID", "custidl")
                    .addLength("TITLE", "titlel")
                    .addLength("CUPHONE", "cuphonel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("CUCITY", "cucityl")
                    .build();
        }
        return FieldMapping.empty();
    }
}
