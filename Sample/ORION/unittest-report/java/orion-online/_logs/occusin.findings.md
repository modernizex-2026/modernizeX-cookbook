# OCCUSIN — Unit Test Findings

## 1. Verify result

Final `mvn verify`: **26/26 tests passed**, 0 failures, 0 errors. `OccusinService` (the converted
business logic) line coverage: **364/367 = 99.2%** C0. Module-wide C0 (including auto-generated
`OccusinFields` accessor and `OccusinBmsMetadata`) is **~50%** — see §3.

Note: the module's `pom.xml` was missing `spring-boot-starter-test` and the
surefire/jacoco `<build>` plugin block present in sibling modules (e.g. occrdin); both were added
to `back-end/programs/occusin/pom.xml` so the module could compile and report coverage at all.

## 2. CONVERT-GAP tests

None found. OCCUSIN's Java conversion tracks the COBOL PROCEDURE DIVISION closely for every
paragraph checked (0000-MAIN, 2000/2100/2200/2300, 3000-CALL-SUB, 4000/4100/4200/4300,
5000/5100/5200/5300, 6000-PARSE-NUM) — mode dispatch, filter validation order/short-circuiting,
paging-key propagation, and the two distinct empty-result messages (PF7 → "No customers match" vs
PF8 → "End of list") all match ground truth. Unlike the sibling OCCRDIN module, the low-value→space
replacements here use a single-character target, so `String.replace()` correctly emulates COBOL's
`INSPECT ... REPLACING ALL LOW-VALUE BY SPACE` (no case-fold-style substring bug).

## 3. Uncovered remainder

- `OccusinService` (3 missed lines): the `WS-NC-DIGITS = 0` branch inside `parseNumericField`
  (all-blank-after-scan edge, reached only when input passes the outer non-blank check yet resolves
  to zero digits) was not constructed — the realistic paths already trigger validation failure via
  the character-loop branch instead. `registerFsetFields` (line 59) could not be tested: Mockito
  cannot mock `com.appruntime.AppRunner` on this JDK 26 toolchain ("Mockito cannot mock this
  class"), so the delegate is exercised only implicitly via `getFieldMapping`/`getButtonDefs`.
- `OccusinFields` (858/1054 lines missed) and `OccusinBmsMetadata` (6/322 lines missed): both are
  auto-generated field-mapping boilerplate (typed getter/setter delegates, screen field/button
  registration tables) with no independent business logic. Exercising each unused typed accessor
  individually would be reflection-style testing of getters/setters, which the skill's own
  "meaningless test" guidance says to avoid — coverage here is a byproduct of which fields the
  service actually touches, not a gap in business-logic testing.

## 4. Reviewer notes

- The pom.xml fix (test deps + surefire/jacoco plugin) is a real, durable change to the module, not
  test-only scaffolding — please keep it when reviewing/committing.
- `ctx.f` (the `OccusinFields` working-storage buffer) is a single mutable object reused across an
  entire `mainLine()` call; tests only assert against the *last* `sendMap` invocation's captured
  reference for content, since earlier captures alias the same object and reflect later mutations.
