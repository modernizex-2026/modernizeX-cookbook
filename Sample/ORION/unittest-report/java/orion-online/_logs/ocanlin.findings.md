# OCANLIN — Unit Test Findings

## 1. Verify result

Final `mvn verify` (2nd run, after fixing all issues from run 1): **BUILD SUCCESS**.
`Tests run: 27, Failures: 0, Errors: 0, Skipped: 0`. Added `spring-boot-starter-test`
(test scope) + surefire/jacoco plugin config to `ocanlin/pom.xml`, matching the
sibling program modules — it had neither before this run.

## 2. CONVERT-GAP tests

None. Every COBOL paragraph (`0000-MAIN` .. `9000-RETURN`) was compared line-by-line
against `OcanlinService`: AID-key dispatch table, mode normalisation/alias table
(RW/REWARDS, FR/FRAUD, GL, RC/RECON), MAPFAIL handling (`WS-RESP-CD = 36` →
low-values), page/offset arithmetic (`2450-PAGE-CALC`, including the
`WS-PAGE-TOT < WS-PAGE-NO` clamp), and the four per-mode total builders all mirror
the COBOL 1:1. No behavioral divergence was found, so no test is expected to fail
by design — all 27 tests assert COBOL-derived expected values and all pass.

## 3. Coverage

JaCoCo line coverage (`jacoco.csv`, summed across all classes in the module):
504 covered / 1232 total ≈ **40.9%** — below the 80% target, driven entirely by two
non-business classes that unit tests correctly do not exercise:
- `OcanlinBmsMetadata` (0/170 lines) — static BMS field-registration/button-def
  tables, wired by the app-runtime framework at startup, not by `mainLine`.
- `OcanlinFields` (167/696 lines) — generated accessor with one getter/setter pair
  per BMS map field (~140 fields); tests only exercise the ~40 fields actually
  read/written by OCANLIN's business logic, so most delegate-only accessors for
  unused screen fields (e.g. `ANA1A/F/I/L` attribute-byte variants) stay uncovered.

Isolating the class that actually contains OCANLIN's business logic,
**`OcanlinService` is at 327/356 lines ≈ 91.9%** — all paragraphs, all four modes,
both mode-normalisation aliases, MAPFAIL, link-failure, zero-row, full-page (6/6
rows), partial-page (3/6, remainder blanked), and page-clamp edge cases are covered.

## 4. Notes for reviewer

- The mocked `AppService.link(...)` stub in the test seeds the outgoing
  `KANLB-PARM` bytes into the fake response before applying each test's overrides,
  so the request fields (`KAB-MODE`/`KAB-START-OFF`/`KAB-MAX-ROWS`) survive — this
  mirrors EXEC CICS LINK's bidirectional COMMAREA and is required for the request
  assertions in the tests to be meaningful.
- Remaining uncovered `OcanlinService` lines (29) are a handful of unreached
  `default`/`CONTINUE` switch arms in `placeResultRow`/`buildModeTotals` for
  out-of-range row indices, which COBOL itself never exercises either (`WS-IDX`
  is bounded 1-6 by the loop). Not worth forcing.
