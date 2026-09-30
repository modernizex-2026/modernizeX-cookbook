# OCUSRA — Unit Test Findings

## 1. Final result
20/20 tests pass (Failures: 0, Errors: 0, Skipped: 0). No convert-gap tests were needed —
`OcusraService` matches COBOL `OCUSRA.cbl` paragraph-by-paragraph (0000-MAIN, 1000-SEND-INITIAL,
2000-PROCESS-INPUT, 2100/2110/2120-ADD/RECEIVE/VALIDATE, 3000-WRITE-USER, 4000/4100-REPOPULATE/
CONFIRM, 7000-XCTL-ADMEN, 8000/8100/8500-HEADER/DATAONLY/DATETIME) with no logic divergence found.
CA-FIRST-ENTER (`CA-PGM-CONTEXT = 0`), the required-field short-circuit order in 2120-VALIDATE-INPUT,
the DUPREC(14)/OTHER write-error branching in 3000-WRITE-USER, and the EIBAID dispatch (PF3/PF4/ENTER/
OTHER) all convert 1:1 into Java.

## 2. CONVERT-GAP tests
None. No intentionally-failing test was written — the review found no behavioral divergence
between the COBOL source and the converted service worth flagging.

## 3. Uncovered remainder and why
- Overall C0 (JaCoCo, summed across all classes in the module) = **51.4%** (245/477 lines),
  driven almost entirely by `OcusraFields` (accessor class): only 65/290 lines covered (22%).
  This class exposes ~150 typed getter/setter pairs generated for the *entire* shared
  ORION-COMMAREA/BMS layout (fields like `CA-ACCT-ID`, `CA-CARD-NUM`, `US-*`, `WS-*`...), but
  OCUSRA's business logic only touches ~20 of them. Writing tests to exercise the remaining
  getters/setters would be reflection-style "test vô nghĩa" per the skill's own guidance
  (asserting a setter round-trips a value proves nothing about OCUSRA behavior) and was
  deliberately skipped.
- **`OcusraService` itself is 121/123 lines = 98.4% covered.** The only 2 uncovered lines are
  the body of `registerFsetFields(AppRunner)` (delegates to `OcusraBmsMetadata.registerFsetFields`).
  A test for it was written but had to be removed — Mockito's inline mock maker cannot mock
  `AppRunner` on this machine's JDK 26 (Homebrew OpenJDK 26.0.2): `MockitoException: Mockito
  cannot mock this class`, caused by the same JDK-26/byte-buddy class-file-version-70
  incompatibility that also makes JaCoCo emit (harmless, non-fatal) instrumentation warnings for
  several `java.base`/`sun.nio.cs` classes during the run. This is an environment/tooling gap,
  not a code defect — on a JDK ≤21 environment this method would be trivially coverable with the
  removed test (mock `AppRunner`, call `service.registerFsetFields(runner)`, verify the delegate).
- `OcusraBmsMetadata`: 49/54 lines (91%) — `getMapNames()`/`getLayoutResource()` (tooling helpers,
  not exercised by `mainLine`) remain uncovered; out of scope for a service-level test suite.

## 4. Reviewer notes
- Added `spring-boot-starter-test` (test scope) + `maven-surefire-plugin`
  (`testFailureIgnore=true`) + `jacoco-maven-plugin` 0.8.12 to `back-end/programs/ocusra/pom.xml`
  — these were present in sibling modules (e.g. `occarda`) but missing here, so the module could
  not compile or run tests at all before this change.
- If C0 ≥ 80% is required for this module specifically, the only path is testing `OcusraFields`
  getters/setters directly (bypassing `OcusraService`), which is boilerplate and low-value; not
  recommended unless a project-wide coverage gate demands it.
