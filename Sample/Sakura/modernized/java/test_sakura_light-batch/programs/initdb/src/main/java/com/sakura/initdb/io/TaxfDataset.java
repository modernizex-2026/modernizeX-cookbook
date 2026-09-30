package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL TAXF — ASSIGN TO TAXF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/TAXF.xml
 */
public class TaxfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "TAXF";
    }

    @Override
    public String getAssignTo() {
        return "TAXF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/TAXF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "TX-CODE TX-START-DATE";
    }
}
