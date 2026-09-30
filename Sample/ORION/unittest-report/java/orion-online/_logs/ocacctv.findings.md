# OCACCTV — Unit Test Findings

## 1. Verify result
Final `mvn verify` (3rd/last run): **Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.**
JaCoCo was not configured on this module; added `jacoco-maven-plugin` + `maven-surefire-plugin`
(`testFailureIgnore`) to `pom.xml`, matching sibling modules (ocaccta/ocacctu/ocacctl).

## 2. CONVERT-GAP tests
None. OCACCTV is explicitly marked in the COBOL header as the "GOLDEN reference for all OC*
online programs," and paragraph-by-paragraph comparison (0000-MAIN through 9000-RETURN)
confirms the Java service is a faithful 1:1 conversion — same EIBAID routing (PF3/PF4/ENTER),
same CA-PGM-CONTEXT / CA-FIRST-ENTER first-entry logic, same READ-RESP evaluation (NORMAL/
NOTFND/OTHER), same header/detail MOVE chains. No intentionally-failing test was needed.

One non-obvious but *correct* behavior is covered by a dedicated test
(`mainLine_enterKey_readFileUnexpectedError_finalMessageIsStillNotFoundPerCobolOverwrite`):
on an unexpected READ error, COBOL's 3000-READ-ACCT sets an interim "Error reading account
file." message but never sets REC-FOUND, so 2100-READ-AND-SHOW's ELSE branch unconditionally
overwrites it with WS-MSG-NOTFND. The Java mirrors this exactly — not a bug.

Also verified: WS-ED-BAL/WS-ED-AMT (16-byte edited numeric) truncates its rightmost byte when
MOVEd into ACBALO/ACCRLIMO/ACCSLIMO (15-byte PIC X fields per MACCTV.cpy) — e.g. balance
1234.56 displays as "1,234.5". This reproduces identically in COBOL (alphanumeric MOVE
truncates on the right) and in the Java conversion; the test computes its expected value via
the same MOVE chain rather than a literal, so it documents the behavior without asserting a
convert bug.

## 3. Uncovered remainder
Module C0 (sum across all classes) = 182/511 ≈ **35.6%**, driven entirely by generated
boilerplate, not business logic:
- `OcacctvFields` (accessor): 77/336 lines — only the ~30 field getters/setters actually used
  by the 12 tests are exercised; the other ~300 generated delegation one-liners are untouched.
- `OcacctvBmsMetadata`: 0/66 lines — `getButtonDefs`/`registerFsetFields`/`getFieldMapping`
  are static BMS registration tables, never invoked by `mainLine`.
- `OcacctvService` itself (the converted business logic): **95/99 lines ≈ 96%** — the actual
  target of this test suite is well covered.
Verify budget (3 runs) is exhausted; further coverage would require dedicated tests that call
`getButtonDefs()/registerFsetFields()/getFieldMapping()` directly (cheap, but deferred — no
business branches there) and would not move the needle much given `OcacctvFields`' size.

## 4. Reviewer notes
- JaCoCo agent throws instrumentation errors against JDK 26 system classes (major version 70,
  unsupported by jacoco-maven-plugin 0.8.12) — cosmetic stderr noise only; surefire and the
  jacoco report both completed and are accurate.
- Tests drive only the public `mainLine(AppService)` entry point with a mocked `AppService`;
  `WorkingStorage`/`OcacctvFields` are real objects (constructed fresh per call, matching
  production), never mocked.
