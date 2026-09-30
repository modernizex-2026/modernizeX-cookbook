# OUCYCL — Unit Test Findings

## 1. Verify result

`mvn -q -pl back-end/programs/oucycl verify` — **BUILD SUCCESS**.
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 (2 verify runs used: first run caught a wrong
expected-invocation-count assertion and a JDK-26/Byte Buddy `mock(AppRunner.class)` incompatibility
in my own test code — both fixed, no production code changed).

## 2. CONVERT-GAP tests

None. OUCYCL's Java service (`OucyclService`) mirrors every COBOL paragraph 1:1
(0000-MAIN..9000-FINALISE), including the DFHRESP(NORMAL/NOTFND/ENDFILE) numeric constants
(0/13/20), the EVALUATE bucket order in 4200-CLASSIFY, and the "END-BROWSE always runs once
BR-STARTED, even with 0 loop iterations" structure. No behavioral divergence from the COBOL
ground truth was found, so no test was written to assert a known-bad Java behavior.

## 3. Coverage

- `OucyclService` (the only class with real business logic): **92.6% C0** (125/135 lines) —
  every paragraph/branch is covered by the 10 tests (no-accounts, STARTBR failure, initial
  READNEXT failure/EOF, filter mismatch, a 4-account sweep hitting all three classify buckets
  (C1/C2/C3) plus both reject paths (READ-UPDATE fail, REWRITE fail), and the EIBCALEN<=0
  commarea-skip branch).
- Module-wide C0 is only **55.4%** (195/352) because `OucyclFields` — an auto-generated
  typed-accessor wrapper class shared across ~150 copybook fields (CA-*, KO-*, AC-*, WS-*) —
  contributes 143 missed lines: most of its getters/setters are for fields OUCYCL's business
  logic never touches (e.g. `CA-CARD-NUM`, `CA-CUST-ID`, statement/report fields). Calling them
  just to move the coverage number would be a pure tautology test (getter→setter round-trip),
  which the "no vô nghĩa test" rule explicitly excludes. Treat `OucyclService`'s 92.6% as the
  meaningful signal for this module.

## 4. Reviewer notes

- Results (`KO-READ-CNT`, `KO-STATUS`, etc.) are only observable from outside `mainLine()`
  via the COMMAREA byte[] written back through `appService.setCommarea(...)` — every
  business-logic test builds its input COMMAREA and decodes the output the same way
  (`aliasGroup("KOPS-AREA","CA-WORK-AREA")` + `sliceBytes`/`writeBytes` on `ORION-COMMAREA`),
  mirroring the real `SET ADDRESS OF KOPS-AREA TO ADDRESS OF CA-WORK-AREA` overlay in 0000-MAIN.
- `getEibresp()` is stubbed via a `Deque<Integer>` polled in exact CICS-call order (STARTBR,
  each READNEXT, each READ-UPDATE, each REWRITE, ENDBR) — reviewers changing call order in
  `OucyclService` must update the queue contents in the affected test.
- JaCoCo 0.8.12 logs (harmless, does not fail the build) instrumentation errors against the
  JDK's internal `sun.nio.cs.ext.MS932` charset class under JDK 26 ("Unsupported class file
  major version 70") — pre-existing environment mismatch, unrelated to this module's code.
