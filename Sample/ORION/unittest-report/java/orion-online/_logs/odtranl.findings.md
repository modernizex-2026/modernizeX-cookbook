# ODTRANL unit test findings

## 1. Final verify result
Tests run: 18, Passed: 17, Failed: 1 (intentional CONVERT-GAP), Errors: 0, Skipped: 0.
`OdtranlService` line coverage (JaCoCo): **127/128 = 99.2%** (the class holding all converted
business logic). Module-wide C0 across all 6 classes in the JaCoCo report is **45%**
(310/689 lines) — see "Uncovered remainder" below for why this number is misleading.

## 2. CONVERT-GAP test (fails on purpose)
`mainLine_enterKey_cursorOpenReportsSqlError_convertGapJavaIgnoresOpenFailure`

- **COBOL (3000-LIST-TRANS)**: `EXEC SQL OPEN TRANCSR`, then `IF SQLCODE NOT = 0` →
  set `ERRMSGO = 'Error opening transaction cursor.'` and skip the FETCH loop/CLOSE entirely.
- **Java (`fetchTransactionList`)**: calls `ctx.dao.openTrancsr(...)` and **discards its
  return value**, then unconditionally executes `ctx.f.setSqlcode(0)` right after — so
  `if (ctx.f.getSqlcode() != 0)` can never be true. The "Error opening transaction cursor."
  branch is structurally dead code; a failing OPEN is silently treated as success and the
  code proceeds straight into the FETCH loop.
- Test stubs `dao.openTrancsr(...)` to return `SQLCODE=8` and asserts the COBOL-expected
  outcome (error message shown, `fetchTrancsr`/`closeTrancsr` never called). It fails
  against current Java, which instead calls fetch/close as usual (observed actual: still
  reports "Transactions listed." because the unstubbed default `fetchTrancsr()` mock
  answer, an empty map with no `SQLCODE` key, is treated as a successful row on every
  call — reinforcing that any open failure is invisible downstream).

## 3. Uncovered remainder + why
- `OdtranlService`: 1 line missed — the `default`/`CONTINUE` arm of `populateTransactionRow`'s
  switch on `WS-IDX` (COBOL 3200-MOVE-ROW `WHEN OTHER CONTINUE`). It is unreachable under
  normal execution because the enclosing loop's own guard (`WS-IDX FROM 1 ... UNTIL WS-IDX > 5`)
  never lets `populateTransactionRow` run with idx outside 1..5 — dead code by construction,
  not a gap in test coverage.
- `OdtranlFields` (accessor, 340/418 lines missed): auto-generated typed getter/setter
  wrappers for every WORKING-STORAGE/BMS field in the copybooks; only the ~20 fields ODTRANL
  actually touches are exercised. Testing the rest would mean asserting generated
  boilerplate, not ODTRANL behavior — out of scope per the "no meaningless tests" rule.
- `OdtranlDaoImpl` (0/35 lines covered): real `JdbcTemplate`/`SqlPaging` implementation of
  the DB2 cursor. It is mocked (`OdtranlDao`) in all service tests per skill rules ("mock
  all DB dependencies, no real DB hits"); exercising it needs a JDBC/Testcontainers
  integration test, out of scope for this unit-test pass.
- These two classes account for nearly all of the 379 missed lines that pull module-wide C0
  down to 45%; the actual converted business logic (`OdtranlService`) is essentially fully
  covered (99.2%).

## 4. Reviewer notes
- Added `spring-boot-starter-test` (test scope) + `jacoco-maven-plugin` 0.8.12 to
  `back-end/programs/odtranl/pom.xml` — neither was present before.
- JaCoCo 0.8.12 logs (non-fatal) `IllegalClassFormatException` while instrumenting
  `sun.nio.cs.ext.MS932` (JDK-internal Shift-JIS charset class, triggered by
  `RecordBuffer.applyInitialValues`); tests and the coverage report still complete.
- Budget used: 3/3 `mvn verify` runs. 1st surfaced a test-authoring bug (a 3-char type code
  overflowing the 2-char `TYPx` field). 2nd (after that fix) surfaced a brittle exact-string
  amount assertion, replaced with a `BigDecimal` numeric comparison. 3rd was the final
  confirming run, leaving only the intentional CONVERT-GAP failure.
