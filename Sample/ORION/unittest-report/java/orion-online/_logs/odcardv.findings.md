# ODCARDV — Unit Test Findings

## 1. Verify result

Final confirmed state: **14/14 tests passed**, 0 failures, 0 errors, 0 skipped
(`OdcardvServiceTest`). JaCoCo is configured; aggregate **C0 line coverage ≈ 47%**
(217 covered / 462 total lines across the module), below the 80% target.

## 2. CONVERT-GAP tests

**None.** Line-by-line comparison of `ODCARDV.cbl` against `OdcardvService.java`
found no behavioral divergence — every paragraph (0000-MAIN, 1000-SEND-INITIAL,
2000-PROCESS-INPUT, 2100-READ-AND-SHOW, 3000-READ-CARD, 4000-POPULATE-DETAIL,
7000-XCTL-MENU, 8000/8100/8500, 9000-RETURN) converts 1:1, including the
double-message-overwrite quirk in the SQL-error path (3000-READ-CARD sets
"Error reading card table." but 2100-READ-AND-SHOW's else-branch unconditionally
overwrites it with "Record not found." — verified as original COBOL behavior,
not a bug, and covered by
`mainLine_enterValidCardNumber_sqlErrorOtherCode_finalMessageIsNotFound`).

One near-miss worth recording: `CARDNUMO` and `CARDNUMI` share the same buffer
offset in the layout XML (`MCARDVAO redefines MCARDVAI`, per the BMS map). This
is correct BMS semantics (input/output symbolic maps genuinely overlap in
storage), not a convert gap — confirmed against `MCARDV.cpy`. The not-found test
asserts `CARDNUMO` mirrors whatever was typed into `CARDNUMI`, which is the
correct ground truth.

## 3. Uncovered remainder

- `OdcardvFields` (231/290 lines missed): auto-generated field accessor with
  ~140 typed getter/setter pairs; only the ~15 fields ODCARDV's business logic
  actually touches are exercised. Testing the rest would be reflection-style
  getter/setter testing with no business value (see skill's "test vô nghĩa"
  rule) — not attempted.
- `OdcardvDaoImpl` (0/22 lines covered): the JdbcTemplate wrapper is never
  exercised because `OdcardvServiceTest` mocks the `OdcardvDao` interface, not
  the impl. An attempt to add `OdcardvDaoImplTest` (mocking `JdbcTemplate`
  directly) was made but **Mockito's inline mock-maker cannot mock
  `JdbcTemplate` on this environment's JDK 26** (`Unsupported class file major
  version 70` / byte-buddy incompatibility) — an environment/toolchain
  limitation, not a code defect. The file was removed rather than leave a
  broken test in place, since the 3-verify budget was exhausted confirming this.
- `OdcardvBmsMetadata` (6/54 lines missed): unreachable branches in
  `getFieldMapping` for map names other than `MCARDVA`, and `getLayoutResource`/
  `getMapNames` (tooling helpers never called by the service).

## 4. Reviewer notes

- If JDK is downgraded to ≤21 or Mockito/byte-buddy is upgraded, re-attempt an
  `OdcardvDaoImplTest` to recover the DAO impl's 22 lines.
- No git commands were run; test file left on disk at
  `back-end/programs/odcardv/src/test/java/com/generated/orion/odcardv/service/OdcardvServiceTest.java`
  for review/commit by the user.
- `pom.xml` for `odcardv` was missing `spring-boot-starter-test` and the
  surefire/JaCoCo `<build>` block (present in sibling modules like `occardl`) —
  added both so `verify` and coverage reporting work.
