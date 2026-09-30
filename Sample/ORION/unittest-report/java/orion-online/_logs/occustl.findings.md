# OCCUSTL Unit Test Findings

## 1. Final result

`mvn verify` (JUnit5 + Mockito, `-pl back-end/programs/occustl -am`): **BUILD SUCCESS**.
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0.
All tests live in `OccustlServiceTest` (package `com.generated.orion.occustl.service`).

## 2. CONVERT-GAP tests

**None.** OCCUSTL's Java service mirrors every COBOL paragraph (0000-MAIN through
9000-RETURN) 1:1, including the CICS response-code constants it depends on
(NORMAL=0, NOTFND=13, ENDFILE=20, MAPFAIL=36 — cross-checked against `AppResp`),
the PIC Z9 zero-suppressed row-count edit, the STARTBR/READNEXT/ENDBR paging loop,
and the numeric start-key parser (paragraph 6000-PARSE-NUM). No behavioral
divergence from the COBOL ground truth was found, so no test is expected to fail.

Two COBOL "overwrite" quirks were deliberately locked in as tests (not gaps):
an interim STARTBR/READNEXT error message is always overwritten by
2300-BUILD-SUMMARY's NONE-FOUND/count message once WS-ROW-CNT is evaluated
(`mainLine_enterKey_browseStartError_...`, `mainLine_enterKey_readNextError_...`);
2200-PAGE-FWD instead shows its own END-FILE message on zero rows, never
NONE-FOUND — this distinction is asserted explicitly.

## 3. Uncovered remainder

Module C0 (line) coverage = **398/735 = 54.2%** — below the 80% target, entirely
because of `OccustlFields`, the generated field accessor (24% covered, 333/438
lines missed). It exposes typed getter/setter wrappers for every field in the
shared copybooks (CU-ADDR-*, CU-SSN, CU-GOVT-ID, WS-ACCTFILE/BILLFILE/CARDFILE/...,
SQLCODE, etc.) — most are unused by OCCUSTL's own paragraphs and were never
expected to be exercised by this program's tests.
Excluding that generated class, the actual business-logic classes score:
`OccustlService` 200/201 lines (99.5%), `OccustlBmsMetadata` 83/86 (96.5%),
`WorkingStorage`/`TaskContext` 100%.

## 4. Reviewer notes

- Two `mvn verify` runs hit `NoSuchMethodError: Utility.toCobolInt(long,int)` and a
  stale `getFieldMapping` assertion — both were tooling/test-authoring mistakes
  (stale `orion-common` jar from a verify run without `-am`; wrong expected value),
  not defects in `OccustlService`. Fixed; final confirming run is clean.
- No `pom.xml` had JaCoCo/Mockito configured before this run; added the standard
  `spring-boot-starter-test` + `jacoco-maven-plugin` block (copied from sibling
  module `ocacctv`) to `back-end/programs/occustl/pom.xml`.
