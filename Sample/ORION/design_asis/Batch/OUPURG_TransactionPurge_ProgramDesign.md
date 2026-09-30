# Batch Program Design Document — OUPURG_TransactionPurge

**Common header**

| System name | Program ID | Created/updated on／Author | Changed lines |
|---|---|---|---|
| ORION-CCMS (ORION Credit Card Management System) | OUPURG | Created 2026-09-28／modernizeX | — |

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
    IN_WS_TRANFILE[("Transaction file<br>WS-TRANFILE")] -- "read" --> PG["OUPURG<br>Purge Aged Transactions"]
    PG -- "D" --> OUT_WS_TRANFILE[("Transaction file<br>WS-TRANFILE")]
    PG -- "counts / status" --> CA[("COMMAREA result area")]
```

### 1.2 Function overview

on-line port of batch OBPURG. Browse TRANFILE in key order; transactions dated before the cutoff are captured (key + amount) into a bounded save table and those on or after the cutoff are counted as kept. After the browse ends each captured key is removed with DELETE FROM TRANFILE. Counts and money totals are returned in the KPURG block that is overlaid on the shared

### 1.3 I/O overview

| File / table / area | DD / access | Copybook | I/O | Type | Organisation |
|---|---|---|---|---|---|
| Transaction file | EXEC CICS FILE(WS-TRANFILE) | RTRAN | I-O | Main | VSAM KSDS |
| Request / result area | COMMAREA (linkage) | KCOMM | I-O | Main | linkage |

### 1.4 Called subprograms / modules

| Program ID | Call type | Function | Interface |
|---|---|---|---|
| — | — | This program calls no sub-program | — |

### 1.5 DB tables & SQL

No SQL used — pure VSAM/CICS file I/O.

### 1.6 Special notes

- Invocation: EXEC CICS LINK PROGRAM('OUPURG') COMMAREA(ORION-COMMAREA) LENGTH(LENGTH OF ORION-COMMAREA) END-EXEC. FILES : TRANFILE browse and DELETE. RETURN : ends with GOBACK (subroutine convention). MAINTENANCE LOG 0001 2026-08-07 Original - on-line transaction purge sub.
- Linked from: OCUTIL.

## 2. Processing details（処理内容）

### 1. Main（0000-MAIN）　[0.0]　(L75)
1) Main: drive the overall processing — run initialisation, the main work and termination in turn.
2) When kpg status NOT = '99' (L78), take the branch for that case.
3) In turn, carry out: init, position, scan loop, peek next, end browse, purge saved, set status.

### 2. Init（1000-INIT）　[1.0]　(L94)
1) Init: prepare the work area, clear the result counters and set the initial status.
2) When ws max > ZEROS AND ws max < ws max save (L107), take the branch for that case.
3) When ws dt ok NOT = 'Y' (L112), take the branch for that case.
4) In turn, carry out: chk date.

### 3. Chk date（1100-CHK-DATE）　[1.1]　(L119)
1) Chk date: perform the chk date step of the processing.
2) When ws dt(5:1) = '-' AND ws dt(8:1) = '-' (L122), take the branch for that case.

### 4. Position（2000-POSITION）　[2.0]　(L132)
1) Position: position a browse on Transaction file.
2) When kpg start tran = SPACES OR kpg start tran = low values (L133), take the branch for that case.
3) Depending on ws resp cd (L144): response = normal, response = not-found, response (ENDFILE), OTHER.

### 5. Scan loop（3000-SCAN-LOOP）　[3.0]　(L158)
1) Scan loop: perform the scan loop step of the processing.
2) When NOT ws eof yes (L160), take the branch for that case.
3) In turn, carry out: read tran, classify.

### 6. Read tran（3100-READ-TRAN）　[3.1]　(L175)
1) Read tran: read the next record from Transaction file.
2) Depending on ws resp cd (L182): response = normal, response (ENDFILE), OTHER.

### 7. Classify（3200-CLASSIFY）　[3.2]　(L196)
1) Classify: classify the current item and set the corresponding indicator.
2) When ws proc date < kpg cutoff (L199), take the branch for that case.

### 8. Peek next（4000-PEEK-NEXT）　[4.0]　(L213)
1) Peek next: read the next record from Transaction file.
2) When ws eof yes (L214), take the branch for that case.
3) Depending on ws resp cd (L224): response = normal, response (ENDFILE), OTHER.

### 9. Exit（4000-EXIT）　[4.0]　(L234)
1) Exit: perform the exit step of the processing.

### 10. End browse（5000-END-BROWSE）　[5.0]　(L239)
1) End browse: end the browse on Transaction file.

### 11. Purge saved（5500-PURGE-SAVED）　[5.5]　(L247)
1) Purge saved: perform the purge saved step of the processing.
2) In turn, carry out: inline, purge one.

### 12. Purge one（5600-PURGE-ONE）　[5.6]　(L255)
1) Purge one: delete the Transaction file record.
2) When ws resp cd = response = normal (L262), take the branch for that case.

### 13. Set status（6000-SET-STATUS）　[6.0]　(L271)
1) Set status: perform the set status step of the processing.
2) When kpg status = '99' (L272), take the branch for that case.
3) When kpg read = ZEROS (L275), take the branch for that case.

### 14. Exit（6000-EXIT）　[6.0]　(L282)
1) Exit: perform the exit step of the processing.

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
    N1["Init<br>1000-INIT<br>1.0"]
    N2["Chk date<br>1100-CHK-DATE<br>1.1"]
    N3["Position<br>2000-POSITION<br>2.0"]
    N4["Scan loop<br>3000-SCAN-LOOP<br>3.0"]
    N5["Read tran<br>3100-READ-TRAN<br>3.1"]
    N6["Classify<br>3200-CLASSIFY<br>3.2"]
    N7["Peek next<br>4000-PEEK-NEXT<br>4.0"]
    N8["Exit<br>4000-EXIT<br>4.0"]
    N9["End browse<br>5000-END-BROWSE<br>5.0"]
    N10["Purge saved<br>5500-PURGE-SAVED<br>5.5"]
    N11["Purge one<br>5600-PURGE-ONE<br>5.6"]
    N12["Set status<br>6000-SET-STATUS<br>6.0"]
    N13["Exit<br>6000-EXIT<br>6.0"]
    N0 --> N1
    N0 --> N3
    N0 --> N4
    N0 --> N7
    N0 --> N9
    N0 --> N10
    N0 --> N12
    N1 --> N2
    N4 --> N5
    N4 --> N6
    N10 --> N11
```

