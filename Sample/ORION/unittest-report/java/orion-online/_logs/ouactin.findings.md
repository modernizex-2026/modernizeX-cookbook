# OUACTIN — Unit Test Findings

## 1. Final verify result
Tests run: 29, Failures: 0, Errors: 8, Skipped: 0 (3rd/final verify run, budget exhausted).
All 8 errors are the same pre-existing infra bug (see §4), not test defects.
C0 line coverage (JaCoCo): **67.58%** overall (271/401 lines), but the module's own
converted logic (`OuactinService`) is **88.8%** (174/196) — the average is pulled down by
the auto-generated `OuactinFields` accessor (45.3%, mostly unused typed getter/setters for
copybook fields OUACTIN's paragraphs never touch) and `OuactinBmsMetadata` (33%, unused
stub methods `getMapNames`/`getFieldMapping`/`getLayoutResource`).

## 2. CONVERT-GAP tests (intentionally COBOL-correct, expected to fail)
Both stem from the same root cause: COBOL `COMPUTE ... ROUNDED` (half-up to 2 decimals)
for `WS-UTIL-BIG` / `WS-MIN-DUE` is converted to Java arithmetic that keeps extra scale,
then the field-buffer writer (`RecordCodec.encodeField`, orion-common) always truncates
with `RoundingMode.DOWN` when persisting to the 2-decimal-scale field — never HALF_UP.
- `mainLine_highUtilFilter_CONVERT_GAP_roundedUtilCrossesThreshold`: bal=1599.90/limit=2000.00
  → ratio 79.995. COBOL rounds to 80.00 (HIGH-UTIL matches). Java truncates to 79.99 (no match).
- `mainLine_delinquentFilter_CONVERT_GAP_roundedMinDueCrossesThreshold`: bal=2500.75 →
  min-due 50.015. COBOL rounds to 50.02, so credit 50.01 < 50.02 matches DELINQUENT. Java
  truncates min-due to 50.01, so 50.01 < 50.01 is false — no match.
(Both currently also hit the §4 crash before reaching this assertion — see below.)

## 3. Uncovered remainder
- `OuactinService` 22 missed lines: the DELINQUENT/OVER-LIMIT/HIGH-UTIL match branches,
  blocked by the §4 crash (any positive `AC-CURR-BAL` throws before the filter runs).
- `OuactinFields`/`OuactinBmsMetadata` gaps are unused generated boilerplate (getters for
  copybook fields not referenced by OUACTIN's paragraphs, and metadata stubs with no
  caller in this program) — not meaningfully testable without inventing fake business need.

## 4. CRITICAL — reviewer must know (blocking infra bug, NOT a CONVERT-GAP)
`RecordBuffer.applyInitialValues` (orion-common, shared by all modules) mis-encodes the
default `VALUE` of any DISPLAY-numeric field whose COBOL literal contains a decimal point
(e.g. `WS-MIN-FLOOR PIC S9(04)V99 VALUE 25.00`): it writes the literal ASCII text "25.00"
into the field instead of encoding it as a zoned/display decimal, because its "all digits"
padding check rejects the `.` character. Any later read (`getDecimal`) throws
`NumericValueException: DISPLAY non-numeric byte 0x2E ...`.
`OuactinService.deriveAccountFigures` reads `WS-MIN-FLOOR` whenever `AC-CURR-BAL > 0` —
i.e. **every real account with a positive balance crashes**, regardless of filter. This
is a production-breaking defect, reproduced here by 8 tests (DELINQUENT ×2, OVER-LIMIT ×2,
HIGH-UTIL ×2, both CONVERT-GAP ×2), kept in the suite as COBOL-ground-truth-correct and
documented rather than papered over with unrealistic zero-balance data.
Confirmed NOT unique to OUACTIN: `ouchgf/OUCHGF_WS.xml` has the same pattern
(`WS-CO-FACTOR VALUE 1.20`) and will hit the identical crash. Fix belongs in
`orion-common` (RecordBuffer.applyInitialValues), out of this module's scope.

## Environment note
JDK is Homebrew OpenJDK 26.0.2 (no alternate JDK available); JaCoCo 0.8.12 and Mockito's
bundled Byte Buddy both predate JDK 26 support. One test (`registerFsetFields`) was
adjusted to pass `null` instead of `mock(AppRunner.class)` since Byte Buddy cannot
instrument concrete classes on this JDK; this is unrelated to OUACTIN's own code.
