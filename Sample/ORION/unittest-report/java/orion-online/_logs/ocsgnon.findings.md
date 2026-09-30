# OCSGNON — Unit Test Findings

## 1. Verify result

Final `mvn verify` (3rd/last run in budget): **Tests run: 12, Failures: 2, Errors: 0, Skipped: 0** (build succeeded — `testFailureIgnore=true`). The 2 failures are intentional CONVERT-GAP tests (see §2); all other 10 tests pass, covering happy-path sign-on, first-time-context, invalid-key, blank-userid, user-not-found, wrong-password, admin/non-admin transfer.

## 2. CONVERT-GAP tests (intentionally failing — real bugs found)

1. **`mainLine_signOff_sendsThankYouMessage_convertGap`** — COBOL `7000-SIGN-OFF` does `EXEC CICS SEND TEXT FROM(WS-MSG-THANKYOU)`, i.e. sends the literal text "Thank you for using ORION-CCMS.". The converted `signOff()` (OcsgnonService.java:172) instead calls `appService.sendText(String.valueOf(ctx.f), true, true)` — `String.valueOf` on the `OcsgnonFields` accessor object returns `Object#toString()` (e.g. `com.generated.orion.ocsgnon.accessor.OcsgnonFields@44976b08`), never the actual message. The sign-off screen sent to the terminal is garbage. **Fix**: use `ctx.f.getWsMsgThankyou()` instead of `String.valueOf(ctx.f)`.

2. **`mainLine_signOff_doesNotAlsoReturnTransid_convertGap`** — In COBOL, `7000-SIGN-OFF` issues its own `EXEC CICS RETURN RESP(...)` (no TRANSID), which ends the CICS task immediately; control never returns to `0000-MAIN`'s trailing `PERFORM 9000-RETURN`. In the Java conversion, `executeMainLine()` (OcsgnonService.java:70-84) calls `processInput()` → `signOff()` → `appService.returnProgram()`, then unconditionally falls through to `returnTransaction(ctx)` → `appService.returnTransid(...)`. This issues an extra/incorrect RETURN not present in the original control flow (double return-transaction call after sign-off). **Fix**: make `processInput`/`executeMainLine` short-circuit after sign-off (e.g. return early) instead of always calling `returnTransaction`.

## 3. Uncovered remainder

Overall module C0 (JaCoCo) is ~36%, but this is dragged down entirely by two generated, non-business-logic classes, not by the tested service:
- `OcsgnonFields` (accessor): only ~49/256 lines covered — it has ~120 trivial 1-line getter/setter pairs auto-generated for every copybook field; only the subset actually touched by OCSGNON's business paths (errmsgo, useridi, passwdi, us-*, ca-*, ws-hdr-*, etc.) is exercised. Testing every unused getter/setter individually adds no business value.
- `OcsgnonBmsMetadata`: 0% covered — `getButtonDefs`/`registerFsetFields`/`getFieldMapping` are thin static-metadata delegations not exercised (would require a real `AppRunner`/BMS map setup with no business logic to protect).
- `OcsgnonService` itself: **84/88 lines covered (~95%)** — the 4 missed lines are the `AppProgram` metadata passthroughs (`getButtonDefs`, `registerFsetFields`, `getFieldMapping`) not covered per above.

Verify budget (3 runs) is exhausted per run policy; not re-attempting further coverage passes.

## 4. Reviewer notes

- Both CONVERT-GAP failures are real production bugs in `OcsgnonService.signOff()` / `executeMainLine()` — recommend a follow-up fix ticket, not a test change.
- Fixed-width fields (PIC X) return space-padded strings from `OcsgnonFields` getters; tests compare with `.trim()` where the COBOL value itself isn't already full-width.
- Test/pom changes: added `spring-boot-starter-test` (test scope) + `jacoco-maven-plugin`/surefire config to `back-end/programs/ocsgnon/pom.xml`, mirroring the pattern already used by sibling program modules (e.g. `ocmenu`) — this module had no test infra before.
