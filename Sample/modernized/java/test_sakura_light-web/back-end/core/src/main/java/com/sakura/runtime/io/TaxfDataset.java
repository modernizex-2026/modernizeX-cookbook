package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL TAXF — ASSIGN TO TAXF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FTAX__TAXF.xml (shared)
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
        return "/record-schema/shared/FTAX__TAXF.xml";
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
