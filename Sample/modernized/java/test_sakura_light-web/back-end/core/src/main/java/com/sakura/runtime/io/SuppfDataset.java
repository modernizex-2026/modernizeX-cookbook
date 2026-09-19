package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL SUPPF — ASSIGN TO SUPPF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FSUPP__SUPPF.xml (shared)
 */
public class SuppfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "SUPPF";
    }

    @Override
    public String getAssignTo() {
        return "SUPPF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FSUPP__SUPPF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "SP-CODE";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("SP-NAME");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("SP-NAME".equals(name)) {
            return true;
        }
        return false;
    }
}
