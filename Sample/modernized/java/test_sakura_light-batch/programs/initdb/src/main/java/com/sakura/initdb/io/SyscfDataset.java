package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL SYSCF — ASSIGN TO SYSCF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/SYSCF.xml
 */
public class SyscfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "SYSCF";
    }

    @Override
    public String getAssignTo() {
        return "SYSCF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/SYSCF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "SY-KEY";
    }
}
