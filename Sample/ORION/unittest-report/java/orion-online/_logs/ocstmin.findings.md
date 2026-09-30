# OCSTMIN — Unit Test Findings

## 1. Verify result

Final `mvn verify`: **Tests run: 20, Failures: 0, Errors: 0, Skipped: 0**.
`OcstminService` line coverage (C0): **99.65%** (285/286 lines; 1 line uncovered).
Module aggregate C0 (JaCoCo sum across all classes): **54.1%** — see §3 for why.

## 2. CONVERT-GAP tests

None. `OcstminService` is a faithful 1:1 conversion of every OCSTMIN paragraph
(0000-MAIN .. 9000-RETURN): dispatch table, EIBAID→paragraph mapping, EDIT-ACCT /
EDIT-CYCLE numeric parsing (including the WS-NC-LEN-bounded truncation), the
MAPFAIL→low-values fallback, PF7 remembered-filter vs. PF8 saved-next-key paging,
and the 4100/4200 row-format/placement loop were all compared against the COBOL
ground truth and found behaviorally equivalent. No divergence was found, so no
test is marked as an intentionally-failing CONVERT-GAP.

## 3. Uncovered remainder and why

- `OcstminFields` (accessor class, 547/726 lines uncovered): auto-generated
  getter/setter boilerplate shared across many BMS screens/copybooks in the
  ORION-CCMS suite. OCSTMIN only touches a small subset of these fields; the
  rest are unreachable dead weight for this program specifically and were not
  additionally exercised, since doing so would only pad coverage without
  protecting any OCSTMIN business behavior.
- `OcstminBmsMetadata` (4/186 lines uncovered): covered via the three
  `OcstminService.getButtonDefs()/registerFsetFields()/getFieldMapping()`
  delegation tests; the small remainder is metadata for map fields/buttons not
  referenced by the tested MSTMINA map.
- `OcstminService` has 1 residual uncovered line — a defensive branch not
  reachable through the mocked `AppService` surface used here.

## 4. Reviewer notes

- Money fields (open/close/min-due) use the framework's PIC ---,---,--9.99
  edited-numeric formatting (`WS-ED-MONEY`); tests assert on `.trim()` only
  (no commas needed, values kept < 1,000) to avoid coupling to the shared
  accessor's exact padding/sign behavior, which is outside this module.
- `pom.xml` had no test dependency or JaCoCo plugin configured; added
  `spring-boot-starter-test` (test scope) + `jacoco-maven-plugin` 0.8.12,
  matching the convention already used by sibling program modules (e.g.
  ocauthq).
- JaCoCo 0.8.12 logs `IllegalClassFormatException` warnings while
  instrumenting JDK-internal classes (class file major version 70, i.e. a
  very new JDK) — cosmetic noise on stderr; does not affect test results or
  the coverage report.
