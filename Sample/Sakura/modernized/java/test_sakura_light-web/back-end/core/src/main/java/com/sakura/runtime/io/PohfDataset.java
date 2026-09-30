package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL POHF — ASSIGN TO POHF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FPOH__POHF.xml (shared)
 */
public class PohfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "POHF";
    }

    @Override
    public String getAssignTo() {
        return "POHF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FPOH__POHF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "PH-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("PH-SUPP PH-DATE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("PH-SUPP PH-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
