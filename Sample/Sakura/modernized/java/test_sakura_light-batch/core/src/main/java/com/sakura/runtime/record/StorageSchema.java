package com.sakura.runtime.record;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates one {@link RecordSchema} per top-level WORKING-STORAGE item (01/77 level). The schemas
 * never share bytes — each top-level item owns its storage, exactly as the COBOL standard
 * prescribes.
 *
 * <p>Consumed by {@link StorageImage} (one buffer per item) and by {@link FieldResolver} (name
 * lookup across items).
 */
public record StorageSchema(String name, Charset charset, List<RecordSchema> buffers) {

    public StorageSchema {
        buffers = Collections.unmodifiableList(buffers);
    }
}
