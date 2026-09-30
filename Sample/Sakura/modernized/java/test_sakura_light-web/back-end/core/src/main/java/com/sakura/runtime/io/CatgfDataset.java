package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL CATGF — ASSIGN TO CATGF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FCATG__CATGF.xml (shared)
 */
public class CatgfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "CATGF";
    }

    @Override
    public String getAssignTo() {
        return "CATGF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FCATG__CATGF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "CT-CODE";
    }
}
