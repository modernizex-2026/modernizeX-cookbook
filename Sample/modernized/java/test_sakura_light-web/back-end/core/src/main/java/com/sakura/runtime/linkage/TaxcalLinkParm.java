package com.sakura.runtime.linkage;

import com.sakura.runtime.FieldWidth;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Linkage parameters for COBOL CALL TAXCAL. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class TaxcalLinkParm {
    /** COBOL: KTAX (group) */
    private Ktax ktax = new Ktax();

    /** COBOL group: KTAX */
    @Getter
    @Setter
    public static class Ktax {
        /** COBOL: KT-CATEGORY PIC 9(1) */
        @FieldWidth(1)
        private int ktCategory;

        /** COBOL: KT-TAX-TYPE PIC 9(1) */
        @FieldWidth(1)
        private int ktTaxType;

        /** COBOL: KT-ROUND PIC 9(1) */
        @FieldWidth(1)
        private int ktRound;

        /** COBOL: KT-DATE PIC 9(8) */
        @FieldWidth(8)
        private int ktDate;

        /** COBOL: KT-AMOUNT PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal ktAmount = BigDecimal.ZERO;

        /** COBOL: KT-TAX PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal ktTax = BigDecimal.ZERO;

        /** COBOL: KT-NET PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal ktNet = BigDecimal.ZERO;

        /** COBOL: KT-GROSS PIC S9(11) */
        @FieldWidth(6)
        private BigDecimal ktGross = BigDecimal.ZERO;

        /** COBOL: KT-RATE PIC S9(2)V9(3) */
        @FieldWidth(3)
        private BigDecimal ktRate = BigDecimal.ZERO;

        /** COBOL: KT-STATUS PIC X(2) */
        @FieldWidth(2)
        private String ktStatus = "  ";
    }
}
