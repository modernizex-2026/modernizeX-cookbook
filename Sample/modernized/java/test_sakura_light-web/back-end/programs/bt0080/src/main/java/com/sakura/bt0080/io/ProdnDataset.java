package com.sakura.bt0080.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL PRODN — ASSIGN TO PRODN-MSD. Organization: SEQUENTIAL. Layout:
 * /record-schema/bt0080/PRODN.xml
 */
public class ProdnDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "PRODN";
    }

    @Override
    public String getAssignTo() {
        return "PRODN-MSD";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/bt0080/PRODN.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }
}
