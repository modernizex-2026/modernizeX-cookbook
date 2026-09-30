package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL INVDF — ASSIGN TO INVDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/INVDF.xml
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
        return "/record-schema/initdb/INVDF.xml";
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
