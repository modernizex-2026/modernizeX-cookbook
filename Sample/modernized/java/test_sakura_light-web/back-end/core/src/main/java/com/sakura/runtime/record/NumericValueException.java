package com.sakura.runtime.record;

/**
 * Raised when the bytes backing a numeric field cannot be decoded.
 *
 * <p>On the mainframe such data ends the run (SOC7 abend, file status "09"); this runtime keeps
 * that contract and fails fast instead of substituting zero. A caller that needs graceful
 * degradation may catch the exception and set a file status explicitly.
 *
 * <p>Triggered by:
 *
 * <ul>
 *   <li>DISPLAY — a digit byte outside space / NUL / '0'-'9' / a valid overpunch sign
 *   <li>COMP-3 — a digit nibble above 9, or a trailing sign nibble below 0xA
 * </ul>
 */
public class NumericValueException extends RuntimeException {

    private final String fieldName;
    private final int byteOffset;

    public NumericValueException(String message, String fieldName, int byteOffset) {
        super(message + " (field=" + fieldName + " offset=" + byteOffset + ")");
        this.fieldName = fieldName;
        this.byteOffset = byteOffset;
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getByteOffset() {
        return byteOffset;
    }
}
