package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL STOKF — ASSIGN TO STOKF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FSTOK__STOKF.xml (shared)
 */
public class StokfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "STOKF";
    }

    @Override
    public String getAssignTo() {
        return "STOKF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FSTOK__STOKF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "SK-PROD SK-WHSE";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("SK-WHSE SK-PROD");
    }
}
