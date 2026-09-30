# OUMQREQ — Unit Test Findings

## 1. Final result

`mvn -q -pl back-end/programs/oumqreq verify` — **BUILD SUCCESS**.
Surefire: **14 tests run, 0 failures, 0 errors, 0 skipped.**
JaCoCo C0 line coverage: **53.9% overall** (313/581), but the business-logic
class `OumqreqService` itself is **97.7%** (215/220) — the overall figure is
pulled down by `OumqreqFields` (25.1%, an auto-generated accessor with ~350
one-line getter/setter delegates, most fields unused by OUMQREQ's logic) and
`OumqreqBmsMetadata` (33.3%, trivial no-op/empty-list methods unrelated to
this program's flow). Covering those exhaustively would add no defect
protection, so effort was concentrated on `OumqreqService`.

Note: the module's `pom.xml` was missing `spring-boot-starter-test` and the
surefire/JaCoCo `<build>` block present in sibling program modules; both were
added (copied from `ocusrl`'s pom) since tests could not compile/run without them.

## 2. CONVERT-GAP findings

**None.** OUMQREQ's Java conversion (`OumqreqService`) was checked line-by-line
against the COBOL PROCEDURE DIVISION and copybooks (KAUTH, KMQ, RACCT, RXREF,
KCOMM): EIBRESP codes (0/13/other), the EVALUATE decision ladder in
3300/3600, the `MQ-BUFFER-LEN` literals (160 for AUTH-REQUEST, 300 for
WS-REPLY-BUF — both verified against the copybook field lengths), the
RC=2033 "no reply queued" branch, and the CA-ERR-FLG/CA-ERR-MSG STRING
build all match. No test in this suite is an intentionally-failing
CONVERT-GAP marker.

## 3. Uncovered remainder

- `OumqreqFields`: untouched getters/setters for fields OUMQREQ never reads
  (e.g. most MQ-MD-*/MQ-GMO-*/MQ-PMO-* sub-fields, CA-GENERAL/CA-SELECTED
  fields other than CA-ERR-FLG/MSG). Out of scope — generated boilerplate.
- `OumqreqService` 5 remaining missed lines: the `mainLine` commarea
  copy-in/out variants for a plain (non-array, non-byte[]) commarea object
  and the Object[]-with-non-byte[]-payload sub-branch (lines ~41-48). One
  variant (Object[] wrapping a byte[]) IS covered by a dedicated test.
- `OumqreqBmsMetadata.getMapNames/getLayoutResource/getFieldMapping`: not
  called by `OumqreqService` at all, so out of scope for this suite.

## 4. Reviewer notes

- Because `mainLine`'s WorkingStorage/TaskContext is fully internal (no
  getter exposes it), tests assert outcomes by decoding the byte[] passed to
  `appService.setCommarea(...)` with a second `OumqreqFields` instance,
  re-applying the same `aliasGroup("AUTH-MSG-AREA","CA-WORK-AREA")` the
  service does at entry — this mirrors real CICS LINK commarea-out semantics.
- Request input (`AQ-AMOUNT`, `XR-ACCT-ID`, `AC-*`) is injected via the
  `readFile` mock's "into" argument (the same live `OumqreqFields`), since
  that is the only seam into the WorkingStorage before local authorization
  ran — a deliberate, documented technique, not a shortcut around real logic.
