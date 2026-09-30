# Batch Program Design Document — OUCHGF_ChargeOff

**Common header**

| System name | Program ID | Created/updated on／Author | Changed lines |
|---|---|---|---|
| ORION-CCMS (ORION Credit Card Management System) | OUCHGF | Created 2026-09-28／modernizeX | — |

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
    IN_WS_CTRLFILE[("Control file<br>WS-CTRLFILE")] -- "read" --> PG["OUCHGF<br>Charge-Off Processing"]
    IN_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")] -- "read" --> PG["OUCHGF<br>Charge-Off Processing"]
    PG -- "C/U" --> OUT_WS_CTRLFILE[("Control file<br>WS-CTRLFILE")]
    PG -- "U" --> OUT_WS_ACCTFILE[("Account master<br>WS-ACCTFILE")]
    PG -- "C" --> OUT_WS_TRANFILE[("Transaction file<br>WS-TRANFILE")]
    PG -- "counts / status" --> CA[("COMMAREA result area")]
```

### 1.2 Function overview

write off severely delinquent accounts on demand. Browses the account master (ACCTFILE); an account is charged off when it is active, carries a positive balance materially over the credit limit (balance > limit * 1.20) and has taken no payment this cycle (cycle credit = 0). For each such account a reversing charge-off transaction (type 'CO', amount = -balance) is WRITTEN to TRANFILE, the account is READ for UPDATE, its status set to 'C' with a zeroed balance, and it is REWRITTEN. Ports the batch charge-off OBCHGOFF.

### 1.3 I/O overview

| File / table / area | DD / access | Copybook | I/O | Type | Organisation |
|---|---|---|---|---|---|
| Control file | EXEC CICS FILE(WS-CTRLFILE) | RCTRL | I-O | Main | VSAM KSDS |
| Account master | EXEC CICS FILE(WS-ACCTFILE) | RACCT | I-O | Sub | VSAM KSDS |
| Transaction file | EXEC CICS FILE(WS-TRANFILE) | RTRAN | O | Sub | VSAM KSDS |
| Request / result area | COMMAREA (linkage) | KCOMM | I-O | Main | linkage |

### 1.4 Called subprograms / modules

| Program ID | Call type | Function | Interface |
|---|---|---|---|
| — | — | This program calls no sub-program | — |

### 1.5 DB tables & SQL

No SQL used — pure VSAM/CICS file I/O.

### 1.6 Special notes

- Invocation: EXEC CICS LINK PROGRAM('OUCHGF') COMMAREA(ORION-COMMAREA) END-EXEC. REQUEST : KO-PARM-ACCT optional single account (0 = all).
- Linked from: OCOPS.
- Result / status: KO-READ read KO-SELECT charged off KO-UPDATE rewritten KO-TRAN adjustments KO-AMT-1 total charged-off amount.

## 2. Processing details（処理内容）

### 1. Main（0000-MAIN）　[0.0]　(L102)
1) Main: drive the overall processing — run initialisation, the main work and termination in turn.
2) In turn, carry out: initialise, load counter, browse driver, save counter, finalise.

### 2. Initialise（1000-INITIALISE）　[1.0]　(L113)
1) Initialise: prepare the work area, clear the result counters and set the initial status.
2) When ko parm acct > ZERO (L130), take the branch for that case.

### 3. Load counter（1500-LOAD-COUNTER）　[1.5]　(L147)
1) Load counter: read the Control file record.
2) When ws resp cd = response = normal (L158), take the branch for that case.

### 4. Browse driver（3000-BROWSE-DRIVER）　[3.0]　(L168)
1) Browse driver: perform the browse driver step of the processing.
2) When br started (L170), take the branch for that case.
3) In turn, carry out: start browse, read next acct, process acct, end browse.

### 5. Start browse（3100-START-BROWSE）　[3.1]　(L179)
1) Start browse: position a browse on Account master.
2) When filter active (L180), take the branch for that case.
3) Depending on ws resp cd (L191): response = normal, response = not-found, OTHER.

### 6. Read next acct（3200-READ-NEXT-ACCT）　[3.2]　(L205)
1) Read next acct: read the next record from Account master.
2) Depending on ws resp cd (L212): response = normal, response (ENDFILE), OTHER.

### 7. Process acct（4000-PROCESS-ACCT）　[4.0]　(L225)
1) Process acct: perform the process acct step of the processing.
2) When filter active AND ac id NOT = ws filter acct (L226), take the branch for that case.
3) In turn, carry out: charge off, read next acct.

### 8. Charge off（4100-CHARGE-OFF）　[4.1]　(L242)
1) Charge off: perform the charge off step of the processing.
2) In turn, carry out: post adjustment, update acct.

### 9. Post adjustment（4200-POST-ADJUSTMENT）　[4.2]　(L251)
1) Post adjustment: add a record to the Transaction file.
2) When ws resp cd = response = normal (L275), take the branch for that case.

### 10. Update acct（4300-UPDATE-ACCT）　[4.3]　(L281)
1) Update acct: read the Account master record; save the updated Account master record.
2) When ws resp cd NOT = response = normal (L289), take the branch for that case.
3) When ws resp cd = response = normal (L301), take the branch for that case.

### 11. Exit（4300-EXIT）　[4.3]　(L309)
1) Exit: perform the exit step of the processing.

### 12. End browse（3400-END-BROWSE）　[3.4]　(L314)
1) End browse: end the browse on Account master.
2) When br started (L315), take the branch for that case.

### 13. Save counter（5500-SAVE-COUNTER）　[5.5]　(L324)
1) Save counter: read the Control file record; save the updated Control file record; add a record to the Control file.
2) When ko tran cnt = ZERO (L325), take the branch for that case.
3) When ws resp cd = response = normal (L336), take the branch for that case.

### 14. Exit（5500-EXIT）　[5.5]　(L354)
1) Exit: perform the exit step of the processing.

### 15. Finalise（9000-FINALISE）　[9.0]　(L359)
1) Finalise: set the final status and return the accumulated counts to the caller.
2) When ko error (L360), take the branch for that case.

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
    N2["Load counter<br>1500-LOAD-COUNTER<br>1.5"]
    N3["Browse driver<br>3000-BROWSE-DRIVER<br>3.0"]
    N4["Start browse<br>3100-START-BROWSE<br>3.1"]
    N5["Read next acct<br>3200-READ-NEXT-ACCT<br>3.2"]
    N6["Process acct<br>4000-PROCESS-ACCT<br>4.0"]
    N7["Charge off<br>4100-CHARGE-OFF<br>4.1"]
    N8["Post adjustment<br>4200-POST-ADJUSTMENT<br>4.2"]
    N9["Update acct<br>4300-UPDATE-ACCT<br>4.3"]
    N10["Exit<br>4300-EXIT<br>4.3"]
    N11["End browse<br>3400-END-BROWSE<br>3.4"]
    N12["Save counter<br>5500-SAVE-COUNTER<br>5.5"]
    N13["Exit<br>5500-EXIT<br>5.5"]
    N14["Finalise<br>9000-FINALISE<br>9.0"]
    N0 --> N1
    N0 --> N2
    N0 --> N3
    N0 --> N12
    N0 --> N14
    N3 --> N4
    N3 --> N5
    N3 --> N6
    N3 --> N11
    N6 --> N7
    N6 --> N5
    N7 --> N8
    N7 --> N9
```

