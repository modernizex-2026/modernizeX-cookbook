package com.sakura.runtime.linkage;

import com.sakura.runtime.FieldWidth;

import lombok.Getter;
import lombok.Setter;

/**
 * Linkage parameters for COBOL CALL DATEUT. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class DateutLinkParm {
    /** COBOL: KDATE (group) */
    private Kdate kdate = new Kdate();

    /** COBOL group: KDATE */
    @Getter
    @Setter
    public static class Kdate {
        /** COBOL: KD-FUNC PIC X(4) */
        @FieldWidth(4)
        private String kdFunc = "    ";

        /** COBOL: KD-DATE1 PIC 9(8) */
        @FieldWidth(8)
        private int kdDate1;

        /** COBOL: KD-DATE2 PIC 9(8) */
        @FieldWidth(8)
        private int kdDate2;

        /** COBOL: KD-DAYS PIC S9(7) */
        @FieldWidth(7)
        private int kdDays;

        /** COBOL: KD-WEEKDAY PIC 9(1) */
        @FieldWidth(1)
        private int kdWeekday;

        /** COBOL: KD-STATUS PIC X(2) */
        @FieldWidth(2)
        private String kdStatus = "  ";
    }
}
