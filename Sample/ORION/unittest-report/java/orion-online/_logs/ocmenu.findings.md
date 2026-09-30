# OCMENU — Unit Test Findings

## 1. Verify result

Final `mvn clean verify` (2nd of 3 allowed runs): **BUILD SUCCESS**.
Tests: **23 run / 23 passed / 0 failed / 0 skipped** (`OcmenuServiceTest`).
First verify run had 1 failing test (`mainLine_eibcalenZero_...`) due to a missing
`formatTime` stub causing an NPE in `populateHeader`; fixed in the same batch by
adding `givenFormatTimeStub()`, then the confirming verify passed clean.

## 2. CONVERT-GAP tests

**None.** OCMENU's Java service (`OcmenuService`) mirrors the COBOL source
1:1, paragraph by paragraph (0000-MAIN → 9000-RETURN):
- `CA-FIRST-ENTER` (KCOMM 88-level on `CA-PGM-CONTEXT` = 0) ↔ `caPgmContext == 0` check.
- `EIBAID` EVALUATE (DFHPF3="3", DFHENTER="'", OTHER) ↔ `fieldEquals` branches.
- `OPTIONI NUMERIC` 1–10 dispatch table (OCACCTV..OCRPTMN) ↔ Java `switch` — exact match.
- `WS-XCTL-PGM = SPACES` invalid-option guard, message literals
  ("Select an option (1-10)...", "Invalid option.", WS-MSG-INVALID-KEY from WMSG.cpy,
  WS-HDR-TITLE from WHEAD.cpy) — all verified against copybooks, all match.
- Dispatch branch (valid option) sets only `CA-FROM-PROGRAM` (not `CA-FROM-TRANID`),
  exactly as COBOL 2100-DISPATCH does (unlike sibling OCADMEN, which sets both) —
  Java correctly omits the TRANID assignment too.

No behavioral divergence found; no fail-on-purpose tests were needed.

## 3. Uncovered remainder

Overall module C0 = **45.1%** (169/375 lines), driven entirely by two
auto-generated, boilerplate classes with dozens of unused getters/setters for
COMMAREA/WS fields OCMENU never touches:
- `OcmenuFields` (accessor): 38/236 lines (16%) — only fields OCMENU actually
  reads/writes are exercised; the rest are dead for this program.
- `OcmenuBmsMetadata`: 32/38 lines (84%, mostly static init) — button-def/field-mapping
  accessor methods are covered indirectly via the metadata smoke tests.

`OcmenuService` itself (the actual business logic under test) is **97.8%**
line-covered (89/91), well above the 80% bar. Testing the remaining unused
accessor getters/setters directly would be meaningless (no behavior to break —
see skill's "test vô nghĩa" rule) and was intentionally skipped. This exact
profile (business-logic class ~98%, module-wide ~45–48%) also occurs in the
sibling `OCADMEN` module, confirming it's structural, not specific to this test suite.

## 4. Reviewer notes

- `ocmenu/pom.xml` was missing `spring-boot-starter-test` (test scope) and the
  JaCoCo plugin entirely — added both (copied from `ocadmen/pom.xml`) so the
  module could compile/run tests and produce coverage at all.
- Only 2 of 3 allowed verify runs were used.
