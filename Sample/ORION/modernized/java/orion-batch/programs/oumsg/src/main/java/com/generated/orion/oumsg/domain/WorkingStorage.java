package com.generated.orion.oumsg.domain;

import com.generated.orion.runtime.record.SchemaLoader;
import com.generated.orion.runtime.record.StorageImage;

/**
 * WorkingStorage backed by a multi-buffer layout (one buffer per 01-level). Layout:
 * /record-schema/OUMSG_WS.xml
 */
public class WorkingStorage {

    private final StorageImage buffer;

    public WorkingStorage() {
        this.buffer =
                new StorageImage(SchemaLoader.loadWorkingStorage("/record-schema/OUMSG_WS.xml"));
    }

    public StorageImage buffer() {
        return buffer;
    }
}
