# OCSTMV — Unit Test Findings

## 1. Verify result

`mvn -pl back-end/programs/ocstmv -am verify`: **BUILD reaches surefire/JaCoCo report** (1 of 3
allowed verify runs used). Tests run: **17**, Passed: **16**, Failed: **1** (intentional
CONVERT-GAP, see below), Errors: 0, Skipped: 0.

Two pre-existing defects had to be fixed just to make the module *buildable* (not test-writing
choices — see section 4):
- `OcstmvService.formatEdBalance` was declared `(TaskContext, long)` but called with
  `BigDecimal` at 3 call sites — a main-source compile error. Fixed the parameter type to
  `BigDecimal`.
- `pom.xml` was missing `spring-boot-starter-test` (test scope) and the `jacoco-maven-plugin`
  that every sibling program module (e.g. `ocdgrp`, `ocacctv`) already has. Added both, mirrored
  from `ocdgrp/pom.xml`.

## 2. CONVERT-GAP test (intentional failure)

`mainLine_enterKey_readFileUnexpectedError_abendsAndSendsErrorText_CONVERT_GAP`:

- **COBOL** (9500-ABEND-RTN): `MOVE 'OCSTMV: unrecoverable file error...' TO WS-MSG-TEXT` then
  `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT)` — the operator terminal receives that exact message.
- **Java** (`_9500AbendRtn`): sets the same message into `WS-MSG-TEXT` but then calls
  `appService.sendText(String.valueOf(ctx.f), true, true)` — `ctx.f` is the whole
  `OcstmvFields` accessor object, not the field. `String.valueOf(ctx.f)` therefore yields the
  default `Object.toString()` (e.g. `OcstmvFields@612af486`), so the real error text never
  reaches the terminal.
- Fix suggestion: change the call to `ctx.appService.sendText(ctx.f.getWsMsgText(), true, true)`.
- Note: sibling module `ocdgrp` has the exact same `sendText(String.valueOf(ctx.f), ...)` pattern
  in its own abend routine — this looks like a systemic codegen defect, not OCSTMV-specific.

All other paragraphs (0000-MAIN through 9000-RETURN, including numeric parsing, key validation,
balance editing via WS-ED-BAL(2:15), and the abend-triggers-`ReturnException` control-flow) match
COBOL 1:1 — no other divergence found.

## 3. Uncovered remainder

Overall module C0 (JaCoCo line coverage) is **44.4%** (249/561), pulled down entirely by
auto-generated boilerplate, not business logic:
- `OcstmvBmsMetadata` (0/58 lines) — static button/field-mapping metadata never exercised by
  `mainLine()`; would need dedicated metadata-accessor tests, out of scope for a service-behavior
  suite.
- `OcstmvFields` (97/344 lines) — hundreds of trivial typed getter/setter delegates for shared
  copybook fields (CA-*, SQLCODE, etc.) that OCSTMV's own logic never touches. Testing these
  directly would be reflection-style getter/setter tests with no behavior to protect (explicitly
  an anti-pattern per the test-writing guidelines).
- The actual business logic, `OcstmvService`, is at **142/149 lines (95.3%)** — the number that
  matters for this module.

## 4. Reviewer notes

- JaCoCo/test infra (pom test dependency + plugin) was missing from `ocstmv/pom.xml` entirely;
  now added and confirmed working.
- The `formatEdBalance` signature fix and the CONVERT-GAP finding above are both pre-existing
  main-source issues, not artifacts of test authoring — please confirm intended fix with the
  codegen/COBOL owner before changing production behavior.
