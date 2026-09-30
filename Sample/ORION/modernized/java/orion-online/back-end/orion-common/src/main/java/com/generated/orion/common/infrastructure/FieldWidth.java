package com.generated.orion.common.infrastructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * COBOL byte width of a generated Java field. Set by JavaLinkParamGenerator + JavaModelGenerator on
 * elementary linkage / model fields. Read by {@code Utility.parseIntoGroup} to slice a fixed-layout
 * source string (e.g. a serialized RecordBuffer) into typed Java fields.
 *
 * <p>Example: a field annotated {@code @FieldWidth(6)} backed by COBOL {@code PIC 9(6)} →
 * parseIntoGroup consumes 6 chars and parses as int.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldWidth {
    int value();
}
