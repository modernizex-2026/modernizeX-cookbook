package com.sakura.oe0010.domain;

import com.sakura.runtime.record.SchemaLoader;
import com.sakura.runtime.record.StorageImage;

/**
 * WorkingStorage backed by a multi-buffer layout (one buffer per 01-level). Layout:
 * /record-schema/OE0010_WS.xml
 */
public class WorkingStorage {

    private final StorageImage buffer;

    public WorkingStorage() {
        this.buffer =
                new StorageImage(SchemaLoader.loadWorkingStorage("/record-schema/OE0010_WS.xml"));
    }

    public StorageImage buffer() {
        return buffer;
    }
}
