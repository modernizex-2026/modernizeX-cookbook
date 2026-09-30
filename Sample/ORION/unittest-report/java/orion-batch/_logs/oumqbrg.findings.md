# OUMQBRG unit test findings

## 1. Verify result

`mvn -q -pl programs/oumqbrg verify` completed (testFailureIgnore=true in the
module pom, so the build reports success). Surefire: **6 tests run, 3 passed,
3 intentionally-failing CONVERT-GAP tests**, 0 errors, 0 skipped. C0 line
coverage (JaCoCo, `OumqbrgService`): **168/174 = 96.5%** (≥ 80% threshold).

## 2. CONVERT-GAP: unconditional paragraph fall-through

Every generated paragraph method in `OumqbrgService` ends with an
unconditional direct call to "the next paragraph in source order" (a
`// fall-through to next paragraph` comment followed by a bare method call).
This pattern is meant for COBOL code that relies on physical fall-through
(no PERFORM/GOTO), but **OUMQBRG never relies on fall-through** — every
paragraph is invoked strictly via `PERFORM` and returns normally. The
generated fall-through calls are therefore spurious extra executions that
cascade paragraphs COBOL invokes once (or conditionally) into running many
times, out of order, and regardless of guard flags. Concretely:

- `_1000Initialize` calls `_1100MqConnect()` a second time, unconditionally,
  after already running it via `runChain`.
- `_1100MqConnect` → `_1200MqOpenOut` → `_2000BridgeTrans` → `_2100ReadTran`
  → `_2200PutTran` → `_3000Finalize` → `_3100MqCloseOut` →
  `_3200MqDisconnect` → `_3900DisplaySummary` chain unconditionally into one
  another, so a single `execute()` call performs dozens of duplicate
  MQCONN/MQOPEN/MQPUT/MQCLOSE/MQDISC and log-summary executions.

Three tests encode the COBOL-correct behavior and **fail on purpose** to
document the gap:

- `execute_tranFileOpenFails_convertGapStillPutsToQueue` — COBOL: when
  `OPEN INPUT TRAN-FILE` fails, `TRAN-OPEN` is never set, so `0000-MAIN`'s
  `PERFORM 2000-BRIDGE-TRANS UNTIL END-OF-TRAN` runs zero times and MQPUT is
  never reached. Java: `mq.mqPut(...)` is still invoked, because
  `2100-READ-TRAN`'s translation falls straight into `2200-PUT-TRAN`
  regardless of file-open state.
- `execute_mqOpenFails_convertGapStillPutsTransactions` — COBOL: when MQOPEN
  fails, `2000-BRIDGE-TRANS`'s `IF MQ-IS-UP AND MQ-Q-OPEN` guard is false for
  the whole run, so MQPUT never happens. Java: MQPUT still happens, via the
  same unconditional `2100-READ-TRAN` → `2200-PUT-TRAN` fall-through (this
  guard is bypassed entirely, not just weakened).
- `execute_mqOpenFails_convertGapStillClosesUnopenedQueue` — COBOL guards
  `3100-MQ-CLOSE-OUT` with `IF MQ-Q-OPEN`. Java: `3000-FINALIZE`'s
  translation ends with an unconditional fall-through call to
  `_3100MqCloseOut()`, outside that guard, so `mq.mqClose(...)` fires even
  though the queue was never opened.

**Reviewer must know**: this is not a narrow edge case — it affects every
run of the program (the happy path also performs many duplicate MQ calls and
duplicate summary log lines; visible in surefire's captured log output).
Because exact call counts are an artifact of this bug rather than a stable
contract, the passing tests deliberately assert `atLeastOnce()`/business-state
outcomes (via reflection on the private `ws` field) instead of exact
`verify(...)` counts.

## 3. Secondary CONVERT-GAP (not separately tested): MQCONN/MQDISC not wired

`_1100MqConnect`/`_3200MqDisconnect` call private no-op stub methods
(`mqconn()`/`mqdisc()`) instead of the injected `MqService mq` — unlike
MQOPEN/MQPUT/MQCLOSE, which correctly delegate to `mq`. MQ-CC/MQ-RC are
therefore never touched by these two verbs, so MQCONN can never be exercised
as failing through the public API. Noted for the reviewer; not covered by a
dedicated test since it cannot be triggered without reflection/bytecode
tricks, which the "no PowerMock" constraint rules out.

## 4. Uncovered remainder

Remaining 6 missed lines in `OumqbrgService` are minor logging branches on
paths not exercised (e.g. non-'00'/'10' READ file-status logging, MQCLOSE
non-'00' close-file-status logging) — reachable but not worth a dedicated
test given the fall-through bug already dominates behavior on those paths.
`OumqbrgJobFlow` (Spring Batch wiring), `OumqbrgFieldAccess`,
`TranFileDataset`, `OumqbrgDatasets` are thin generated wiring/accessor
classes exercised indirectly through `OumqbrgService`'s tests but not
JaCoCo-tracked as separate rows (no dedicated unit tests written for them,
per the skill's "logic chính ở Service" guidance and the no-Spring-context
constraint for the Configuration class).
