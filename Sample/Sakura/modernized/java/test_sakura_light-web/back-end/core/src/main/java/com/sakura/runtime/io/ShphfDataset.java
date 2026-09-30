package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL SHPHF — ASSIGN TO SHPHF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FSHPH__SHPHF.xml (shared)
 */
public class ShphfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "SHPHF";
    }

    @Override
    public String getAssignTo() {
        return "SHPHF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FSHPH__SHPHF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "XH-NO";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("XH-ORDER", "XH-DATE XH-NO");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("XH-ORDER".equals(name)) {
            return true;
        }
        return false;
    }
}
