# OCTTYP — Unit Test Findings

## 1. Final verify result
`mvn -pl back-end/programs/octtyp verify` — **Tests run: 28, Failures: 2 (intentional), Errors: 0, Skipped: 0.**
Both failures are deliberate CONVERT-GAP tests (see §2) that assert COBOL ground-truth behavior against the
current Java, which diverges — they are meant to fail until the gap is fixed, not test bugs.
`OcttypService` itself reaches **100% line coverage** (146/146). Module-wide C0 is 54.1% — see §3.

## 2. CONVERT-GAP tests (intentionally failing)

- **`mainLine_lookup_fileReadUnrecoverableError_convertGap_sendTextShouldCarryMessageText`**
  COBOL `9500-ABEND-RTN` does `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT)` — sends the literal error message.
  Java `abendUnrecoverableFileError()` calls `ctx.appService.sendText(String.valueOf(ctx.f), true, true)`,
  which stringifies the whole `OcttypFields` accessor object (default `Object.toString()`) instead of
  `ctx.f.getWsMsgText()`. The operator never sees the intended message.

- **`mainLine_lookup_fileReadUnrecoverableError_convertGap_returnTransidShouldNotFollowAbend`**
  COBOL's `9500-ABEND-RTN` issues `EXEC CICS RETURN` (no TRANSID) which ends the pseudo-conversational
  task immediately — `9000-RETURN`/`returnTransid` is never reached afterward. The converted
  `abendUnrecoverableFileError()` calls `appService.returnProgram()` but doesn't stop the Java call stack:
  execution falls through `readTicketTypeRecord()` → `lookupTicketType()`'s not-found branch →
  `sendDataOnlyScreen()` → `runMainLine()` still calls `returnTransid()`. Same shape of gap as seen in the
  sibling OCACCTA module — likely a systemic pattern in the ABEND-after-file-error code path across the
  converted programs, worth a generator-level fix rather than a per-module patch.

## 3. Uncovered remainder and why
Overall module C0 (54.1%) is dragged down entirely by `OcttypFields` (67/286 lines, 23%), the
auto-generated field accessor with ~150 typed getter/setter one-liners covering every field in the shared
working-storage layout. OCTTYP's business logic only touches a subset of those fields; the rest are
boilerplate delegates to the string-key API, unreachable through the `mainLine()` entry point and not
meaningful to test individually (would be pure getter/setter assertions, explicitly discouraged). Metadata
class (`OcttypBmsMetadata`, 39/42 lines) is fully covered except `getMapNames()`/`getLayoutResource()`,
which OcttypService never calls (tooling/smoke-test helpers, not business logic).

## 4. Notes for reviewer
- `back-end/programs/octtyp/pom.xml` was missing `spring-boot-starter-test` (test scope), the
  `maven-surefire-plugin` `testFailureIgnore` config, and the `jacoco-maven-plugin` — all present in
  sibling modules (e.g. `ocaccta`) but absent here. Added them to get JUnit5/Mockito/AssertJ on the test
  classpath and JaCoCo report generation; no other module already had a test suite so this gap was latent.
- One test (`registerFsetFields_delegatesMttypaFieldsToRunner`) originally tried to Mockito-mock the
  concrete `AppRunner` class, which fails under this JDK 26 + current Mockito/bytebuddy combination
  ("Byte Buddy could not instrument all classes within the mock's type hierarchy"). Switched to
  constructing a real `AppRunner(Map.of(), Map.of())` and asserting via its real `getFsetFields()` getter
  instead — worth keeping in mind for other modules' tests that need an `AppRunner`.
