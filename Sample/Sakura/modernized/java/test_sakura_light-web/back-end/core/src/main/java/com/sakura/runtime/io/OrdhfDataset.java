package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL ORDHF — ASSIGN TO ORDHF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FORDH__ORDHF.xml (shared)
 */
public class OrdhfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "ORDHF";
    }

    @Override
    public String getAssignTo() {
        return "ORDHF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FORDH__ORDHF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "OH-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("OH-CUST OH-DATE", "OH-DATE OH-NO");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("OH-CUST OH-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
