package com.generated.orion.occustl.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCCUSTL. Contains button definitions, FSET fields, and field mappings.
 */
public class OccustlBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF8", "PF8", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MCUSTLA", Set.of("FRCUST"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MCUSTLA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCCUSTL_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MCUSTLA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("CUF3", "cuf3i")
                    .addDataIn("CUF2", "cuf2i")
                    .addDataIn("CUF1", "cuf1i")
                    .addDataIn("CUL4", "cul4i")
                    .addDataIn("CUL3", "cul3i")
                    .addDataIn("CUL2", "cul2i")
                    .addDataIn("CUN4", "cun4i")
                    .addDataIn("CUL1", "cul1i")
                    .addDataIn("CUN3", "cun3i")
                    .addDataIn("FRCUST", "frcusti")
                    .addDataIn("CUN2", "cun2i")
                    .addDataIn("CUN1", "cun1i")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataIn("CUF4", "cuf4i")
                    .addDataOut("CUF3", "cuf3o")
                    .addDataOut("CUF2", "cuf2o")
                    .addDataOut("CUF1", "cuf1o")
                    .addDataOut("CUL4", "cul4o")
                    .addDataOut("CUL3", "cul3o")
                    .addDataOut("CUL2", "cul2o")
                    .addDataOut("CUN4", "cun4o")
                    .addDataOut("CUL1", "cul1o")
                    .addDataOut("CUN3", "cun3o")
                    .addDataOut("FRCUST", "frcusto")
                    .addDataOut("CUN2", "cun2o")
                    .addDataOut("CUN1", "cun1o")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addDataOut("CUF4", "cuf4o")
                    .addAttr("CUF3", "cuf3a")
                    .addAttr("CUF2", "cuf2a")
                    .addAttr("CUF1", "cuf1a")
                    .addAttr("CUL4", "cul4a")
                    .addAttr("CUL3", "cul3a")
                    .addAttr("CUL2", "cul2a")
                    .addAttr("CUN4", "cun4a")
                    .addAttr("CUL1", "cul1a")
                    .addAttr("CUN3", "cun3a")
                    .addAttr("FRCUST", "frcusta")
                    .addAttr("CUN2", "cun2a")
                    .addAttr("CUN1", "cun1a")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addAttr("CUF4", "cuf4a")
                    .addLength("CUF3", "cuf3l")
                    .addLength("CUF2", "cuf2l")
                    .addLength("CUF1", "cuf1l")
                    .addLength("CUL4", "cul4l")
                    .addLength("CUL3", "cul3l")
                    .addLength("CUL2", "cul2l")
                    .addLength("CUN4", "cun4l")
                    .addLength("CUL1", "cul1l")
                    .addLength("CUN3", "cun3l")
                    .addLength("FRCUST", "frcustl")
                    .addLength("CUN2", "cun2l")
                    .addLength("CUN1", "cun1l")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("TITLE", "titlel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .addLength("CUF4", "cuf4l")
                    .build();
        }
        return FieldMapping.empty();
    }
}
