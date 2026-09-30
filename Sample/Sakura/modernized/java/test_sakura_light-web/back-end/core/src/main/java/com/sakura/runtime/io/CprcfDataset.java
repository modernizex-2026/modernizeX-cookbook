package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL CPRCF — ASSIGN TO CPRCF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FCPRC__CPRCF.xml (shared)
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
        return "/record-schema/shared/FCPRC__CPRCF.xml";
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
