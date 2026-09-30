package com.sakura.runtime.linkage;

import com.sakura.runtime.FieldWidth;

import lombok.Getter;
import lombok.Setter;

/**
 * Linkage parameters for COBOL CALL GETMSG. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class GetmsgLinkParm {
    /** COBOL: KMSG (group) */
    private Kmsg kmsg = new Kmsg();

    /** COBOL group: KMSG */
    @Getter
    @Setter
    public static class Kmsg {
        /** COBOL: KM-CODE PIC X(6) */
        @FieldWidth(6)
        private String kmCode = "      ";

        /** COBOL: KM-TEXT PIC X(60) */
        @FieldWidth(60)
        private String kmText = " ".repeat(60);

        /** COBOL: KM-STATUS PIC X(2) */
        @FieldWidth(2)
        private String kmStatus = "  ";
    }
}
