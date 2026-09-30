package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL MSGF — ASSIGN TO MSGF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FMSG__MSGF.xml (shared)
 */
public class MsgfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "MSGF";
    }

    @Override
    public String getAssignTo() {
        return "MSGF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FMSG__MSGF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "MG-CODE";
    }
}
