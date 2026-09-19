package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL DEPTF — ASSIGN TO DEPTF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FDEPT__DEPTF.xml (shared)
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
        return "/record-schema/shared/FDEPT__DEPTF.xml";
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
