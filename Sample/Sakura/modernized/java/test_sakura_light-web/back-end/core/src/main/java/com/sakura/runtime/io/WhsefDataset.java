package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL WHSEF — ASSIGN TO WHSEF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FWHSE__WHSEF.xml (shared)
 */
public class WhsefDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "WHSEF";
    }

    @Override
    public String getAssignTo() {
        return "WHSEF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FWHSE__WHSEF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "WH-CODE";
    }
}
