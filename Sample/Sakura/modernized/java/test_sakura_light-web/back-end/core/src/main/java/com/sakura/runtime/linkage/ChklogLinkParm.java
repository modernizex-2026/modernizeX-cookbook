package com.sakura.runtime.linkage;

import com.sakura.runtime.FieldWidth;

import lombok.Getter;
import lombok.Setter;

/**
 * Linkage parameters for COBOL CALL CHKLOG. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class ChklogLinkParm {
    /** COBOL: KLOGIN (group) */
    private Klogin klogin = new Klogin();

    /** COBOL group: KLOGIN */
    @Getter
    @Setter
    public static class Klogin {
        /** COBOL: KL-LOGIN PIC X(12) */
        @FieldWidth(12)
        private String klLogin = "            ";

        /** COBOL: KL-PASSWORD PIC X(16) */
        @FieldWidth(16)
        private String klPassword = "                ";

        /** COBOL: KL-USER-CODE PIC 9(6) */
        @FieldWidth(6)
        private int klUserCode;

        /** COBOL: KL-USER-NAME PIC X(30) */
        @FieldWidth(30)
        private String klUserName = "                              ";

        /** COBOL: KL-ROLE PIC 9(1) */
        @FieldWidth(1)
        private int klRole;

        /** COBOL: KL-AUTH PIC X(5) */
        @FieldWidth(5)
        private String klAuth = "     ";

        /** COBOL: KL-STATUS PIC X(2) */
        @FieldWidth(2)
        private String klStatus = "  ";
    }
}
