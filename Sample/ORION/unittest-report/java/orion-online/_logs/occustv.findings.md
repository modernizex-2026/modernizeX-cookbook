# OCCUSTV — Unit Test Findings

## 1. Verify result
Single `mvn verify` run (1 of 3 budgeted). **All 15 tests passed** (0 failures, 0 errors,
0 skipped) — `OccustvServiceTest`. No compile issues; batch `test-compile` was clean
before this run.

## 2. CONVERT-GAP tests
**None.** Line-by-line comparison of OCCUSTV.cbl (0000-MAIN through 9000-RETURN)
against `OccustvService.java` found no behavioral divergence — every COBOL paragraph
maps 1:1 to a Java method with identical branching (EIBCALEN/CA-PGM-CONTEXT dispatch,
PF3/PF4/ENTER/other EVALUATE, ID edit, numeric validation, CUSTFILE read RESP handling,
name formatting via first-token split, header population). This mirrors the sibling
`OcacctvServiceTest`, the documented "golden skeleton" this program follows, which
also found zero convert gaps.

One near-gap worth flagging for the reviewer (not a defect, same as OCACCTV): in
`3000-READ-CUST` / `readCustomerRecord`, an unexpected READ RESP (not NORMAL/NOTFND)
sets an interim "Error reading the customer file." message, but the caller
(`2100-READ-AND-SHOW` / `readAndShowCustomer`) unconditionally overwrites it with
"Customer not found..." on any non-found outcome. The interim message is unreachable
in both COBOL and Java — covered by test
`mainLine_enterKey_readFileUnexpectedError_finalMessageIsStillNotFoundPerCobolOverwrite`.

## 3. Uncovered remainder
Module aggregate JaCoCo C0 = 221/545 = **40.6%**, but this is dominated by generated
boilerplate outside the tested business logic:
- `OccustvBmsMetadata` (58 lines, 0% — BMS field/button metadata registration, not
  exercised by mainLine-driven tests; same 0% pattern in sibling module OCACCTV).
- `OccustvFields` (261/350 lines missed — hundreds of generated typed getter/setter
  wrappers; only the ~90 fields actually touched by OCCUSTV's logic are exercised).

**`OccustvService` itself (the actual converted business logic) is 122/127 lines =
96.1% C0** — on par with OCACCTV's service class (96.0%). The 5 missed lines are
trivial delegators (`getButtonDefs`, `registerFsetFields`, `getFieldMapping`) not
called by `mainLine`.

## 4. Reviewer notes
- `occustv/pom.xml` was updated to add `spring-boot-starter-test` + surefire/jacoco
  plugin config (copied verbatim from `ocacctv/pom.xml`) — it was missing entirely,
  which would have blocked any test from compiling/running.
- JaCoCo instrumentation prints many `IllegalClassFormatException` warnings against
  JDK-internal classes (locale providers) due to a JaCoCo 0.8.12 / current JDK class
  file version mismatch. These are noise, not test failures — the jacoco.csv/xml
  report is still produced correctly.
