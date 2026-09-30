# OCCUSTU — Unit Test Findings

## 1. Verify result
`mvn -q -pl back-end/programs/occustu verify` — **BUILD SUCCESS**.
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0 (1 verify run used out of 3-run budget).
Note: `pom.xml` was missing `spring-boot-starter-test`, surefire and jacoco-maven-plugin config
(present in sibling modules) — added them (matching `occustl/pom.xml`) so the module could
compile/run tests and produce coverage at all.

## 2. CONVERT-GAP tests
**None.** OCCUSTU's Java service mirrors every COBOL paragraph (0000-MAIN..9000-RETURN) 1:1,
including the two-step load/save conversation state machine and CICS response-code handling
(NORMAL=0, NOTFND=13, OTHER). No behavioral divergence found against `OCCUSTU.cbl`.

Two COBOL "self-overwrite" quirks were preserved faithfully and are covered as ground-truth
(not gaps) with explanatory comments in the test file:
- `mainLine_enterKey_stateNotLoaded_readErrorOtherRespCode_...`: on an unexpected READ resp
  code, `3000-READ-CUST` sets an interim "Error reading the customer file." message, but
  `2300-LOAD-CUST`'s NOT-FOUND branch always overwrites it with "Customer not found..." —
  reproduced identically in Java.

## 3. Uncovered remainder
Overall JaCoCo C0 = 383/634 = **60.4%** (below the 80% target), but this is driven entirely by
`OccustuFields` (the generated field accessor): only 121/368 lines covered (~33%). That class
exposes a getter/setter pair for every field in the shared COBOL copybooks (CA-ACCT-ID,
CA-CARD-NUM, CU-DOB, CU-FICO-SCORE, CU-SSN, CU-GOVT-ID, etc.) — most are never referenced by
OCCUSTU's own business logic (they belong to other programs sharing the same commarea/record
layout). Calling them directly would be meaningless assertions per the "no test vô nghĩa" rule.
Sibling module `occustl` shows the identical pattern (54.2% overall, ~24% on its own Fields
class) — this is a pre-existing, structural characteristic of the generated accessor classes,
not a gap introduced here.

`OccustuService` itself (the actual business logic): 197/198 lines (99.5%), 65/74 branches
(87.8%) covered — every paragraph and edit branch is exercised.

## 4. Reviewer notes
- pom.xml change (test deps + surefire/jacoco plugin) is required for this module to build with
  tests at all; please review/commit it alongside the new test class.
- JaCoCo prints noisy `IllegalClassFormatException` stack traces to stderr while attempting to
  instrument JDK internal classes (`sun.util.resources...`, class file version 70) — these are
  harmless instrumentation-agent warnings, not test failures (surefire report confirms 27/27 pass).
