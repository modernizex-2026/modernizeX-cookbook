# OCBILLP unit test findings

## 1. Verify result

Final `mvn verify`: **30 passed / 1 failed (intentional) / 0 errors / 0 skipped**, 31 tests total.
JaCoCo configured (plugin added to `pom.xml`, matching sibling modules). Module-wide C0
(line) = 53.8% (866 lines: OcbillpFields accessor + OcbillpBmsMetadata screen metadata drag
the average down — see §3). **`OcbillpService` itself (the actual business logic under
test) is 250/254 lines = 98.4% C0.**

## 2. CONVERT-GAP (intentionally failing test)

`mainLine_postPayment_confirmNumber_convertGap_missingZeroPadding` — **fails on purpose**.

- COBOL `4100-BUILD-CONFIRM`: `STRING 'ORB' WS-ID-11 '00' INTO WS-CONFIRM-NUM`. `WS-ID-11`
  is `PIC 9(11)`, so STRING moves its full zero-padded 11-digit display form. For a bumped
  counter of 42 the expected confirmation number is `"ORB" + "00000000042" + "00"` =
  `"ORB0000000004200"` (16 chars).
- Java `buildConfirmationNumber()` does `sb.append(String.valueOf(ctx.f.getWsId11()))`,
  which yields the bare decimal `"42"` with no zero-padding, producing `"ORB4200"` instead
  (8 chars, missing the 9 padding zeros).
- Contrast: `populatePayScreenFields()` correctly does `String.format("%011d", ...)` for
  the same kind of field (`WS-SV-ACCT-ID`), so this is a localized miss in one paragraph's
  conversion, not a systemic pattern.
- Impact: every posted payment's confirmation number shown to the customer/operator is
  shorter than COBOL's and lacks the zero-padding, so confirmation numbers won't match
  historical/COBOL-produced ones for the same bill id.

## 3. Uncovered remainder

- `OcbillpFields` (0.8k-line generated accessor, all delegating getters/setters over a
  raw record buffer) and `OcbillpBmsMetadata` (screen field registration/button defs) are
  boilerplate produced by the conversion tool, not hand-written business logic — only the
  handful of accessor methods actually touched by the service under test get exercised;
  the rest (190+ unused typed wrappers for fields OCBILLP never references) stay uncovered.
  Not a test gap in the reviewed sense; adding tests for every unused getter/setter would
  be reflection-style busywork per the "meaningless test" rule.
- `OcbillpService`'s 4 remaining missed lines are trivial (e.g. an unreachable branch tail
  after the 3rd verify's budget was spent investigating higher-value gaps first).

## 4. Reviewer notes

- `back-end/programs/ocbillp/pom.xml` was updated to add `spring-boot-starter-test`,
  `maven-surefire-plugin` (testFailureIgnore) and `jacoco-maven-plugin` — it had neither
  before, unlike sibling modules (e.g. `ocaccta`) that already carry this config.
- The 3-verify budget was spent as: (1) initial run — 23/31 failed, root cause: test
  helper never seeded `WS-STATE-AREA`/`WS-STAGE` into `CA-WORK-AREA`, so every
  ENTER-key test silently fell through to the "unknown stage" branch; (2) fixed that,
  reran — 12/31 errored, root cause: file-name constants held field names
  (`"WS-ACCTFILE"`) instead of the field's actual string value (`"ACCTFILE"`); (3) fixed,
  reran — 30/31 pass, the 1 failure being the intentional CONVERT-GAP above.
