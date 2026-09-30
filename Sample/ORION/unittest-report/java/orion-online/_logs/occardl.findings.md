# OCCARDL — Unit Test Findings

## 1. Final verify result

`mvn -pl back-end/programs/occardl verify`: **25 tests run, 22 passed, 3 intentionally-failing (CONVERT-GAP), 0 errors.**
`OccardlService` line coverage (JaCoCo): 205/211 = **~97%**. Module-wide aggregate C0 (including the
auto-generated `OccardlFields` accessor class) is ~54.5% — see §3.

`back-end/programs/occardl/pom.xml` was missing test dependencies (`spring-boot-starter-test`) and the
JaCoCo plugin entirely; both were added (copied from sibling module `ocacctl`) so the module can run tests
and produce coverage at all.

## 2. CONVERT-GAP tests (fail on purpose — flag real Java bugs)

- **`mainLine_enterFullPage_convertGap_resumeCardShouldBeLastCardReadNotStartKey`**
  COBOL's `EXEC CICS READNEXT RIDFLD(WS-BROWSE-KEY)` is bidirectional — CICS writes the key of the record
  just read back into `WS-BROWSE-KEY`, which `3100-READ-NEXT` copies into `WS-RESUME-CARD` on every
  successful read. The converted `AppService.readNext(fileName, into)` has no RIDFLD out-parameter, so
  `readNextCard()` does `wsResumeCard = wsBrowseKey`, where `wsBrowseKey` is set once in
  `startCardBrowse()` and never advanced. Result: the saved resume key stays frozen at the browse's
  starting key instead of the last card actually read — **forward paging (PF8) is broken** for any
  non-trivial listing. This is the most important finding in this run.

- **`mainLine_startBrowseUnexpectedError_convertGap_sendTextShouldCarryMessageText`**
  COBOL's `9500-ABEND-RTN` sends `WS-MSG-TEXT` (the literal error message). The converted
  `abendWithFileError()` calls `appService.sendText(String.valueOf(ctx.f), true, true)`, which stringifies
  the whole `OccardlFields` accessor object (default `Object.toString()`) instead of
  `ctx.f.getWsMsgText()`. The terminal would show a Java object reference, not the error message.

- **`mainLine_startBrowseUnexpectedError_convertGap_returnTransidShouldNotFollowAbend`**
  COBOL's `9500-ABEND-RTN` ends with a bare `EXEC CICS RETURN` (no TRANSID/COMMAREA), terminating the task
  immediately — `9000-RETURN` is never reached. `abendWithFileError()` calls `appService.returnProgram()`,
  but that call does not unwind the Java call stack, so `runMainProgram()` falls through and
  `returnTransid()` fires anyway. Same root cause/pattern as previously found in sibling program OCCARDA.

## 3. Uncovered remainder + why

The module-wide JaCoCo aggregate is dragged down by `OccardlFields` (307 lines missed / 402 total): this is
an auto-generated delegate-getter/setter class covering every field from ALL copybooks referenced by
OCCARDL's working storage (`WCONST`, `WMSG`, `WHEAD`, `KCOMM`, `RCARD`, `MCARDL`), most of which OCCARDL's
business logic never touches (e.g. `WS-STMTFILE`, `CA-USER-ID`, `WS-TCATFILE`). Writing tests to call each
unused accessor directly would be reflection-style getter/setter testing with no behavior to verify — the
skill's own anti-pattern guidance rules this out. `OccardlBmsMetadata` (92% covered) is likewise
mostly static generated metadata already exercised via the two `getButtonDefs`/`getFieldMapping` tests.

## 4. Reviewer notes

- The gap in §2 (resume key never advances) likely affects every browse-based CICS program converted with
  this codegen pattern (READNEXT without RIDFLD echo) — worth a systemic fix in `AppService`/`AppRunner`
  rather than a per-program patch.
- `pom.xml` changes (test deps + JaCoCo) are a prerequisite fix, not a test-content change — please review
  separately from the test file itself.
