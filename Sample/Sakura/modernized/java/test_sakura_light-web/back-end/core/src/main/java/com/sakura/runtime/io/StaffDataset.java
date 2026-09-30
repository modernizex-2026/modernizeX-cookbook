package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL STAFF — ASSIGN TO STAFF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FSTAF__STAFF.xml (shared)
 */
public class StaffDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "STAFF";
    }

    @Override
    public String getAssignTo() {
        return "STAFF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FSTAF__STAFF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "SF-CODE";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("SF-DEPT SF-CODE");
    }
}
