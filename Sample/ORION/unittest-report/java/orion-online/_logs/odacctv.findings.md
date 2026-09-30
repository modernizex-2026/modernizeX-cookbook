# ODACCTV — Unit Test Findings

## 1. Final result

`mvn -pl back-end/programs/odacctv verify` — **16/16 tests passed**, 0 failures, 0 errors.
`OdacctvService` line coverage: **98.2%** (110/112 lines). Module-wide C0 (incl. generated
accessor/DAO-impl infra): **~47.7%** — see §3.

## 2. Convert-gap tests

None found and none intentionally failing. `OdacctvService` is a faithful, line-for-line
conversion of `ODACCTV.cbl`. One behavior that looks like a bug but is **not** a gap is
covered explicitly: `3000-READ-ACCT` sets `ERRMSGO` to `'Error reading account table.'` on
`SQLCODE` other than 0/100, but `2100-READ-AND-SHOW` unconditionally overwrites `ERRMSGO`
with `WS-MSG-NOTFND` whenever `REC-FOUND` is false — this happens identically in the COBOL
source, so the Java correctly reproduces it. Test
`mainLine_enterPressed_sqlError_stillShowsNotFoundMessage` asserts the COBOL-faithful
"Record not found." outcome (not the DB2 error text) and documents why in its comment.

## 3. Uncovered remainder

- `OdacctvFields` (262/332 lines uncovered): a mechanically generated accessor exposing
  ~150 typed getters/setters for every COBOL field in the copybooks pulled into ODACCTV's
  working storage, but the program only touches a couple dozen of them. Testing the unused
  delegate getters/setters would be pure reflection-style boilerplate (getter/setter tests
  are explicitly the kind of low-value test this skill's rubric asks to avoid).
- `OdacctvDaoImpl` (6/6 lines uncovered): real JDBC implementation of `OdacctvDao`; the
  service layer only depends on the `OdacctvDao` interface (mocked in all tests), so the
  impl needs a DB-backed integration test, out of scope for pure Mockito unit tests.
- `OdacctvBmsMetadata` (5/66 lines uncovered): static BMS metadata table; exercised
  indirectly via `getButtonDefs`/`getFieldMapping` tests, a couple of unreached branches for
  map/field names not used by this program's screens.
- `OdacctvService`: the 2 remaining missed lines are inside `registerFsetFields`, a
  one-line delegate to `OdacctvBmsMetadata.registerFsetFields(runner)`. A test was attempted
  using `mock(AppRunner.class)`, but Mockito/ByteBuddy in this environment (JDK 26) cannot
  instrument the concrete `AppRunner` class ("Java 26 (70) is not supported by ... Byte
  Buddy") — an environment/toolchain limitation, not a code defect. Removed rather than left
  failing.

## 4. Reviewer notes

- pom.xml for this module was missing `spring-boot-starter-test` (test scope) and the
  surefire/jacoco `<build>` plugin block present in sibling modules (e.g. `ocacctu`) — added
  both so tests and coverage can run at all.
- Verify budget used: 3/3 runs (1st: compile/stub-strictness/test-data fixes needed; 2nd:
  16/16 green, confirmed; 3rd: added one more test which failed for the environment reason
  above, then reverted without a 4th run).
