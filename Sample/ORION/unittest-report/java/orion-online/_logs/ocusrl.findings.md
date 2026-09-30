# OCUSRL — Unit Test Findings

## 1. Verify result

`mvn -q -pl back-end/programs/ocusrl verify` — **BUILD SUCCESS**.
Surefire: **18 tests run, 0 failures, 0 errors, 0 skipped** (2 verify runs used of the
3-run budget; 1st run caught one test-authoring bug on my side, fixed, 2nd run green).

`OcusrlServiceTest.java` was created new (module had no test dependencies /
JaCoCo / surefire config at all — added `spring-boot-starter-test`, the
JaCoCo `prepare-agent`/`report` executions, and `testFailureIgnore=true` to
`pom.xml`, mirroring the sibling `ocanlin` module's setup).

## 2. CONVERT-GAP tests

**None.** OCUSRL's COBOL (0000-MAIN..9000-RETURN) was compared paragraph-by-
paragraph against `OcusrlService.java`, including: the `CA-FIRST-ENTER` /
`CA-PGM-CONTEXT=0` condition (verified against KCOMM copybook), the CICS RESP
code mapping (STARTBR NOTFND=13, READNEXT ENDFILE=20), the `copyBytes(dest,
src)` direction used for WS-STATE-AREA ↔ CA-WORK-AREA round-tripping in both
7000-XCTL-ADMEN and 9000-RETURN, and the `STRING ... DELIMITED BY SPACE`
full-name build (mirrored exactly by `split(" ", 2)[0]`, confirmed with an
embedded-space test case). No behavioral divergence was found — this is a
faithful 1:1 conversion.

## 3. Coverage

JaCoCo is configured (line coverage). Sums from `jacoco.csv`:

- **OcusrlService** (the actual business logic, exercised via `mainLine`):
  169/175 lines = **96.6%**.
- **Module-wide C0: 39.8%** (253/636), pulled down by two generated classes
  the tests cannot meaningfully cover:
  - `OcusrlFields` (74/370, 20%): a shared typed-accessor class generated
    from the copybooks (KCOMM/RUSER/WHEAD/etc.) — most of its getter/setter
    pairs (e.g. `getCaAcctId`, `getSqlcode`) are for fields OCUSRL never
    reads or writes. Calling them just to inflate coverage would be a
    getter/setter test with no behavior to protect (explicitly called out as
    meaningless in the test-writing guidance).
  - `OcusrlBmsMetadata` (0/81, 0%): static BMS field/button registration
    consumed by the (mocked) `AppService.sendMap`/`registerFsetFields`
    plumbing, not exercised when `AppService` itself is a mock.
- Remaining 6 uncovered lines in `OcusrlService` are minor: `getButtonDefs`/
  `registerFsetFields`/`getFieldMapping` delegate wrappers (framework
  plumbing, not called by `mainLine`) — not part of the COBOL business logic
  path and out of scope for paragraph-level business tests.

## 4. Reviewer notes

- Test doubles use an `AtomicInteger eibresp` driven by `doAnswer` stubs on
  `startBrowse`/`readNext` rather than fixed `when(...).thenReturn(a,b,c)`
  sequences, because `getEibresp()` is polled after every CICS-style call
  (STARTBR, READNEXT×N, ENDBR, FORMATTIME, SENDMAP) and a positional sequence
  would be extremely fragile to reorder.
- `pom.xml` for `ocusrl` was modified (test deps + JaCoCo/surefire plugins)
  since it previously had none — please review that change too.
