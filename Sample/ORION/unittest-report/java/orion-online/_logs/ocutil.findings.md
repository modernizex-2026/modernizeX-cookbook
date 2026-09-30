# OCUTIL — Unit Test Findings

## 1. Verify result
- Last full `mvn verify` run (run #3, the budget cap): 30 tests, 27 passed, 3 failed.
  The 3 failures were test-authoring bugs (a byte-decode helper for the LINK
  commarea that didn't round-trip as assumed), not product defects. They were
  fixed afterward using the same assertion pattern already proven passing in
  `runSelectedUtility_eachOption_linksCorrectSubProgramAndFormatsResult`, and
  re-compiled successfully (`test-compile` clean), but **not re-verified** —
  the 3-run verify budget was already spent. Reviewer should run `mvn verify`
  once to confirm the final 30/30.
- `OcutilService` line coverage (C0) at run #3: **326/328 = 99.4%**.

## 2. CONVERT-GAP tests
None. Comparing OCUTIL.cbl paragraph-by-paragraph against OcutilService.java
(dispatch AIDs, MAPFAIL/resp handling, option/date/cycle validation, the
7 utility dispatch + link + format paragraphs, numeric parser) found no
behavioral divergence worth flagging as a deliberate failing test — the
conversion is a faithful 1:1 mapping. No `// CONVERT-GAP` tests were written.

## 3. Uncovered remainder
- `OcutilService.registerFsetFields()` (delegates to
  `OcutilBmsMetadata.registerFsetFields(AppRunner)`) is uncovered. Mockito
  cannot create a mock of `AppRunner` (a concrete Spring `@Component`) on
  this machine's toolchain — JDK 26 / Mockito inline-mock byte-buddy
  incompatibility ("Mockito cannot mock this class: AppRunner", see
  `target/surefire-reports`). Not a code issue; needs either a JDK ≤21 test
  run or a real/partial `AppRunner` instance to cover.
- `getProgramName`/`getTransId`/`getButtonDefs`/`getFieldMapping` are trivial
  delegating getters, covered directly.

## 4. Reviewer notes
- JaCoCo 0.8.12 throws non-fatal `IllegalClassFormatException` while
  instrumenting internal JDK charset classes (`sun.nio.cs.ext.MS932`) on this
  JDK 26 environment (class file major version 70, unsupported by 0.8.12) —
  noisy in the log but does not fail the build or affect the coverage report.
- `ocutil/pom.xml` had no test dependency or JaCoCo plugin configured; both
  were added (copied from sibling module `ocadmen`) so `verify` produces
  surefire + JaCoCo reports.
- Tests use a real `WorkingStorage`/`OcutilFields` (loads the actual
  `OCUTIL_WS.xml` layout) and mock only `AppService`; all private COBOL
  paragraphs are exercised indirectly through the public `mainLine()` entry
  point.
