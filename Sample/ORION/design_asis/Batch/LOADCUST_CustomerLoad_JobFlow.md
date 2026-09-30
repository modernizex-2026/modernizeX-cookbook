# Job flow — LOADCUST

| Item | Content |
|---|---|
| JOBID | LOADCUST（`LOADCUST.jcl`） |
| Process name（処理名称） | Initial IDCAMS REPRO load of the customer KSDS |
| System / Category | ORION-CCMS (ORION Credit Card Management System) / Batch (IBM z/OS mainframe) |
| Runtime environment (current) | z/OS JCL — IDCAMS utility |
| Job data source | JCL (`datastore/jcl_jobs.json` ／ `LOADCUST.jcl`) |
| Launch command / schedule | Submitted manually / by the operations schedule — ※ not determined (ask the team) |

### Job flow diagram

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TD
  JOB0["JOB LOADCUST"]
  S0["REPRO — IDCAMS"]
  JOB0 --> S0
  E["Abnormal end — the job stops and the failing step is reported (COND / RC check)"]
  S0 -->|"normal"| DONE["End — normal completion"]
  S0 -->|"error"| E
```

> **Symbol legend**: ★＝the step runs a program that has its own program design in this folder ｜ IN:／OUT:／SYSIN:／ENV:／LOG:＝DD direction ｜ `(concat)`＝library/dataset concatenation ｜ `&&name`＝temporary dataset (deleted at step end) ｜ SYSOUT＝report/log output

| Step | Line | Processing | ＤＤ | Dataset / File | Notes |
|---|---|---|---|---|---|
| **JOB** | L1 | JOB card — CLASS=A, MSGCLASS=X, REGION=0M | ENV:JOBLIB | `ORION.LOADLIB` | load library |
| **◆ Overview** |  | Initial IDCAMS REPRO load of the customer KSDS. | ・Overall: | `ORION.CUSTFILE.SEQ → updated tables / report` |  |
|  |  |  | ・P0: | `1 step(s)` |  |
|  |  |  | ・Input: | `ORION.CUSTFILE.SEQ, ORION.CUSTFILE` |  |
|  |  |  | ・Output: | `updated VSAM/DB2 + SYSOUT report` |  |
| **◆ P0 Main processing** |  | the job's regular run |  |  |  |
| **REPRO** | L0 | IDCAMS utility step — VSAM cluster define / REPRO load (no business COBOL logic). |  |  | PGM=IDCAMS |
|  | L3 |  | LOG:SYSPRINT |  | SYSOUT=* |
|  | L4 |  | IN:SEQIN | `ORION.CUSTFILE.SEQ` | DISP=SHR |
|  | L5 |  | IN:VSAMOUT | `ORION.CUSTFILE` | DISP=SHR |
|  | L6 |  | SYSIN:SYSIN | `(instream)` |  |
| **End** | - | Normal completion sets RC=0; a step return code above the COND threshold stops the job and the failing step is reported. Abends are captured to CEEDUMP/SYSUDUMP. | LOG:- | `SYSOUT / MSGCLASS` | - |
