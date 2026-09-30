package com.generated.orion.oumqbrg.io;

import com.generated.orion.runtime.record.RawDatasetBase;

/**
 * File COBOL TRAN-FILE — ASSIGN TO TRANFILE. Organization: INDEXED. Layout:
 * /record-schema/oumqbrg/TRAN-FILE.xml
 */
public class TranFileDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "TRAN-FILE";
    }

    @Override
    public String getAssignTo() {
        return "TRANFILE";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/oumqbrg/TRAN-FILE.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "TR-ID";
    }
}
