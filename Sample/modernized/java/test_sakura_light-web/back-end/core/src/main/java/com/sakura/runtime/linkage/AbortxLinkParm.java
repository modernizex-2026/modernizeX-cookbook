package com.sakura.runtime.linkage;

import com.sakura.runtime.FieldWidth;

import lombok.Getter;
import lombok.Setter;

/**
 * Linkage parameters for COBOL CALL ABORTX. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class AbortxLinkParm {
    /** COBOL: KABEND (group) */
    private Kabend kabend = new Kabend();

    /** COBOL group: KABEND */
    @Getter
    @Setter
    public static class Kabend {
        /** COBOL: KA-PROGID PIC X(6) */
        @FieldWidth(6)
        private String kaProgid = "      ";

        /** COBOL: KA-FILE PIC X(8) */
        @FieldWidth(8)
        private String kaFile = "        ";

        /** COBOL: KA-FSTS PIC X(2) */
        @FieldWidth(2)
        private String kaFsts = "  ";

        /** COBOL: KA-MSGCODE PIC X(6) */
        @FieldWidth(6)
        private String kaMsgcode = "      ";

        /** COBOL: KA-DETAIL PIC X(40) */
        @FieldWidth(40)
        private String kaDetail = "                                        ";
    }
}
