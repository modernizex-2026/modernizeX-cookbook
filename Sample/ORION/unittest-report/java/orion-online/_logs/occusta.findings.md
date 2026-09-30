# OCCUSTA unit test findings

## 1. Verify result

`mvn -pl back-end/programs/occusta -am verify` — **BUILD SUCCESS** on the 1st (and only) run.
Surefire: **21 tests run, 0 failures, 0 errors, 0 skipped.**
Note: the pre-existing main source did not compile with `-pl occusta` alone (`orion-common`'s
installed jar in `~/.m2` was stale — missing `Utility.toCobolInt(long,int)`); building with
`-am` (rebuilds `orion-common` first) resolved it. No production code was changed.

## 2. CONVERT-GAP tests

**None.** OCCUSTA's Java service (`OccustaService`) mirrors the COBOL paragraphs 0000-MAIN
through 9000-RETURN 1:1: dispatch (PF3/PF4/ENTER/other), MAPFAIL handling (resp 36),
the 5000-VALIDATE-ALL cascade (5010..5070, first-failure-wins), 6000-PARSE-NUM digit
scanning, and 3500-WRITE-CUST's DFHRESP(NORMAL=0)/DUPREC=14/OTHER response handling all
match the COBOL ground truth exactly. No behavioral divergence was found, so every test
asserts the COBOL-expected outcome and all pass — no intentionally-failing test was needed.

## 3. Coverage

- `OccustaService` (the business logic under test): **~96% line coverage** (177/185 lines
  per `jacoco.csv`), all validation branches, MAPFAIL, PF3/PF4/ENTER/other dispatch, and
  WRITE NORMAL/DUPREC/OTHER outcomes are exercised.
- Module-wide C0 (all classes) is **~46%**, pulled down by two generated classes that are
  out of scope for behavioral testing: `OccustaBmsMetadata` (BMS map/button-def registration
  boilerplate, 0% — never invoked by `mainLine`) and `OccustaFields` (generated 1:1
  copybook field accessors; only the ~15 fields OCCUSTA actually reads/writes were
  exercised, out of ~140 defined for the MCUSTAA map/CUST-REC layout). Testing the untouched
  getters/setters would be assertion-free "test vô nghĩa" (getter/setter reflection tests),
  so they were intentionally left uncovered.
- Two validation branches are structurally unreachable and were not (cannot be) covered:
  `WS-NC-DIGITS > 9` in 5010-VAL-CUST and `WS-NC-DIGITS > 3` in 5070-VAL-FICO — CUSTIDI is a
  fixed 9-char field and CUFICOI a fixed 3-char field, so the digit count can never exceed
  the scan length. This dead code exists identically in the COBOL source (not a convert gap).

## 4. Reviewer notes

- `pom.xml` was updated to add `spring-boot-starter-test` (test scope) plus the
  `maven-surefire-plugin` (`testFailureIgnore=true`) and `jacoco-maven-plugin` (0.8.12)
  build plugins, copied from the `ocacctv` module's already-working pattern.
- JaCoCo prints `IllegalClassFormatException` warnings for JDK internal classes
  (`sun.nio.cs.ext.MS932`, class file major version 70) during the run — this is a
  JaCoCo-0.8.12-vs-newer-JDK instrumentation warning, not a test failure; it does not affect
  results (tests and report both completed successfully).
