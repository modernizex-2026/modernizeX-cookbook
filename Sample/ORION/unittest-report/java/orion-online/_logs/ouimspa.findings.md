# OUIMSPA — Unit Test Findings

## 1. Verify result
`mvn -q -pl back-end/programs/ouimspa verify` — **BUILD SUCCESS**.
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0 (1 verify run used, well within the 3-run budget).
C0 line coverage: 201/225 = **89.3%** (above the 80% gate).

## 2. CONVERT-GAP tests
**None.** OUIMSPA.cbl's paragraphs (0000-MAIN through 9000-FINALIZE) map 1:1 onto
`OuimspaService`'s `_0000Main`/`_1000Initialize`/.../`_9000Finalize` methods with no
behavioral divergence found: function/key validation, the INQ/NXT/ADD/UPD/DEL dispatch,
the GU/GHU/ISRT/REPL/DLET call sequencing (including the 2110 "GE → reposition with GT"
browse-repositioning logic), status-code EVALUATEs, and the CA-ERR-FLG/CA-ERR-MSG
finalize logic all convert faithfully. All 18 generated tests assert the COBOL-expected
outcome and are expected to (and do) PASS — this is a clean convert, not a gap.

## 3. Uncovered remainder (24 missed lines, ~11%)
Not covered by these tests (all pre-existing infrastructure edges, not OUIMSPA business
logic branches):
- `mainLine()` commarea plumbing edge cases in `AppService`-facing wrapper code (e.g.
  `Object[]` commarea variant with non-`byte[]` element at index 1, and the
  `eibcalen <= 0` / null-commarea skip path) — these are generic `AppProgram` framework
  glue shared across all converted programs, not OUIMSPA-specific logic.
- A few defensive branches in `TaskContext`/field accessor plumbing not exercised because
  every test drives a full `mainLine()` call with a valid EIBCALEN and byte[] commarea.

These are framework/glue lines outside the COBOL PROCEDURE DIVISION's business logic;
the paragraph-level branches (validate, dispatch, DL/I status EVALUATEs, finalize) are
fully covered.

## 4. Reviewer notes
- Pre-existing test file (`OuimspaServiceTest.java`) was already present, comprehensive,
  and matched COBOL ground truth on review — no rewrite was needed, only verification.
- Tests mock `AppService`/`DliService` only; no DB or Spring context involved, per the
  module's stateless per-request `TaskContext` design.