## 4. Output specifications (file / table)

### 4.1 Control file（RCTRL） — 60 bytes (output record)

| Level | Item name | Field name | Type | Bytes | Position | Source | How the value is set |
|---|---|---|---|---|---|---|---|
| 01 | Rec | CTRL-REC | group | 60 | 1 | — | group item |
| 05 | Key | CT-KEY | X(08) | 8 | 1 | Master/COMMAREA | record key |
| 05 | Last Value | CT-LAST-VALUE | 9(11) | 11 | 9 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Desc | CT-DESC | X(30) | 30 | 20 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Filler | FILLER | X(11) | 11 | 50 | Master/COMMAREA | set from the transaction / account being processed |

### 4.2 Account master（RACCT） — 300 bytes (output record)

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

### 4.3 Transaction file（RTRAN） — 350 bytes (output record)

| Level | Item name | Field name | Type | Bytes | Position | Source | How the value is set |
|---|---|---|---|---|---|---|---|
| 01 | Rec | TRAN-REC | group | 350 | 1 | — | group item |
| 05 | Id | TR-ID | X(16) | 16 | 1 | Master/COMMAREA | record key |
| 05 | Type Cd | TR-TYPE-CD | X(02) | 2 | 17 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Cat Cd | TR-CAT-CD | 9(04) | 4 | 19 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Source | TR-SOURCE | X(10) | 10 | 23 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Desc | TR-DESC | X(100) | 100 | 33 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Amt | TR-AMT | S9(09)V99 | 11 | 133 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Merchant Id | TR-MERCHANT-ID | 9(09) | 9 | 144 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Merchant Name | TR-MERCHANT-NAME | X(50) | 50 | 153 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Merchant City | TR-MERCHANT-CITY | X(50) | 50 | 203 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Merchant Zip | TR-MERCHANT-ZIP | X(10) | 10 | 253 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Card Num | TR-CARD-NUM | X(16) | 16 | 263 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Orig Ts | TR-ORIG-TS | X(26) | 26 | 279 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Proc Ts | TR-PROC-TS | X(26) | 26 | 305 | Master/COMMAREA | set from the transaction / account being processed |
| 05 | Filler | FILLER | X(20) | 20 | 331 | Master/COMMAREA | set from the transaction / account being processed |

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

