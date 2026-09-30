package com.sakura.numgen.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL NUMCF — ASSIGN TO NUMCF-RDB. Organization: INDEXED. Layout:
 * /record-schema/numgen/NUMCF.xml
 */
public class NumcfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "NUMCF";
    }

    @Override
    public String getAssignTo() {
        return "NUMCF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/numgen/NUMCF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "NM-KEY";
    }
}
