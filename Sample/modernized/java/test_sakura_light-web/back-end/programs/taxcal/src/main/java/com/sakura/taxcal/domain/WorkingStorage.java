package com.sakura.taxcal.domain;

import com.sakura.runtime.record.SchemaLoader;
import com.sakura.runtime.record.StorageImage;

/**
 * WorkingStorage backed by a multi-buffer layout (one buffer per 01-level). Layout:
 * /record-schema/TAXCAL_WS.xml
 */
public class WorkingStorage {

    private final StorageImage buffer;

    public WorkingStorage() {
        this.buffer =
                new StorageImage(SchemaLoader.loadWorkingStorage("/record-schema/TAXCAL_WS.xml"));
    }

    public StorageImage buffer() {
        return buffer;
    }
}
