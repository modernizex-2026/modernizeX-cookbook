# OCACTIN — Unit Test Findings

## 1. Verify result

`mvn verify` (3rd/final run, budget exhausted): **Tests run: 26, Failures: 1, Errors: 1, Skipped: 0**, build exit 0
(surefire `testFailureIgnore=true`). C0 line coverage (JaCoCo, whole module):
**978 / 1965 = 49.8%** overall, but the actual business logic class
`OcactinService` alone is **393/405 = 97.0%** covered; `OcactinBmsMetadata`
is 98.6% covered. The remainder (`OcactinFields`, 970/1180 lines missed) is
pure generated BMS field-accessor boilerplate — see §3.

## 2. CONVERT-GAP test (intentional failure)

`mainLine_enterLowercaseDelFilter_convertGap_shouldBeAcceptedCaseInsensitivePerCobol` **fails as designed**.

- **COBOL** (`6000-PARSE-FILTER`): `INSPECT WS-FILT-IN CONVERTING 'abcdefghijklmnopqrstuvwxyz' TO 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'` translates each lowercase character individually, so operator input `"del"` becomes `"DEL"` and is accepted as the DELINQUENT filter.
- **Java** (`parseFilterInput`): `wsFiltIn.replace("abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ")` looks for the *entire 26-character alphabet* as one literal substring inside a 10-character field — that can never match, so the `.replace()` is a permanent no-op. Lowercase filter input is never upper-cased, so `"del"` fails to match any key in `FILTER_CODE_TO_STATUS` (all uppercase) and is rejected with "Filter not recognised" instead of running the DELINQUENT page.
- **Impact**: any operator typing a lowercase filter keyword gets an incorrect "bad filter" error where COBOL would have accepted it. Real functional regression, not a test bug.

## 3. Uncovered remainder

`OcactinFields` (auto-generated BMS field accessor) has ~970 uncovered lines: these are the `*_a` (attribute), `*_f` (FAC), and `*_i` (initial-value) accessor variants for all 13 screen rows × 6 columns, plus unused `*L` length setters. `OcactinService` never touches these — only the `*_o` (output) accessors are used by the business logic. Exercising them would mean testing raw generated getters/setters with no behavior to protect (violates the "test vô nghĩa" rule), so they were intentionally left uncovered rather than padded with meaningless assertions.

## 4. Reviewer must know

- One test errors in this environment: `registerFsetFields_registersFiltFieldOnMactina` throws `MockitoException: cannot mock class com.appruntime.AppRunner` (ByteBuddy inline-mock instrumentation failure on this JDK/Mockito combo) — an environment/tooling limitation, not a code defect. `AppRunner` may need to be mocked via a subclass/spy or an interface extraction if this needs to pass here.
- `ocactin`'s `pom.xml` was missing the `spring-boot-starter-test` dependency and the `surefire`/`jacoco-maven-plugin` build config present in sibling modules (e.g. `ocacctl`) — added both so `verify`/JaCoCo could run at all.
- Test file: `back-end/programs/ocactin/src/test/java/com/generated/orion/ocactin/service/OcactinServiceTest.java` (26 tests, mocks `AppService`, simulates the `OUACTIN` browse sub-program via a `link()` stub that mutates the commarea byte[] in place).
