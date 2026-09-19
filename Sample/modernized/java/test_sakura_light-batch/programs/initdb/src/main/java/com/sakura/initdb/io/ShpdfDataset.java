package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL SHPDF — ASSIGN TO SHPDF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/SHPDF.xml
 */
public class ShpdfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "SHPDF";
    }

    @Override
    public String getAssignTo() {
        return "SHPDF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/SHPDF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "XD-NO XD-LINE";
    }
}
