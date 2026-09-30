# OUMSG — Unit Test Findings

## 1. Verify result

`mvn -q -pl programs/oumsg verify`: **BUILD reaches JaCoCo report** (surefire
`testFailureIgnore=true` added locally to `programs/oumsg/pom.xml` so the
intentional CONVERT-GAP failures below don't abort before `verify` phase).

Tests run: **11**, Passed: **4**, Failed: **7** (all intentional CONVERT-GAP),
Errors: 0, Skipped: 0.

C0 line coverage (JaCoCo): `(9+11) / (18+1+9+11) = 20/39 = 51.3%` — below the
80% target, root cause explained in §3.

## 2. CONVERT-GAP — intentionally failing tests

`OumsgServiceTest.execute_knownMessageCode_returnsMappedTextAndOkStatus` (7
parameterized cases: I0001/E0001/E0002/E0003/E0004/E0005/W0001) fail on
purpose:

- **COBOL** (`OUMSG.cbl` 0000-MAIN): `EVALUATE KM-CODE WHEN 'I0001' ... END-EVALUATE`.
  `KM-CODE` is `PIC X(06)`; COBOL space-pads the 5-char literal to 6 bytes
  before comparing, so `'I0001 '` (stored value) equals `'I0001'` (literal) and
  the WHEN branch fires → `KM-STATUS = '00'`, `KM-TEXT` = mapped message.
- **Java** (`OumsgService.resolveKmStatusMessage`): `switch (String.valueOf(ws.getKmCode()))`
  compares the raw space-padded 6-byte field value against the unpadded 5-char
  literal (`"I0001"`). They are never equal, so **every** known code falls
  into `default` → `KM-STATUS = "01"`, `KM-TEXT` = the code itself echoed back.
- **Impact**: OUMSG's entire lookup table is dead — no caller ever gets a
  mapped message text or a "00" success status, for any of the 7 defined
  codes. This is a functional regression affecting the program's sole purpose.
- Tests keep the COBOL-ground-truth expectation (`status="00"`, mapped text)
  per the CONVERT-GAP convention; the failures are the detection, not a test bug.

## 3. Uncovered remainder

The 7 switch-case bodies (`ws.setKmText("Operation completed successfully.")`,
etc., `OumsgService.java` lines ~67-89) are **unreachable** given the bug in
§2: no input string can make `String.valueOf(ws.getKmCode())` equal an
unpadded literal, since `KM-CODE` is a fixed 6-byte field and any value
written into it is read back padded to 6 bytes. No test can cover these lines
without a source fix (out of scope for this test-writing pass) — hence the
51.3% C0 ceiling.

## 4. Reviewer notes

- Fix candidate: compare `ws.getKmCode().trim()` (or pad the switch literals)
  so the match logic mirrors COBOL's space-padding comparison semantics.
- Passing tests (4): unknown code, empty code, lowercase code (all correctly
  hit `WHEN OTHER` since they never matched any WHEN clause even before the
  bug), and completion-code-stays-zero on normal exit.
- No DB/file/screen dependencies exist in this module — no mocks were needed.
