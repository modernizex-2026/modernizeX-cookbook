package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL ARLF — ASSIGN TO ARLF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FARL__ARLF.xml (shared)
 */
public class ArlfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "ARLF";
    }

    @Override
    public String getAssignTo() {
        return "ARLF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FARL__ARLF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "AL-SEQ";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("AL-CUST AL-DATE", "AL-CUST AL-CLOSE-YM");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("AL-CUST AL-DATE".equals(name)) {
            return true;
        }
        if ("AL-CUST AL-CLOSE-YM".equals(name)) {
            return true;
        }
        return false;
    }
}
