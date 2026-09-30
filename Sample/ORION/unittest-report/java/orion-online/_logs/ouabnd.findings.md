# OUABND — Unit Test Findings

## 1. Verify result
`mvn verify` completed successfully (exit 0, `testFailureIgnore=true`).
**Tests run: 9, Passed: 8, Failed: 1 (intentional CONVERT-GAP), Skipped: 0.**
C0 line coverage (module-wide, JaCoCo): **80.2%** (65 covered / 81 total lines).
`OuabndService` itself (the class under test): **100%** line coverage.

## 2. CONVERT-GAP test (intentionally failing)

**`mainLine_abendCall_shouldPassResolvedAbcodeFieldValuePerCobol`** — FAILS on purpose.

- **COBOL** (`OUABND.cbl` line 17, 36-39): `WS-ABCODE PIC X(04) VALUE 'OABN'`; `EXEC CICS ABEND ABCODE(WS-ABCODE)` resolves the data-name to its runtime value — the abend is always tagged `'OABN'`.
- **Java** (`OuabndService.abendProgram`, line 97): `ctx.appService.abend("WS-ABCODE")` passes the literal field-**name** string `"WS-ABCODE"` instead of resolving `ctx.f.getWsAbcode()` (`"OABN"`). Confirmed via `AppRunner.abend()`, which does `throw new AbendException(abcode)` with no name-to-value resolution.
- **Impact:** every abend raised by OUABND is tagged `"WS-ABCODE"` in production instead of `"OABN"`. Any downstream abend-code-based routing/monitoring/dashboards keyed on `OABN` will silently miss these events.
- **Fix suggestion:** change the call to `ctx.appService.abend(ctx.f.getWsAbcode())`.

## 3. Uncovered remainder

- `OuabndFields` (12/26 lines missed): typed wrapper getters/setters never exercised by OUABND's business logic — `getCompletionCode/setCompletionCode`, `getKaRespCd/setKaRespCd`, `getKaReasCd/setKaReasCd`, `getSqlcode/setSqlcode`, `getWsAbcode/setWsAbcode`. COBOL's `0000-MAIN` never reads/writes these fields directly (KA-RESP-CD/KA-REAS-CD are LINKAGE fields set only by the *caller*, not OUABND). Low value to cover — pure boilerplate delegation to the string-key API already covered elsewhere.
- `OuabndBmsMetadata` (4/6 lines missed): `getMapNames()`, `getLayoutResource()`, `getFieldMapping()` — tooling/smoke-test helpers not invoked from `OuabndService.mainLine`/`AppProgram` contract paths actually exercised. `getButtonDefs()`/`registerFsetFields()` (the two methods `OuabndService` actually delegates to) are covered.

## 4. Notes for reviewer

- `ouabnd/pom.xml` was missing `spring-boot-starter-test` and the `surefire`/`jacoco-maven-plugin` build block present in sibling program modules — added to match convention (required for compilation/coverage to run at all).
- JaCoCo prints `IllegalClassFormatException` stack traces for JDK-internal `sun.nio.cs.ext.MS932` classes during instrumentation (JDK/JaCoCo 0.8.12 class-file-version mismatch). This is noise from instrumenting the JVM's own charset classes triggered by the layout buffer's MS932 charset use — it does not affect test results or the coverage numbers reported above.
- Two edge tests (`mainLine_objectArrayCommareaWithNonByteElement...`, `mainLine_eibcalenPositiveButCommareaNull...`) cover Java-only COMMAREA-marshaling plumbing with no COBOL correspondence (COBOL's LINKAGE SECTION is statically typed; the polymorphic `Object`/`Object[]`/`byte[]` dispatch is a framework artifact of the web-runtime port).
