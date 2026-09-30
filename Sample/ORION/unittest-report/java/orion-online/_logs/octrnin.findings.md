# OCTRNIN — Unit Test Findings

## 1. Verify result

Final `mvn verify`: **Tests run: 30, Failures: 1, Errors: 0, Skipped: 0.**
The 1 failure is an intentional CONVERT-GAP test (see below), not a test defect.
Coverage (JaCoCo line, C0): **OctrninService class: 394/399 = 98.7%**. Module-wide
(incl. generated `OctrninFields` accessor, `OctrninBmsMetadata`, `WorkingStorage`):
911/1699 = **53.6%**, below the 80% target — see §3 for why this is expected and
not something more service-level tests can close.

## 2. CONVERT-GAP test (intentional failure)

`mainLine_enterAmountFilterValidWithCommasAndOneDecimal_convertGap_thresholdLostToZero`

- **COBOL ground truth**: `01 WS-AM-NUM REDEFINES WS-AM-BUILD PIC 9(09)V99.`
  (line 88). `6500-PARSE-AMT` accumulates digits into `WS-AB-INT`/`WS-AB-DEC`
  (= `WS-AM-BUILD`), then `MOVE WS-AM-NUM TO WS-AM-VALUE` reads those same bytes
  via the REDEFINES alias. A valid input like `"1,234.5"` must parse to threshold
  **1234.50**.
- **Java bug**: the generated data layout `OCTRNIN_WS.xml` places `WS-AM-NUM` as a
  field of the unrelated `WS-SYS` group (offset 4, alongside `COMPLETION-CODE`/
  `SQLCODE`) with `redefines="WS-AM-BUILD"` recorded only as inert metadata — the
  accessor's `getDecimal("WS-AM-NUM")` reads `WS-SYS`'s own never-written storage,
  not `WS-AM-BUILD`'s bytes. So `getWsAmNum()` always returns **0.00**, and every
  non-blank amount-filter (mode `'A'`) search silently gets threshold zero instead
  of the value the operator typed.
- **Root cause is in the generated layout XML, not `OctrninService.java`** — fixing
  it requires re-scoping the `WS-AM-NUM` field to alias `WS-AM-BUILD`'s offset/size
  (or regenerating the layout with correct REDEFINES resolution).

No other behavioral differences from COBOL were found; all other filter modes
(C/D/M/T), paging (PF7/PF8), and the numeric parser (`6000-PARSE-NUM`) match the
COBOL ground truth exactly and pass.

## 3. Uncovered remainder

The 46.4% of module lines not covered are entirely inside generated boilerplate:
`OctrninFields` (accessor get/set for ~250 copybook fields, most unused by any
filter/screen path exercised here), `OctrninBmsMetadata` (static BMS map tables),
and `WorkingStorage`. This mirrors the sibling `occrdin` module (same pattern,
~45.6% module-wide C0 with its service class near 79%) — these accessor classes
are not meaningfully unit-testable one field at a time and are exercised
incidentally, not exhaustively, by driving `mainLine()`. `OctrninService` itself
(the actual business logic) is at 98.7% C0; the 5 missed lines are defensive/
unreached formatting branches not reachable via any COBOL-valid input.

## 4. Reviewer note

`jacoco-maven-plugin` and `spring-boot-starter-test` were not present in
`octrnin/pom.xml` and were added (mirroring `occrdin/pom.xml`) so coverage could
be measured — no other production code was modified.
