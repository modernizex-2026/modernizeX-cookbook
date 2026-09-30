# OCUSRD — Unit Test Findings

## 1. Verify result
Final `mvn verify`: **19/19 tests passed**, 0 failures, 0 errors, 0 skipped.
`OcusrdService` line coverage: **145/147 = 98.6%** (jacoco). Module-wide C0 (all
classes summed) is 53%, dragged down by the auto-generated `OcusrdFields`
accessor — see §3.

## 2. CONVERT-GAP tests
None. `OcusrdService` is a faithful 1:1 conversion of `OCUSRD.cbl` — every
paragraph (0000-MAIN through 9000-RETURN) maps directly with matching
branch conditions, response-code handling, and literal message text
(cross-checked against `WMSG.cpy`, `WHEAD.cpy`, `RUSER.cpy`). No behavioral
divergence was found, so no test was written with an intentionally-failing
`// CONVERT-GAP` expectation.

One **COBOL quirk correctly reproduced** (not a convert bug, documented in
`mainLine_enterStageZeroReadFileError_showsNotFoundBecauseFetchUserOverwritesErrorMessage`):
in `3000-READ-USER`, the `OTHER` (file error) branch sets `ERR-FLG-ON` and
moves `'Error reading user file.'` to `ERRMSGO`, but never sets
`REC-FOUND`. Back in `2200-FETCH-USER`, `IF REC-FOUND` is still false, so
the `ELSE` branch unconditionally overwrites `ERRMSGO` with
`'User not found.'` — the read-error message is set then immediately
clobbered by COBOL's own logic. The Java conversion reproduces this via
`WS-FOUND-FLG` staying `"N"` for both NOTFND and OTHER read outcomes.

## 3. Uncovered remainder
- `OcusrdFields` (accessor): 58/274 lines covered (~21%). It exposes typed
  getter/setter wrappers for every field in the shared `ORION-COMMAREA` /
  copybook layout (`CA-ACCT-ID`, `CA-CARD-NUM`, `CURDATEA/F/I/L`, etc.), the
  large majority of which OCUSRD's business flow never touches. Per the
  skill's "meaningless test" guidance, getter/setter-only coverage was not
  added — it would not protect any business logic.
- `OcusrdService`: 2 lines uncovered — trivial fall-through branches not
  worth a dedicated stub (residual EIBRESP bookkeeping lines).

## 4. Reviewer notes
- pom.xml for `ocusrd` had no `jacoco-maven-plugin`/`spring-boot-starter-test`
  configured; added the same block used by sibling modules (`occusin`,
  `occrdin`, etc.) so `mvn verify` produces coverage — please review this
  pom diff along with the test class.
- No other issues; all business branches (stage 0/1 dialogue, PF3/PF4/ENTER,
  read/delete response-code handling, name-building) are exercised.
