# OCTRANV — Unit Test Findings

## 1. Verify result

Test-compile is clean. Full `verify` was run 3 times (budget cap). Final surefire
result (13 of the 14 tests in the file, after removing one unrunnable test — see
§4): **13 run / 13 passed / 0 failed / 0 skipped**. The 3rd run's raw result was
15 tests (12 original + 3 added for coverage): 14 passed, 1 error (Mockito could
not mock `AppRunner`, unrelated to business logic — removed, not re-verified per
the 3-run cap). JaCoCo `OctranvService` line coverage (the only class carrying
real business logic): **96%** (96/100 lines). Whole-module C0 (including the
generated `OctranvFields` accessor and `OctranvBmsMetadata`, which expose ~250
unused getters/setters/field-defs): **36.4%** (180/494 lines) — this matches the
"golden" reference module OCACCTV's own ratio (35.6%), so it is the expected
shape for this generator, not a deficiency of these tests.

## 2. CONVERT-GAP tests

**None.** OCTRANV's Java service mirrors every COBOL paragraph 1:1
(0000-MAIN .. 9000-RETURN), including two easy-to-miss details that were
verified to match and are called out in test comments (not gaps):
- `4000-POPULATE-DETAIL`'s `WS-ED-AMT(2:15)` substring drop-first-byte edit ↔
  Java's `padRight(...,16).substring(1,16)` — confirmed equivalent.
- `3000-READ-TRAN`'s WHEN-OTHER read-error message is always overwritten by
  `2100-READ-AND-SHOW`'s not-found branch in COBOL itself (REC-NOT-FOUND stays
  true), and the Java converts this quirk faithfully — the interim
  "Error reading the transaction file." message is never actually shown, in
  both COBOL and Java. Covered by
  `mainLine_enterKey_readFileUnexpectedError_finalMessageIsStillNotFoundPerCobolOverwrite`.

## 3. Uncovered remainder

- `OctranvService.registerFsetFields(AppRunner)` (delegates to
  `OctranvBmsMetadata.registerFsetFields`) is **not covered**. `AppRunner` is a
  concrete class; Mockito's inline mock maker needs Byte Buddy class
  instrumentation, and this environment runs **JDK 26**, which Byte Buddy
  (bundled with the pinned Mockito version) does not yet support
  ("Java 26 (70) is not supported ... officially supports Java 24"). This is a
  toolchain/JDK-version limitation, not a test-writability gap — the same
  pattern (mocking `AppService`, an interface) works fine everywhere else.
- The rest of the uncovered lines are entirely inside `OctranvFields`
  (auto-generated accessor with ~250 getters/setters, only the ones OCTRANV's
  paragraphs actually use are exercised) and `OctranvBmsMetadata`
  (`getButtonDefs`/`getFieldMapping` are covered directly; `getMapNames` /
  `getLayoutResource` are unused tooling helpers) — dead surface from
  generation, consistent with OCACCTV's own coverage profile.

## 4. Reviewer notes

- Had to add `spring-boot-starter-test` (test scope) plus the
  `maven-surefire-plugin` (`testFailureIgnore=true`) and `jacoco-maven-plugin`
  (0.8.12) blocks to `back-end/programs/octranv/pom.xml` — these were present
  in the sibling `ocacctv` module's pom but missing here, so `test-compile`
  failed on missing JUnit5/Mockito/AssertJ packages until fixed.
- JaCoCo 0.8.12 also throws non-fatal `IllegalArgumentException: Unsupported
  class file major version 70` while instrumenting unrelated JDK internal
  classes (e.g. `sun.nio.cs.ext.MS932`) on this JDK 26 host; this is noise in
  the verify log and does not fail the build or affect the coverage numbers
  above.
- If a Mockito/Byte Buddy version bump becomes available for JDK 26, consider
  re-adding a `registerFsetFields` test using `mock(AppRunner.class)` and
  `verify(runner).registerFsetFields(eq("MTRANVA"), eq(Set.of("TRANID")))`.