## 4. Output specifications (file / table)

### 4.1 Transaction file（RTRAN） — 350 bytes (output record)

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
| Linkage (KPURG) | KPG-CUTOFF | X(10) | cutoff | request/result field in the work area |
| Linkage (KPURG) | KPG-START-TRAN | X(16) | start tran | request/result field in the work area |
| Linkage (KPURG) | KPG-MAX | 9(07) | max | request/result field in the work area |
| Linkage (KPURG) | KPG-STATUS | X(02) | status | request/result field in the work area |
| Linkage (KPURG) | KPG-MSG | X(50) | msg | request/result field in the work area |
| Linkage (KPURG) | KPG-READ | 9(07) | read | request/result field in the work area |
| Linkage (KPURG) | KPG-PURGED | 9(07) | purged | request/result field in the work area |
| Linkage (KPURG) | KPG-KEPT | 9(07) | kept | request/result field in the work area |
| Linkage (KPURG) | KPG-ERRORS | 9(07) | errors | request/result field in the work area |
| Linkage (KPURG) | KPG-PURGE-AMT | S9(13)V99 | purge amt | request/result field in the work area |
| Linkage (KPURG) | KPG-KEEP-AMT | S9(13)V99 | keep amt | request/result field in the work area |
| Linkage (KPURG) | KPG-NEXT-TRAN | X(16) | next tran | request/result field in the work area |
| Linkage (KPURG) | KPG-MORE | X(01) | more | request/result field in the work area |
| Return | COMMAREA status | flag / text | outcome of the call | set by this program before it returns |

### 5.2 Checks

| No. | Check type | Target | Trigger condition | Action / message |
|---|---|---|---|---|
| 1 | Response | CICS file response | a file response other than normal or not-found | the error is recorded in the COMMAREA status and the browse/step ends |

## 6. Modification summary

| No. | Change date | Title | Summary of the change |
|---|---|---|---|
| 1 | 2026.09.28 | Initial AS-IS reverse design | Document generated by modernizeX from the extracted AST and datastore; no functional change. |

