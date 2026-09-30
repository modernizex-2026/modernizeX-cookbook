# OUPOST — unit test findings

## 1. Verify result

`mvn -q -pl back-end/programs/oupost verify` → **BUILD SUCCESS**.
Tests run: **22**, Failures: 0, Errors: 0, Skipped: 0.

## 2. CONVERT-GAP tests

None. `OupostService` is a faithful line-by-line conversion of `OUPOST.cbl`:
STARTBR/READNEXT resp handling, blank/low-value card rejection, XREF resp handling
(NOTFND vs other error, with/without KO-C1), the optional KO-PARM-ACCT filter,
account READ-FOR-UPDATE resp handling (NOTFND vs other, with/without KO-C2), the
PY/CR credit vs. debit classification, the `>` (not `>=`) credit-limit guard, the
REWRITE resp handling, and 9000-FINALISE's status/message logic all match the
COBOL exactly. One COBOL quirk was deliberately reproduced (not a gap): when
STARTBR returns NOTFND, 3100-START-BROWSE sets KO-STATUS-MSG to "NO TRANSACTIONS
ON FILE TO POST.", but 9000-FINALISE unconditionally overwrites it with
"TRANSACTION POSTING COMPLETE." whenever KO-STATUS ≠ 'E' — the Java does the same
overwrite, and `mainLine_startBrowseNotFound_finaliseOverwritesMessageAndSetsOkStatus`
asserts this as expected (COBOL-ground-truth) behavior, not a failure.

## 3. Uncovered remainder

Overall module C0 = 229/410 = **55.9%** (below the 80% target), but this is
entirely attributable to `OupostFields` (170 of 240 lines uncovered): an
auto-generated accessor exposing ~130 typed getter/setter pairs for the *entire*
copybook surface (WCONST/RTRAN/RXREF/RACCT/KCOMM/KOPS), of which OUPOST's business
logic only touches a small subset (TR-*, XR-*, AC-CURR-BAL/CREDIT-LIMIT/CYC-*,
KO-*). Writing tests solely to invoke the remaining unused getters/setters would
be reflection-style coverage padding with no behavioral value (explicitly an
anti-pattern per the test-writing guidelines) and was not done.
Excluding `OupostFields`, the actual logic classes are well covered:
`OupostService` 145/155 lines (93.5%), `OupostService.TaskContext` 5/5 (100%),
`WorkingStorage` 4/4 (100%), `OupostBmsMetadata` 5/6 (83%).

## 4. Reviewer notes

- Tests drive the service exclusively through the public `mainLine(AppService)`
  entry point (all paragraph methods are private); the AppService mock emulates
  CICS STARTBR/READNEXT/READ/READ-UPDATE/REWRITE/ENDBR via a shared `eibresp`
  AtomicInteger set immediately before each corresponding `getEibresp()` read.
- Request/response commarea round-trips through a real `OupostFields` +
  `WorkingStorage` instance with `aliasGroup("KOPS-AREA", "CA-WORK-AREA")` called
  (mirroring `0000-MAIN`'s `SET ADDRESS OF`) so KO-* result fields can be read back
  from the captured `setCommarea(byte[])` argument.
- pom.xml was updated to add `spring-boot-starter-test` (test scope) and the
  `jacoco-maven-plugin` (0.8.12), matching the pattern already used by sibling
  modules (e.g. `ocmenu`, `ouactin`) — this module had neither before.
