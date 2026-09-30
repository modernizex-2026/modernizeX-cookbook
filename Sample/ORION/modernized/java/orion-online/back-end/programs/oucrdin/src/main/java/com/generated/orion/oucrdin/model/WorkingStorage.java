package com.generated.orion.oucrdin.model;

import com.generated.orion.common.infrastructure.layout.LayoutLoader;
import com.generated.orion.common.infrastructure.layout.WorkingStorageBuffer;

/**
 * WorkingStorage backed by a multi-buffer layout (one buffer per 01-level). Layout:
 * /layout/OUCRDIN_WS.xml
 */
public class WorkingStorage {

    private final WorkingStorageBuffer buffer;

    public WorkingStorage() {
        this.buffer =
                new WorkingStorageBuffer(LayoutLoader.loadWorkingStorage("/layout/OUCRDIN_WS.xml"));
    }

    public WorkingStorageBuffer buffer() {
        return buffer;
    }
}
