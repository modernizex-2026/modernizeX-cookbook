package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL CPRCF — ASSIGN TO CPRCF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/CPRCF.xml
 */
public class CprcfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "CPRCF";
    }

    @Override
    public String getAssignTo() {
        return "CPRCF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/CPRCF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "CP-CUST CP-PROD";
    }
}
