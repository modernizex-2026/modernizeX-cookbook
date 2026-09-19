package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL PURDF — ASSIGN TO PURDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/PURDF.xml
 */
public class PurdfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "PURDF";
    }

    @Override
    public String getAssignTo() {
        return "PURDF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/PURDF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "VD-NO VD-LINE";
    }
}
