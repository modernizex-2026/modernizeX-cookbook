# OUPAY unit test findings

## Verify result
`mvn -q -pl back-end/programs/oupay verify` — **BUILD SUCCESS**.
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0.
C0 line coverage: **93.6%** (162/173 lines, JaCoCo — `OupayService` 157/168, inner `TaskContext` 5/5).

## CONVERT-GAP tests
None. All 19 tests assert COBOL ground truth and pass — the Java conversion of OUPAY.cbl's
paragraphs (1000-INITIALISE, 2050/2100/2200/2300/2400/2500, 9000-FINALISE) faithfully
matches the COBOL logic for validation order, EIBRESP branching (NORMAL/NOTFND/OTHER),
balance/credit math, bill-sequence increment and truncation to 9 digits (`Utility.toCobolInt`,
verified to keep low-order digits like COBOL MOVE truncation), and the KO-STATUS/KO-STATUS-MSG
finalisation rules (including the "posted but warn" case not being overwritten by 9000-FINALISE).

## One non-testable observation (not a failing test)
COBOL's `2900-UNLOCK-ACCT` issues `EXEC CICS UNLOCK FILE(ACCTFILE)` when a rejected
request had the account locked. The converted `postPayment()` only resets the
`WS-ACCT-LOCK-SW` flag and replaces the actual unlock with a comment
(`/* EXEC CICS UNLOCK — file unlock */`), because `AppService` has no `unlockFile`
method at all — there is nothing to mock/verify. Since this runtime is stateless
per-HTTP-request (no persistent CICS-style record lock across requests), this is
likely intentional, but flagging it for the reviewer since it is a real drop of a
COBOL file-control call with no equivalent in the target architecture.

## Uncovered remainder (11 lines missed)
The 11 missed lines in `OupayService` are in the generic `mainLine()` COMMAREA
marshaling wrapper (the `Object[]`/`byte[]` branches for `getCommarea()`/`setCommarea()`
used by other call conventions such as `CALL ... USING` with byte-array params). OUPAY is
only ever invoked via `EXEC CICS LINK ... COMMAREA(...)`, which all 19 tests exercise
through the `String`-typed `getCommarea()` path; the `Object[]` and raw `byte[]` marshaling
branches are boilerplate shared across all generated programs and are exercised by other
modules' tests, so they were not duplicated here to stay within the verify budget.

## Reviewer notes
- Tests decode the outbound COMMAREA via `setCommarea()` capture + `writeBytes` +
  `aliasGroup("KOPS-AREA","CA-WORK-AREA")` to read KO-* result fields — mirrors the
  production `SET ADDRESS OF KOPS-AREA TO ADDRESS OF CA-WORK-AREA` overlay.
- Default pay-date test compares against `LocalDate.now()`; negligible flake risk only
  if the suite runs exactly at local midnight.
