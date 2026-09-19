package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL DEPTF — ASSIGN TO DEPTF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/DEPTF.xml
 */
public class DeptfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "DEPTF";
    }

    @Override
    public String getAssignTo() {
        return "DEPTF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/DEPTF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "DP-CODE";
    }
}
