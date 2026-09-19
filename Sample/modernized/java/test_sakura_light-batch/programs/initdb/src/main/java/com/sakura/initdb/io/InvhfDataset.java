package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL INVHF — ASSIGN TO INVHF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/INVHF.xml
 */
public class InvhfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "INVHF";
    }

    @Override
    public String getAssignTo() {
        return "INVHF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/INVHF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "IH-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("IH-CUST IH-DATE", "IH-DATE IH-NO");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("IH-CUST IH-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
