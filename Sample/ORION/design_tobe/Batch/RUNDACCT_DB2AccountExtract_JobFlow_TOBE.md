# TO-BE Job Flow — RUNDACCT

> The TO-BE counterpart of `RUNDACCT_DB2AccountExtract_JobFlow.md`. The step order and the branch
> conditions are preserved; where a step changes form factor it is recorded in §4.

| Item | Value |
|---|---|
| JOBID | RUNDACCT (DB2 account posting / extract) |
| Process name | DB2 account posting / extract — unchanged in business terms |
| Platform | Java 17 · Spring Boot 3.x · Oracle (H2 in dev) — handled in the database / by the on-line account service, not Spring Batch |
| How it is run | there is no `batch-runner` job for this run; the account posting/extract is handled in the database and by the on-line account browse service (see §4) |
| Schedule | not determined (ask the team) — historically submitted manually or by the operations schedule |
| Log ／ monitoring | the database / application log |
| Author | modernizeX |

> **Form-factor change (business impact):** in AS-IS this job ran an external DB2 plan (`ODACCTP`)
> under TSO batch (`DSN RUN`); the plan has **no COBOL source** in the reg (external module). In TO-BE
> there is **no Spring Batch job** for it — the account rows now live in the relational table
> `acctfile`, and the equivalent posting/extract is handled in the database and by the on-line account
> browse service `OUACTIN`. The business content — the extracted set of accounts — is the same; only
> the run mechanism changed. See §4.

### Job flow diagram

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TD
  S0["Account extract — read the account data and produce the extract"] -->|"normal"| DONE["End — the account extract is produced"]
  S0 -->|"error"| E["The step fails; the run stops and the failing step is reported"]
```

## 1. Step table — 1-to-1 mapping

One bold row per step, one sub-row per I/O.

| Step | Program | I/O | File ／ table | Notes |
|---|---|---|---|---|
| **◆ Overview** |  | ・Overall: | account data (`acctfile`) → account extract |  |
| **◆ P0 Main processing** |  |  |  |  |
| **DACCTP** | (none — DB2 DSN RUN; no COBOL business logic) |  |  | was `IKJEFT01` running DB2 plan `ODACCTP`; now handled in the database / by `OUACTIN` — see §4 |
|  |  | IN | table `acctfile` | keyed by `ac_id`; was the DB2 account tables reached through the plan |
|  |  | OUT | account extract (was `ORION.DB2.ACCT.EXTRACT`) | produced by the account browse / extract logic for downstream consumers |

## 2. Branch conditions & dependencies

| Condition | Flow |
|---|---|
| the account data must exist before the extract is produced | the account master (`acctfile`) is populated by set-up and on-line maintenance before this extract runs |
| the step ends normally (was RC=0 below the COND threshold) | the account extract is produced and the run completes |
| the step fails (was a return code above the COND threshold, or an abend) | the run stops and the failing step is reported |

## 3. Termination & abnormal handling

| Item | Value |
|---|---|
| Normal termination | the account extract is produced and the run completes successfully (was RC=0) |
| Business error | a failure stops the run and the failing step is reported (was a return code above the COND threshold, or an abend captured to CEEDUMP/SYSUDUMP) |
| Rerun | re-run the extract; it reads the current account data and reproduces the output |
| Cleanup | none — the extract is regenerated on each run; no temporary datasets remain |

## 4. Programs in the job

| # | Program | Module | Detailed document |
|---|---|---|---|
| 1 | (none — DB2 DSN RUN; no COBOL business logic) | external DB2 plan `ODACCTP`, replaced by the database and the on-line account browse service `OUACTIN`; the account data lives in `acctfile` | no program design — external DB2 plan, no COBOL business logic |
