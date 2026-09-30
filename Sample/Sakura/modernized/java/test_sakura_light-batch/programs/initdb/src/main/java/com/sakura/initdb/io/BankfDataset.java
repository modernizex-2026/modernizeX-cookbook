package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL BANKF — ASSIGN TO BANKF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/BANKF.xml
 */
public class BankfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "BANKF";
    }

    @Override
    public String getAssignTo() {
        return "BANKF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/BANKF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "BK-CODE";
    }
}
