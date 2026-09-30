# OUIMP — Unit Test Findings

## 1. Verify result
`mvn -q -pl back-end/programs/ouimp verify` — **Tests run: 38, Failures: 3, Errors: 0, Skipped: 0**.
The 3 failures are intentional CONVERT-GAP tests (see below); all other 35 tests pass.
C0 (line) coverage: **OuimpService = 90.9%** (229/252 lines); module-wide (incl. the
generated `OuimpFields` accessor) = **69.6%** (361/519 lines) — see §3.

## 2. CONVERT-GAP tests (intentionally failing — correct detection, not a test bug)

**Root cause — one gap, three symptoms:** `parseImportLine` (COBOL `3300-PARSE-LINE`)
never performs the `UNSTRING IMP-DATA ... INTO WS-F-TYPE ...`. The Java method only
does `setWsFields(" ")` + `setWsFldCnt(0)` — it never reads `IMP-DATA` at all. So
`WS-F-TYPE` is always blank, and the `3200-PROCESS-LINE` switch can **never** match
`RECTYPE`, `CUST`, or `ACCT` — every staged line falls into `WHEN OTHER` and is
rejected as `UNKNOWN RECTYPE`, regardless of its actual content.

- `mainLine_CONVERT_GAP_rectypeHeaderLine_cobolSkipsButJavaRejects` — COBOL: a
  `RECTYPE` header row is skipped (`KIM-SKIPPED` +1). Java: rejected as unknown
  (`KIM-REJECTED` +1, `KIM-SKIPPED` stays 0).
- `mainLine_CONVERT_GAP_custLine_cobolSkipsButJavaRejects` — same gap for `CUST` rows.
- `mainLine_CONVERT_GAP_validAcctLine_cobolAddsAccountButJavaRejects` — COBOL: a
  fully well-formed 13-field `ACCT` line passes all `3400-VALIDATE` edits and is
  `WRITE`n as a new account (`KIM-ADDED`/`KIM-ACCEPTED` +1). Java: rejected as
  unknown, `ACCTFILE` is never touched (`readFileForUpdate`/`writeFile` never called).

**Impact:** this makes OUIMP a no-op in practice — no staged line can ever reach
validation or the account master, no matter how it's formatted.

## 3. Uncovered remainder

- `OuimpFields` (accessor) sits at 121/252 lines (48%) — it carries ~90 trivial
  getter/setter one-liners for the full shared `KCOMM`/`RACCT` copybook field set
  (e.g. `CA-CARD-NUM`, `WS-BILLFILE`, `WS-CARDFILE`, …), most of which OUIMP's
  business logic never touches. Writing tests solely to call these unused
  delegates would be pure getter/setter testing with no behavior to protect
  (explicitly avoided per the "meaningless test" rule) — the module-wide 69.6% C0
  reflects this dead accessor weight, not undertested business logic.
  `OuimpService` itself (the actual paragraph logic) is at 90.9%.
- Inside `OuimpService`, the remaining ~23 uncovered lines are the `case "RECTYPE"`,
  `case "CUST"`, and `case "ACCT" ->` bodies in `processImportLine`'s switch (the
  call sites to `validateImportRecord`/`applyAccountUpdate`/the skip-counter
  increments) — these are genuinely **dead code** while the §2 gap persists;
  `parseImportLine` can never populate a type that reaches them. The underlying
  paragraphs they'd call (`validateImportRecord`, `validateAccountId`, `validateStatusFlag`,
  `validateAmountFields`, `validateDateFields`, `validateDateFormat`, `applyAccountUpdate`,
  `buildAccountRecord`, `recordRejection`) are still fully unit-tested via direct
  reflection invocation on a hand-built `TaskContext`, bypassing the broken parse —
  so their business logic remains regression-guarded and will start being exercised
  end-to-end automatically once `parseImportLine` is fixed.

## 4. Reviewer notes

- Fixing `parseImportLine` (implement the UNSTRING) should be treated as the
  primary follow-up; until then, OUIMP silently rejects 100% of its input in
  production.
- Test file: `back-end/programs/ouimp/src/test/java/com/generated/orion/ouimp/service/OuimpServiceTest.java`.
