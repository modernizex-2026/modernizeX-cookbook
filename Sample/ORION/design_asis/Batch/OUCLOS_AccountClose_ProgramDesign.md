# Batch Program Design Document — OUCLOS_AccountClose

**Common header**

| System name | Program ID | Created/updated on／Author | Changed lines |
|---|---|---|---|
| ORION-CCMS (ORION Credit Card Management System) | OUCLOS | Created 2026-09-28／modernizeX | — |

## 1. Program overview

### 1.1 I/O diagram

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    IN_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")] -- "read" --> PG["OUCLOS<br>Account Close"]
    PG -- "U" --> OUT_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")]
    PG -- "counts / status" --> CA[("COMMAREA result area")]
```

### 1.2 Function overview

close expired or flagged accounts on demand. Browses the account master (ACCTFILE); an active account is closed (status set to 'N') when its expiry date is present and earlier than the run date (EXPIRED) or its credit limit is zero (ZEROLIMIT). EXPIRED takes precedence so each closed account maps to one reason. Closed accounts are READ for UPDATE and REWRITTEN; accounts already carrying status 'N' are counted and left untouched. Ports the batch account-close OBACCT2.

### 1.3 I/O overview

| File / table / area | DD / access | Copybook | I/O | Type | Organisation |
|---|---|---|---|---|---|
| Account master | EXEC CICS FILE(WS-ACCTFILE) | RACCT | I-O | Main | VSAM KSDS |
| Request / result area | COMMAREA (linkage) | KCOMM | I-O | Main | linkage |

### 1.4 Called subprograms / modules

| Program ID | Call type | Function | Interface |
|---|---|---|---|
| — | — | This program calls no sub-program | — |

### 1.5 DB tables & SQL

No SQL used — pure VSAM/CICS file I/O.

### 1.6 Special notes

- Invocation: EXEC CICS LINK PROGRAM('OUCLOS') COMMAREA(ORION-COMMAREA) END-EXEC. REQUEST : KO-PARM-ACCT optional single account (0 = all).
- Linked from: OCOPS.
- Result / status: KO-READ read KO-POSTED total closed KO-UPDATE rewritten KO-SKIP already closed KO-C1 closed expired KO-C2 closed zero-limit KO-C3 kept active KO-AMT-1 balance of closed.

## 2. Processing details（処理内容）

### 1. Main（0000-MAIN）　[0.0]　(L84)
1) Main: drive the overall processing — run initialisation, the main work and termination in turn.
2) In turn, carry out: initialise, browse driver, finalise.

### 2. Initialise（1000-INITIALISE）　[1.0]　(L93)
1) Initialise: prepare the work area, clear the result counters and set the initial status.
2) When ko parm acct > ZERO (L110), take the branch for that case.

### 3. Browse driver（3000-BROWSE-DRIVER）　[3.0]　(L124)
1) Browse driver: perform the browse driver step of the processing.
2) When br started (L126), take the branch for that case.
3) In turn, carry out: start browse, read next acct, process acct, end browse.

### 4. Start browse（3100-START-BROWSE）　[3.1]　(L135)
1) Start browse: position a browse on Account master.
2) When filter active (L136), take the branch for that case.
3) Depending on ws resp cd (L147): response = normal, response = not-found, OTHER.

### 5. Read next acct（3200-READ-NEXT-ACCT）　[3.2]　(L161)
1) Read next acct: read the next record from Account master.
2) Depending on ws resp cd (L168): response = normal, response (ENDFILE), OTHER.

### 6. Process acct（4000-PROCESS-ACCT）　[4.0]　(L181)
1) Process acct: perform the process acct step of the processing.
2) When filter active AND ac id NOT = ws filter acct (L182), take the branch for that case.
3) In turn, carry out: check conditions, close acct, read next acct.

### 7. Check conditions（4100-CHECK-CONDITIONS）　[4.1]　(L200)
1) Check conditions: perform the check conditions step of the processing.
2) When ac expiry date NOT = SPACES AND ac expiry date < ws run date (L203), take the branch for that case.

### 8. Close acct（4200-CLOSE-ACCT）　[4.2]　(L216)
1) Close acct: read the Account master record; save the updated Account master record.
2) When ws resp cd NOT = response = normal (L224), take the branch for that case.
3) When ws resp cd = response = normal (L234), take the branch for that case.

### 9. Exit（4200-EXIT）　[4.2]　(L248)
1) Exit: perform the exit step of the processing.

### 10. End browse（3400-END-BROWSE）　[3.4]　(L253)
1) End browse: end the browse on Account master.
2) When br started (L254), take the branch for that case.

### 11. Finalise（9000-FINALISE）　[9.0]　(L263)
1) Finalise: set the final status and return the accumulated counts to the caller.
2) When ko error (L264), take the branch for that case.

## 3. Structure diagram（構造図）

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TB
    N0["Main<br>0000-MAIN<br>0.0"]
    N1["Initialise<br>1000-INITIALISE<br>1.0"]
    N2["Browse driver<br>3000-BROWSE-DRIVER<br>3.0"]
    N3["Start browse<br>3100-START-BROWSE<br>3.1"]
    N4["Read next acct<br>3200-READ-NEXT-ACCT<br>3.2"]
    N5["Process acct<br>4000-PROCESS-ACCT<br>4.0"]
    N6["Check conditions<br>4100-CHECK-CONDITIONS<br>4.1"]
    N7["Close acct<br>4200-CLOSE-ACCT<br>4.2"]
    N8["Exit<br>4200-EXIT<br>4.2"]
    N9["End browse<br>3400-END-BROWSE<br>3.4"]
    N10["Finalise<br>9000-FINALISE<br>9.0"]
    N0 --> N1
    N0 --> N2
    N0 --> N10
    N2 --> N3
    N2 --> N4
    N2 --> N5
    N2 --> N9
    N5 --> N6
    N5 --> N7
    N5 --> N4
```

