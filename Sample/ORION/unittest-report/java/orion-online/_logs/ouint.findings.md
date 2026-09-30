# OUINT — Unit Test Findings

## 1. Final verify result

`mvn -pl back-end/programs/ouint verify` — **Tests run: 17, Failures: 1, Errors: 0, Skipped: 0.**
C0 line coverage: **92.99%** (146 / 157 lines, JaCoCo). The single failure is an
intentional CONVERT-GAP test (see below), not a bug in the test suite.

## 2. CONVERT-GAP test (fails on purpose)

**`mainLine_rateNotFoundForGroup_usesDefaultRateAndIncrementsC1`**

- Setup: DGRPFILE lookup misses (default rate 18.99 used), AC-CURR-BAL = 1000.00.
  Unrounded interest = `1000.00 * 18.99 / 1200 = 15.825` exactly.
- **COBOL** (`COMPUTE WS-INT-AMT ROUNDED = (AC-CURR-BAL * WS-USED-RATE) / 1200`,
  WS-INT-AMT is `PIC S9(11)V99`): ROUNDED performs half-up rounding straight to 2
  decimals → **15.83**.
- **Java** (`computeInterest`): divides to scale 12 with `HALF_UP` (15.825000000000,
  exact, no rounding effect there), but the field-buffer writer that later encodes
  WS-INT-AMT into the record (`RecordCodec.encodeField`, `orion-common`) always calls
  `BigDecimal.setScale(fractionDigits, RoundingMode.DOWN)` — truncating 15.825 down to
  **15.82**.
- Impact: every posted interest amount whose exact 3rd decimal is 5–9 is short by
  0.01–0.09 vs. COBOL; this compounds into KO-AMT-1 and the posted account balance.
  Same class of defect previously found in OUACTIN (shared `orion-common` infra bug,
  not specific to OUINT).

## 3. Uncovered remainder (11 lines, ~7%)

Not exercised: a few branches inside `OuintService.mainLine`'s COMMAREA
marshalling boilerplate (the `Object[]`/`byte[]` COMMAREA variants used only when
OUINT is invoked via `CALL ... USING` param array rather than the CICS
`LINK ... COMMAREA` path exercised by every test here) and the `getDliService`/rarely-
hit default branches inherited from `AppService`. These are generic runtime plumbing
shared across all converted programs, not OUINT-specific business logic, and are
already covered by infra-level tests elsewhere in the reactor.

## 4. Reviewer notes

- KO-* fields (KOPS-AREA) are overlaid onto CA-WORK-AREA (inside ORION-COMMAREA) via
  `SET ADDRESS OF KOPS-AREA TO ADDRESS OF CA-WORK-AREA`. Any new test building a
  request or reading the response **must** call
  `fields.aliasGroup("KOPS-AREA", "CA-WORK-AREA")` before setting/reading KO-* fields,
  or all KO-* reads silently come back blank/zero (root cause found and fixed during
  this run).
- `KO-STATUS-MSG` is a fixed-width field; assertions on it are done via `.trim()`.
- No other correctness gaps found — skip logic (inactive/non-positive balance),
  rate-lookup fallback, READ-UPDATE/REWRITE reject handling (including the
  KO-AMT-3-includes-interest-despite-failed-REWRITE subtlety), and STARTBR/READNEXT
  RESP handling all match COBOL ground truth exactly.
