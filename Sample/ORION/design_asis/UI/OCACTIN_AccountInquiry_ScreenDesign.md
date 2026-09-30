# Screen Design Document — OCACTIN_AccountInquiry

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Account Inquiry (consolidated) |
| Function ID | OCACTIN |
| CICS transaction | ORAI |
| Version | 1 |
| Author / Created on | modernizeX／2026-09-28 |
| Updated by / Updated on | modernizeX／2026-09-28 |

Approval frame: Customer｜Author｜Approver｜Responsible person

## 1. Function overview

### 1.1 Overview diagram

System architecture — the transaction program, the files/tables it touches, the sub-programs it links to, and where it hands control.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart LR
    OP["Operator"] --> OCACTIN["OCACTIN<br>Account Inquiry (consolidated)"]
    OCACTIN -- "EXEC CICS LINK" --> S_OUACTIN["OUACTIN<br>Account Inquiry browse engine"]
    OCACTIN -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the search key for the account inquiry.
2. Retrieve and display the matching account information for review.
3. Let the operator refine the key and inquire again.

**Roles:** authenticated operator (signed on through the Sign On screen).

**Function keys**

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=Apply` | validate the entry and process the current step |
| `PF7` | `PF7=Top` | page backward through the list |
| `PF8` | `PF8=Fwd` | page forward through the list |
| `PF3` | `PF3=Menu` | return to the main menu (OCMENU) |

### 1.3 Databases used

| № | Logical table name | Physical table/file name | Create(C) | Read(R) | Update(U) | Delete(D) | Notes |
|---|---|---|---|---|---|---|---|
| 1 | — | — | - | - | - | - | Not applicable — navigation only; file access is performed by the linked sub-programs |

## 2. Screen spec

Screen transition of the whole function — every screen a box, every edge labelled with what moves the operator.

```mermaid
---
config:
  layout: elk
  look: neo
  theme: base
