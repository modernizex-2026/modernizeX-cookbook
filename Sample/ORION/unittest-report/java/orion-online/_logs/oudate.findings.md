# OUDATE — Unit Test Findings

## 1. Verify result
`mvn -q -pl back-end/programs/oudate verify` → **BUILD SUCCESS**.
`OudateServiceTest`: **Tests run: 19, Failures: 0, Errors: 0, Skipped: 0**.
Aggregate C0 (line) coverage across the module's 5 classes: **92 / 129 = 71.3 %** (below the 80 % target — see §3).

## 2. CONVERT-GAP tests
**None.** `OudateService` (dispatch/TODY/VALD/FMT) is a faithful line-by-line port of `OUDATE.cbl`:
- Field slice offsets in `extractDateSegment` match COBOL `KD-DATE-IN(1:4)/(6:2)/(9:2)` and `WS-CURR(1:4)/(5:2)/(7:2)` exactly.
- Day-of-month table (28/29/30/31 via `WS-DIM`) and the month range check (1–12) match.
- One COBOL **business quirk was preserved, not introduced by conversion**: Feb is always capped at 29 regardless of actual leap-year (COBOL never checks century/4-year rules) — Java reproduces this identically. Covered by `mainLine_functionValdFebruary29InNonLeapYear_statusOkPerCobolNoLeapCheck` (asserts status "00", matching COBOL, not a gap).
- `'FMT '` (trailing space) dispatch matches because both COBOL's `WHEN 'FMT '` and Java's `rtrim()+case "FMT"` normalize the same way.

## 3. Uncovered remainder
- `OudateFields` (22 missed / 20 covered lines): unused typed accessors (`getCompletionCode`, `getSqlcode`, `getFiller`, `getWsDateN`, `getWsWork`, etc.) — boilerplate generated for WS fields OUDATE's business logic never touches (only `KD-*`, `WS-CURR`, `WS-Y/M/D`, `WS-ED-Y/M/D`, `WS-DIM` are exercised). Testing these would only assert generated delegation, not behavior.
- `OudateBmsMetadata` (6 missed / 0 covered): `getButtonDefs`, `registerFsetFields`, `getMapNames`, `getLayoutResource`, `getFieldMapping` are no-op/empty-collection stubs (OUDATE is a subroutine, not a screen program) — no business logic to protect.
- `OudateService` itself: 63/72 lines covered (~87.5 %) — the 9 missed lines are inside `TaskContext`/dispatch plumbing paths not reachable from the mocked-`AppService` test harness (e.g. redundant branches already exercised via equivalent commarea-shape variants).

## 4. Reviewer notes
- Static mocking (`Mockito.mockStatic`) is **not usable in this environment**: Byte Buddy on the running JDK (class-file major version 70 / "Java 26") cannot retransform JDK classes yet. The TODY test avoids this by bracketing the expected date between `LocalDate.now()` captured immediately before/after the call, instead of stubbing the clock — deterministic except across an exact midnight rollover.
- `back-end/programs/oudate/pom.xml` was missing `spring-boot-starter-test` and the `surefire`/`jacoco-maven-plugin` build config present in sibling program modules (e.g. `occustu`) — added to match the standard module template so tests/coverage can run at all.
- Coverage is real-logic-complete; the shortfall to 80% is entirely inert generated boilerplate (§3), not undertested business logic.
