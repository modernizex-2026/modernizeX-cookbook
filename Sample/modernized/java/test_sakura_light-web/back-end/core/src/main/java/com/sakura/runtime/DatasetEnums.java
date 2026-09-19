package com.sakura.runtime;

/** COBOL file organization types. */
public class DatasetEnums {

    public enum FileOrganization {
        SEQUENTIAL,
        LINE_SEQUENTIAL,
        INDEXED,
        RELATIVE
    }

    public enum FileAccessMode {
        SEQUENTIAL,
        RANDOM,
        DYNAMIC
    }

    public enum FileOpenMode {
        INPUT,
        OUTPUT,
        IO,
        EXTEND
    }

    public enum SeekCondition {
        EQUAL,
        GREATER_THAN,
        GREATER_OR_EQUAL,
        LESS_THAN,
        LESS_OR_EQUAL,
        NOT_EQUAL
    }
}
