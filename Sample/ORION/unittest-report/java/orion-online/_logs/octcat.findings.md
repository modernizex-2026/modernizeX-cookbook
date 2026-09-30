# OCTCAT — Findings

## 1. Verify result
Final `mvn verify` (3rd/3 of budget): **27 tests run, 2 failures, 0 errors, 0 skipped**.
Both failures are intentional CONVERT-GAP tests (see below) — not regressions.
`OctcatService` line coverage (JaCoCo): **181/183 = 98.9%**. Module-wide C0 (incl.
generated accessor/metadata classes): 328/559 ≈ 58.7% (see §3).

## 2. Intentional CONVERT-GAP failures
Both trace to the same root cause: COBOL's `9500-ABEND-RTN` issues
`EXEC CICS SEND TEXT ... EXEC CICS RETURN` **with no TRANSID** — a hard task
termination. Control never returns to the calling paragraph (`2100-LOOKUP` /
`3500-UPDATE-TCAT`) nor reaches `9000-RETURN`. The converted `abendWithFileError()`
only calls `appService.sendText(...)` and `appService.returnProgram()` — both plain
interface methods, not exceptions — so Java execution **falls through** and keeps
running: the caller still calls `8100-SEND-DATAONLY` (a second, spurious screen
send) and `0000-MAIN` still calls `RETURN TRANSID`.

- `mainLine_enterKey_readFileUnrecoverableError_convertGap_...` (3000-READ-TCAT
  WHEN OTHER path).
- `mainLine_pf5Pressed_stateExisting_readForUpdateUnrecoverableError_convertGap_...`
  (3500-UPDATE-TCAT WHEN OTHER path).

Expected (COBOL): exactly one `sendText`, no `sendMap`, no `returnTransid`.
Actual (Java): `sendText` fires, then a `sendMap` (dataonly) and `returnTransid`
also fire. Reviewer should decide whether to fix (make `returnProgram()` throw/
short-circuit) or accept as a known runtime-wide pattern (same gap likely exists
in every OC* program that calls its own 9500-ABEND-RTN equivalent).

## 3. Uncovered remainder
- `OctcatService`: 2 lines uncovered — `registerFsetFields(AppRunner)`'s
  delegating body. Not testable in this environment: Mockito's inline mock
  maker (Byte Buddy) does not yet support JDK 26 (`Java 26 (70) is not supported...
  officially supports Java 24`), so mocking the concrete `AppRunner` class throws.
  Environment limitation, not a code defect.
- `OctcatFields` (473/1671 instr., ~29% line) and `OctcatBmsMetadata` (141/159
  instr.) are auto-generated accessor/metadata classes with large unused
  getter/setter/button-def surface; only the fields actually touched by
  `OctcatService`'s business paths were exercised. Testing every unused
  accessor would be reflection-style busywork with no behavioral value.
- The COBOL `WS-NC-DIGITS > 4` branch in `6100-VALIDATE-KEY` is unreachable
  through the screen: `TCCDI`/`WS-NC-LEN` is fixed at 4 characters, so parsed
  digit count can never exceed 4. Not tested — dead branch inherited from COBOL.

## 4. Reviewer notes
- `octcat/pom.xml` was missing `spring-boot-starter-test` (test scope) and the
  surefire/jacoco plugin config that sibling modules (e.g. `ocacctv`) already
  have — added both so the module can build and produce coverage at all.
- readFile/writeFile/rewriteFile key format used in mocks is
  `"<TC-TYPE-CD>|<TC-CD>"` (e.g. `"AB|25"`), matching `OctcatService`'s actual
  `String.valueOf(...) + "|" + String.valueOf(...)` concatenation — not
  zero-padded, since `TC-CD` is stored as a Java `int`.
