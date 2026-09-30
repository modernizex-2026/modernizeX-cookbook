# OCCARDU — Unit Test Findings

## 1. Verify result
Final `mvn verify` (2nd run, after fixing an unnecessary-stubbing issue): **Tests run: 30, Failures: 2, Errors: 0, Skipped: 0.**
The 2 failures are intentional CONVERT-GAP tests (see below) — everything else passes.

## 2. Intentional CONVERT-GAP tests (fail on purpose)

Both gaps are in `handleAbend()` (COBOL `9500-ABEND-RTN`), triggered when `readFile`/`readFileForUpdate` returns an unexpected EIBRESP (not NORMAL/NOTFND):

- **`mainLine_lookupReadUnexpectedError_convertGap_sendTextShouldCarryMessageText`**
  COBOL: `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT)` — sends the literal message "OCCARDU: unrecoverable file error. Contact support."
  Java: `ctx.appService.sendText(String.valueOf(ctx.f), true, true)` stringifies the whole `OccarduFields` accessor object (`Object.toString()`), not `ctx.f.getWsMsgText()`. The terminal would show a Java object reference instead of the error message.

- **`mainLine_lookupReadUnexpectedError_convertGap_returnTransidShouldNotFollowAbend`**
  COBOL: `9500-ABEND-RTN` issues `EXEC CICS RETURN` (no TRANSID) — ends the task immediately, `9000-RETURN` is never reached.
  Java: `handleAbend()` calls `appService.returnProgram()` but does not stop the call stack, so `processMainLine()` falls through and `returnTransid()` fires anyway afterward. Behavior after an abend diverges from COBOL (extra RETURN TRANSID would resume a task the COBOL side already ended).

These same two gaps were previously found in the sibling module OCCARDL — same abend-routine code-generation pattern is shared across programs modelled on the OCACCTV template.

## 3. Coverage
- `OccarduService` (all business logic, the class this skill targets): **201/204 lines covered ≈ 98.5% C0** — only the abend `sendText` path's exact string plus a couple of unreachable-in-practice lines remain uncovered.
- Module-wide C0 (incl. `OccarduFields` accessor): **344/580 ≈ 59.3%** — dragged down by `OccarduFields`, a generated field-accessor class with ~150 typed getter/setter wrappers, most unused by any single test scenario. This is architectural (identical pattern in OCCARDL: ~54.5% module-wide with accessor covered) and not a gap in this test suite's coverage of business logic.

## 4. Reviewer notes
- `pom.xml` was missing `spring-boot-starter-test` + surefire/JaCoCo config entirely (present in sibling modules) — added it to make `verify`/JaCoCo work; no other module files were touched besides the new test class.
- `OUDATE` CALL is mocked via `callProgram("OUDATE", ...)`, mutating the shared `KDATE-PARM` group string to set `KD-STATUS`, mirroring the pass-by-reference COBOL semantics.
