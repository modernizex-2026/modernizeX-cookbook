# OUARCH — Unit Test Findings

## 1. Verify result

Final `mvn -pl back-end/programs/ouarch -am verify`: **BUILD SUCCESS**, 19/19 tests passed, 0 failures, 0 errors, 0 skipped.
C0 line coverage: `OuarchService` (the converted business logic) = **98.1%** (151/154 lines).
Whole-module C0 (includes generated accessor class) = **60.2%** (227/377 lines) — see §3.

No CONVERT-GAP tests were needed: every branch checked against OUARCH.cbl (STARTBR NORMAL/NOTFND/ENDFILE/OTHER,
READNEXT NORMAL/ENDFILE/OTHER, cutoff validation, WS-MAX/WS-CAP capping, PEEK-NEXT NORMAL/ENDFILE/OTHER,
WRITE NORMAL/DUPREC/OTHER, DELETE NORMAL/OTHER, 6000-SET-STATUS) matched the Java conversion exactly, including
the CICS RESP literals (13=NOTFND, 14=DUPREC, 20=ENDFILE) used directly as ints in `OuarchService`.

## 2. CONVERT-GAP tests

None. No intentionally-failing tests were required — no divergence from COBOL was found in `OuarchService`.

## 3. Uncovered remainder

- `OuarchService.java` lines 37-41, 43, 46 (3 lines): the `Object[]`-commarea branch where the first element is
  **not** a `byte[]` (falls back to `String.valueOf(...)` / `setGroup`), and the plain-`Object` (non-array,
  non-`byte[]`) commarea branch. These are generic CICS-commarea-marshalling scaffolding shared by every
  converted program, not OUARCH-specific logic; only the `byte[]` and `Object[]{byte[]}` shapes are exercised
  (matching how `AppRunner` actually invokes `mainLine`), so building a fake `Object` commarea to hit the
  string-fallback path would test a transport shape the runtime never produces.
- `OuarchFields.java` (784 instructions / 143 lines missed): this is the shared generated field accessor for the
  **whole COMMAREA/KARCH/RTRAN layout** (CA-*, other WS-* fields unrelated to OUARCH, TR-* fields not touched by
  this program's paragraphs). `OuarchService` only calls a subset of its ~110 getter/setter pairs; the rest are
  dead from OUARCH's point of view. Adding tests that call the remaining accessors directly would just assert
  pass-through plumbing with no business behavior to verify (explicitly the "meaningless test" pattern the test
  skill asks to avoid), so they were left uncovered by design. This is why whole-module C0 (60.2%) is below the
  80% target even though the actual converted logic (`OuarchService`) is at 98.1%.

## 4. Reviewer notes

- JaCoCo 0.8.12 cannot instrument a JDK 26 charset class (`sun.nio.cs.ext.MS932`, class file major version 70)
  that gets lazily loaded by `RecordBuffer.applyInitialValues`. This prints a large stack trace to stderr during
  `verify` but does **not** fail the build or affect the reported coverage numbers for `ouarch` classes — safe
  to ignore, or upgrade the `jacoco-maven-plugin` version if it becomes noisy.
- `ouarch/pom.xml` had no test dependencies or JaCoCo plugin configured (unlike sibling program modules); both
  were added, copied verbatim from `ouactin/pom.xml`'s pattern (`spring-boot-starter-test` + `jacoco-maven-plugin`
  0.8.12 with `prepare-agent`/`report`).
- Tests drive `OuarchService.mainLine(AppService)` end-to-end (no reflection into the private `TaskContext`):
  input/output COMMAREA bytes are built/decoded with a throwaway `OuarchFields` harness that mirrors the
  `aliasGroup("KAR-PARM","CA-WORK-AREA")` the service itself performs, so assertions read real decoded KAR-* values.
