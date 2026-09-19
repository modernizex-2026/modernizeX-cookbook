package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL RCVDF — ASSIGN TO RCVDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/RCVDF.xml
 */
public class RcvdfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "RCVDF";
    }

    @Override
    public String getAssignTo() {
        return "RCVDF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/RCVDF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "RD-NO RD-LINE";
    }
}
