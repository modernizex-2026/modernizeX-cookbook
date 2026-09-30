# OCACCTU Unit Test Findings

## 1. Final verify result

`mvn -pl back-end/programs/ocacctu verify` (3rd/final run): **52 tests run, 2 failures, 0 errors**.
The 2 failures are intentional CONVERT-GAP regressions (see below) — all other 50 tests pass.
C0 line coverage (JaCoCo): **97.6%** (702/719 lines), well above the 80% bar.

## 2. Intentional CONVERT-GAP failures

Both gaps are in `abendWithFileError` (COBOL paragraph `9500-ABEND-RTN`), triggered when
`3000-READ-ACCT` / `3500-UPDATE-ACCT` hits an unexpected CICS file-read RESP.

- **`mainLine_lookupRead_convertGap_sendTextShouldCarryMessageText`**: COBOL does
  `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT)` — it sends the literal error message string.
  The converted Java calls `sendText(String.valueOf(ctx.f), true, true)`, which stringifies
  the *entire* `OcacctuFields` accessor object (`Object.toString()`, e.g.
  `OcacctuFields@29f95272`) instead of `ctx.f.getWsMsgText()`. Operator never sees the
  actual error text.
- **`mainLine_lookupRead_convertGap_returnTransidShouldNotFollowAbend`**: COBOL's
  `9500-ABEND-RTN` issues `EXEC CICS RETURN` (no TRANSID), ending the task immediately —
  `9000-RETURN` is never reached. The converted `abendWithFileError()` calls
  `appService.returnProgram()` but doesn't stop the Java call stack, so `runMainProgram()`
  falls through to `returnToCics()`/`returnTransid()` regardless. Per COBOL, `returnTransid`
  must NOT fire after an abend.

Both tests fail against current Java on purpose — this is the intended detection of a
convert gap, not a test bug.

## 3. Uncovered remainder (17 lines / 2.4%)

- `OcacctuBmsMetadata` (5 missed lines): `getMapNames()` / `getLayoutResource()` helper
  methods, not called by production code paths exercised here — pure tooling/introspection
  helpers, not part of any COBOL paragraph.
- `OcacctuFields` (4 missed lines): a few getter/setter pairs for shared-copybook fields
  (`RACCT` fields like `FILLER-122`) with no accessor wrapper exercised, plus a couple of
  edge branches inside the generic `DynamicFieldAccessor`/`FieldRouter` not reached by the
  round-trip smoke test.
- `OcacctuService` (8 missed lines): minor branches inside `abendWithFileError`'s unreachable
  tail (see gap #2 above — code after the would-be `RETURN` never executes in real CICS) and
  one or two ancillary getters.

None of these represent untested COBOL business-logic branches; all `5000-VALIDATE-ALL`
sub-paragraphs, `2200-LOOKUP`/`2250-VALIDATE-ONLY`/`2300-SAVE`, `3000/3500/3600`, and AID
dispatch are covered by `OcacctuServiceTest`.

## 4. Reviewer notes

- Had to add `spring-boot-starter-test` dependency and the `maven-surefire`/`jacoco` build
  config to `ocacctu/pom.xml` (copied from the sibling `ocaccta` module) — the pom shipped
  without any test dependencies or JaCoCo wiring.
- A `registerFsetFields(AppRunner)` test was dropped: Mockito's inline mock maker cannot
  instrument `AppRunner` on this JDK (Java 26 / Homebrew build) — `MockitoException: Could
  not modify all classes`. Not a code defect, an environment/tooling limitation.
- New file: `OcacctuFieldsTest` — round-trips every generated field accessor to catch
  string-key typos in the generated `OcacctuFields` class (unrelated to COBOL logic per se,
  but protects the code-gen wiring).
