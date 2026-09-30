package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL PURHF — ASSIGN TO PURHF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/PURHF.xml
 */
public class PurhfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "PURHF";
    }

    @Override
    public String getAssignTo() {
        return "PURHF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/PURHF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "VH-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("VH-SUPP VH-DATE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("VH-SUPP VH-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
