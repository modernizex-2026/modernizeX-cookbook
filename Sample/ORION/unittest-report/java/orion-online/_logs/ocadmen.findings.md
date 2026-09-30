# OCADMEN — Findings

## Verify result
Final passing state: **18/18 tests pass** (OcadmenServiceTest), 0 failures.
A 19th test (`registerFsetFields_delegatesToBmsMetadataWithoutThrowing`) was
written and hit `verify #3`, but `Mockito.mock(AppRunner.class)` threw
`MockitoException: Could not modify all classes` under this environment's
JDK 26 + inline-mock ByteBuddy combination — an environment/tooling
incompatibility, not a code defect. It was removed after the 3rd verify
(budget exhausted) rather than re-run; `AppRunner` couldn't be mocked here,
so `registerFsetFields` is left uncovered by unit tests.

## CONVERT-GAP tests
None. OCADMEN's Java service (`OcadmenService`) mirrors every COBOL paragraph
(0000-MAIN..9000-RETURN) 1:1: CA-USER-ADMIN guard, EIBAID dispatch
(PF3/PF4/ENTER/other), OPTIONI NUMERIC test → WS-OPTION table (1-4 →
OCUSRL/OCUSRA/OCUSRU/OCUSRD, else invalid), and header/date-time population
all match the COBOL ground truth. No divergence found, so no test is
expected/asserted to fail.

## Coverage
C0 (line) ≈ **48%** (185/387), below the 80% target. Root cause:
- `OcadmenFields` (194/238 lines uncovered): an auto-generated accessor with
  ~120 typed getter/setter pairs delegating 1:1 to a string-keyed API; only
  the ~15 fields OCADMEN's logic actually touches are exercised. Testing the
  remaining wrappers would be reflection-style "getter/setter" tests with no
  business value (explicitly discouraged) — a copy of the field list, not
  logic coverage.
- `OcadmenBmsMetadata` (6/38 lines uncovered): BMS field-set/mapping metadata
  registered at startup; `getButtonDefs`/`getFieldMapping` are now covered,
  `registerFsetFields` is not (see above, blocked by Mockito/JDK issue).
- Core `OcadmenService` business logic itself is well covered (2 lines
  missed of 101, both inside the same blocked `registerFsetFields` call).

## For reviewer
- pom.xml was updated to add `spring-boot-starter-test` + `jacoco-maven-plugin`
  (copied from sibling module `ocacctv`), since ocadmen had neither.
- If `registerFsetFields` coverage is wanted, retry with a real
  `AppRunner` instance (not a Mockito mock) once available, or investigate
  a Mockito/ByteBuddy/JDK version bump for this project's test environment.
