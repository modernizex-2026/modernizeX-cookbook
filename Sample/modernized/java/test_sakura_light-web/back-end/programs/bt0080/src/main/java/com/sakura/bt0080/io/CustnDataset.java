package com.sakura.bt0080.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL CUSTN — ASSIGN TO CUSTN-MSD. Organization: SEQUENTIAL. Layout:
 * /record-schema/bt0080/CUSTN.xml
 */
public class CustnDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "CUSTN";
    }

    @Override
    public String getAssignTo() {
        return "CUSTN-MSD";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/bt0080/CUSTN.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }
}
