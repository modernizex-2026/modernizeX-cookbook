# TO-BE Job Flow — LOADTTYP

> The TO-BE counterpart of `LOADTTYP_TranTypeLoad_JobFlow.md`. The step order and the branch
> conditions are preserved; where a step changes form factor it is recorded in §4.

| Item | Value |
|---|---|
| JOBID | LOADTTYP (initial load of the transaction-type reference data) |
| Process name | Initial load of the transaction-type reference data — unchanged |
| Platform | Java 17 · Spring Boot 3.x · Oracle (H2 in dev) — data initialization, not Spring Batch |
| How it is run | one-time database seed at environment set-up (see §4); there is no `batch-runner` job for this load |
| Schedule | not determined (ask the team) — historically a one-time initial load |
| Log ／ monitoring | the database migration / seed log |
| Author | modernizeX |

> **Form-factor change (business impact):** in AS-IS this job was an IDCAMS REPRO utility that copied
> a sequential file into the transaction-type VSAM cluster. In TO-BE the transaction-type reference
> data is the relational table `ttypfile`; there is **no Spring Batch job** for this load. The same rows
> are created by the schema/seed step that builds the database. The business content — the initial set
> of transaction types — is the same; only the loading mechanism changed. See §4.

### Job flow diagram

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TD
  S0["Transaction-type seed — load the initial transaction-type rows into ttypfile"] -->|"normal"| DONE["End — the transaction-type reference data is populated"]
  S0 -->|"error"| E["The seed step fails and the set-up stops; the failing step is reported"]
```

## 1. Step table — 1-to-1 mapping

One bold row per step, one sub-row per I/O.

| Step | Program | I/O | File ／ table | Notes |
|---|---|---|---|---|
| **◆ Overview** |  | ・Overall: | initial transaction-type rows → `ttypfile` |  |
| **◆ P0 Initial load** |  |  |  |  |
| **TTYPSEED** | (no program — data seed) |  |  | was IDCAMS REPRO; now a database seed — see §4 |
|  |  | IN | initial transaction-type data (was the sequential dataset) |  |
|  |  | OUT | table `ttypfile` | keyed by `tt_cd`; rows created once |

## 2. Branch conditions & dependencies

| Condition | Flow |
|---|---|
| the seed runs once when the database is first built | the transaction-type reference data must be populated before the on-line and posting functions can use it |
| the seed step must complete before a transaction type is read anywhere | otherwise a store error is reported and set-up stops |

## 3. Termination & abnormal handling

| Item | Value |
|---|---|
| Normal termination | the seed completes and the transaction-type reference data holds the initial rows (success) |
| Business error | a failed seed stops the set-up and reports the failing step |
| Rerun | re-run the database seed against a fresh (empty) schema |
| Cleanup | drop and recreate the schema to reload from scratch |

## 4. Programs in the job

| # | Program | Module | Detailed document |
|---|---|---|---|
| 1 | (none — IDCAMS REPRO utility) | replaced by the database schema/seed that creates the `ttypfile` rows | no program design — utility step, no COBOL business logic |