## 4. Output specifications (file / table)

### 4.1 Account master（RACCT） — 300 bytes (output record)

| Level | Item name | Field name | Type | Bytes | Position | Source | How the value is set |
|---|---|---|---|---|---|---|---|
| 01 | Rec | ACCT-REC | group | 300 | 1 | — | group item |
| 05 | Id | AC-ID | 9(11) | 11 | 1 | Master/COMMAREA | record key |
| 05 | Active Status | AC-ACTIVE-STATUS | X(01) | 1 | 12 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Curr Bal | AC-CURR-BAL | S9(10)V99 | 12 | 13 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Credit Limit | AC-CREDIT-LIMIT | S9(10)V99 | 12 | 25 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Cash Limit | AC-CASH-LIMIT | S9(10)V99 | 12 | 37 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Open Date | AC-OPEN-DATE | X(10) | 10 | 49 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Expiry Date | AC-EXPIRY-DATE | X(10) | 10 | 59 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Reissue Date | AC-REISSUE-DATE | X(10) | 10 | 69 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Cyc Credit | AC-CYC-CREDIT | S9(10)V99 | 12 | 79 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Cyc Debit | AC-CYC-DEBIT | S9(10)V99 | 12 | 91 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Addr Zip | AC-ADDR-ZIP | X(10) | 10 | 103 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Group Id | AC-GROUP-ID | X(10) | 10 | 113 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Filler | FILLER | X(178) | 178 | 123 | Master/COMMAREA | set from the transaction / account being processed |

## 5. In/out parameters & check specifications

### 5.1 Parameters

| Category | Name | Type / length | Meaning | Value / source |
|---|---|---|---|---|
| Linkage (COMMAREA) | ORION-COMMAREA | group (KCOMM) | shared communication area passed on every EXEC CICS LINK | supplied by the calling screen |
| Linkage (KOPS) | KO-FN-POST | — | fn post | request/result field in the work area |
| Linkage (KOPS) | KO-FN-PAY | — | fn pay | request/result field in the work area |
| Linkage (KOPS) | KO-FN-INT | — | fn int | request/result field in the work area |
| Linkage (KOPS) | KO-FN-FEE | — | fn fee | request/result field in the work area |
| Linkage (KOPS) | KO-FN-CHGF | — | fn chgf | request/result field in the work area |
| Linkage (KOPS) | KO-FN-CLOS | — | fn clos | request/result field in the work area |
| Linkage (KOPS) | KO-FN-RNEW | — | fn rnew | request/result field in the work area |
| Linkage (KOPS) | KO-FN-CYCL | — | fn cycl | request/result field in the work area |
| Linkage (KOPS) | KO-PARM-ACCT | 9(11) | parm acct | request/result field in the work area |
| Linkage (KOPS) | KO-PARM-CARD | X(16) | parm card | request/result field in the work area |
| Linkage (KOPS) | KO-PARM-AMT | S9(10)V99 | parm amt | request/result field in the work area |
| Linkage (KOPS) | KO-PARM-DATE | X(10) | parm date | request/result field in the work area |
| Linkage (KOPS) | KO-OK | — | ok | request/result field in the work area |
| Linkage (KOPS) | KO-WARN | — | warn | request/result field in the work area |
| Return | COMMAREA status | flag / text | outcome of the call | set by this program before it returns |

### 5.2 Checks

| No. | Check type | Target | Trigger condition | Action / message |
|---|---|---|---|---|
| 1 | Response | CICS file response | a file response other than normal or not-found | the error is recorded in the COMMAREA status and the browse/step ends |

## 6. Modification summary

| No. | Change date | Title | Summary of the change |
|---|---|---|---|
| 1 | 2026.09.28 | Initial AS-IS reverse design | Document generated by modernizeX from the extracted AST and datastore; no functional change. |

