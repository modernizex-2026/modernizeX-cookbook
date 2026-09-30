package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL APLF — ASSIGN TO APLF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FAPL__APLF.xml (shared)
 */
public class AplfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "APLF";
    }

    @Override
    public String getAssignTo() {
        return "APLF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FAPL__APLF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "PL-SEQ";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("PL-SUPP PL-DATE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("PL-SUPP PL-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
