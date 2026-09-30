# OUPURG — Unit Test Findings

## 1. Verify result
`mvn -q -pl back-end/programs/oupurg verify` — **BUILD SUCCESS**.
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0.
All 12 COBOL paragraphs (0000-MAIN through 6000-SET-STATUS) are exercised, including
private paragraph methods invoked via reflection and 3 full `runMainProgram`
(0000-MAIN) integration scenarios (invalid cutoff, immediate NOTFND, one aged record
purged with ENDFILE).

## 2. CONVERT-GAP tests
None. Line-by-line comparison of every OUPURG.cbl paragraph against
`OupurgService.java` found no behavioral divergence: cap/WS-CAP computation (1000-INIT),
YYYY-MM-DD shape check (1100-CHK-DATE), STARTBR/READNEXT RESP handling (2000/3100/4000),
aged-vs-kept classification (3200-CLASSIFY), and the DELETE loop (5500/5600) all match
COBOL exactly, including the on-purpose asymmetry that 6000-SET-STATUS never overwrites
an existing `KPG-STATUS = '99'`. No test was written expecting an intentional fail.

## 3. Coverage
Whole-module JaCoCo C0 = 194/368 = **52.7%** (below the 80% target). Breakdown:
- `OupurgService` (business logic — the class under test): 120/145 = **82.8%**, meets target.
- `OupurgFields` (accessor): 65/208 = 31.3%. This generated class exposes ~90 typed
  getters/setters for the whole shared COMMAREA/copybook (CA-*, WS-ACCTFILE, WS-CARDFILE,
  WS-EDIT, TR-MERCHANT-*, ...); OUPURG.cbl only ever touches ~20 of those fields
  (KPG-*, WS-TRANFILE, WS-CAP/CAPPED, TR-ID/AMT/PROC-TS, WS-P-ID/AMT, WS-EOF/BROWSE-SW).
  The remaining getters/setters are dead code for this program — writing tests to touch
  them would just be calling unrelated accessors with no OUPURG business meaning.
- `OupurgBmsMetadata`: 0/6. OUPURG is a CICS SUB (file-control only, no BMS map — see
  COBOL header "TYPE: SUB"); `getButtonDefs()`/`registerFsetFields()` are never invoked by
  `mainLine`/`runMainProgram` and return empty/static boilerplate.

## 4. Notes for reviewer
- Tests call private paragraph methods via reflection (`OupurgService.class.getDeclaredMethod(name, TaskContext.class)`)
  and construct `OupurgService.TaskContext` directly (package-private ctor, same package)
  rather than going through `mainLine`'s raw-COMMAREA byte marshalling — this keeps each
  paragraph independently testable per COBOL ground truth.
- `runMainProgram` re-points `KPG-PARM` onto `CA-WORK-AREA` via `aliasGroup` (COBOL
  `SET ADDRESS OF`) as its first statement; the 3 integration tests call
  `ctx.f.aliasGroup("KPG-PARM","CA-WORK-AREA")` themselves before seeding `KPG-*` fields
  so the seeded values survive the re-alias — otherwise they're silently discarded.
- Added `spring-boot-starter-test` (test scope) + `jacoco-maven-plugin` to
  `back-end/programs/oupurg/pom.xml`, matching the pattern already used by ~65 sibling
  program modules in this reactor.
