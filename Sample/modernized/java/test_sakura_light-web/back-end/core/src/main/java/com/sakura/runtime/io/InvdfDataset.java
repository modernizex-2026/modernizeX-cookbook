package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL INVDF — ASSIGN TO INVDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FINVD__INVDF.xml (shared)
 */
public class InvdfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "INVDF";
    }

    @Override
    public String getAssignTo() {
        return "INVDF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FINVD__INVDF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "ID-NO ID-LINE";
    }
}
