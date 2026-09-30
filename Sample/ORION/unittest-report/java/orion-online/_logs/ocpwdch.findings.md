# OCPWDCH — Unit Test Findings

## 1. Verify result

Single `mvn -pl back-end/programs/ocpwdch verify` run (1 of 3 budget used): **18/18 tests passed**,
0 failures, 0 errors, 0 skipped. `OcpwdchService` (the actual business logic, all COBOL paragraphs
0000-MAIN → 9000-RETURN) reaches **100% line coverage** (87/87 lines), and `TaskContext` /
`WorkingStorage` are also 100% covered.

## 2. CONVERT-GAP tests

None. `OcpwdchService` mirrors OCPWDCH.cbl 1:1 paragraph-by-paragraph (EVALUATE EIBAID dispatch,
WS-FOUND-FLG lookup via 3000-READ-USER, password/confirm compare, CICS RESP handling for
READ UPDATE / REWRITE). No behavioral divergence was found against the COBOL ground truth, so no
test was written to fail on purpose.

## 3. Uncovered remainder

Overall module C0 = 45.37% (191/421 lines), driven entirely by `OcpwdchFields`
(auto-generated accessor with 291 typed getter/setter wrappers covering the *entire* WS layout —
e.g. `WS-AMT`, `CA-ACCT-ID`, `WS-CDT-*`), of which OCPWDCH's password-change screen only exercises
a few dozen. Only 47/274 accessor lines are hit; the rest are dead for this screen by design (the
accessor is shared boilerplate generated from the layout XML, not OCPWDCH-specific code).
`OcpwdchBmsMetadata` is 94% covered (3 lines uncovered, same shape as sibling modules).
This matches the precedent already on disk for `occustu` (same generator, same accessor pattern,
overall C0 ≈ 60%, also below the 80% floor for the identical reason) — not a gap introduced by
this test suite.

## 4. Notes for reviewer

- `pom.xml` for `ocpwdch` was missing `spring-boot-starter-test` and the surefire/JaCoCo plugin
  config that sibling program modules (e.g. `occustu`) already have; added both so `verify` and
  coverage reporting work. No other module files were touched.
- If 80% module-wide C0 is a hard gate, the fix is either to exclude the generated `accessor`
  package from JaCoCo scope, or to accept service-level coverage as the real signal — not to add
  tests that call unrelated accessor getters/setters just to move the number.
