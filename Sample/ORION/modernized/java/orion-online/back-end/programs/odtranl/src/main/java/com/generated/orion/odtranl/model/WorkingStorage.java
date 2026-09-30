package com.generated.orion.odtranl.model;

import com.generated.orion.common.infrastructure.layout.LayoutLoader;
import com.generated.orion.common.infrastructure.layout.WorkingStorageBuffer;

/**
 * WorkingStorage backed by a multi-buffer layout (one buffer per 01-level). Layout:
 * /layout/ODTRANL_WS.xml
 */
public class WorkingStorage {

    private final WorkingStorageBuffer buffer;

    public WorkingStorage() {
        this.buffer =
                new WorkingStorageBuffer(LayoutLoader.loadWorkingStorage("/layout/ODTRANL_WS.xml"));
    }

    public WorkingStorageBuffer buffer() {
        return buffer;
    }
}
