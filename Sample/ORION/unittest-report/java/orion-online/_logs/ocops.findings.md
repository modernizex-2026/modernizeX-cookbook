# OCOPS — Unit Test Findings

## 1. Verify result
Final `mvn verify`: **Tests run: 33, Failures: 2, Errors: 0, Skipped: 0** — build **SUCCESS**
(`testFailureIgnore=true` added to pom so the 2 intentional CONVERT-GAP failures don't fail the
build). Both failures are deliberate ground-truth assertions, not test bugs.

## 2. CONVERT-GAP (intentional failing tests)
`OcopsService.parseInputParm()` (Java, paragraph 6000-PARSE-PARM) computes
`String[] unstringParts = ...split(...)` but **never assigns the result to WS-TOK1/WS-TOK2**.
COBOL does `UNSTRING WS-PARM-IN DELIMITED BY ALL SPACE INTO WS-TOK1 WS-TOK2`, so token one/two
actually get populated and feed 6100-PARSE-ACCT / 6200-PARSE-AMT. In Java, WS-TOK1/WS-TOK2 stay
fixed at `" "` forever, so **WS-P-ACCT and WS-P-AMT are always 0**, regardless of the PARM screen
input — every option except 7 (RNEW, which uses WS-PARM-IN directly) silently loses its numeric
parameter.
- `mainLine_parmWithAccountAndAmount_parsesAccountIdPerCobol`: expected KO-PARM-ACCT=12345 (from
  PARM "12345 67.89"), Java sends 0.
- `mainLine_parmWithAccountAndAmount_parsesAmountPerCobol`: expected KO-PARM-AMT=67.89, Java
  sends 0.00.
This is a real, high-severity convert bug: every OU* sub-program (OUPOST/OUPAY/OUINT/OUFEE/
OUCHGF/OUCLOS/OUCYCL) always receives account id 0 and amount 0.00. Recommended fix: assign
`unstringParts[0]`/`[1]` to WS-TOK1/WS-TOK2 in `parseInputParm`.

## 3. Uncovered remainder
- `OcopsService` core logic: 177/195 lines (~90.8%) — all paragraphs reachable from `mainLine`
  are exercised. `abendWithErrorMessage` (9500-ABEND-RTN) is uncovered because it is dead code
  in both COBOL and Java (never PERFORMed/called anywhere) — correctly-converted unreachable code.
- `OcopsFields` accessor class: only 112/430 lines covered. This class is ~100 generated typed
  getter/setter pairs (one per copybook field); only the ~30 fields OcopsService actually touches
  get exercised. The untouched pairs are boilerplate delegating to the same `getInt/getString/
  getDecimal` calls already covered elsewhere — testing each one adds no business-logic value.
- Module-wide C0 (sum of all classes) = 307/649 ≈ **47.3%**, pulled down almost entirely by
  OcopsFields boilerplate above; the business-logic class itself is at ~90.8%.

## 4. Reviewer notes
- `back-end/programs/ocops/pom.xml` was missing `spring-boot-starter-test` and the
  surefire/jacoco plugin config present in sibling modules (e.g. ocmenu) — added both so tests
  compile/run and coverage can be measured.
- JaCoCo logged one benign instrumentation warning for a JDK-internal class
  (`sun.nio.cs.ext.MS932$DecodeHolder`, "unsupported class file version") — unrelated to OCOPS
  code, report generation completed normally.
