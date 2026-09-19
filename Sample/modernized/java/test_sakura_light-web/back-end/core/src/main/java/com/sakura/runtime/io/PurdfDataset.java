package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL PURDF — ASSIGN TO PURDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FPURD__PURDF.xml (shared)
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
        return "/record-schema/shared/FPURD__PURDF.xml";
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
