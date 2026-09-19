package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL SMOVF — ASSIGN TO SMOVF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/SMOVF.xml
 */
public class SmovfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "SMOVF";
    }

    @Override
    public String getAssignTo() {
        return "SMOVF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/SMOVF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "SM-SEQ";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("SM-PROD SM-DATE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("SM-PROD SM-DATE".equals(name)) {
            return true;
        }
        return false;
    }
}
