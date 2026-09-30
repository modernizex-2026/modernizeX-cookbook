# OUFLAG — Unit Test Findings

## 1. Final verify result
`mvn -q -pl back-end/programs/ouflag verify` (3rd/final run, budget exhausted): **34 tests run, 0 failures, 8 errors, 0 skipped** (26 passing). JaCoCo `report` goal fails separately (see §3) — line coverage is **N/A**, not a test-quality issue.

## 2. Blocking defect (not a CONVERT-GAP — a shared-runtime crash)
All 8 errors are the *same* root cause, not 8 distinct bugs: `com.generated.orion.common.infrastructure.layout.RecordBuffer.applyInitialValues()` mis-encodes the WORKING-STORAGE `VALUE` literal for `WS-MIN-DUE-PCT` (`PIC S9(01)V9(04) VALUE +0.0200`, 5-byte DISPLAY field). The loader falls through to the raw-ASCII-literal path, copies the first 5 bytes of the literal string `"+0.0200"` (i.e. `"+0.02"`) straight into the buffer instead of numeric-encoding it, so every read of `WS-MIN-DUE-PCT` throws `NumericValueException: DISPLAY non-numeric byte 0x2B ...`. This fires from `OuflagService.evaluateDelinquency` (OuflagService.java:233) the first time any active account has `AC-CURR-BAL > 0` under DELQ/BOTH mode — i.e. it crashes essentially all real delinquency evaluation, not an edge case.
This is **pre-existing and out of `ouflag`'s scope**: `back-end/programs/oufee` has the identical `WS-MIN-DUE-PCT +0.0200` constant and its already-committed test suite shows the exact same 8-error pattern (`OufeeServiceTest`, same stack trace, same offset semantics). Fixing it means editing `orion-common` (shared across the whole reactor), which this run's HARD OVERRIDE explicitly forbids touching. **Two tests were originally designed as CONVERT-GAP tests** for a *different*, more subtle issue — COBOL's `COMPUTE ... ROUNDED` (half-up) vs. Java's `setDecimal` truncation (`RoundingMode.DOWN`) when a BigDecimal is persisted to a scale-2 buffer field:
- `mainLine_minDueRoundingTruncation_cobolFlagsDelqButJavaTreatsCurrent` — expects COBOL's rounded min-due (25.01) to flag delinquency where Java's truncated min-due (25.00) would instead count the account as current.
- `mainLine_utilRoundingTruncation_cobolBucketB60ButJavaBucketsB30` — expects COBOL's rounded utilisation (90.00, bucket B60) vs. Java's truncated value (89.99, bucket B30).
Both are now masked by the crash above (they error before reaching the assertion) but remain valid, COBOL-derived regression tests that will start exercising the rounding-mode gap the moment the shared defect is fixed.

## 3. Uncovered remainder
- Line coverage is N/A: `jacoco-maven-plugin:0.8.12:report` fails with "Unknown block type 6e" on every run (also reproduced pre-existing in sibling module `oufee`), consistent with a JDK/JaCoCo version mismatch (JVM reports class file major version 70) — an environment/toolchain issue, not fixable from this module's pom.
- Any branch downstream of a successful `WS-MIN-DUE-PCT` read (bucket classification B30/B60/B90, `KFL-DELQ-BAL`/`KFL-SHORTFALL` accumulation, min-due floor interaction) is untested at runtime for the same reason — the 8 errored tests already encode the correct COBOL-derived expectations and will start asserting real coverage once the shared defect is fixed.

## 4. Reviewer must-know
- Do not "fix" the 8 errors by loosening assertions — they surface a genuine production-breaking defect in `orion-common`'s WORKING-STORAGE default-value loader, not a test bug.
- 26 tests pass cleanly, covering: mode/cutoff validation (1000-INIT), STARTBR outcomes (2000-POSITION), READNEXT failure handling (3100), inactive-account skip (3200), expiry capture incl. spaces/low-values/cutoff boundary and the 300-entry cap (3400), WS-MAX read cap + peek-next (3000/4000), the full deactivate-one state machine incl. read-for-update error / already-inactive / rewrite error (5600), and 6000-SET-STATUS.
