# OCREPT — Unit Test Findings

## 1. Verify result

Final `mvn verify` (2 of 3 budgeted runs, both green): **21/21 tests passed, 0 failures, 0 errors, 0 skipped.**
`OcreptService` line coverage (JaCoCo): **206/212 lines = 97.2% (C0)**.

## 2. CONVERT-GAP tests

None. `OcreptService.java` mirrors every COBOL paragraph (`0000-MAIN` .. `9000-RETURN`) 1:1 — dispatch keys,
validation order/messages, STARTBR/READNEXT/ENDBR response-code handling, and date-range accumulation all
match `OCREPT.cbl` exactly. No intentionally-failing test was needed.

## 3. Uncovered remainder

- **6 lines in `OcreptService`** (of 212): the trivial metadata delegates `getButtonDefs()`,
  `registerFsetFields()`, `getFieldMapping()` — one-line pass-throughs to `OcreptBmsMetadata`, not part of
  the COBOL business logic, left untested (low value per anti-vacuous-test guidance) — plus the dead
  `else` branch in `_2100RunReport` ("Unsupported report type.") which mirrors COBOL's own unreachable
  `WHEN OTHER` in the `EVALUATE TRUE` (report type is already restricted to 01/02 by `2120-VALIDATE-TYPE`
  before this switch runs), so it cannot be hit without bypassing validation.
- **Module-wide JaCoCo sum is diluted to ~43%** by two generated classes outside this review's scope:
  `OcreptFields` (accessor with hundreds of typed getter/setters for fields OCREPT never touches) and
  `OcreptBmsMetadata` (static BMS map/button registration, exercised only by integration/smoke tests, not
  unit tests). This is expected and consistent with sibling program modules already reviewed (e.g. ocdgrp).

## 4. Reviewer notes

- Tests drive the service exclusively through `mainLine(AppService)` with a mocked `AppService`; `OcreptFields`
  is used as a **real** object (backed by a real `WorkingStorage` buffer) since it's the record-buffer engine,
  not a collaborator to mock — `doAnswer` callbacks read/write fields directly on the instance passed by the
  service.
- `OUDATE` (`callProgram`) is stubbed by rewriting the last 2 bytes (`KD-STATUS`) of the 26-byte `KDATE-PARM`
  string passed by reference, matching the COBOL `CALL 'OUDATE' USING KDATE-PARM` contract.
- Added `spring-boot-starter-test` (test scope) + `maven-surefire-plugin`/`jacoco-maven-plugin` to
  `ocrept/pom.xml`, matching the config already present in sibling program modules.