---
flowchart TB
    ENTRY["Calling menu / transaction"] -- "ORAI / selection" --> S["MACTINA<br>Account Inquiry (consolidated)"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "EXEC CICS LINK" --> L_OUACTIN["OUACTIN<br>Account Inquiry browse engine"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MACTINA"]
```

### 2.1 MACTINA — Account Inquiry (consolidated) (OCACTIN)

**Layout**

![AS-IS mockup of MACTINA](../Image/MACTIN_MACTINA.png)

*This screen appears when the operator opens the inquiry function.* Physical size 24×80 (mapset `MACTIN`, map `MACTINA`).

**Item details**

| № | Field (BMS) | COBOL symbolic | Kind | Attributes | Line | Col | Character count (screen columns) | Colour | picin | picout | Initial value / caption | Notes |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | (literal) | — | caption | ASKIP/NORM | 1 | 1 | 5 | BLUE | — | — | Tran: | field header |
| 2 | TRNNAME | TRNNAMEO | display | ASKIP/NORM | 1 | 7 | 4 | BLUE | — | — | — | program-supplied display |
| 3 | TITLE | TITLEO | display | ASKIP/NORM | 1 | 21 | 40 | YELLOW | — | — | — | program-supplied display |
| 4 | (literal) | — | caption | ASKIP/NORM | 1 | 65 | 5 | BLUE | — | — | Date: | field header |
| 5 | CURDATE | CURDATEO | display | ASKIP/NORM | 1 | 71 | 10 | BLUE | — | — | — | program-supplied display |
| 6 | (literal) | — | caption | ASKIP/NORM | 2 | 1 | 5 | BLUE | — | — | Pgm : | field header |
| 7 | PGMNAME | PGMNAMEO | display | ASKIP/NORM | 2 | 7 | 8 | BLUE | — | — | — | program-supplied display |
| 8 | (literal) | — | caption | ASKIP/NORM | 2 | 65 | 5 | BLUE | — | — | Time: | field header |
| 9 | CURTIME | CURTIMEO | display | ASKIP/NORM | 2 | 71 | 8 | BLUE | — | — | — | program-supplied display |
| 10 | (literal) | — | caption | ASKIP/NORM | 4 | 2 | 7 | BLUE | — | — | Filter: | field header |
| 11 | FILT | FILTO | input | UNPROT/IC/FSET | 4 | 10 | 10 | GREEN | — | — | — | operator input |
| 12 | FDESC | FDESCO | display | ASKIP/NORM | 4 | 23 | 12 | YELLOW | — | — | — | program-supplied display |
| 13 | (literal) | — | caption | ASKIP/NORM | 5 | 2 | 33 | TURQUOISE | — | — | ALL DELINQUENT OVER-LIMIT DORMANT | field header |
| 14 | (literal) | — | caption | ASKIP/NORM | 6 | 2 | 20 | TURQUOISE | — | — | CLOSED NEW HIGH-UTIL | field header |
| 15 | (literal) | — | caption | ASKIP/NORM | 7 | 2 | 7 | BLUE | — | — | Acct ID | field header |
| 16 | (literal) | — | caption | ASKIP/NORM | 7 | 14 | 2 | BLUE | — | — | St | field header |
| 17 | (literal) | — | caption | ASKIP/NORM | 7 | 17 | 7 | BLUE | — | — | Balance | field header |
| 18 | (literal) | — | caption | ASKIP/NORM | 7 | 33 | 10 | BLUE | — | — | Credit Lim | field header |
| 19 | (literal) | — | caption | ASKIP/NORM | 7 | 49 | 9 | BLUE | — | — | Available | field header |
| 20 | (literal) | — | caption | ASKIP/NORM | 7 | 64 | 5 | BLUE | — | — | Util% | field header |
| 21 | AID1 | AID1O | display | ASKIP/NORM | 8 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 22 | AST1 | AST1O | display | ASKIP/NORM | 8 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 23 | ABL1 | ABL1O | display | ASKIP/NORM | 8 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 24 | ALM1 | ALM1O | display | ASKIP/NORM | 8 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 25 | AAV1 | AAV1O | display | ASKIP/NORM | 8 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 26 | AUT1 | AUT1O | display | ASKIP/NORM | 8 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 27 | AID2 | AID2O | display | ASKIP/NORM | 9 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 28 | AST2 | AST2O | display | ASKIP/NORM | 9 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 29 | ABL2 | ABL2O | display | ASKIP/NORM | 9 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 30 | ALM2 | ALM2O | display | ASKIP/NORM | 9 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 31 | AAV2 | AAV2O | display | ASKIP/NORM | 9 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 32 | AUT2 | AUT2O | display | ASKIP/NORM | 9 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 33 | AID3 | AID3O | display | ASKIP/NORM | 10 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 34 | AST3 | AST3O | display | ASKIP/NORM | 10 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 35 | ABL3 | ABL3O | display | ASKIP/NORM | 10 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 36 | ALM3 | ALM3O | display | ASKIP/NORM | 10 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 37 | AAV3 | AAV3O | display | ASKIP/NORM | 10 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 38 | AUT3 | AUT3O | display | ASKIP/NORM | 10 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 39 | AID4 | AID4O | display | ASKIP/NORM | 11 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 40 | AST4 | AST4O | display | ASKIP/NORM | 11 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 41 | ABL4 | ABL4O | display | ASKIP/NORM | 11 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 42 | ALM4 | ALM4O | display | ASKIP/NORM | 11 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 43 | AAV4 | AAV4O | display | ASKIP/NORM | 11 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 44 | AUT4 | AUT4O | display | ASKIP/NORM | 11 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 45 | AID5 | AID5O | display | ASKIP/NORM | 12 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 46 | AST5 | AST5O | display | ASKIP/NORM | 12 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 47 | ABL5 | ABL5O | display | ASKIP/NORM | 12 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 48 | ALM5 | ALM5O | display | ASKIP/NORM | 12 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 49 | AAV5 | AAV5O | display | ASKIP/NORM | 12 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 50 | AUT5 | AUT5O | display | ASKIP/NORM | 12 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 51 | AID6 | AID6O | display | ASKIP/NORM | 13 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 52 | AST6 | AST6O | display | ASKIP/NORM | 13 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 53 | ABL6 | ABL6O | display | ASKIP/NORM | 13 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 54 | ALM6 | ALM6O | display | ASKIP/NORM | 13 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 55 | AAV6 | AAV6O | display | ASKIP/NORM | 13 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 56 | AUT6 | AUT6O | display | ASKIP/NORM | 13 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 57 | AID7 | AID7O | display | ASKIP/NORM | 14 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 58 | AST7 | AST7O | display | ASKIP/NORM | 14 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 59 | ABL7 | ABL7O | display | ASKIP/NORM | 14 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 60 | ALM7 | ALM7O | display | ASKIP/NORM | 14 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 61 | AAV7 | AAV7O | display | ASKIP/NORM | 14 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 62 | AUT7 | AUT7O | display | ASKIP/NORM | 14 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 63 | AID8 | AID8O | display | ASKIP/NORM | 15 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 64 | AST8 | AST8O | display | ASKIP/NORM | 15 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 65 | ABL8 | ABL8O | display | ASKIP/NORM | 15 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 66 | ALM8 | ALM8O | display | ASKIP/NORM | 15 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 67 | AAV8 | AAV8O | display | ASKIP/NORM | 15 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 68 | AUT8 | AUT8O | display | ASKIP/NORM | 15 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 69 | AID9 | AID9O | display | ASKIP/NORM | 16 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 70 | AST9 | AST9O | display | ASKIP/NORM | 16 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 71 | ABL9 | ABL9O | display | ASKIP/NORM | 16 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 72 | ALM9 | ALM9O | display | ASKIP/NORM | 16 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 73 | AAV9 | AAV9O | display | ASKIP/NORM | 16 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 74 | AUT9 | AUT9O | display | ASKIP/NORM | 16 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 75 | AID10 | AID10O | display | ASKIP/NORM | 17 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 76 | AST10 | AST10O | display | ASKIP/NORM | 17 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 77 | ABL10 | ABL10O | display | ASKIP/NORM | 17 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 78 | ALM10 | ALM10O | display | ASKIP/NORM | 17 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 79 | AAV10 | AAV10O | display | ASKIP/NORM | 17 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 80 | AUT10 | AUT10O | display | ASKIP/NORM | 17 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 81 | AID11 | AID11O | display | ASKIP/NORM | 18 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 82 | AST11 | AST11O | display | ASKIP/NORM | 18 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 83 | ABL11 | ABL11O | display | ASKIP/NORM | 18 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 84 | ALM11 | ALM11O | display | ASKIP/NORM | 18 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 85 | AAV11 | AAV11O | display | ASKIP/NORM | 18 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 86 | AUT11 | AUT11O | display | ASKIP/NORM | 18 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 87 | AID12 | AID12O | display | ASKIP/NORM | 19 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 88 | AST12 | AST12O | display | ASKIP/NORM | 19 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 89 | ABL12 | ABL12O | display | ASKIP/NORM | 19 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 90 | ALM12 | ALM12O | display | ASKIP/NORM | 19 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 91 | AAV12 | AAV12O | display | ASKIP/NORM | 19 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 92 | AUT12 | AUT12O | display | ASKIP/NORM | 19 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 93 | AID13 | AID13O | display | ASKIP/NORM | 20 | 2 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 94 | AST13 | AST13O | display | ASKIP/NORM | 20 | 14 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 95 | ABL13 | ABL13O | display | ASKIP/NORM | 20 | 16 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 96 | ALM13 | ALM13O | display | ASKIP/NORM | 20 | 32 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 97 | AAV13 | AAV13O | display | ASKIP/NORM | 20 | 48 | 15 | TURQUOISE | — | — | — | program-supplied display |
| 98 | AUT13 | AUT13O | display | ASKIP/NORM | 20 | 64 | 6 | TURQUOISE | — | — | — | program-supplied display |
| 99 | (literal) | — | caption | ASKIP/NORM | 21 | 2 | 5 | BLUE | — | — | Page: | field header |
| 100 | PAGENO | PAGENOO | display | ASKIP/NORM | 21 | 8 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 101 | (literal) | — | caption | ASKIP/NORM | 21 | 13 | 8 | BLUE | — | — | Matched: | field header |
| 102 | RUNTOT | RUNTOTO | display | ASKIP/NORM | 21 | 22 | 7 | TURQUOISE | — | — | — | program-supplied display |
| 103 | (literal) | — | caption | ASKIP/NORM | 21 | 31 | 8 | BLUE | — | — | PortBal: | field header |
| 104 | PGBAL | PGBALO | display | ASKIP/NORM | 21 | 40 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 105 | (literal) | — | caption | ASKIP/NORM | 22 | 31 | 8 | BLUE | — | — | PortAvl: | field header |
| 106 | PGAVL | PGAVLO | display | ASKIP/NORM | 22 | 40 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 107 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 108 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 39 | TURQUOISE | — | — | ENTER=Apply  PF7=Top  PF8=Fwd  PF3=Menu | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (key prompt) | Result displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | FILT | ○ | □ |
| 7 | FDESC | □ | □ |
| 8 | AID1 | □ | □ |
| 9 | AST1 | □ | □ |
| 10 | ABL1 | □ | □ |
| 11 | ALM1 | □ | □ |
| 12 | AAV1 | □ | □ |
| 13 | AUT1 | □ | □ |
| 14 | AID2 | □ | □ |
| 15 | AST2 | □ | □ |
| 16 | ABL2 | □ | □ |
| 17 | ALM2 | □ | □ |
| 18 | AAV2 | □ | □ |
| 19 | AUT2 | □ | □ |
| 20 | AID3 | □ | □ |
| 21 | AST3 | □ | □ |
| 22 | ABL3 | □ | □ |
| 23 | ALM3 | □ | □ |
| 24 | AAV3 | □ | □ |
| 25 | AUT3 | □ | □ |
| 26 | AID4 | □ | □ |
| 27 | AST4 | □ | □ |
| 28 | ABL4 | □ | □ |
| 29 | ALM4 | □ | □ |
| 30 | AAV4 | □ | □ |
| 31 | AUT4 | □ | □ |
| 32 | AID5 | □ | □ |
| 33 | AST5 | □ | □ |
| 34 | ABL5 | □ | □ |
| 35 | ALM5 | □ | □ |
| 36 | AAV5 | □ | □ |
| 37 | AUT5 | □ | □ |
| 38 | AID6 | □ | □ |
| 39 | AST6 | □ | □ |
| 40 | ABL6 | □ | □ |
| 41 | ALM6 | □ | □ |
| 42 | AAV6 | □ | □ |
| 43 | AUT6 | □ | □ |
| 44 | AID7 | □ | □ |
| 45 | AST7 | □ | □ |
| 46 | ABL7 | □ | □ |
| 47 | ALM7 | □ | □ |
| 48 | AAV7 | □ | □ |
| 49 | AUT7 | □ | □ |
| 50 | AID8 | □ | □ |
| 51 | AST8 | □ | □ |
| 52 | ABL8 | □ | □ |
| 53 | ALM8 | □ | □ |
| 54 | AAV8 | □ | □ |
| 55 | AUT8 | □ | □ |
| 56 | AID9 | □ | □ |
| 57 | AST9 | □ | □ |
| 58 | ABL9 | □ | □ |
| 59 | ALM9 | □ | □ |
| 60 | AAV9 | □ | □ |
| 61 | AUT9 | □ | □ |
| 62 | AID10 | □ | □ |
| 63 | AST10 | □ | □ |
| 64 | ABL10 | □ | □ |
| 65 | ALM10 | □ | □ |
| 66 | AAV10 | □ | □ |
| 67 | AUT10 | □ | □ |
| 68 | AID11 | □ | □ |
| 69 | AST11 | □ | □ |
| 70 | ABL11 | □ | □ |
| 71 | ALM11 | □ | □ |
| 72 | AAV11 | □ | □ |
| 73 | AUT11 | □ | □ |
| 74 | AID12 | □ | □ |
| 75 | AST12 | □ | □ |
| 76 | ABL12 | □ | □ |
| 77 | ALM12 | □ | □ |
| 78 | AAV12 | □ | □ |
| 79 | AUT12 | □ | □ |
| 80 | AID13 | □ | □ |
| 81 | AST13 | □ | □ |
| 82 | ABL13 | □ | □ |
| 83 | ALM13 | □ | □ |
| 84 | AAV13 | □ | □ |
| 85 | AUT13 | □ | □ |
| 86 | PAGENO | □ | □ |
| 87 | RUNTOT | □ | □ |
| 88 | PGBAL | □ | □ |
| 89 | PGAVL | □ | □ |
| 90 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MACTINA — Account Inquiry (consolidated)

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MACTINA — Account Inquiry (consolidated)

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the search key is accepted and the matching account information is displayed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF7` | when pressed | page backward to the previous page of rows | stays on the screen |
| 3 | `PF8` | when pressed | page forward to the next page of rows | stays on the screen |
| 4 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 5 | `PF12` | when pressed | cancel the current action and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 6 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 7 | `CLEAR` | when pressed | clear the screen and redisplay it | stays on the screen |
| **Data entry section** | | | | |
| 8 | Key field(s) | on ENTER | the search key is accepted and the matching account information is displayed | stays on `MACTINA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Account Inquiry (consolidated) screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| — | — | none | none |

