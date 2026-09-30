# OCACCTA — Unit Test Findings

## 1. Verify result
Final `verify`: **26 tests, 24 passed, 2 intentionally-failing (CONVERT-GAP)**, 0 errors.
Used the full 3-run verify budget: (1) fixed a pre-existing compile error, (2) fixed
genuine test bugs, (3) confirming run + JaCoCo report.

## 2. CONVERT-GAP tests (fail on purpose — they detect real conversion defects)

- **`mainLine_customerFileReadError_convertGap_sendTextShouldCarryMessageText`**
  COBOL `9500-ABEND-RTN` does `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT)` — sends the literal
  error message. Java's `abendWithFileError()` calls
  `appService.sendText(String.valueOf(ctx.f), true, true)`, which stringifies the whole
  `OcacctaFields` accessor object (`Object.toString()`, e.g. `OcacctaFields@1a2b3c`)
  instead of `ctx.f.getWsMsgText()`. The operator never sees the intended error text.

- **`mainLine_customerFileReadError_convertGap_returnTransidShouldNotFollowAbend`**
  COBOL's `9500-ABEND-RTN` issues `EXEC CICS RETURN` (no TRANSID), ending the
  pseudo-conversational task immediately — `9000-RETURN` is never reached afterward.
  Java's `abendWithFileError()` calls `appService.returnProgram()`, which does not stop
  Java's call stack, so `runMainProgram()` falls through and calls `returnToCics()` →
  `returnTransid()` anyway. The task re-arms transaction `OROA` after an abend, which
  COBOL never does.

## 3. Uncovered remainder
Overall module C0 ≈ 55% (sum of jacoco.csv), but the actual business logic under test,
`OcacctaService`, reaches **~97.6% line coverage** (279/286 lines; only 7 lines missed,
all in the never-triggered write-branch of `writeCrossReferenceRecord`'s untaken code
path combinations). The low module aggregate is dragged down by two files with no
business logic of their own:
- `OcacctaFields` (~300 missed lines): a generated field accessor with one delegate
  getter/setter per COBOL field; OCACCTA's logic only touches ~40 of them.
- `OcacctaBmsMetadata` (58 missed lines): static screen/button/field-mapping metadata,
  not exercised because `getButtonDefs`/`registerFsetFields`/`getFieldMapping` aren't
  called by the business flow tested here.

## 4. Reviewer must know
- **Pre-existing compile-blocking bug** (not introduced by this task): `Utility.toCobolInt(long,int)`
  was called by `OcacctaService` (and 8 other program modules) but did not exist anywhere
  in `orion-common`'s `Utility.java` — the module could not compile at all. Added a
  minimal implementation (COBOL low-order-digit truncation, no rounding) to unblock
  compilation for this and the other 8 affected modules. Please review.
- `ocaccta/pom.xml` had no test framework or coverage tooling configured. Added
  `spring-boot-starter-test` (test scope), `jacoco-maven-plugin` (prepare-agent +
  verify-phase report), and `maven-surefire-plugin` `testFailureIgnore=true` (required
  so `verify` still produces the JaCoCo report given the 2 intentional CONVERT-GAP failures).
- `mainLine_writeCrossReference_fails_stillReportsSuccessPerCobolOverwrite` documents a
  genuine COBOL quirk (not a convert gap): `2100-OPEN-ACCT` always overwrites `ERRMSGO`
  with `WS-M-PROMPT` then `WS-M-OK` right after a xref-write failure, so the xref error
  message is set but never actually shown to the operator — this is original COBOL
  behavior, faithfully reproduced in Java.
