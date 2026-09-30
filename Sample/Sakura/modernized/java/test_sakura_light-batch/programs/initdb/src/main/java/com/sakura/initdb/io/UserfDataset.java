package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL USERF — ASSIGN TO USERF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/USERF.xml
 */
public class UserfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "USERF";
    }

    @Override
    public String getAssignTo() {
        return "USERF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/USERF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "US-CODE";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("US-LOGIN");
    }
}
