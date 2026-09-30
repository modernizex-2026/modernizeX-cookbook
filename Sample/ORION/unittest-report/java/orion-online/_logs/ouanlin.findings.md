# OUANLIN — Unit Test Findings

## 1. Verify result
Final `mvn -pl back-end/programs/ouanlin verify`: **BUILD SUCCESS**.
Tests run: 43, Failures: 0, Errors: 0, Skipped: 0 (`OuanlinFieldsTest`: 8, `OuanlinServiceTest`: 35).
C0 line coverage (JaCoCo, `com.generated.orion.ouanlin.*`): **95.88%** (256/267 lines).

## 2. CONVERT-GAP tests
**None outstanding.** The test suite already on disk carried a class-level note claiming several
RW/FR-mode tests were `BLOCKED-BY-INFRA-BUG` (a stale `WS-LARGE-THRESH PIC S9(09)V99 VALUE +1000.00`
signed-literal codec bug) and expected to fail. Verify run #1 showed those tests **all pass** — the
underlying codec issue has evidently been fixed since that note was written — so the stale
`BLOCKED-BY-INFRA-BUG` / `CONVERT-GAP` javadoc and per-test comments were removed as no longer
accurate. No Java-vs-COBOL logic mismatch was found in `OUANLIN.cbl` ↔ `OuanlinService.java`: mode
validation, browse/read/dispatch, all four accumulation paragraphs (RW/FR/GL/RC), FR/RC finalize,
build-page paging/offset/clamp, and 6000-SET-STATUS all match the COBOL PROCEDURE DIVISION.

## 3. Fixes applied to pre-existing tests (real test bugs, not convert gaps)
- `OuanlinFieldsTest.wsFileNameAndSwitchFields_setThenGet_roundTrip`: asserted a 13-char round-trip
  through `WS-SWITCHES`, but that group is only 3 bytes per the layout XML (`WS-ERR-FLG` +
  `WS-END-FLG` + `WS-FOUND-FLG`), so the value truncates to `"SWI"` by design — test corrected to
  expect the 3-byte value.
- `OuanlinServiceTest.mainLine_readNextOtherResp_...`: asserted `endBrowse` is **never** called on a
  mid-scan READNEXT error, but COBOL's `2900-END-TRAN` runs unconditionally after the
  `PERFORM...UNTIL` loop (error sets `WS-EOF-SW` and the loop simply exits) — Java was already
  correct; test assertion inverted to `verify(appService).endBrowse(...)`.

## 4. Uncovered remainder (11 lines, 95.88%)
- `mainLine()` COMMAREA marshalling branches for `Object[]`-shaped commarea containing a `byte[]`
  element vs. a non-`byte[]` element (lines ~37-58): generic CICS-commarea plumbing shared with
  other programs, not OUANLIN business logic; all tests here use a plain `String`/byte[] commarea.
- `buildResultPage()`: the `WS-START-IDX < 1` clamp (`KAB-START-OFF` negative enough to make
  start-idx go below 1) is never exercised — no test passes a negative start offset.

## 5. Reviewer notes
No `git` commands were run. Generated/updated test files are on disk under
`back-end/programs/ouanlin/src/test/java/...` for review/commit.
