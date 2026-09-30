# OCDGRP Unit Test Findings

## 1. Verify result

Final `mvn verify` (3rd/last run, budget cap reached): **Tests run: 28, Failures: 2, Errors: 0, Skipped: 0**.
The 2 failures are intentional CONVERT-GAP tests (see below); all other 26 tests pass.
C0 line coverage (module-wide, JaCoCo): **56.3%** (377/670 lines). `OcdgrpService` itself
(the actual business logic): **~95.6%** (237/248 lines) — see §3 for why the module total is lower.

## 2. CONVERT-GAP tests (intentionally failing)

Both failures trace to the same bug in `handleAbend` (`OcdgrpService.java:461`):

```java
ctx.f.setWsMsgText("OCDGRP: unrecoverable file error. Contact support.");
ctx.appService.sendText(String.valueOf(ctx.f), true, true);
```

- **COBOL (9500-ABEND-RTN)** does `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT) LENGTH(...)` —
  the operator terminal receives the literal error message.
- **Java** passes `String.valueOf(ctx.f)`, i.e. the `OcdgrpFields` accessor's default
  `Object#toString()` (e.g. `com.generated.orion.ocdgrp.accessor.OcdgrpFields@a9e8da1`),
  never `ctx.f.getWsMsgText()`. The intended message is never sent.
- Affected tests: `mainLine_enterKey_readFileUnexpectedError_abendsAndSendsErrorText`,
  `mainLine_pf5_updateReadFileUnexpectedError_abendsAndSendsErrorText`. Both assert the
  COBOL-truth expectation (`sendText` argument contains the abend message) and fail against
  current Java behavior — this is the correct detection outcome, not a test bug.
- Fix: change the call to `ctx.appService.sendText(ctx.f.getWsMsgText(), true, true);`.

No other behavioral divergence was found — `handleAbend`'s use of `appService.returnProgram()`
does correctly unwind the stack in production (`AppRunner.returnProgram()` throws
`ReturnException`), so the abend paths otherwise terminate the transaction exactly like
COBOL's task-ending `EXEC CICS RETURN`.

## 3. Uncovered remainder and why

- `OcdgrpFields` (1249/1908 instructions, 232/362 lines missed): generated accessor exposes
  getters/setters for the *entire* shared ORION commarea + working storage (CA-ACCT-ID,
  CA-CARD-NUM, WS-CDT-*, etc.), most of which OCDGRP's own logic never touches. Only the
  ~15% of fields OCDGRP actually reads/writes are exercised. Not further testable without
  writing tests for fields that are dead code from OCDGRP's perspective.
- `OcdgrpBmsMetadata` (0% covered): pure static screen-metadata declarations
  (`getButtonDefs`, `registerFsetFields`, `getFieldMapping`) — data tables with no branches,
  never invoked directly by `OcdgrpService`'s business methods (only by the runtime's map
  dispatch, outside this unit's scope). Adding direct calls would be assertion-free
  reflection tests with no behavior to protect, so skipped per the "meaningless test" rule.
- `OcdgrpService`: 11 lines missed, mostly the `9500-ABEND-RTN`/`handleAbend` companion
  branches already exercised by the two CONVERT-GAP tests, plus trivial accessor
  passthroughs (`getButtonDefs`, `registerFsetFields`, `getFieldMapping`) delegating to
  `OcdgrpBmsMetadata` above.

## 4. Reviewer notes

- `back-end/programs/ocdgrp/pom.xml` was missing the `spring-boot-starter-test` dependency
  and the surefire/JaCoCo `<build>` plugin block entirely (present in sibling modules like
  `ocacctv`) — added both so this module can compile/run tests and produce coverage.
- `back-end/orion-common` and `back-end/app-runtime` were reinstalled to the local `~/.m2`
  repo during this run (`Utility.toCobolInt(long,int)` was missing from the previously
  installed jar) — verify with a clean `~/.m2` if CI behaves differently.
- Recommend fixing the `sendText` CONVERT-GAP (§2) then re-running verify; the two failing
  tests should then flip to passing without further changes.
