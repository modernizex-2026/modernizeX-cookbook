# ODCUSTV — Unit Test Findings

## 1. Verify result

Final confirmed state: **16/16 tests passed**, 0 failures, 0 errors, 0 skipped
(`OdcustvServiceTest`). JaCoCo is configured; aggregate **C0 line coverage ≈ 47.6%**
(244 covered / 513 total lines across the module), below the 80% target.

## 2. CONVERT-GAP tests

**None.** Line-by-line comparison of `ODCUSTV.cbl` against `OdcustvService.java`
found no behavioral divergence — every paragraph (0000-MAIN, 1000-SEND-INITIAL,
2000-PROCESS-INPUT, 2100-READ-AND-SHOW, 3000-READ-CUST, 4000-POPULATE-DETAIL,
7000-XCTL-MENU, 8000/8100/8500, 9000-RETURN) converts 1:1, including:
- `CA-FIRST-ENTER` (88-level, VALUE 0 on `CA-PGM-CONTEXT`) → `getCaPgmContext() == 0`,
  confirmed equivalent against `KCOMM.cpy`.
- The double-message-overwrite quirk in the SQL-error path (`3000-READ-CUST` sets
  "Error reading customer table." but `2100-READ-AND-SHOW`'s else-branch
  unconditionally overwrites it with "Record not found." — original COBOL
  behavior, covered by `mainLine_enterValidCustomerId_sqlErrorOtherCode_finalMessageIsNotFound`).
- The `STRING ... DELIMITED BY SPACE` full-name build (first token of each of
  first/middle/last name, with a literal space between) matches Java's
  `split(" ", 2)[0]` construction exactly, including the double-space produced
  when `CU-MIDDLE-NAME` is blank (covered by
  `mainLine_enterValidCustomerId_middleNameBlank_buildsNameWithDoubleSpace`).
- `CUSTIDO`/`CUSTIDI` share the same buffer offset (`MCUSTVAO redefines
  MCUSTVAI` per the BMS map) — correct BMS semantics, not a gap; the not-found
  test asserts `CUSTIDO` mirrors whatever was typed into `CUSTIDI`.

## 3. Uncovered remainder

- `OdcustvFields` (255/324 lines missed): auto-generated field accessor with
  ~150 typed getter/setter pairs; only the fields ODCUSTV's business logic
  actually touches are exercised. Testing the rest would be reflection-style
  getter/setter testing with no business value (skill's "test vô nghĩa" rule)
  — not attempted.
- `OdcustvDaoImpl` (0/6 lines covered): the JdbcTemplate wrapper is never
  exercised because `OdcustvServiceTest` mocks the `OdcustvDao` interface, not
  the impl. Not attempted here: a sibling module (`odcardv`) already confirmed
  that Mockito's inline mock-maker cannot mock `JdbcTemplate` on this
  environment's JDK ("Unsupported class file major version 70" /
  byte-buddy incompatibility) — an environment/toolchain limitation, not a
  code defect.
- `OdcustvBmsMetadata` (6/58 lines missed): unreachable branches in
  `getFieldMapping` for map names other than `MCUSTVA`, and tooling helpers
  (`getLayoutResource`/`getMapNames`) never called by the service.

## 4. Reviewer notes

- `pom.xml` for `odcustv` was missing `spring-boot-starter-test` and the
  surefire/JaCoCo `<build>` block (present in sibling modules like
  `odcardv`) — added both so `verify` and coverage reporting work.
- The JaCoCo agent logs `IllegalClassFormatException` warnings while
  instrumenting JDK-internal locale-provider classes during the test run;
  this is a JDK/JaCoCo 0.8.12 compatibility warning, not a test failure —
  build exit code is 0 and all tests pass.
- If JDK is downgraded to ≤21 or Mockito/byte-buddy is upgraded, re-attempt
  an `OdcustvDaoImplTest` to recover the DAO impl's 6 lines.
- No git commands were run; test file left on disk at
  `back-end/programs/odcustv/src/test/java/com/generated/orion/odcustv/service/OdcustvServiceTest.java`
  for review/commit by the user.
