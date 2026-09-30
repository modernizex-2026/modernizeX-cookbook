package com.generated.orion.common.linkage;

import lombok.Getter;
import lombok.Setter;

/**
 * Linkage parameters for COBOL CALL OCCUSTV. Shared between caller and callee — lives in
 * batch-common/linkage/.
 */
@Getter
@Setter
public class OccustvLinkParm {
    /** COBOL: DFHCOMMAREA (group) */
    private Dfhcommarea dfhcommarea = new Dfhcommarea();

    /** COBOL group: DFHCOMMAREA */
    @Getter
    @Setter
    public static class Dfhcommarea {
        /** COBOL: LK-COMMAREA PIC X(01) */
        private String[] lkCommarea = new String[] {""};
    }
}
