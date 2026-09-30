# OUCRDIN — Unit Test Findings

## 1. Verify result

Final `mvn -pl back-end/programs/oucrdin verify`: **BUILD SUCCESS**.
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0.

## 2. CONVERT-GAP tests

None. Line-by-line comparison of `OucrdinService.java` against `OUCRDIN.cbl`
(paragraphs 0000-MAIN through 9000-FINALISE, filter routing 4500/4510/4520/4530/4540,
STARTBR/READNEXT response handling in 3100/3300, and the 6000-COMPUTE-KPI population
pass) found no behavioral divergence — every branch, response-code mapping (0/13/20/other),
loop-termination condition, and filter rule (ALL/ACTIVE/INACTIVE/EXPIRING-SOON, including
the unknown-filter-defaults-to-ALL fallback) matches the COBOL ground truth exactly. This
is a clean, faithful conversion; no test was written to intentionally fail.

## 3. Uncovered remainder

Overall module C0 (JaCoCo, sum of LINE_COVERED/(LINE_MISSED+LINE_COVERED) across all
classes) is **~69%** (247/357 lines), below the 80% target — but this is driven entirely
by two auto-generated, largely out-of-scope classes:
- `OucrdinFields` (accessor): only 69/162 lines covered. It exposes typed getters/setters
  for the *entire* shared `WCONST`/`RCARD` copybook field set (e.g. WS-ACCTFILE,
  WS-BILLFILE, WS-CUSTFILE, WS-STMTFILE, CD-CVV, SQLCODE...), most of which OUCRDIN's own
  logic never touches. These are dead code from this program's perspective.
- `OucrdinBmsMetadata`: only 1/6 lines covered — OUCRDIN is a LINK-only SUB program with no
  BMS screen, so `getButtonDefs()`/`registerFsetFields()` are trivial no-op-ish passthroughs.

Restricting to the actual business logic, `OucrdinService` itself is at **93.3%** line
coverage (168/180) — every paragraph, filter branch, and error path is exercised.
`WorkingStorage`/`TaskContext` are fully covered (100%).

## 4. Reviewer notes

- `computeCutoffDate` uses `LocalDateTime.now()` directly (no injected Clock), matching
  COBOL's `FUNCTION CURRENT-DATE`. Tests avoid flakiness by computing expected expiry
  dates as offsets from `LocalDate.now()` rather than mocking the clock.
- Tests build the KCRDIN-AREA commarea via a real `OucrdinFields`/`WorkingStorage` pair
  (not `@Mock`) and pass it through `AppService.getCommarea()` as a String — the
  layout's default for unset X-fields is SPACES, not LOW-VALUES, so the "first page"
  helper explicitly calls `fillLowValues("KCI-START-KEY")` to match what a real caller
  does (per the COBOL guard `KCI-START-KEY NOT = LOW-VALUES`).
- No further verify runs were spent past the 3-run budget; the coverage gap above is a
  known, explainable shortfall rather than an unresolved failure.
