package com.sakura.rp0050.domain;

import com.sakura.runtime.record.SchemaLoader;
import com.sakura.runtime.record.StorageImage;

/**
 * WorkingStorage backed by a multi-buffer layout (one buffer per 01-level). Layout:
 * /record-schema/RP0050_WS.xml
 */
public class WorkingStorage {

    private final StorageImage buffer;

    public WorkingStorage() {
        this.buffer =
                new StorageImage(SchemaLoader.loadWorkingStorage("/record-schema/RP0050_WS.xml"));
    }

    public StorageImage buffer() {
        return buffer;
    }
}
