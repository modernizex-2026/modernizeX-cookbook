package com.sakura.runtime.linkage;

import com.sakura.runtime.FieldWidth;

import lombok.Getter;
import lombok.Setter;

/**
 * Linkage parameters for COBOL CALL NUMGEN. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class NumgenLinkParm {
    /** COBOL: KNUM (group) */
    private Knum knum = new Knum();

    /** COBOL group: KNUM */
    @Getter
    @Setter
    public static class Knum {
        /** COBOL: KNUM-KEY PIC X(8) */
        @FieldWidth(8)
        private String knumKey = "        ";

        /** COBOL: KNUM-NUMBER PIC 9(10) */
        @FieldWidth(10)
        private long knumNumber;

        /** COBOL: KNUM-STATUS PIC X(2) */
        @FieldWidth(2)
        private String knumStatus = "  ";
    }
}
