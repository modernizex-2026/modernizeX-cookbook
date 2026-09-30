# OCTRANL Unit Test Findings

## 1. Verify result
Final `mvn verify` (2nd run, after fixing missing test deps in `octranl/pom.xml`):
**Tests run: 18, Failures: 0, Errors: 0, Skipped: 0.** All pass; no CONVERT-GAP tests were needed.

## 2. CONVERT-GAP tests
None. Line-by-line comparison of `OCTRANL.cbl` against `OctranlService.java` found no behavioral
divergence — dispatch (EIBAID → PF3/PF4/ENTER/other), commarea context check, card-key blank/
low-values validation, STARTBR/READNEXT/ENDBR response handling (0/13/20/other), the 5-row cap,
the summary-message overwrite logic, and XCTL-to-menu all match the COBOL paragraphs faithfully.
One behavior worth flagging as *expected, not a bug* (also true in COBOL): when STARTBR or
READNEXT hits an unexpected error response, `2200-BUILD-SUMMARY` unconditionally re-derives
ERRMSGO from `WS-ROW-CNT` (still 0), so the final screen shows "No transactions found..." instead
of the browse-error message set moments earlier. Covered by
`mainLine_enterValidCardNumber_startBrowseUnexpectedError_summaryOverwritesBrowseErrorMessage`.

## 3. Coverage
Module-wide C0 (JaCoCo, sum of all classes) = **48.9%** (343/701 lines), below the 80% target.
This is driven entirely by `OctranlFields` (the generated field accessor): 350 of its ~434 lines
are untouched because it exposes typed getter/setter wrappers for every copybook field in the
program's working storage, and OCTRANL's business logic only touches a small subset (TR-*,
CARDNUM*, WS-CARD-KEY, ERRMSGO, header fields, etc.). The **business-logic class itself,
`OctranlService`, is at 157/159 lines = 98.7% C0** — every paragraph/branch is exercised.
For comparison, the pre-existing sibling suite `occardl` (same accessor-heavy pattern) sits at
54.5% module-wide with its main service class at ~97%, confirming this is a systemic trait of the
generated-accessor architecture, not a gap specific to this test suite. No further verify cycles
were spent chasing the accessor's dead getters/setters (out of the 3-run budget, this was the 2nd
run; the 1st failed only on a missing `spring-boot-starter-test` test dependency, now fixed).

## 4. Reviewer notes
- Had to add `spring-boot-starter-test` (test scope) + `maven-surefire-plugin`
  (`testFailureIgnore=true`) + `jacoco-maven-plugin` (0.8.12, prepare-agent + verify-phase report)
  to `back-end/programs/octranl/pom.xml` — it was missing entirely, unlike sibling program modules
  (e.g. `occardl`) that already had this block.
- JaCoCo prints harmless `IllegalClassFormatException` / "Unsupported class file major version 70"
  warnings while instrumenting the JDK's own `sun.nio.cs.ext.MS932` charset class (triggered by
  `Utility.groupToString`/commarea serialization using MS932). These are stderr noise from the
  agent skipping JDK-internal classes it can't instrument — they do not fail the build or affect
  reported coverage of project classes.
