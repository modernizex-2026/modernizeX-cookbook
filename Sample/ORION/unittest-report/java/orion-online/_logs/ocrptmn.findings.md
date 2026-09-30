# OCRPTMN — Unit Test Findings

## 1. Verify result
`mvn -pl back-end/programs/ocrptmn verify` → **BUILD SUCCESS**.
Tests run: **21, Failures: 0, Errors: 0, Skipped: 0**.
C0 line coverage (module total, JaCoCo): **89.5%** (196/219 lines). `OcrptmnService` itself (the
only class carrying converted business logic) is **100%** line-covered (109/109), including its
nested `TaskContext` (6/6).

## 2. CONVERT-GAP tests
None. OCRPTMN's Java service (`OcrptmnService`) mirrors every COBOL paragraph 1:1 against
`OCRPTMN.cbl`: `0000-MAIN` (EIBCALEN=0 vs CA-PGM-CONTEXT re-entry), `1000-SEND-INITIAL`,
`2000-PROCESS-INPUT` (DFHPF3 / DFHENTER / WHEN-OTHER dispatch), `2100-DISPATCH` /
`2200-SELECT-TARGET` (OPTIONI NUMERIC 1-6 → OCACCTL/OCCARDL/OCCUSTL/OCTRANL/OCSTMIN/OCANLIN),
`7000-XCTL-MAIN`, `7100-XCTL-TARGET`, `8000/8100/8500` header+send-dataonly+date-time, and
`9000-RETURN`. DFHRESP(MAPFAIL) is correctly mapped to `36` (`AppResp.MAPFAIL`). No behavioral
divergence was found, so no test is expected to fail by design.

## 3. Uncovered remainder
The 23 uncovered lines are entirely in `OcrptmnFields` (52/244 lines missed) and
`OcrptmnBmsMetadata` (4 lines) — generated boilerplate accessor/metadata classes shared across
the whole commarea layout (e.g. `getCaAcctId`, `getCaCardNum`, `getCaCustId`, `getCaTranId`,
`getWsAmt`, edit-field getters like `WS-ED-AMT`/`WS-ED-BAL`/`WS-ED-ID`). OCRPTMN's business logic
never touches these fields (they belong to other inquiry programs sharing the same
`ORION-COMMAREA`/copybook layout), so they are legitimately untested dead surface for this
module — not a gap in the OCRPTMN test suite itself.

## 4. Notes for reviewer
- `ocrptmn/pom.xml` was missing the `spring-boot-starter-test` dependency and the
  surefire/jacoco `<build>` plugin block present in the sibling `ocmenu` module's pom — added
  both (copied verbatim from `ocmenu/pom.xml`) so the module could compile/run tests and produce
  JaCoCo output at all.
- Test file mirrors the established `OcmenuServiceTest` convention in this repo: `AppService` is
  mocked; `OcrptmnFields`/`WorkingStorage` are used as real (non-mocked) objects, since business
  state is asserted by inspecting the same mutable field-accessor instance captured from
  `sendMap`/`receiveMap`/`xctl` mock invocations.
- One test (`registerFsetFields_...`) required constructing a real `AppRunner` instead of a
  Mockito mock: Mockito's inline mock maker (bytebuddy) cannot instrument classes on this
  environment's JDK 26 (`Unsupported class file major version 70`), so `AppRunner` — a concrete
  class, not an interface — could not be mocked. Worked around by instantiating a real
  `AppRunner(Map.of(), Map.of())` and asserting via its real `getFsetFields(...)` getter.
- A transient JaCoCo instrumentation error against the JDK's own `sun.nio.cs.ext.MS932` charset
  class (same JDK 26 vs. JaCoCo 0.8.12 class-file-version mismatch) appeared once and did not
  recur on the next verify; it did not fail the build. If it resurfaces consistently, upgrading
  the `jacoco-maven-plugin` version would be the fix (out of scope for this test-only change).
