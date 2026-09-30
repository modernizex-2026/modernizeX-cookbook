# OCTRANA — Unit Test Findings

## 1. Verify result

Final `mvn verify`: **Tests run: 31, Failures: 1 (intentional CONVERT-GAP), Errors: 0, Skipped: 0**.
The one failure is a documented, expected-to-fail CONVERT-GAP test (see below), not a
regression. `OctranaService` line coverage: 267/272 = **98.2%** (branch 93/100 = 93%).
Module-wide JaCoCo C0 (incl. generated `OctranaFields` accessor and `OctranaBmsMetadata`
wiring classes) is ~55%; see §3.

## 2. CONVERT-GAP test

**`mainLine_enterKey_amountHasEmbeddedSpaceAndComma_convertGapCommaNotStrippedByNumval`**

- **COBOL expects**: 7700-VALIDATE-AMT explicitly allows `,` as a grouping separator
  (`WHEN WS-VAL-CH = ',' CONTINUE`), and `FUNCTION NUMVAL` strips grouping commas before
  conversion, so an amount like `"1,234.56"` is valid and yields `WS-AMT-NUM = 1234.56`.
- **Java does**: `validateAmountField` also accepts the comma character (no edit failure),
  but the numeric conversion is delegated to the shared `Utility.parseNumeric()`, which
  calls `new BigDecimal(norm)` directly. `BigDecimal` rejects the comma with
  `NumberFormatException`, which `parseNumeric` silently catches and returns `ZERO`.
  `WS-AMT-NUM` ends up `0.00`, so 5400-EDIT-AMT's own `<= 0` check then (incorrectly)
  rejects the transaction with "Amount must be greater than zero" instead of accepting it.
- This is a **shared-infrastructure bug** in `Utility.parseNumeric` (orion-common), not
  specific to OCTRANA, but OCTRANA is where it is exercised/visible. Reviewer should check
  whether other programs that accept comma-formatted amounts are silently affected too.

## 3. Uncovered remainder

- `OctranaBmsMetadata` (58 lines) and most of `OctranaFields` (278 of 416 lines) are
  generated screen-metadata/field-accessor boilerplate (per-field getter/setter wrappers,
  BMS button/field-set registration) not exercised by `OctranaService` business logic —
  same pattern as the existing `OcacctvServiceTest` baseline (golden reference for this
  program), whose module-wide C0 is ~36%. Not pursued further given the 3-verify budget;
  these classes have no business branches to protect.
- `OctranaService.getButtonDefs/registerFsetFields/getFieldMapping` (screen-wiring
  passthroughs, lines 58/63/64/68) are untested for the same reason (mirrors baseline).

## 4. Reviewer notes

- Test file: `back-end/programs/octrana/src/test/java/.../OctranaServiceTest.java`.
- `pom.xml` for `octrana` was missing the `spring-boot-starter-test` test dependency and
  the surefire/jacoco plugin config present in sibling modules (e.g. `ocacctv`) — added
  both so `mvn verify` and coverage reporting work; no other module code was touched.
