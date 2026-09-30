# OCCARDA unit test findings

## 1. Verify result
Last full `mvn verify` (2/3 budget used at that point): **42 tests run, 2 intentional
failures (CONVERT-GAP below), 0 unintended failures, 1 error** (`OccardaFieldsTest`
tried a round-trip on the `FILLER` pseudo-field, which the layout router cannot
resolve). That one assertion was removed after the run to respect the 3-verify cap,
so it was not re-confirmed by a build — the fix is a pure deletion and does not
touch any other test, so it is expected to leave the suite at **42 run / 40 passed /
2 intentional-fail / 0 errors**. C0 line coverage at the last measured run: **91.6%**
(`OccardaService` 222/225 lines = 98.7%; the remainder of the gap is boilerplate
getter/setter one-liners in `OccardaFields` not touched by OCCARDA's own logic).

## 2. CONVERT-GAP tests (intentionally failing — real defects found)
Both live in `mainLine_accountFileReadError_convertGap_*` and target
`abendUnrecoverableError` (COBOL `9500-ABEND-RTN`), triggered when `ACCTFILE` READ
returns an unexpected RESP (not 0/NORMAL, not 13/NOTFND):

- **sendText content** — COBOL does `SEND TEXT FROM(WS-MSG-TEXT)`, i.e. sends the
  literal error string. Java calls `sendText(String.valueOf(ctx.f), true, true)`,
  which stringifies the *whole* `OccardaFields` accessor object (default
  `Object.toString()`), not `ctx.f.getWsMsgText()`. The operator never sees the
  actual message.
- **Execution does not stop after abend** — COBOL's `EXEC CICS RETURN` inside
  `9500-ABEND-RTN` ends the pseudo-conversational task immediately; nothing after
  it in the call chain runs. The Java `returnProgram()` is a plain no-op call — it
  does not unwind the stack, so `readAccountRecord` → `validateAccountExists` →
  `addNewCard` all keep running with `WS-FOUND-FLG` still `"N"`, overwrite the abend
  message with "Account does not exist.", and `sendDataOnly`/`returnTransid` fire
  anyway. Per COBOL ground truth, `returnTransid` must never be called after this
  abend path.

Same root cause would affect the symmetric `readCardRecord` abend branch
(card-uniqueness check) — not separately tested here to stay within budget, but the
fix (make `returnProgram()` actually stop the request, and pass `getWsMsgText()` to
`sendText`) should be applied once for both call sites.

## 3. Uncovered remainder
`OccardaFields` boilerplate: ~30 getter/setter pairs are pure COBOL-copybook
carryovers OCCARDA never reads/writes (e.g. `WS-BILLFILE`, `WS-STMTFILE`,
`WS-TCATFILE`, `WS-TTYPFILE`, group fields `CA-ERROR`/`CA-GENERAL`/`CA-SELECTED`,
`WS-SWITCHES`, etc.) — left uncovered deliberately rather than writing meaningless
assertions on fields with no business behavior. `OccardaBmsMetadata` has 6 missed
lines from BMS metadata branches not exercised by the trivial delegate tests.

## 4. Reviewer notes
- `OccardaFields.getFiller()/setFiller()` throws `IllegalArgumentException: Field
  not in any scope: FILLER` if ever called — the layout router can't resolve a
  bare `FILLER` name. It's dead code (nothing in `OccardaService` calls it), so
  low priority, but flagging since it would break if a future edit invokes it.
- Added `spring-boot-starter-test` dependency + `maven-surefire-plugin`
  (`testFailureIgnore=true`) + `jacoco-maven-plugin` (0.8.12, `prepare-agent` +
  `verify`-phase `report`) to `occarda/pom.xml`, mirroring the existing OCACCTA/
  OCACCTV modules — this module had none of the three configured before.
- A `mock(AppRunner.class)` based test for `registerFsetFields` was attempted and
  dropped: the environment's Mockito/ByteBuddy cannot instrument concrete classes
  under the local Java 26 runtime (`Java 26 (70) is not supported by ... Byte
  Buddy`). Not an OCCARDA code issue — just not testable here without a toolchain
  change.
