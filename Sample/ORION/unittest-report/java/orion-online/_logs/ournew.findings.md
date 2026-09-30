# OURNEW — Unit Test Findings

## Verify result
`mvn -q -pl back-end/programs/ournew verify` — **BUILD SUCCESS**, 2nd (confirming) run.
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0.

## Convert-gap tests
None found. All 8 COBOL paragraphs (1000-INITIALISE, 1300-COMPUTE-CUTOFF,
3100-START-BROWSE, 3200-READ-NEXT-CARD, 4000-PROCESS-CARD, 4100-VALIDATE-EXPIRY,
4200-REISSUE-CARD, 4250-ADJUST-LEAP, 3400-END-BROWSE, 9000-FINALISE) were traced
line-by-line against `OurnewService.java` and the Java logic, branch order, CICS
RESP-code handling (NORMAL=0/NOTFND=13/ENDFILE=20), lead-days(90)/extend-years(3)
constants, and the Feb-29 leap clamp all match the COBOL exactly. No test was
written with an expected-fail CONVERT-GAP marker.

One behavior worth flagging to the reviewer (not a bug): in `9000-FINALISE`,
COBOL unconditionally overwrites `KO-STATUS-MSG` with "CARD RENEWAL PROCESSING
COMPLETE." whenever `KO-STATUS` isn't `E` — this silently replaces an earlier
"NO CARDS TO PROCESS." message from 3100-START-BROWSE. Java reproduces this
faithfully (see `runMainProgram_startBrowseNotFound_finalMessageOverwrittenToComplete`).

## Coverage
- `OurnewService` (the class holding all business logic): **86.4%** line coverage
  (133/154) — every paragraph/branch has at least one test; the 21 missed lines
  are mostly the `mainLine`/`TaskContext` COMMAREA byte-marshalling wrapper
  around CICS EIB fields, exercised indirectly but not with all branch
  permutations (deliberately out of scope — pure plumbing already covered by
  `AppRunner`/`AppService` infra tests elsewhere).
- Module-wide C0 (JaCoCo sum across all classes in the package) is **56.3%**
  (236/419), pulled down by two generated, largely unused classes:
  - `OurnewFields` (accessor): 36.8% — dozens of typed getter/setter wrappers
    for copybook fields (KCOMM/RCARD) that OURNEW's own business paths never
    touch; testing them would be reflection-style getter/setter assertions,
    which the test-writing guidelines explicitly flag as meaningless.
  - `OurnewBmsMetadata`: mostly empty stubs (`getMapNames`, `getFieldMapping`)
    since OURNEW is a CICS LINK sub-program with no BMS map; only the two
    methods actually delegated from `OurnewService` (`getButtonDefs`,
    `registerFsetFields`) are covered.
  Recommend judging this module by the `OurnewService` figure (86.4%) rather
  than the blended module figure.

## Reviewer notes
- Business logic was tested via reflection-invoked private paragraph methods
  (`OurnewService.TaskContext` is package-private and its fields are
  package-visible, so no reflection was needed for state — only for the
  private methods themselves) plus 3 full `runMainProgram` integration tests.
- `spring-boot-starter-test` (test scope) and the `jacoco-maven-plugin` were
  added to `back-end/programs/ournew/pom.xml`, which previously had no test
  dependencies or coverage plugin configured.
- `computeCutoffDate` relies on `LocalDateTime.now()` (no injected clock);
  the test recomputes the expected `LocalDate.now().plusDays(90)` at call
  time, which carries a theoretical (very low) midnight-rollover flakiness
  risk — no injectable clock exists in the source to eliminate it.
