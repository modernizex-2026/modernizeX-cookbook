package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL RCVHF — ASSIGN TO RCVHF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/RCVHF.xml
 */
public class RcvhfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "RCVHF";
    }

    @Override
    public String getAssignTo() {
        return "RCVHF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/RCVHF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "RH-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("RH-PO", "RH-DATE RH-NO");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("RH-PO".equals(name)) {
            return true;
        }
        return false;
    }
}
