package com.generated.orion.ocanlin.metadata;

import com.appruntime.AppRunner;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;

import java.util.*;

/**
 * BMS screen metadata for OCANLIN. Contains button definitions, FSET fields, and field mappings.
 */
public class OcanlinBmsMetadata {

    public static List<ScreenResponse.ButtonDef> getButtonDefs() {
        return List.of(
                new ScreenResponse.ButtonDef("ENTER", "ENTER=PROCESS", ""),
                new ScreenResponse.ButtonDef("PF3", "PF3=BACK", ""),
                new ScreenResponse.ButtonDef("PF4", "PF4=CLEAR", ""),
                new ScreenResponse.ButtonDef("PF7", "PF7", ""),
                new ScreenResponse.ButtonDef("PF8", "PF8", ""));
    }

    public static void registerFsetFields(AppRunner runner) {
        runner.registerFsetFields("MANLINA", Set.of("ANMODE"));
    }

    /** Danh sách BMS map của program (cho smoke test / tooling). */
    public static List<String> getMapNames() {
        return List.of("MANLINA");
    }

    /** Classpath resource của WORKING-STORAGE layout (FieldStore đọc lúc runtime). */
    public static String getLayoutResource() {
        return "layout/OCANLIN_WS.xml";
    }

    public static FieldMapping getFieldMapping(String mapName) {
        if ("MANLINA".equalsIgnoreCase(mapName)) {
            return FieldMapping.builder()
                    .addDataIn("ANMODE", "anmodei")
                    .addDataIn("ANV5", "anv5i")
                    .addDataIn("ANV6", "anv6i")
                    .addDataIn("ANV3", "anv3i")
                    .addDataIn("ERRMSG", "errmsgi")
                    .addDataIn("CURTIME", "curtimei")
                    .addDataIn("ANV4", "anv4i")
                    .addDataIn("ANV1", "anv1i")
                    .addDataIn("ANV2", "anv2i")
                    .addDataIn("ANTOT1", "antot1i")
                    .addDataIn("ANHEAD", "anheadi")
                    .addDataIn("ANTOT2", "antot2i")
                    .addDataIn("ANA6", "ana6i")
                    .addDataIn("ANA4", "ana4i")
                    .addDataIn("ANC6", "anc6i")
                    .addDataIn("ANA5", "ana5i")
                    .addDataIn("ANA2", "ana2i")
                    .addDataIn("ANC4", "anc4i")
                    .addDataIn("ANA3", "ana3i")
                    .addDataIn("ANC5", "anc5i")
                    .addDataIn("ANC2", "anc2i")
                    .addDataIn("ANA1", "ana1i")
                    .addDataIn("ANC3", "anc3i")
                    .addDataIn("ANI6", "ani6i")
                    .addDataIn("ANC1", "anc1i")
                    .addDataIn("ANI4", "ani4i")
                    .addDataIn("ANK6", "ank6i")
                    .addDataIn("ANI5", "ani5i")
                    .addDataIn("ANI2", "ani2i")
                    .addDataIn("ANK4", "ank4i")
                    .addDataIn("ANI3", "ani3i")
                    .addDataIn("ANK5", "ank5i")
                    .addDataIn("ANK2", "ank2i")
                    .addDataIn("ANI1", "ani1i")
                    .addDataIn("ANK3", "ank3i")
                    .addDataIn("ANK1", "ank1i")
                    .addDataIn("TRNNAME", "trnnamei")
                    .addDataIn("TITLE", "titlei")
                    .addDataIn("PGMNAME", "pgmnamei")
                    .addDataIn("CURDATE", "curdatei")
                    .addDataOut("ANMODE", "anmodeo")
                    .addDataOut("ANV5", "anv5o")
                    .addDataOut("ANV6", "anv6o")
                    .addDataOut("ANV3", "anv3o")
                    .addDataOut("ERRMSG", "errmsgo")
                    .addDataOut("CURTIME", "curtimeo")
                    .addDataOut("ANV4", "anv4o")
                    .addDataOut("ANV1", "anv1o")
                    .addDataOut("ANV2", "anv2o")
                    .addDataOut("ANTOT1", "antot1o")
                    .addDataOut("ANHEAD", "anheado")
                    .addDataOut("ANTOT2", "antot2o")
                    .addDataOut("ANA6", "ana6o")
                    .addDataOut("ANA4", "ana4o")
                    .addDataOut("ANC6", "anc6o")
                    .addDataOut("ANA5", "ana5o")
                    .addDataOut("ANA2", "ana2o")
                    .addDataOut("ANC4", "anc4o")
                    .addDataOut("ANA3", "ana3o")
                    .addDataOut("ANC5", "anc5o")
                    .addDataOut("ANC2", "anc2o")
                    .addDataOut("ANA1", "ana1o")
                    .addDataOut("ANC3", "anc3o")
                    .addDataOut("ANI6", "ani6o")
                    .addDataOut("ANC1", "anc1o")
                    .addDataOut("ANI4", "ani4o")
                    .addDataOut("ANK6", "ank6o")
                    .addDataOut("ANI5", "ani5o")
                    .addDataOut("ANI2", "ani2o")
                    .addDataOut("ANK4", "ank4o")
                    .addDataOut("ANI3", "ani3o")
                    .addDataOut("ANK5", "ank5o")
                    .addDataOut("ANK2", "ank2o")
                    .addDataOut("ANI1", "ani1o")
                    .addDataOut("ANK3", "ank3o")
                    .addDataOut("ANK1", "ank1o")
                    .addDataOut("TRNNAME", "trnnameo")
                    .addDataOut("TITLE", "titleo")
                    .addDataOut("PGMNAME", "pgmnameo")
                    .addDataOut("CURDATE", "curdateo")
                    .addAttr("ANMODE", "anmodea")
                    .addAttr("ANV5", "anv5a")
                    .addAttr("ANV6", "anv6a")
                    .addAttr("ANV3", "anv3a")
                    .addAttr("ERRMSG", "errmsga")
                    .addAttr("CURTIME", "curtimea")
                    .addAttr("ANV4", "anv4a")
                    .addAttr("ANV1", "anv1a")
                    .addAttr("ANV2", "anv2a")
                    .addAttr("ANTOT1", "antot1a")
                    .addAttr("ANHEAD", "anheada")
                    .addAttr("ANTOT2", "antot2a")
                    .addAttr("ANA6", "ana6a")
                    .addAttr("ANA4", "ana4a")
                    .addAttr("ANC6", "anc6a")
                    .addAttr("ANA5", "ana5a")
                    .addAttr("ANA2", "ana2a")
                    .addAttr("ANC4", "anc4a")
                    .addAttr("ANA3", "ana3a")
                    .addAttr("ANC5", "anc5a")
                    .addAttr("ANC2", "anc2a")
                    .addAttr("ANA1", "ana1a")
                    .addAttr("ANC3", "anc3a")
                    .addAttr("ANI6", "ani6a")
                    .addAttr("ANC1", "anc1a")
                    .addAttr("ANI4", "ani4a")
                    .addAttr("ANK6", "ank6a")
                    .addAttr("ANI5", "ani5a")
                    .addAttr("ANI2", "ani2a")
                    .addAttr("ANK4", "ank4a")
                    .addAttr("ANI3", "ani3a")
                    .addAttr("ANK5", "ank5a")
                    .addAttr("ANK2", "ank2a")
                    .addAttr("ANI1", "ani1a")
                    .addAttr("ANK3", "ank3a")
                    .addAttr("ANK1", "ank1a")
                    .addAttr("TRNNAME", "trnnamea")
                    .addAttr("TITLE", "titlea")
                    .addAttr("PGMNAME", "pgmnamea")
                    .addAttr("CURDATE", "curdatea")
                    .addLength("ANMODE", "anmodel")
                    .addLength("ANV5", "anv5l")
                    .addLength("ANV6", "anv6l")
                    .addLength("ANV3", "anv3l")
                    .addLength("ERRMSG", "errmsgl")
                    .addLength("CURTIME", "curtimel")
                    .addLength("ANV4", "anv4l")
                    .addLength("ANV1", "anv1l")
                    .addLength("ANV2", "anv2l")
                    .addLength("ANTOT1", "antot1l")
                    .addLength("ANHEAD", "anheadl")
                    .addLength("ANTOT2", "antot2l")
                    .addLength("ANA6", "ana6l")
                    .addLength("ANA4", "ana4l")
                    .addLength("ANC6", "anc6l")
                    .addLength("ANA5", "ana5l")
                    .addLength("ANA2", "ana2l")
                    .addLength("ANC4", "anc4l")
                    .addLength("ANA3", "ana3l")
                    .addLength("ANC5", "anc5l")
                    .addLength("ANC2", "anc2l")
                    .addLength("ANA1", "ana1l")
                    .addLength("ANC3", "anc3l")
                    .addLength("ANI6", "ani6l")
                    .addLength("ANC1", "anc1l")
                    .addLength("ANI4", "ani4l")
                    .addLength("ANK6", "ank6l")
                    .addLength("ANI5", "ani5l")
                    .addLength("ANI2", "ani2l")
                    .addLength("ANK4", "ank4l")
                    .addLength("ANI3", "ani3l")
                    .addLength("ANK5", "ank5l")
                    .addLength("ANK2", "ank2l")
                    .addLength("ANI1", "ani1l")
                    .addLength("ANK3", "ank3l")
                    .addLength("ANK1", "ank1l")
                    .addLength("TRNNAME", "trnnamel")
                    .addLength("TITLE", "titlel")
                    .addLength("PGMNAME", "pgmnamel")
                    .addLength("CURDATE", "curdatel")
                    .build();
        }
        return FieldMapping.empty();
    }
}
