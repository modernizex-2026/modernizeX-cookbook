# OCUSRU — Unit Test Findings

## 1. Verify result

`mvn -pl back-end/programs/ocusru verify` — **BUILD SUCCESS**, 1 run used of the 3-run budget.
**Tests: 20 run / 20 passed / 0 failed / 0 skipped.**

`pom.xml` had no test dependency or JaCoCo/surefire config (unlike sibling `ocusrl`); added
`spring-boot-starter-test` (test scope), `maven-surefire-plugin` (`testFailureIgnore=true`) and
`jacoco-maven-plugin` 0.8.12 to match the project's other program modules.

## 2. CONVERT-GAP tests

None. Every COBOL paragraph (0000-MAIN..9000-RETURN) was compared line-by-line against
`OcusruService`, including the RESP mapping (NORMAL=0, NOTFND=13), the
`CA-FIRST-ENTER`/`CA-PGM-CONTEXT=0` "first enter" condition, and the `WS-SV-USERID`-as-update-key
rule in `3100-UPDATE-USER`/`updateUserRecord`. No behavioral divergence found — no test is
expected to fail.

One pre-existing **COBOL-inherent quirk** (not a conversion defect — Java reproduces it exactly)
is covered by `mainLine_enterPressed_readErrorOnFetch_stillShowsUserNotFoundPerCobolBehavior`:
`3000-READ-USER` sets `ERR-FLG-ON` + "Error reading user file." on any RESP other than
NORMAL/NOTFND, but `2200-FETCH-USER` only branches on `REC-FOUND`, so that error message is
silently overwritten by "User not found." This is documented via the test, not "fixed."

## 3. Uncovered remainder

Overall C0 = 248/531 = **46.7%** (`ΣLINE_COVERED/(ΣLINE_MISSED+ΣLINE_COVERED)`), driven almost
entirely by two boilerplate classes, not business logic:
- `OcusruFields` (accessor): 73/296 lines — ~220 unused generated getters/setters for fields not
  referenced by this program's flow (shared multi-program field superset).
- `OcusruBmsMetadata`: 0/54 lines — `registerFsetFields`/`getButtonDefs`/`getFieldMapping` are
  framework wiring invoked by the runtime, not by `mainLine`, so unit tests never touch them.

The actual business logic, `OcusruService`, is **165/171 lines ≈ 96.5%** covered — every
paragraph/branch has a test. This split matches the sibling module `ocusrl` exactly (same
accessor/metadata pattern, same ~40% overall / ~96% service-class split), so it reflects this
codebase's structural convention rather than a gap introduced here.

## 4. Reviewer notes

- `USERIDI`'s ignored-during-update behavior is explicitly asserted
  (`mainLine_updateStage_usesSavedUserIdAsKeyNotReceivedUserId`) since COBOL always keys the
  REWRITE off `WS-SV-USERID`, never the currently-displayed `USERIDI`.
- `pom.xml` was modified to add test infra (see §1) — please confirm this matches the intended
  Maven baseline for all `back-end/programs/*` modules.
