# OUCUSIN — write-unit-test findings

## 1. Verify result
Final `mvn -q -pl back-end/programs/oucusin verify`: **23 tests run, 22 passed, 1 intentional failure, 0 errors, 0 skipped.**
Module `pom.xml` was missing `spring-boot-starter-test` + surefire/jacoco config (present in sibling modules like `occustl`) — added it, mirroring `occustl/pom.xml`, so the module could compile/run tests and produce coverage at all.

## 2. CONVERT-GAP (intentional failing test)
`mainLine_ficoAverageRounding_cobolRoundsHalfUpButJavaTruncatesBeforeRounding` — **FAILS by design**.

- COBOL `3000-FINALIZE`: `COMPUTE KUI-FICO-AVG ROUNDED = KUI-FICO-TOT / KUI-MATCH-COUNT` evaluates the division as a real (decimal) quotient and then rounds it.
- Java `finalizeSummary()` (`OucusinService.java:281`): `Math.round((double)(ctx.f.getKuiFicoTot() / ctx.f.getKuiMatchCount()))` — `ficoTot`/`matchCount` are both integer types, so the division truncates **before** the cast to `double`; `Math.round` then has nothing left to round.
- Example: total=1007, count=4 → COBOL rounds 251.75 → **252**; Java truncates 1007/4 → 251 → `Math.round(251.0)` → **251**.
- Every whole-number average matches (verified in other tests); the gap only surfaces on non-exact averages. Reviewer should decide whether to fix `finalizeSummary` to divide in `double`/`BigDecimal` before rounding.

## 3. Uncovered remainder
Overall C0 (line) coverage = 231/332 = **69.6%**, below the 80% target. Breakdown:
- `OucusinService` (business logic): **100% line coverage** (141/141) — all paragraphs/branches covered, including the three `mainLine` commarea marshalling shapes (`byte[]`, `Object[]{byte[]}`, `Object[]{String}`, plain `String`).
- `OucusinService.TaskContext`, `WorkingStorage`: 100% covered.
- `OucusinBmsMetadata`: 5/6 lines (1 trivial line uncovered, no business logic).
- `OucusinFields` (generated field accessor): only 76/176 lines (43%) — this class is a shared typed-wrapper generated from the full WORKING-STORAGE/LINKAGE layout (`OUCUSIN_WS.xml`), and it exposes many fields OUCUSIN's own paragraphs never touch (e.g. `CU-DOB`, `CU-GOVT-ID`, `CU-SSN`, `CU-PHONE-1/2`, `CU-ADDR-CITY/COUNTRY/LINE-1/2`, `CU-MIDDLE-NAME`, `WS-AMT`, `WS-ACCTFILE/BILLFILE/CARDFILE/CTRLFILE/DGRPFILE/STMTFILE/TCATFILE/TRANFILE/TTYPFILE/XREFFILE`, `SQLCODE`, `COMPLETION-CODE`, etc.). Exercising these directly would just be getter/setter reflection tests with no business behavior to protect (explicitly discouraged by the test-writing guidance), so they were left uncovered rather than padded with meaningless assertions.

This accounts for essentially all the coverage gap: business logic is fully covered; the shortfall is entirely in unused generated boilerplate.

## 4. Notes for reviewer
- No other convert-gaps were found: STARTBR/READNEXT/ENDBR RESP handling (NORMAL/NOTFND=13/ENDFILE=20/OTHER), the id-range/FICO/state+ZIP filters, the `KUI-START-KEY` resume logic, the `WS-MAX-ROWS`(13)/`KUI-MORE-SW` paging cap, and `2600-BUILD-NAME`'s STRING-delimited-by-two-spaces behavior all match COBOL exactly.
- Test file: `back-end/programs/oucusin/src/test/java/com/generated/orion/oucusin/service/OucusinServiceTest.java`.
- `pom.xml` was modified (test dependency + surefire/jacoco plugin block added) — please review before merge.
