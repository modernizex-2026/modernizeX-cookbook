package com.generated.orion.common.linkage;

import com.generated.orion.common.infrastructure.FieldWidth;

import lombok.Getter;
import lombok.Setter;

/**
 * Linkage parameters for COBOL CALL OUIMSPA. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class OuimspaLinkParm {
    /** COBOL: LK-PAU-PCB (group) */
    private LkPauPcb lkPauPcb = new LkPauPcb();

    /** COBOL group: LK-PAU-PCB */
    @Getter
    @Setter
    public static class LkPauPcb {
        /** COBOL: LK-PCB-DBD-NAME PIC X(08) */
        @FieldWidth(8)
        private String lkPcbDbdName = "        ";

        /** COBOL: LK-PCB-SEG-LEVEL PIC X(02) */
        @FieldWidth(2)
        private String lkPcbSegLevel = "  ";

        /** COBOL: LK-PCB-STATUS PIC X(02) */
        @FieldWidth(2)
        private String lkPcbStatus = "  ";

        /** COBOL: LK-PCB-PROC-OPTS PIC X(04) */
        @FieldWidth(4)
        private String lkPcbProcOpts = "    ";

        /** COBOL: LK-PCB-RESERVED PIC S9(05) */
        @FieldWidth(5)
        private int lkPcbReserved;

        /** COBOL: LK-PCB-SEG-NAME PIC X(08) */
        @FieldWidth(8)
        private String lkPcbSegName = "        ";

        /** COBOL: LK-PCB-KFB-LEN PIC S9(05) */
        @FieldWidth(5)
        private int lkPcbKfbLen;

        /** COBOL: LK-PCB-SENSEGS PIC S9(05) */
        @FieldWidth(5)
        private int lkPcbSensegs;

        /** COBOL: LK-PCB-KEY-FB PIC X(16) */
        @FieldWidth(16)
        private String lkPcbKeyFb = "                ";
    }
}
