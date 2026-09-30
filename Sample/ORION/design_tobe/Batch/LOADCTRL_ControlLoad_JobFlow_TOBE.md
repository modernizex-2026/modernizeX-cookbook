# TO-BE Job Flow — LOADCTRL

> The TO-BE counterpart of `LOADCTRL_ControlLoad_JobFlow.md`. The step order and the branch
> conditions are preserved; where a step changes form factor it is recorded in §4.

| Item | Value |
|---|---|
| JOBID | LOADCTRL (initial load of the control file) |
| Process name | Initial load of the control file — unchanged |
| Platform | Java 17 · Spring Boot 3.x · Oracle (H2 in dev) — data initialization, not Spring Batch |
| How it is run | one-time database seed at environment set-up (see §4); there is no `batch-runner` job for this load |
| Schedule | not determined (ask the team) — historically a one-time initial load |
| Log ／ monitoring | the database migration / seed log |
| Author | modernizeX |

> **Form-factor change (business impact):** in AS-IS this job was an IDCAMS REPRO utility that copied
> a sequential file into the control-file VSAM cluster. In TO-BE the control file is the relational
> table `ctrlfile`; there is **no Spring Batch job** for this load. The same rows are created by the
> schema/seed step that builds the database. The business content — the initial control totals and
> counters — is the same; only the loading mechanism changed. See §4.

### Job flow diagram

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TD
  S0["Control-file seed — load the initial control rows into ctrlfile"] -->|"normal"| DONE["End — the control file is populated"]
  S0 -->|"error"| E["The seed step fails and the set-up stops; the failing step is reported"]
```

## 1. Step table — 1-to-1 mapping

One bold row per step, one sub-row per I/O.

| Step | Program | I/O | File ／ table | Notes |
|---|---|---|---|---|
| **◆ Overview** |  | ・Overall: | initial control rows → `ctrlfile` |  |
| **◆ P0 Initial load** |  |  |  |  |
| **CTRLSEED** | (no program — data seed) |  |  | was IDCAMS REPRO; now a database seed — see §4 |
|  |  | IN | initial control data (was the sequential dataset) |  |
|  |  | OUT | table `ctrlfile` | keyed by `ct_key`; rows created once |

## 2. Branch conditions & dependencies

| Condition | Flow |
|---|---|
| the seed runs once when the database is first built | the control file must be populated before the on-line and interest/posting functions can use it |
| the seed step must complete before the control file is read anywhere | otherwise a store error is reported and set-up stops |

## 3. Termination & abnormal handling

| Item | Value |
|---|---|
| Normal termination | the seed completes and the control file holds the initial rows (success) |
| Business error | a failed seed stops the set-up and reports the failing step |
| Rerun | re-run the database seed against a fresh (empty) schema |
| Cleanup | drop and recreate the schema to reload from scratch |

## 4. Programs in the job

| # | Program | Module | Detailed document |
|---|---|---|---|
| 1 | (none — IDCAMS REPRO utility) | replaced by the database schema/seed that creates the `ctrlfile` rows | no program design — utility step, no COBOL business logic |
