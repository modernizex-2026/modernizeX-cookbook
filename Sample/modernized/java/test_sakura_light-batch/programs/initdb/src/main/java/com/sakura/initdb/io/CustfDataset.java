package com.sakura.initdb.io;

import com.sakura.runtime.record.RawDatasetBase;

/**
 * File COBOL CUSTF — ASSIGN TO CUSTF-RDB. Organization: INDEXED. Layout:
 * /record-schema/initdb/CUSTF.xml
 */
public class CustfDataset extends RawDatasetBase {

    @Override
    public String getFileName() {
        return "CUSTF";
    }

    @Override
    public String getAssignTo() {
        return "CUSTF-RDB";
    }

    @Override
    protected String layoutResourcePath() {
        return "/record-schema/initdb/CUSTF.xml";
    }

    @Override
    protected boolean hasFileStatusClause() {
        return true;
    }

    @Override
    public String getRecordKey() {
        return "CU-CODE";
    }

    @Override
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.List.of("CU-NAME", "CU-REGION CU-CODE");
    }

    @Override
    public boolean isAlternateKeyDuplicates(String name) {
        if (name == null) {
            return false;
        }
        if ("CU-NAME".equals(name)) {
            return true;
        }
        return false;
    }
}
