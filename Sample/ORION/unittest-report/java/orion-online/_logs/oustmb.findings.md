# OUSTMB — unit test findings

## 1. Verify result
Final (3rd, budget-capped) `mvn -pl back-end/programs/oustmb verify`: **30 tests, 29 passed, 1 error**.
The error (`mainLine_xrefRollsToNextAccount_endsCollectLoopWithoutError`) was a test-authoring bug
(STARTBR response queue under-provisioned by one entry for the TRAN browse triggered by the
first collected card). The fix was applied to the test file **after** the 3rd verify, so per the
3-verify hard cap it was **not re-run**. The fix was hand-traced against `OustmbService`'s logic
line-by-line and is expected to pass; reviewer should re-run `mvn -pl back-end/programs/oustmb verify`
once to confirm. `pom.xml` was missing `spring-boot-starter-test` and the surefire/JaCoCo plugin
config entirely (present in sibling modules like `ouarch`) — added both so this module builds/tests
at all.

## 2. CONVERT-GAP (intentionally failing, by design)
`mainLine_minDueRounding_CONVERT_GAP_javaSkipsCobolRoundedCompute` — **fails on purpose**, and did
fail in every verify run (not a flake):
- COBOL 3400-COMPUTE (`OUSTMB.cbl:351`): `COMPUTE WS-MIN-DUE ROUNDED = WS-CLOSE-BAL * 0.02`,
  rounded half-up into a `PIC S9(10)V99` (2-decimal) field.
- Java `OustmbService.computeStatementBalances` (`OustmbService.java:331`): does
  `ctx.f.getWsCloseBal().multiply(new BigDecimal("0.02"))` with **no rounding**, keeping extra
  decimal scale (e.g. close-bal 2000.01 → COBOL 40.00, Java 40.0002).
- This only surfaces when the un-rounded product still clears the 25.00 floor (COBOL's
  "< 25.00 → 25.00" branch would otherwise mask the discrepancy for small balances).

## 3. Uncovered remainder
- Overall C0 (JaCoCo, whole module) ≈ 63% — dragged down almost entirely by `OustmbFields`
  (96/282 lines, ~34%). That class is the shared, auto-generated accessor for the *entire*
  copybook layout (AC-*, CA-*, TR-*, ST-*, XR-*, WS-* …); OUSTMB's business logic only touches a
  small subset of its ~90 getter/setter pairs. Exercising the rest would mean calling
  getters/setters with no behavior to assert (explicitly discouraged as a "meaningless test").
- `OustmbService` itself: **98.7%** line coverage (228/231). The 3 remaining uncovered lines
  (`OustmbService.java:50,55`) are the generic COMMAREA-marshalling branches for a non-`byte[]`,
  non-`Object[]` commarea shape (`ctx.f.setGroup(...String.valueOf(_commarea))`) — shared
  `mainLine` plumbing identical across every generated program, not OUSTMB-specific logic, and
  not reachable through the `AppService` mock shapes this suite exercises.

## 4. Reviewer notes
- All other branches are covered: cycle validation, ACCTFILE/XREFFILE/TRANFILE STARTBR/READNEXT
  outcomes (NORMAL/NOTFND/ENDFILE/OTHER), alternate-index rollover (both ENDFILE and "next key"
  endings), card-table cap at 20, WRITE/REWRITE STMTFILE paths, MAX-cap + PEEK-NEXT resume-key
  logic, and multi-account grand-total accumulation.
- Test file: `back-end/programs/oustmb/src/test/java/.../service/OustmbServiceTest.java`.
