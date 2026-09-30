# OUSTMIN — Unit Test Findings

## 1. Verify result

`mvn -q -pl back-end/programs/oustmin verify` — **BUILD SUCCESS**.
Tests run: **39, Failures: 0, Errors: 0, Skipped: 0** (1 verify run used, well within the 3-run budget).
The console log shows repeated `IllegalClassFormatException` / "Unsupported class file major version 70" traces from
JaCoCo trying (and failing harmlessly) to instrument JDK-internal `sun.nio.cs.ext.MS932*` classes on JDK 21 —
this is a known JaCoCo/JDK interaction, not a test failure; surefire and jacoco reports were both produced normally.

## 2. CONVERT-GAP tests

**None.** Line-by-line comparison of `OUSTMIN.cbl` against `OustminService.java` found no divergence: RESP-code
mapping (NORMAL/NOTFND=13/ENDFILE=20/OTHER) for STARTBR, READNEXT (page + peek) and ENDBR all match; the
row-cap clamp (1–6), the "ACCT"-only mode validation, the `KSB-STATUS` precedence rules (99 preserved, else
10/00 by row count), and — notably — the fact that COBOL's `5000-END-BROWSE` logs an ENDBR failure but
deliberately does **not** force `KSB-STATUS` to '99' are all faithfully reproduced in Java. A test
(`endStatementBrowse_errorResponse_writesDiagnosticButKeepsStatus`) locks in this exact (non-obvious) behavior
so a future "fix" that starts forcing status 99 there would correctly fail.

## 3. Uncovered remainder

Overall module C0 = 72.2% (231/320 lines), but `OustminService` itself — the actual converted COBOL business
logic — is **100% line-covered (0 of 143 lines missed)**. The gap is entirely infrastructure boilerplate:
- `OustminFields` (85 of 89 total missed lines): auto-generated typed getter/setter wrappers for ~40 copybook
  fields OUSTMIN never touches (e.g. `WS-ACCTFILE/BILLFILE/CARDFILE/...FILE`, `WS-ED-*`, `WS-SWITCHES`,
  `KSB-REQUEST/RESULT/ROW(S)` group accessors, `SQLCODE`, `COMPLETION-CODE`). These are shared-copybook
  pass-through one-liners with no branches; testing them would be assert-nothing getter/setter tests with
  no business meaning, so they were intentionally left uncovered per the "no meaningless tests" rule.
- `OustminBmsMetadata` (4 lines): `getMapNames()`, `getLayoutResource()`, `getFieldMapping()` are tooling/BMS
  metadata helpers never invoked by `OustminService` at all (only `getButtonDefs`/`registerFsetFields` are
  called, both covered).

## 4. Notes for reviewer

- `OustminService`'s public entry (`mainLine`) is thin CICS-commarea plumbing; the actual paragraphs
  (0000-MAIN … 9500-LOG-ERROR) are private. Tests invoke them via reflection on a real `TaskContext`
  (constructed with a mocked `AppService` + real `OustminFields`/`WorkingStorage`, which load the genuine
  `/layout/OUSTMIN_WS.xml`) — this exercises real field packing/unpacking instead of mocking it away.
- `pom.xml` was missing `spring-boot-starter-test` and the `jacoco-maven-plugin`; both were added (copied
  from the sibling `ocpwdch` module pattern) so `verify` and coverage reporting work at all.
