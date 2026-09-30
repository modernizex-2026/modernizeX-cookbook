# OCCARDV Unit Test Findings

## 1. Verify result
`mvn -pl back-end/programs/occardv verify`: **Tests run: 21, Failures: 2, Errors: 0, Skipped: 0**.
The 2 failures are intentional CONVERT-GAP tests (see below) — they pass their purpose by failing.
`OccardvService` business logic itself: ~98% line coverage (2/124 lines missed).
Module-wide C0 (all classes incl. accessor/metadata): ~51% — see §3.

## 2. Intentional CONVERT-GAP failures

**Gap A — `sendText` payload in `abendOnFileError` (paragraph 9500-ABEND-RTN).**
- COBOL: `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT) ...` sends the literal message
  `"OCCARDV: unrecoverable file error. Contact support."`.
- Java (`OccardvService.abendOnFileError`, line 260): calls
  `ctx.appService.sendText(String.valueOf(ctx.f), true, true)`, which stringifies the
  whole `OccardvFields` accessor object (default `Object.toString()`) instead of
  `ctx.f.getWsMsgText()`. Terminal would receive a Java object dump, not the message.
- Test: `mainLine_lookupReadUnexpectedError_convertGap_sendTextShouldCarryMessageText`.

**Gap B — control flow after abend does not stop the pseudo-conversational task.**
- COBOL: `9500-ABEND-RTN` issues `EXEC CICS RETURN` (no TRANSID), which ends the task
  immediately — `9000-RETURN` (which sends `RETURN TRANSID(...)`) is never reached.
- Java: `abendOnFileError` calls `appService.returnProgram()` but this does not unwind
  the Java call stack; `runMainProgram` falls through and calls `returnTransid(...)`
  anyway after the abend, contradicting COBOL's early-exit semantics.
- Test: `mainLine_lookupReadUnexpectedError_convertGap_returnTransidShouldNotFollowAbend`.

Both gaps mirror an identical pattern already flagged in the sibling module OCCARDU's
tests, so this looks like a systemic converter issue in the abend-handling template
rather than an OCCARDV-specific bug.

## 3. Uncovered remainder and why
- `OccardvFields` (231/300 lines missed): a generated field accessor with ~150
  getter/setter pairs for the full COBOL copybook layout; OCCARDV's business logic
  only touches a subset of these fields. Exercising every unused getter/setter with
  no behavior to assert would be a reflection-style test (explicitly an anti-pattern
  per the test-writing rules) and was skipped intentionally.
- `OccardvBmsMetadata` (6 lines missed): static BMS metadata table entries not
  reached by the two used maps/tests; low business risk.
- These two classes account for essentially all of the module's line-coverage gap;
  the actual service logic (`OccardvService`) is covered at ~98%.

## 4. Reviewer notes
- pom.xml for `occardv` was missing `spring-boot-starter-test` + JaCoCo/surefire
  config (present in sibling modules like `occardu`) — added the same block so the
  module can build test sources and produce coverage reports.
- No git commands were run; test file and pom change are left on disk for review.
