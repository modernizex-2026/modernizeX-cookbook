package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL PRODF — ASSIGN TO PRODF-RDB. Organization: INDEXED. Layout:
 * /record-schema/shared/FPROD__PRODF.xml (shared)
 */
public class ProdfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "PRODF";
    }

    @Override
    public String getAssignTo() {
        return "PRODF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/shared/FPROD__PRODF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "PR-CODE";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("PR-NAME", "PR-CATEGORY PR-CODE", "PR-BARCODE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("PR-NAME".equals(name)) {
            return true;
        }
        if ("PR-BARCODE".equals(name)) {
            return true;
        }
        return false;
    }
}
