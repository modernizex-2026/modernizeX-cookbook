# OCACCTL — Unit Test Findings

## 1. Verify result

`mvn -q -pl back-end/programs/ocacctl -am verify`: **Tests run: 19, Failures: 1 (intentional), Errors: 0, Skipped: 0.**
C0 line coverage (JaCoCo): 197/203 = **97.0%** — well above the 80% bar.

## 2. Intentional CONVERT-GAP failure

**`mainLine_enterNumericStart_convertGap_startBrowseKeyShouldBeZeroPadded`** (fails on purpose):

- **COBOL ground truth** (3100-START-BROWSE): `MOVE WS-PAGE-START TO AC-ID` then `EXEC CICS STARTBR RIDFLD(AC-ID)`. `AC-ID` is `PIC 9(11)`, so the browse key is always the fixed-width, zero-padded 11-digit form (e.g. `"00000000100"`), used identically for STARTBR and every subsequent READNEXT.
- **Java** (`startAccountBrowse`, `OcacctlService.java:231`): `appService.startBrowse(ctx.f.getWsAcctfile(), String.valueOf(ctx.f.getAcId()), 0)` — `String.valueOf(long)` produces the *unpadded* decimal string (e.g. `"100"`), not the fixed-width key.
- **Impact**: against any real fixed-width-keyed file/index, `GTEQ` positioning on an unpadded key can behave differently than on the zero-padded key COBOL sends (e.g. lexical vs numeric key comparison mismatches). Purely mock-based tests can't reveal this since the mock doesn't enforce key format, so the gap is asserted directly via `ArgumentCaptor` in this one dedicated test; all other tests that exercise the same code path use `anyString()` for the RIDFLD argument to avoid unrelated noise.

## 3. Uncovered remainder (6 lines missed)

Residual misses are in defensive/formatting branches not central to business logic:
- `getButtonDefs()` / `registerFsetFields()` / `getFieldMapping()` delegate straight to `OcacctlBmsMetadata` (metadata plumbing, not decision logic) — not separately unit-tested.
- A couple of `ctx.f.setWsRespCd(...)` bookkeeping lines after `endBrowse`/`xctl`/`askTime` calls whose value is written but never branched on afterward.

## 4. Notes for reviewer

- The "browse file error → still shows *No accounts found*" behavior (tests `mainLine_enterBrowseFileError_finalMessageIsNoneFoundPerCobolOverwrite` and `mainLine_enterReadNextError_stopsBrowseButKeepsRowsAlreadyRead`) is **original COBOL behavior** (2300-BUILD-SUMMARY unconditionally overwrites `ERRMSGO` based on row count), not a convert gap — verified against source.
- Balance field (`ACBnO`) assertions intentionally check only "non-blank", not exact formatting, since `WS-ED-BAL`'s edited-picture rendering (`-,---,---,--9.99`) is handled by shared runtime infrastructure outside this module's business logic.
