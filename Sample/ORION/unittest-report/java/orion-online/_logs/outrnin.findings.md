# OUTRNIN — Unit Test Findings

## 1. Verify result
Final `mvn -pl back-end/programs/outrnin verify`: **30 tests run, 29 passed, 1 intentional failure**
(the CONVERT-GAP test below), 0 errors, 0 skipped. `OutrninService` (business logic) line
coverage: **99.4%**. Module-wide C0: **73.6%** (see §3).

## 2. CONVERT-GAP (intentional failure)
**Test:** `mainLine_categoryBelow1000_CONVERT_GAP_tycatKeepsLeadingZerosInCobol`

COBOL `2600-LOOKUP-DESC` builds `WS-TYCAT` (PIC X(07)) via
`STRING TR-TYPE-CD '/' WS-CAT-ED`. `WS-CAT-ED` is `PIC 9(04)` DISPLAY, whose in-memory
character form is always 4 zero-padded digits (e.g. category `7` → `"0007"`), and `STRING`
copies those characters verbatim — COBOL produces `"PU/0007"` (exactly 7 chars, matching
`KTR-TYCAT`'s width).

Java (`OutrninService.lookupDescription`) instead does
`String.valueOf(trTypeCd) + "/" + String.valueOf(getWsCatEd())` where `getWsCatEd()` is an
`int`. `String.valueOf(7)` yields `"7"`, not `"0007"` — Java produces `"PU/7"`. Any category
code below 1000 loses its leading zeros in the displayed `KTR-TYCAT` column. Test uses the
COBOL-correct expected value (`"PU/0007"`) and fails against current Java by design.

## 3. Uncovered remainder
- `OutrninFields` (auto-generated field accessor, 103 missed / 218 lines): almost entirely
  typed wrappers for shared WCONST-copybook fields OUTRNIN never touches at runtime
  (`WS-ACCTFILE`, `WS-BILLFILE`, `WS-CARDFILE`, `WS-CUSTFILE`, `WS-DGRPFILE`, `WS-USRSEC`,
  `WS-STMTFILE`, `WS-CTRLFILE`, `WS-XREFFILE`, `WS-EDIT`/`WS-ED-*`, etc.) plus `SQLCODE`/
  `COMPLETION-CODE`/`FILLER` scaffolding. Exercising these directly would be reflection-style
  getter/setter tests with no behavioral value (skill guidance explicitly excludes this).
- `OutrninBmsMetadata` (1 missed line): trivial pass-through not reached by any code path
  under test; low value to chase further.
- `OutrninService` itself: 1 missed line / 10 missed branches out of the full paragraph set —
  essentially every COBOL paragraph (0000-MAIN through 3000-FINALIZE) is exercised, including
  all 5 filter branches (C/D/M/T/A), the unknown-filter no-match path, MAX-ROWS(6) paging cap,
  resume-key skip-but-accumulate behavior, TCAT→TTYP→UNKNOWN description fallback chain, and
  both STARTBR/READNEXT RESP error paths (with/without ENDBR still firing).

## 4. Reviewer notes
- `back-end/programs/outrnin/pom.xml` was missing `spring-boot-starter-test` and the
  surefire/JaCoCo build plugins entirely (present in sibling modules like `ouactin`) — added
  them so tests could compile and JaCoCo could run. No other module files were changed.
- JaCoCo 0.8.12 logs harmless `IllegalClassFormatException` stack traces to stderr while
  instrumenting JDK locale-provider classes (class file major version 70, i.e. a newer JDK
  than JaCoCo 0.8.12 fully supports); this does not fail the build or affect the reported
  coverage numbers for this module's own classes.
