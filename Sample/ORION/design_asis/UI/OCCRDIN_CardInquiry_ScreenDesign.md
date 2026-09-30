# Screen Design Document — OCCRDIN_CardInquiry

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Card Inquiry (consolidated) |
| Function ID | OCCRDIN |
| CICS transaction | ORCI |
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
    OP["Operator"] --> OCCRDIN["OCCRDIN<br>Card Inquiry (consolidated)"]
    OCCRDIN -- "EXEC CICS LINK" --> S_OUCRDIN["OUCRDIN<br>Card Inquiry browse engine"]
    OCCRDIN -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the search key for the card inquiry.
2. Retrieve and display the matching card information for review.
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
    ENTRY["Calling menu / transaction"] -- "ORCI / selection" --> S["MCRDINA<br>Card Inquiry (consolidated)"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "EXEC CICS LINK" --> L_OUCRDIN["OUCRDIN<br>Card Inquiry browse engine"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MCRDINA"]
```

### 2.1 MCRDINA — Card Inquiry (consolidated) (OCCRDIN)

**Layout**

![AS-IS mockup of MCRDINA](../Image/MCRDIN_MCRDINA.png)

*This screen appears when the operator opens the inquiry function.* Physical size 24×80 (mapset `MCRDIN`, map `MCRDINA`).

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
| 13 | (literal) | — | caption | ASKIP/NORM | 5 | 2 | 33 | TURQUOISE | — | — | ALL ACTIVE INACTIVE EXPIRING-SOON | field header |
| 14 | (literal) | — | caption | ASKIP/NORM | 6 | 2 | 11 | BLUE | — | — | Card Number | field header |
| 15 | (literal) | — | caption | ASKIP/NORM | 6 | 20 | 7 | BLUE | — | — | Acct ID | field header |
| 16 | (literal) | — | caption | ASKIP/NORM | 6 | 33 | 13 | BLUE | — | — | Embossed Name | field header |
| 17 | (literal) | — | caption | ASKIP/NORM | 6 | 55 | 6 | BLUE | — | — | Expiry | field header |
| 18 | (literal) | — | caption | ASKIP/NORM | 6 | 67 | 2 | BLUE | — | — | St | field header |
| 19 | CNM1 | CNM1O | display | ASKIP/NORM | 7 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 20 | CAC1 | CAC1O | display | ASKIP/NORM | 7 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 21 | CNA1 | CNA1O | display | ASKIP/NORM | 7 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 22 | CEX1 | CEX1O | display | ASKIP/NORM | 7 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 23 | CST1 | CST1O | display | ASKIP/NORM | 7 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 24 | CNM2 | CNM2O | display | ASKIP/NORM | 8 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 25 | CAC2 | CAC2O | display | ASKIP/NORM | 8 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 26 | CNA2 | CNA2O | display | ASKIP/NORM | 8 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 27 | CEX2 | CEX2O | display | ASKIP/NORM | 8 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 28 | CST2 | CST2O | display | ASKIP/NORM | 8 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 29 | CNM3 | CNM3O | display | ASKIP/NORM | 9 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 30 | CAC3 | CAC3O | display | ASKIP/NORM | 9 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 31 | CNA3 | CNA3O | display | ASKIP/NORM | 9 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 32 | CEX3 | CEX3O | display | ASKIP/NORM | 9 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 33 | CST3 | CST3O | display | ASKIP/NORM | 9 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 34 | CNM4 | CNM4O | display | ASKIP/NORM | 10 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 35 | CAC4 | CAC4O | display | ASKIP/NORM | 10 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 36 | CNA4 | CNA4O | display | ASKIP/NORM | 10 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 37 | CEX4 | CEX4O | display | ASKIP/NORM | 10 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 38 | CST4 | CST4O | display | ASKIP/NORM | 10 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 39 | CNM5 | CNM5O | display | ASKIP/NORM | 11 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 40 | CAC5 | CAC5O | display | ASKIP/NORM | 11 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 41 | CNA5 | CNA5O | display | ASKIP/NORM | 11 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 42 | CEX5 | CEX5O | display | ASKIP/NORM | 11 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 43 | CST5 | CST5O | display | ASKIP/NORM | 11 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 44 | CNM6 | CNM6O | display | ASKIP/NORM | 12 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 45 | CAC6 | CAC6O | display | ASKIP/NORM | 12 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 46 | CNA6 | CNA6O | display | ASKIP/NORM | 12 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 47 | CEX6 | CEX6O | display | ASKIP/NORM | 12 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 48 | CST6 | CST6O | display | ASKIP/NORM | 12 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 49 | CNM7 | CNM7O | display | ASKIP/NORM | 13 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 50 | CAC7 | CAC7O | display | ASKIP/NORM | 13 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 51 | CNA7 | CNA7O | display | ASKIP/NORM | 13 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 52 | CEX7 | CEX7O | display | ASKIP/NORM | 13 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 53 | CST7 | CST7O | display | ASKIP/NORM | 13 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 54 | CNM8 | CNM8O | display | ASKIP/NORM | 14 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 55 | CAC8 | CAC8O | display | ASKIP/NORM | 14 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 56 | CNA8 | CNA8O | display | ASKIP/NORM | 14 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 57 | CEX8 | CEX8O | display | ASKIP/NORM | 14 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 58 | CST8 | CST8O | display | ASKIP/NORM | 14 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 59 | CNM9 | CNM9O | display | ASKIP/NORM | 15 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 60 | CAC9 | CAC9O | display | ASKIP/NORM | 15 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 61 | CNA9 | CNA9O | display | ASKIP/NORM | 15 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 62 | CEX9 | CEX9O | display | ASKIP/NORM | 15 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 63 | CST9 | CST9O | display | ASKIP/NORM | 15 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 64 | CNM10 | CNM10O | display | ASKIP/NORM | 16 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 65 | CAC10 | CAC10O | display | ASKIP/NORM | 16 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 66 | CNA10 | CNA10O | display | ASKIP/NORM | 16 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 67 | CEX10 | CEX10O | display | ASKIP/NORM | 16 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 68 | CST10 | CST10O | display | ASKIP/NORM | 16 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 69 | CNM11 | CNM11O | display | ASKIP/NORM | 17 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 70 | CAC11 | CAC11O | display | ASKIP/NORM | 17 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 71 | CNA11 | CNA11O | display | ASKIP/NORM | 17 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 72 | CEX11 | CEX11O | display | ASKIP/NORM | 17 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 73 | CST11 | CST11O | display | ASKIP/NORM | 17 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 74 | CNM12 | CNM12O | display | ASKIP/NORM | 18 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 75 | CAC12 | CAC12O | display | ASKIP/NORM | 18 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 76 | CNA12 | CNA12O | display | ASKIP/NORM | 18 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 77 | CEX12 | CEX12O | display | ASKIP/NORM | 18 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 78 | CST12 | CST12O | display | ASKIP/NORM | 18 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 79 | CNM13 | CNM13O | display | ASKIP/NORM | 19 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 80 | CAC13 | CAC13O | display | ASKIP/NORM | 19 | 20 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 81 | CNA13 | CNA13O | display | ASKIP/NORM | 19 | 33 | 20 | TURQUOISE | — | — | — | program-supplied display |
| 82 | CEX13 | CEX13O | display | ASKIP/NORM | 19 | 55 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 83 | CST13 | CST13O | display | ASKIP/NORM | 19 | 67 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 84 | (literal) | — | caption | ASKIP/NORM | 21 | 2 | 5 | BLUE | — | — | Page: | field header |
| 85 | PAGENO | PAGENOO | display | ASKIP/NORM | 21 | 8 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 86 | (literal) | — | caption | ASKIP/NORM | 21 | 13 | 7 | BLUE | — | — | Active: | field header |
| 87 | ACTCNT | ACTCNTO | display | ASKIP/NORM | 21 | 21 | 7 | TURQUOISE | — | — | — | program-supplied display |
| 88 | (literal) | — | caption | ASKIP/NORM | 21 | 30 | 9 | BLUE | — | — | Inactive: | field header |
| 89 | INACNT | INACNTO | display | ASKIP/NORM | 21 | 40 | 7 | TURQUOISE | — | — | — | program-supplied display |
| 90 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 91 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 39 | TURQUOISE | — | — | ENTER=Apply  PF7=Top  PF8=Fwd  PF3=Menu | field header |

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
| 8 | CNM1 | □ | □ |
| 9 | CAC1 | □ | □ |
| 10 | CNA1 | □ | □ |
| 11 | CEX1 | □ | □ |
| 12 | CST1 | □ | □ |
| 13 | CNM2 | □ | □ |
| 14 | CAC2 | □ | □ |
| 15 | CNA2 | □ | □ |
| 16 | CEX2 | □ | □ |
| 17 | CST2 | □ | □ |
| 18 | CNM3 | □ | □ |
| 19 | CAC3 | □ | □ |
| 20 | CNA3 | □ | □ |
| 21 | CEX3 | □ | □ |
| 22 | CST3 | □ | □ |
| 23 | CNM4 | □ | □ |
| 24 | CAC4 | □ | □ |
| 25 | CNA4 | □ | □ |
| 26 | CEX4 | □ | □ |
| 27 | CST4 | □ | □ |
| 28 | CNM5 | □ | □ |
| 29 | CAC5 | □ | □ |
| 30 | CNA5 | □ | □ |
| 31 | CEX5 | □ | □ |
| 32 | CST5 | □ | □ |
| 33 | CNM6 | □ | □ |
| 34 | CAC6 | □ | □ |
| 35 | CNA6 | □ | □ |
| 36 | CEX6 | □ | □ |
| 37 | CST6 | □ | □ |
| 38 | CNM7 | □ | □ |
| 39 | CAC7 | □ | □ |
| 40 | CNA7 | □ | □ |
| 41 | CEX7 | □ | □ |
| 42 | CST7 | □ | □ |
| 43 | CNM8 | □ | □ |
| 44 | CAC8 | □ | □ |
| 45 | CNA8 | □ | □ |
| 46 | CEX8 | □ | □ |
| 47 | CST8 | □ | □ |
| 48 | CNM9 | □ | □ |
| 49 | CAC9 | □ | □ |
| 50 | CNA9 | □ | □ |
| 51 | CEX9 | □ | □ |
| 52 | CST9 | □ | □ |
| 53 | CNM10 | □ | □ |
| 54 | CAC10 | □ | □ |
| 55 | CNA10 | □ | □ |
| 56 | CEX10 | □ | □ |
| 57 | CST10 | □ | □ |
| 58 | CNM11 | □ | □ |
| 59 | CAC11 | □ | □ |
| 60 | CNA11 | □ | □ |
| 61 | CEX11 | □ | □ |
| 62 | CST11 | □ | □ |
| 63 | CNM12 | □ | □ |
| 64 | CAC12 | □ | □ |
| 65 | CNA12 | □ | □ |
| 66 | CEX12 | □ | □ |
| 67 | CST12 | □ | □ |
| 68 | CNM13 | □ | □ |
| 69 | CAC13 | □ | □ |
| 70 | CNA13 | □ | □ |
| 71 | CEX13 | □ | □ |
| 72 | CST13 | □ | □ |
| 73 | PAGENO | □ | □ |
| 74 | ACTCNT | □ | □ |
| 75 | INACNT | □ | □ |
| 76 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MCRDINA — Card Inquiry (consolidated)

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MCRDINA — Card Inquiry (consolidated)

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the search key is accepted and the matching card information is displayed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF7` | when pressed | page backward to the previous page of rows | stays on the screen |
| 3 | `PF8` | when pressed | page forward to the next page of rows | stays on the screen |
| 4 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 5 | `PF12` | when pressed | cancel the current action and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 6 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 7 | `CLEAR` | when pressed | clear the screen and redisplay it | stays on the screen |
| **Data entry section** | | | | |
| 8 | Key field(s) | on ENTER | the search key is accepted and the matching card information is displayed | stays on `MCRDINA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Card Inquiry (consolidated) screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| — | — | none | none |

