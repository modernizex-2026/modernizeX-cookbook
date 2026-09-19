package com.sakura.abortx.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL LOGF — ASSIGN TO LOGF-MSD. Organization: LINE_SEQUENTIAL. Layout:
 * /record-schema/abortx/LOGF.xml
 */
public class LogfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "LOGF";
    }

    @Override
    public String getAssignTo() {
        return "LOGF-MSD";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/abortx/LOGF.xml";
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
