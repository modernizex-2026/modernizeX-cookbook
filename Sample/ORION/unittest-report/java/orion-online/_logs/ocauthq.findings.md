# OCAUTHQ unit test findings

## 1. Verify result
Single `mvn verify` run (1 of 3 budget): **BUILD SUCCESS**, 15/15 tests passed, 0 failures, 0 errors, 0 skipped.
`pom.xml` was missing `spring-boot-starter-test` and the `jacoco-maven-plugin` (present in sibling modules) — added both, mirroring the OCACCTV golden reference module, so the tests could compile and JaCoCo could run.

## 2. CONVERT-GAP tests
None. `OcauthqService` mirrors every COBOL paragraph (0000-MAIN..9000-RETURN) 1:1, including:
- `AS-ERRORED` 88-level literal (`'ERROR   '`, 8 chars) reproduced verbatim on LINK failure.
- `WS-ED-BAL(2:15) → AVAILO` reference modification reproduced as `padRight(...,16).substring(1,16)`.
- SPACES/LOW-VALUES validation order (card → amount → amount>0) matches `2200-VALIDATE-INPUT` exactly.

No intentionally-failing test was needed; all 15 tests assert COBOL-derived expected values and pass.

## 3. Uncovered remainder
- `OcauthqService` line coverage: 121/125 (96.8%). The only 4 missed lines are one-line delegations to `OcauthqBmsMetadata` (`getButtonDefs`, `registerFsetFields`, `getFieldMapping`) — framework/BMS-metadata plumbing invoked by the runtime dispatcher, not by `mainLine`, and outside COBOL PROCEDURE DIVISION scope. Not covered because exercising them requires constructing a full `AppRunner` (needs `Map<String,AppProgram>`/`Map<String,FileDao>`), out of proportion to the value for pure pass-through code.
- `OcauthqFields` (generated field accessor, ~245 typed getter/setter wrappers) and `OcauthqBmsMetadata` (BMS layout/button metadata) are largely uncovered — same pattern as the OCACCTV golden reference sibling module. These are boilerplate delegates to `DynamicFieldAccessor`/static metadata tables, not business logic; per skill guidance, testing getters/setters directly is discouraged as meaningless. Module-wide C0 (all classes) is ~41%, dragged down entirely by this generated code, consistent with OCACCTV's own ~36%.

## 4. Reviewer notes
- Test file: `back-end/programs/ocauthq/src/test/java/com/generated/orion/ocauthq/service/OcauthqServiceTest.java`.
- `pom.xml` was edited to add test dependencies/JaCoCo — please review before commit (no git actions were taken).
