package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL RCPTF — ASSIGN TO RCPTF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/RCPTF.xml
 */
public class RcptfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "RCPTF";
    }

    @Override
    public String getAssignTo() {
        return "RCPTF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/RCPTF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "RE-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("RE-CUST RE-DATE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("RE-CUST RE-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
