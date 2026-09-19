package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL ORDDF — ASSIGN TO ORDDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FORDD__ORDDF.xml (shared)
 */
public class OrddfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "ORDDF";
    }

    @Override
    public String getAssignTo() {
        return "ORDDF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FORDD__ORDDF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "OD-NO OD-LINE";
    }
}
