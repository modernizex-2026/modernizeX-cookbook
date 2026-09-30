package com.appruntime;

import java.util.List;

/**
 * Interface for file I/O operations, abstracting CICS file commands to DAO pattern. Each generated
 * CICS file (VSAM dataset) gets an implementation of this interface.
 */
public interface FileDao {

    /** Returns the logical file name this DAO manages (matches CICS FILE() operand). */
    String getFileName();

    /**
     * Read a record by primary key.
     *
     * @param key the record key
     * @return the record object, or null if not found
     */
    Object readByKey(Object key);

    /**
     * Read a record by primary key with intent to update.
     *
     * @param key the record key
     * @return the record object, or null if not found
     */
    Object readByKeyForUpdate(Object key);

    /**
     * Write a new record.
     *
     * @param record the record to write
     * @param key the record key
     */
    void write(Object record, Object key);

    /**
     * Rewrite (update) the last record read for update.
     *
     * @param record the updated record
     */
    void rewrite(Object record);

    /**
     * Delete a record by key.
     *
     * @param key the record key
     */
    void delete(Object key);

    /**
     * Start a browse (sequential read) from the given key position.
     *
     * @param key the starting key
     * @return list of records from the browse
     */
    List<Object> startBrowse(Object key);

    /**
     * Browse records backward: key <= ridfld, ordered DESC. Used by READPREV to navigate backward
     * from STARTBR position. Default implementation reverses startBrowse results.
     */
    default List<Object> startBrowsePrev(Object key) {
        // CICS READPREV from STARTBR at KEY reads: KEY, KEY-1, KEY-2, ...
        // Load all records ASC, find the key position, take records [0..keyPos] reversed.
        // Generated DAOs should override with proper SQL: WHERE key <= ? ORDER BY key DESC.
        List<Object> all = startBrowse("");
        if (all.isEmpty()) {
            return all;
        }
        // Find the position of the first record >= key (matching STARTBR semantics)
        String keyStr = key != null ? key.toString().trim() : "";
        int keyPos = all.size() - 1; // default: last record
        if (!keyStr.isEmpty()) {
            for (int i = 0; i < all.size(); i++) {
                // Use startBrowse(key) to find the matching position
                // All records at index <= i have keys <= all[i]
            }
            // Simpler: startBrowse(key) returns records >= key, the first one IS the key match
            // So we need: all records from beginning up to and including the key match
            List<Object> forward = startBrowse(key);
            if (!forward.isEmpty()) {
                // forward[0] = key match. Find its index in 'all' list.
                // Records <= key = all records up to and including forward[0]
                // Since we can't compare generically, use index math:
                // all has N records, forward has M records (M <= N), forward starts at index N-M
                keyPos = all.size() - forward.size();
            }
        }
        // Take records [0..keyPos] and reverse → DESC from key to beginning
        List<Object> result = new java.util.ArrayList<>(all.subList(0, keyPos + 1));
        java.util.Collections.reverse(result);
        return result;
    }
}
