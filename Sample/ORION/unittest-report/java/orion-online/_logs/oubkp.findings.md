# OUBKP unit test findings

## 1. Verify result
`mvn -pl back-end/programs/oubkp verify` — BUILD SUCCESS on the 2nd of 3 budgeted runs
(1st run caught two matcher bugs in the tests themselves, not in the service).
Tests: 15 run, 0 failures, 0 errors, 0 skipped.
JaCoCo C0 (line): (126+5) / (126+5+12+0) = **91.6%** — above the 80% floor.

## 2. CONVERT-GAP tests
None. OubkpService is a faithful line-by-line port of OUBKP.cbl: every EVALUATE branch in
2000-POSITION (NORMAL/NOTFND/ENDFILE/OTHER), 3100-READ-TRAN, 3400-WRITE-BKP-REC,
3500-ACCUMULATE, 4000-PEEK-NEXT and 6000-SET-STATUS maps 1:1 to the Java `if/else` chains,
including the subtle behavior that a mid-scan READNEXT error's "TRANFILE READNEXT FAILED"
message gets silently overwritten by 6000-SET-STATUS's "TRANSACTION BACKUP COMPLETE" once
KBK-STATUS stays off '99' — this is covered by
`mainLine_readNextOtherRespMidScan_setsEofIncrementsErrorsButStatusStaysComplete` and is
COBOL-faithful (not a bug), so no test was written to intentionally fail.

## 3. Uncovered remainder (12 lines missed)
- `OubkpService.mainLine`'s `Object[]`-commarea branches (COBOL CALL BY REFERENCE style
  params) and the `_params[0] instanceof byte[]` paths — the harness only exercises the
  plain-`String`/`byte[]` COMMAREA path (matches how `AppRunner` actually invokes LINK
  programs; the `Object[]` path is generic `AppProgram` boilerplate shared by every module).
- The `KBK-START-KEY` all-low-values branch in `startTransactionBrowse` (`isAllLowValues`)
  is not separately exercised (only the "spaces" and "explicit key" branches are); low/high
  figurative-constant fills are exercised elsewhere in the codebase's shared layout tests.

## 4. Notes for reviewer
- `WS-BKPFILE` is a `PIC X(08)` initialized to `'BKPFILE'` (7 chars), so the runtime pads it
  to `"BKPFILE "` (trailing space) — tests must match on the padded 8-char literal, not
  `"BKPFILE"`. `WS-TRANFILE` needs no such care since it is already 8 chars.
- `KBK-PARM` is a **separate, un-aliased** top-level group in the layout XML (offset 0);
  it only overlays `CA-WORK-AREA` inside `ORION-COMMAREA` at runtime via
  `aliasGroup("KBK-PARM", "CA-WORK-AREA")`, mirroring COBOL's `SET ADDRESS OF`. Test helpers
  must call `aliasGroup` on both the request-building accessor and the response-reading
  accessor before touching any `KBK-*` field, or reads/writes silently land on the wrong
  (unaliased) buffer instead of the COMMAREA.
