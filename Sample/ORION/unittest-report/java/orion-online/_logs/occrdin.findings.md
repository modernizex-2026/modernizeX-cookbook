# OCCRDIN unit test findings

## 1. Verify result

Final `mvn -q -pl back-end/programs/occrdin verify`: **BUILD SUCCESS**.
Tests run: 29, Failures: 0, Errors: 0, Skipped: 0.
JaCoCo C0 (line) coverage: **100.00%** (367/367 lines, `OccrdinService` + inner `TaskContext`).

## 2. CONVERT-GAP tests

**None.** A pre-existing test in this class was labeled
`mainLine_enterActiveFilterAbbreviation_convertGap_lowercaseNotUppercased` and claimed
`parseFilterInput` used a literal substring `String.replace(...)` instead of COBOL's
`INSPECT ... CONVERTING` char-by-char case fold, so lowercase filters like `"act"` would
wrongly fail validation. This claim was **incorrect**: `Utility.inspectConverting(value,
from, to)` (in `orion-common`) is implemented as a genuine per-character index-mapped
fold, matching COBOL `INSPECT CONVERTING` semantics exactly. The test passed against the
real Java behavior. Renamed to
`mainLine_enterActiveFilterAbbreviationLowercase_foldedToUppercaseAndMatchesActive` and
corrected the comment; no other change to inputs/expectations. Reviewer should verify the
old label wasn't hiding an intent to file a real defect elsewhere — none was found in this
module.

No other divergence from `OCCRDIN.cbl` (`0000-MAIN` through `9000-RETURN`, all filter/page
paragraphs) was observed during this pass.

## 3. Uncovered remainder

None — C0 is 100%. All 13 `placeRowOnScreen` switch cases, both `buildStatusMessage`
suffixes (MORE/DONE), all four filter branches (ALL/ACT/INA/EXP) and their description
labels (ALL/ACTIVE/INACTIVE/EXPIRING), the `receiveScreenInput` RESP=36 (MAPFAIL)
low-values branch, link transport-error and sub-program browse-error branches, and the
trivial metadata delegation methods (`getButtonDefs`, `getFieldMapping`,
`registerFsetFields`) are exercised.

## 4. Reviewer notes

- Tests mock only `AppService`; `OccrdinFields`/`WorkingStorage` are used as real objects
  (buffer-backed COBOL field accessors), consistent with this module having no COBOL file
  I/O of its own (browse work is delegated via `LINK` to the sub-program, which is fully
  mocked).
- `jacoco.csv` only reports the `service` package (`OccrdinService`); accessor/metadata
  classes are pass-through generated code not separately measured — no action needed.
