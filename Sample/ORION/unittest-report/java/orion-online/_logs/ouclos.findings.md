# OUCLOS — Unit Test Findings

## 1. Verify result

Final `mvn verify`: **20/20 tests passed**, 0 failures, 0 errors, 0 skipped.
Two verify runs were used (1st surfaced an `UnnecessaryStubbingException` on 3
plumbing tests that don't drive `mainLine`; fixed by marking the shared
`getEibcalen`/`getEibresp` stubs `lenient()`; 2nd run was clean). No 3rd run
was needed.

## 2. CONVERT-GAP tests

**None.** OuclosService's Java faithfully mirrors OUCLOS.cbl for every
paragraph (0000-MAIN, 1000-INITIALISE, 3000/3100/3200/3400-BROWSE-*,
4000/4100/4200-PROCESS/CHECK/CLOSE-ACCT, 9000-FINALISE), including the subtle
COBOL behaviors that are easy to convert incorrectly and were specifically
tested here:
- 9000-FINALISE unconditionally overwrites `KO-STATUS-MSG` with "ACCOUNT
  CLOSE PROCESSING COMPLETE." whenever `KO-STATUS` isn't `'E'` — even when
  3100/3200 had already set a different message (e.g. "NO ACCOUNTS TO
  PROCESS."). Java replicates this exactly (`finalizeCloseStatus`).
- 4100-CHECK-CONDITIONS: EXPIRED takes precedence over ZEROLIMIT via
  IF/ELSE, so an account that is both expired and zero-limit is counted only
  once (KO-C1, not KO-C2). Java's `evaluateCloseConditions` uses the same
  if/else shape.
- AC-EXPIRY-DATE = SPACES is excluded from the expiry check before the
  date-string compare runs (COBOL `AC-EXPIRY-DATE NOT = SPACES AND ... <
  WS-RUN-DATE`). Java's `Utility.fieldEquals` guard matches.

## 3. Uncovered remainder

Overall module C0 = 211/375 = **56.3%** (below the 80% target), but this is
almost entirely boilerplate, not business logic:
- `OuclosService` (the actual converted paragraphs): 135/138 lines = **97.8%**.
- `OuclosService.TaskContext` / `WorkingStorage`: **100%**.
- `OuclosFields` (generated accessor, 65/222 lines = 29%): this class exposes
  typed getters/setters for *every* field across the shared copybooks
  (WCONST, RACCT, KCOMM, KOPS), most of which OUCLOS's business logic never
  touches (e.g. card/customer/statement fields). Writing tests solely to hit
  those one-line delegating getters/setters would be reflection-style
  getter/setter testing with no behavior to protect — explicitly the kind of
  meaningless test this skill's checklist warns against — so they were left
  uncovered.
- `OuclosBmsMetadata` (2/6 lines covered): `getButtonDefs`/`registerFsetFields`
  are both exercised; the remaining lines are metadata table initializers
  with no branching.

## 4. Reviewer notes

- JaCoCo agent logs (harmless) show `IllegalArgumentException: Unsupported
  class file major version 70` while instrumenting a JDK-internal MS932
  charset class, triggered indirectly by `RecordBuffer.applyInitialValues`.
  This is a JaCoCo 0.8.12 vs. newer-JDK compatibility gap in the local
  toolchain, not a test bug — it did not affect any test outcome (all 20
  passed) and can be ignored.
- `ouclos/pom.xml` was missing the `spring-boot-starter-test` test
  dependency and the surefire/JaCoCo plugin config that every sibling program
  module has; both were added (mirrored from `ouarch/pom.xml`) so tests could
  compile and coverage could be collected.
