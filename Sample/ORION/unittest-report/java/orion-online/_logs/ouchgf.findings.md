# OUCHGF — Unit Test Findings

## 1. Verify result

`mvn -pl back-end/programs/ouchgf verify`: **Tests run: 20, Failures: 0, Errors: 9, Skipped: 0**.
Overall module C0 line coverage (JaCoCo, Σ LINE_COVERED / Σ(LINE_MISSED+LINE_COVERED)): **86.3%** (284/329) — above the 80% floor. `OuchgfService` alone (excluding boilerplate accessor/metadata classes): 117/181 = 64.6%, dragged down by the crash below.

## 2. CONVERT-GAP — critical, blocks ALL charge-off tests (9 of 20)

**Not a COBOL↔Java logic mismatch — a runtime crash bug in shared infrastructure (`orion-common`), surfaced by this module.**

COBOL `WS-CO-FACTOR PIC 9(01)V99 VALUE 1.20` (OUCHGF.cbl line 36) is used to compute the charge-off
threshold (`AC-CREDIT-LIMIT * WS-CO-FACTOR`, line 230-231). The Java layout
(`OUCHGF_WS.xml`) declares `WS-CO-FACTOR` as a 3-byte DISPLAY BigDecimal with `value="1.20"`.
`RecordBuffer.applyInitialValues()` (orion-common `layout/RecordBuffer.java`) only applies
scale-aware numeric encoding to VALUE literals when `usage != DISPLAY`; for a DISPLAY field it
falls through to a raw `iv.getBytes(charset)` copy of the literal string `"1.20"` (4 bytes),
truncated to the field's 3-byte width → bytes `'1'‚'.'‚'2'`. Any later read of `WS-CO-FACTOR`
(`getWsCoFactor()`) then fails to decode byte `0x2E` ('.') as a DISPLAY digit and throws
`NumericValueException`.

Effect: **every account that is active with a positive balance** (COBOL's outer IF, line 229)
reaches the threshold computation and crashes — the entire charge-off feature
(4100-CHARGE-OFF/4200-POST-ADJUSTMENT/4300-UPDATE-ACCT) is unreachable in the current build,
online or batch. This is a production-blocking defect, not a subtle port divergence.

Expected (COBOL ground truth) vs actual (Java): the 9 affected tests assert the correct
COBOL outcome (charge-off happens, counters/amounts/TR-ID update as specified) and are marked
`// CONVERT-GAP` inline; they fail/error by design to surface this defect. Affected tests:
`mainLine_activeAccountOverThreshold_chargesOffAndUpdatesCounters`,
`mainLine_balanceExactlyAtThreshold_notChargedOff`, `mainLine_cycCreditNonZero_blocksChargeOff`,
`mainLine_writeTranfileFails_stillCompletesChargeOffWithoutTranCount`,
`mainLine_acctReadForUpdateFails_rejectsAndWarns`, `mainLine_acctRewriteFails_rejectsAndWarns`,
`mainLine_filterActiveMatchingAccount_chargesOffThenStopsOnNextMismatch`,
`mainLine_ctrlMissingOnLoad_seedsCounterFromZero`, `mainLine_ctrlReadFailsAtSaveTime_writesNewControlRecord`.

This bug was not fixed here: it lives in `orion-common` (shared by ~60 program modules), out of
scope for a module-scoped test-writing pass and too high-blast-radius to patch without review.

## 3. Uncovered remainder

Private methods `chargeOffAccount`/`postChargeOffTransaction`/`updateAccountAfterChargeOff`
have near-zero executed lines because of item 2 above — every test that would exercise them
throws before reaching their bodies. Once the `WS-CO-FACTOR` decode bug is fixed, these 9 tests
should pass unmodified and should push `OuchgfService` line coverage close to 100% (all COBOL
branches are already covered by the test plan; no additional test-writing is needed).

## 4. Reviewer notes

- No other CONVERT-GAP was found: filter gating (`WS-FILTER-ON`/`KO-PARM-ACCT`), all CICS resp
  branches (STARTBR/READNEXT/READ UPDATE/REWRITE/WRITE), TRANID counter load/save (including the
  "control record missing → WRITE new" branch), and `9000-FINALISE`'s status/message logic are
  faithfully ported and covered by passing tests.
- Interesting (verified correct, not a bug): `9000-FINALISE` unconditionally overwrites
  `KO-STATUS-MSG` to `"CHARGE-OFF PROCESSING COMPLETE."` whenever `KO-STATUS` isn't `'E'` — so the
  STARTBR-NOTFND message `"NO ACCOUNTS TO PROCESS."` never survives to the caller in COBOL either;
  Java matches this faithfully (see `mainLine_startBrowseNotFound_...` test).
- Recommend filing the `WS-CO-FACTOR` VALUE-encoding defect against `orion-common`'s
  `RecordBuffer.applyInitialValues` — likely affects any DISPLAY numeric field across other
  modules whose COBOL VALUE literal contains a decimal point.
