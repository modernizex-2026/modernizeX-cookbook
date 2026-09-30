# OCPAUIN — write-unit-test findings

## 1. Verify result
Final `mvn verify` (2nd of 2 used): **Tests run: 21, Failures: 0, Errors: 0, Skipped: 0**.
`OcpauinServiceTest` covers all paragraphs (0000-MAIN, 1000/2000/2100/2200/3000/4000/4100/
7000/8000/8100/8500/9000) plus trivial metadata accessors.

## 2. CONVERT-GAP tests
**None.** OCPAUIN.cbl was compared paragraph-by-paragraph against OcpauinService.java
(EIBAID dispatch, CA-FIRST-ENTER via CA-PGM-CONTEXT=0, AUTHIDI spaces/low-values check,
WS-PAU-LINK ↔ CA-WORK-AREA round-trip protocol, PA-ACCT-ID zero-padding, PA-STATUS →
status-word EVALUATE). No behavioral divergence was found — this module converted
faithfully, so every test asserts the real (matching) COBOL-derived expected value and
all 21 pass. First verify run failed only on two environment/test-authoring issues (both
fixed, no CONVERT-GAP): the pom was missing `spring-boot-starter-test` + JaCoCo config
(added, copied from sibling `ocactin`), and a browse-next test needed `WS-PL-KEY` stubbed
(the COBOL does `MOVE WS-PL-KEY TO AUTHIDO` after populate-detail, overwriting whatever
populate-detail set from PA-AUTH-ID).

## 3. Coverage
JaCoCo `jacoco.csv` (module-wide sum): C0 = 225/487 = **46.2%** — below the 80% target,
but the shortfall is entirely in the auto-generated `OcpauinFields` accessor class
(262/346 lines missed: dozens of typed getter/setter wrappers for fields never touched
by OCPAUIN's actual business logic, e.g. all the `*a`/`*f`/`*l` attribute/flag/length
variants of screen fields). Per the skill's own rule ("test getter/setter via reflection
→ replace with behavior verification"), padding coverage by calling unused accessor
methods directly would be a meaningless test, so it was not done.
**`OcpauinService` itself (the actual converted business logic): 131/131 lines (100%),
26/27 branches (96%)** — effectively complete. `TaskContext` and `WorkingStorage`: 100%.

## 4. Reviewer notes
- pom.xml for `ocpauin` was modified (test dependency + JaCoCo plugin block added) to
  match the pattern already used by other `back-end/programs/*` modules — needed just to
  make `mvn test`/`verify` runable at all for this module.
- The single missed branch in `OcpauinService` is in `processInput`'s EIBAID dispatch
  chain (one of the `else if` conditions) — a fifth targeted test could close it if
  100% branch coverage is required, but was not pursued to stay within the verify budget.
- No git commands were run; test file and pom change are left on disk for review.
