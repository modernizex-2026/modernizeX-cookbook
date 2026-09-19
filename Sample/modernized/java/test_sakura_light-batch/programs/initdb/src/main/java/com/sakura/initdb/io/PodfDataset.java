package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL PODF — ASSIGN TO PODF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/PODF.xml
 */
public class PodfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "PODF";
    }

    @Override
    public String getAssignTo() {
        return "PODF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/PODF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "PD-NO PD-LINE";
    }
}
