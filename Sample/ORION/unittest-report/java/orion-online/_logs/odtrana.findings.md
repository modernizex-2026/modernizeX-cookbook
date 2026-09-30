# ODTRANA — Unit Test Findings

## 1. Result
Final `mvn verify`: **24/24 tests passed** (0 failures, 0 errors, 0 skipped).
`OdtranaServiceTest` exercises `OdtranaService.mainLine(AppService)` end-to-end with
`AppService` and `OdtranaDao` mocked (Mockito, `@InjectMocks` field injection).

## 2. Convert-gap tests
**None found.** Paragraph-by-paragraph comparison of `ODTRANA.cbl` (0000-MAIN through
9000-RETURN) against `OdtranaService` shows a faithful 1:1 conversion — including two
COBOL quirks preserved identically in Java (not bugs, just documented in test comments):
- `3000-CHECK-CARD`/`3100-CHECK-TYPE`'s own "Error reading ... table." message is always
  overwritten by `2100-ADD-TRAN`'s ELSE branch, so it is unreachable in both COBOL and Java.
- `3200-GET-NEXT-ID`'s counter-missing/UPDATE-failure paths have no found-flag gate in
  either COBOL or Java — `3300-BUILD-RECORD`/`3400-INSERT-TRAN` always run regardless.

## 3. Uncovered remainder
Overall module C0 = 312/610 = **51.1%**, but this is dragged down by two out-of-scope
classes, not by untested business logic:
- `OdtranaDaoImpl` (0/44 lines): pure JDBC passthrough (`JdbcTemplate.queryForMap`/
  `update`). Per skill rules this must stay mocked, not hit a real DB — no unit-test
  coverage is expected here; would need a DB integration test (out of scope).
- `OdtranaFields` accessor (86/330 lines, 26%): auto-generated typed getters/setters for
  the full `ODTRANA_WS.xml` layout (~150 fields); only the ~15 fields ODTRANA's logic
  actually touches are exercised. The rest are unused-by-this-program boilerplate.
- `OdtranaService` itself (the real business logic): **164/168 lines = 97.6%** — only
  the trivial `registerFsetFields` one-line delegate is untested (see §4).

## 4. Reviewer must-know
- `back-end/programs/odtrana/pom.xml` was **missing** `spring-boot-starter-test` and the
  `jacoco-maven-plugin`/surefire build config that sibling program modules have — added
  both (copied from `odacctv/pom.xml`) so tests could compile and JaCoCo could run.
- `registerFsetFields_delegatesToMetadataWithRunner` was removed: mocking the concrete
  `AppRunner` class fails on this machine's JDK 26 (Byte Buddy inline-mock-maker doesn't
  support class file major version 70 yet) — an environment limitation, not a code issue.
  The same JDK/JaCoCo version mismatch prints noisy (non-fatal) instrumentation warnings
  for JDK bootstrap classes and Mockito's generated mock classes during `verify`; these
  do not affect the reported coverage numbers or test pass/fail results.
