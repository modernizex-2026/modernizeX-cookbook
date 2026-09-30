# OCTSRCH — Unit Test Findings

## 1. Final verify result
`mvn verify` (2nd run, after fixing 2 wrong test expectations from the 1st run):
**Tests run: 26, Failures: 0, Errors: 0, Skipped: 0.**
JaCoCo line coverage: `OctsrchService` (business logic) = 221/225 lines = **98.2% C0**.
Module-wide C0 (incl. generated `OctsrchFields` accessor) = 397/695 = **57.1%** — see §3.

## 2. CONVERT-GAP findings
None. The Java port of OCTSRCH's paragraphs (0000-MAIN through 9000-RETURN, including
5100-PARSE-AMOUNT/5150-ACCUM-DIGIT) faithfully reproduces the COBOL branch structure,
including one COBOL quirk worth flagging to reviewers (not a conversion defect):

**2200-BUILD-SUMMARY unconditionally overwrites ERRMSGO from WS-ROW-CNT alone**, even when
3100-START-BROWSE or 3200-READ-NEXT already set ERRMSGO to `WS-M-BROWSE-ERR` on a CICS
transport error. Since a browse error leaves WS-ROW-CNT at zero, the "No transactions in
that range" message always clobbers the "Error browsing the transaction file" message — the
operator never actually sees the browse-error text in the original COBOL either. Verified via
`mainLine_startBrowseTransportError_zeroRowsSoNoneFoundMessageWins` and
`mainLine_readNextTransportError_zeroRowsSoNoneFoundMessageWinsAndLoopStops`, both asserting
COBOL ground truth (NONE_FOUND wins). My first draft of these two tests wrongly expected the
browse-error message and failed the 1st verify run; fixed by re-reading 2100-SEARCH/2200
sequencing, not by touching production code.

## 3. Uncovered remainder
- `OctsrchFields` (auto-generated field accessor, 394 lines): 288 lines uncovered. These are
  typed getter/setter pairs for copybook fields (CA-ACCT-ID, CA-CUST-ID, WS-ACCTFILE,
  WS-BILLFILE, WS-CDT-*, WS-STMTFILE, etc.) that OCTSRCH's paragraphs never reference — they
  exist only because the accessor is generated from shared copybooks (WCONST/WMSG/WHEAD/KCOMM).
  Exercising them would not protect any OCTSRCH business logic, so they were left untested.
- `OctsrchBmsMetadata`: 6/66 lines uncovered — minor metadata branches not reached by the
  button-def/field-mapping smoke tests; low risk, no business logic.
- `OctsrchService`: 4/225 lines uncovered — residual defensive branches, negligible.

## 4. Reviewer notes
- Module `pom.xml` was missing `spring-boot-starter-test` and the JaCoCo/Surefire plugin block
  present in sibling modules (e.g. `occusin`); added both so `test-compile`/`verify` work and
  coverage reports generate. This is a build-config fix, not a business-logic change.
- JaCoCo logs (harmless) `IllegalClassFormatException` on JDK's `sun.nio.cs.ext.MS932`
  charset classes during agent instrumentation (JDK/JaCoCo version interaction); does not
  affect test results or the coverage numbers reported above.
