package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL ORDDF — ASSIGN TO ORDDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/ORDDF.xml
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
        return "/record-schema/initdb/ORDDF.xml";
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
