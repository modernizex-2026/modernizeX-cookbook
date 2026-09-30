package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL REPF — ASSIGN TO REPF-MSD. Organization: LINE_SEQUENTIAL. Layout:
 * /record-schema/shared/shared_e3bd7fb0__REPF.xml (shared)
 */
public class RepfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "REPF";
    }

    @Override
    public String getAssignTo() {
        return "REPF-MSD";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/shared_e3bd7fb0__REPF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    /**
     * SEQUENTIAL / LINE SEQUENTIAL → text mode: reader reads until '\n', pads short records with
     * spaces.
     */
    @Override
    protected boolean isBinary() {
        return false;
    }
}
