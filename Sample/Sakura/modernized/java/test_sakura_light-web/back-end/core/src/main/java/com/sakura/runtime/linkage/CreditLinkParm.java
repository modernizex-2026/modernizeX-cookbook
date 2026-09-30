package com.sakura.runtime.linkage;

import com.sakura.runtime.FieldWidth;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Linkage parameters for COBOL CALL CREDIT. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class CreditLinkParm {
    /** COBOL: KCRED (group) */
    private Kcred kcred = new Kcred();

    /** COBOL group: KCRED */
    @Getter
    @Setter
    public static class Kcred {
        /** COBOL: KC-CUST PIC 9(6) */
        @FieldWidth(6)
        private int kcCust;

        /** COBOL: KC-AMOUNT PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal kcAmount = BigDecimal.ZERO;

        /** COBOL: KC-LIMIT PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal kcLimit = BigDecimal.ZERO;

        /** COBOL: KC-BALANCE PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal kcBalance = BigDecimal.ZERO;

        /** COBOL: KC-NEWBAL PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal kcNewbal = BigDecimal.ZERO;

        /** COBOL: KC-EXCEED PIC 9(1) */
        @FieldWidth(1)
        private int kcExceed;

        /** COBOL: KC-STATUS PIC X(2) */
        @FieldWidth(2)
        private String kcStatus = "  ";
    }
}
