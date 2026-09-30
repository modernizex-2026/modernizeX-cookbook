# OUFEE — Unit Test Findings

## 1. Verify result

`mvn -pl back-end/programs/oufee verify` — **BUILD SUCCESS** (surefire fails the run but jar/jacoco still execute).
Tests run: **16**, Passed: **15**, Failed: **1** (intentional CONVERT-GAP, see below), Errors: 0, Skipped: 0.

## 2. CONVERT-GAP (intentionally failing)

**`mainLine_minDueBoundary_CONVERT_GAP_cobolRoundsHalfUpJavaTruncates`** — 🔴 rounding mismatch in `evaluateFeeEligibility` (`OufeeService.java:234-237`).

- COBOL: `COMPUTE WS-MIN-DUE ROUNDED = AC-CURR-BAL * WS-MIN-DUE-PCT` rounds **half-up** to 2 decimals.
- Java: `ctx.f.setWsMinDue(acCurrBal.multiply(minDuePct))` performs no explicit rounding; the underlying `RecordCodec.encodeField` (orion-common) applies `RoundingMode.DOWN` (truncation) when the `BigDecimal` is written into the 2-decimal `WS-MIN-DUE` buffer field.
- Example: `AC-CURR-BAL=1253.75`, `WS-MIN-DUE-PCT=0.0200` → raw product `25.0750`. COBOL rounds to **25.08**; Java truncates to **25.07**. With `AC-CYC-CREDIT=25.07`, COBOL's delinquency test (`AC-CYC-CREDIT < WS-MIN-DUE`) is **true** (25.07<25.08 → delinquent, late fee assessed), Java's is **false** (25.07<25.07) → no fee assessed at all.
- Test asserts the COBOL-truth outcome (`KO-C2=1`, `KO-AMT2=35.00`, `KO-SELECT-CNT=1`); it fails against current Java (`0` in all three), correctly flagging the gap. Any COMPUTE...ROUNDED in this codebase reusing the same DISPLAY-decimal write path is worth auditing.

## 3. Blocking infra defect worked around in test setup (not an OUFEE bug)

`orion-common`'s `RecordBuffer.applyInitialValues()` mis-encodes signed/decimal DISPLAY-numeric `VALUE` literals (e.g. `+35.00`, `+0.0200`) as raw ASCII text instead of zoned-decimal bytes. A freshly constructed `WorkingStorage` therefore throws `NumericValueException` the first time `WS-LATE-FEE` / `WS-OVLIM-FEE` / `WS-MIN-DUE-PCT` / `WS-MIN-DUE-FLOOR` is read — i.e. **any account with a positive balance crashes `evaluateFeeEligibility` in production**, not just in tests. This is out of scope to fix here (shared library, used by ~40 other program modules). The test class re-applies the four correct COBOL constants via `setDecimal` (the working, non-buggy path) in a shared `fixBrokenDisplayDefaults()` helper before any account is evaluated, so OUFEE's own logic could actually be exercised. **Reviewer should raise this as a separate, higher-priority defect against `orion-common`** — it likely affects other modules with signed-decimal working-storage constants too.

## 4. Uncovered remainder (C0 = 63.1%, below the 80% target)

Sum-of-rows JaCoCo: LINE_COVERED=327, LINE_MISSED=191 → **63.1%** (instruction 62.9%, well within the 3-verify budget cap).
- `OufeeService` itself (the actual converted business logic): **95.7%** line coverage (198/207) — effectively complete; only 1 branch (18 missed) relates to COMMAREA edge shapes not fully enumerated.
- `OufeeFields` (auto-generated field accessor, ~140 getter/setter pairs): only 39.9% covered — the uncovered getters/setters are for fields unrelated to fee assessment (CA-*, CUSTFILE/CARDFILE/etc. linkage/file-name fields never touched by OUFEE's paragraphs). Exercising them would require artificial calls with no business justification; not worth adding per the "no meaningless tests" rule.
- `OufeeBmsMetadata`: `getButtonDefs`/`registerFsetFields` are covered; `getMapNames`/`getLayoutResource`/`getFieldMapping` (unused by `OufeeService`) are not — all are trivial one-line stubs.

## 5. Notes for reviewer

- The filter branch (`WS-FILTER-ON`/`KO-PARM-ACCT`) is NOT covered: driving it requires building a correct COMMAREA byte layout for `KOPS-AREA`/`CA-WORK-AREA` (via `aliasGroup`), which wasn't feasible within the verify budget. Two simpler COMMAREA-passthrough tests (byte[] and Object[] wrapping) cover `mainLine`'s I/O wrapper branches instead.
- `updateAccountBalance` failing still lets `writeFeeTransaction` run (COBOL's `GO TO 4400-EXIT` only exits that paragraph; the caller unconditionally performs `4500-WRITE-TRAN` next) — verified explicitly; this is a faithful conversion, not a gap.
- `9000-FINALISE` always overwrites `KO-STATUS-MSG` to "FEE ASSESSMENT COMPLETE." when not in error state, even overwriting "NO ACCOUNTS TO ASSESS." — verified as matching COBOL exactly.
