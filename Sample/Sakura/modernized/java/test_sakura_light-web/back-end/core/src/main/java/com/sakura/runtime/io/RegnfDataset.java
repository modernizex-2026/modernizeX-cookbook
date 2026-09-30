package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL REGNF — ASSIGN TO REGNF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FREGN__REGNF.xml (shared)
 */
public class RegnfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "REGNF";
    }

    @Override
    public String getAssignTo() {
        return "REGNF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FREGN__REGNF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "RG-CODE";
    }
}
