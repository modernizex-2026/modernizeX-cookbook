package com.sakura.ap0010.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL PAYF — ASSIGN TO PAYF-RDB. Organization: INDEXED. Layout:
 * /record-schema/ap0010/PAYF.xml
 */
public class PayfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "PAYF";
    }

    @Override
    public String getAssignTo() {
        return "PAYF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/ap0010/PAYF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "PY-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("PY-SUPP PY-DATE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("PY-SUPP PY-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
