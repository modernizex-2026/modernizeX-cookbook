# Screen Design Document — OCCUSIN_CustomerInquiry

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Customer Inquiry |
| Function ID | OCCUSIN |
| CICS transaction | ORQC |
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
    OP["Operator"] --> OCCUSIN["OCCUSIN<br>Customer Inquiry"]
    OCCUSIN -- "EXEC CICS LINK" --> S_OUCUSIN["OUCUSIN<br>Customer Inquiry browse engine"]
    OCCUSIN -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Accept the search key for the customer inquiry.
2. Retrieve and display the matching customer information for review.
3. Let the operator refine the key and inquire again.

**Roles:** authenticated operator (signed on through the Sign On screen).

**Function keys**

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=Filter` | validate the entry and process the current step |
| `PF7` | `PF7=Restart` | page backward through the list |
| `PF8` | `PF8=Next` | page forward through the list |
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
    ENTRY["Calling menu / transaction"] -- "ORQC / selection" --> S["MCUSINA<br>Customer Inquiry"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "EXEC CICS LINK" --> L_OUCUSIN["OUCUSIN<br>Customer Inquiry browse engine"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MCUSINA"]
```

### 2.1 MCUSINA — Customer Inquiry (OCCUSIN)

**Layout**

![AS-IS mockup of MCUSINA](../Image/MCUSIN_MCUSINA.png)

*This screen appears when the operator opens the inquiry function.* Physical size 24×80 (mapset `MCUSIN`, map `MCUSINA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 4 | 5 | 31 | BLUE | — | — | Mode (I=ID F=FICO S=State/ZIP): | field header |
| 11 | FMODE | FMODEO | input | UNPROT/IC/FSET | 4 | 38 | 1 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 5 | 5 | 13 | BLUE | — | — | Cust ID From: | field header |
| 13 | FRID | FRIDO | input | UNPROT/IC/FSET | 5 | 19 | 9 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 5 | 31 | 3 | BLUE | — | — | To: | field header |
| 15 | TOID | TOIDO | input | UNPROT/IC/FSET | 5 | 35 | 9 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 10 | BLUE | — | — | FICO From: | field header |
| 17 | FRFICO | FRFICOO | input | UNPROT/IC/FSET | 6 | 16 | 3 | GREEN | — | — | — | operator input |
| 18 | (literal) | — | caption | ASKIP/NORM | 6 | 21 | 3 | BLUE | — | — | To: | field header |
| 19 | TOFICO | TOFICOO | input | UNPROT/IC/FSET | 6 | 25 | 3 | GREEN | — | — | — | operator input |
| 20 | (literal) | — | caption | ASKIP/NORM | 6 | 31 | 6 | BLUE | — | — | State: | field header |
| 21 | FSTATE | FSTATEO | input | UNPROT/IC/FSET | 6 | 38 | 2 | GREEN | — | — | — | operator input |
| 22 | (literal) | — | caption | ASKIP/NORM | 6 | 42 | 4 | BLUE | — | — | ZIP: | field header |
| 23 | FZIP | FZIPO | input | UNPROT/IC/FSET | 6 | 47 | 10 | GREEN | — | — | — | operator input |
| 24 | (literal) | — | caption | ASKIP/NORM | 8 | 3 | 7 | BLUE | — | — | Cust ID | field header |
| 25 | (literal) | — | caption | ASKIP/NORM | 8 | 14 | 4 | BLUE | — | — | Name | field header |
| 26 | (literal) | — | caption | ASKIP/NORM | 8 | 46 | 2 | BLUE | — | — | ST | field header |
| 27 | (literal) | — | caption | ASKIP/NORM | 8 | 50 | 3 | BLUE | — | — | ZIP | field header |
| 28 | (literal) | — | caption | ASKIP/NORM | 8 | 62 | 4 | BLUE | — | — | FICO | field header |
| 29 | CUL1 | CUL1O | display | ASKIP/NORM | 9 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 30 | CUN1 | CUN1O | display | ASKIP/NORM | 9 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 31 | CST1 | CST1O | display | ASKIP/NORM | 9 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 32 | CZP1 | CZP1O | display | ASKIP/NORM | 9 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 33 | CFI1 | CFI1O | display | ASKIP/NORM | 9 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 34 | CUL2 | CUL2O | display | ASKIP/NORM | 10 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 35 | CUN2 | CUN2O | display | ASKIP/NORM | 10 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 36 | CST2 | CST2O | display | ASKIP/NORM | 10 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 37 | CZP2 | CZP2O | display | ASKIP/NORM | 10 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 38 | CFI2 | CFI2O | display | ASKIP/NORM | 10 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 39 | CUL3 | CUL3O | display | ASKIP/NORM | 11 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 40 | CUN3 | CUN3O | display | ASKIP/NORM | 11 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 41 | CST3 | CST3O | display | ASKIP/NORM | 11 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 42 | CZP3 | CZP3O | display | ASKIP/NORM | 11 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 43 | CFI3 | CFI3O | display | ASKIP/NORM | 11 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 44 | CUL4 | CUL4O | display | ASKIP/NORM | 12 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 45 | CUN4 | CUN4O | display | ASKIP/NORM | 12 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 46 | CST4 | CST4O | display | ASKIP/NORM | 12 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 47 | CZP4 | CZP4O | display | ASKIP/NORM | 12 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 48 | CFI4 | CFI4O | display | ASKIP/NORM | 12 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 49 | CUL5 | CUL5O | display | ASKIP/NORM | 13 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 50 | CUN5 | CUN5O | display | ASKIP/NORM | 13 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 51 | CST5 | CST5O | display | ASKIP/NORM | 13 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 52 | CZP5 | CZP5O | display | ASKIP/NORM | 13 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 53 | CFI5 | CFI5O | display | ASKIP/NORM | 13 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 54 | CUL6 | CUL6O | display | ASKIP/NORM | 14 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 55 | CUN6 | CUN6O | display | ASKIP/NORM | 14 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 56 | CST6 | CST6O | display | ASKIP/NORM | 14 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 57 | CZP6 | CZP6O | display | ASKIP/NORM | 14 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 58 | CFI6 | CFI6O | display | ASKIP/NORM | 14 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 59 | CUL7 | CUL7O | display | ASKIP/NORM | 15 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 60 | CUN7 | CUN7O | display | ASKIP/NORM | 15 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 61 | CST7 | CST7O | display | ASKIP/NORM | 15 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 62 | CZP7 | CZP7O | display | ASKIP/NORM | 15 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 63 | CFI7 | CFI7O | display | ASKIP/NORM | 15 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 64 | CUL8 | CUL8O | display | ASKIP/NORM | 16 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 65 | CUN8 | CUN8O | display | ASKIP/NORM | 16 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 66 | CST8 | CST8O | display | ASKIP/NORM | 16 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 67 | CZP8 | CZP8O | display | ASKIP/NORM | 16 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 68 | CFI8 | CFI8O | display | ASKIP/NORM | 16 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 69 | CUL9 | CUL9O | display | ASKIP/NORM | 17 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 70 | CUN9 | CUN9O | display | ASKIP/NORM | 17 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 71 | CST9 | CST9O | display | ASKIP/NORM | 17 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 72 | CZP9 | CZP9O | display | ASKIP/NORM | 17 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 73 | CFI9 | CFI9O | display | ASKIP/NORM | 17 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 74 | CUL10 | CUL10O | display | ASKIP/NORM | 18 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 75 | CUN10 | CUN10O | display | ASKIP/NORM | 18 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 76 | CST10 | CST10O | display | ASKIP/NORM | 18 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 77 | CZP10 | CZP10O | display | ASKIP/NORM | 18 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 78 | CFI10 | CFI10O | display | ASKIP/NORM | 18 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 79 | CUL11 | CUL11O | display | ASKIP/NORM | 19 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 80 | CUN11 | CUN11O | display | ASKIP/NORM | 19 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 81 | CST11 | CST11O | display | ASKIP/NORM | 19 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 82 | CZP11 | CZP11O | display | ASKIP/NORM | 19 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 83 | CFI11 | CFI11O | display | ASKIP/NORM | 19 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 84 | CUL12 | CUL12O | display | ASKIP/NORM | 20 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 85 | CUN12 | CUN12O | display | ASKIP/NORM | 20 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 86 | CST12 | CST12O | display | ASKIP/NORM | 20 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 87 | CZP12 | CZP12O | display | ASKIP/NORM | 20 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 88 | CFI12 | CFI12O | display | ASKIP/NORM | 20 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 89 | CUL13 | CUL13O | display | ASKIP/NORM | 21 | 3 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 90 | CUN13 | CUN13O | display | ASKIP/NORM | 21 | 14 | 30 | TURQUOISE | — | — | — | program-supplied display |
| 91 | CST13 | CST13O | display | ASKIP/NORM | 21 | 46 | 2 | TURQUOISE | — | — | — | program-supplied display |
| 92 | CZP13 | CZP13O | display | ASKIP/NORM | 21 | 50 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 93 | CFI13 | CFI13O | display | ASKIP/NORM | 21 | 62 | 3 | TURQUOISE | — | — | — | program-supplied display |
| 94 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 95 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 42 | TURQUOISE | — | — | ENTER=Filter PF7=Restart PF8=Next PF3=Menu | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (key prompt) | Result displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | FMODE | ○ | □ |
| 7 | FRID | ○ | □ |
| 8 | TOID | ○ | □ |
| 9 | FRFICO | ○ | □ |
| 10 | TOFICO | ○ | □ |
| 11 | FSTATE | ○ | □ |
| 12 | FZIP | ○ | □ |
| 13 | CUL1 | □ | □ |
| 14 | CUN1 | □ | □ |
| 15 | CST1 | □ | □ |
| 16 | CZP1 | □ | □ |
| 17 | CFI1 | □ | □ |
| 18 | CUL2 | □ | □ |
| 19 | CUN2 | □ | □ |
| 20 | CST2 | □ | □ |
| 21 | CZP2 | □ | □ |
| 22 | CFI2 | □ | □ |
| 23 | CUL3 | □ | □ |
| 24 | CUN3 | □ | □ |
| 25 | CST3 | □ | □ |
| 26 | CZP3 | □ | □ |
| 27 | CFI3 | □ | □ |
| 28 | CUL4 | □ | □ |
| 29 | CUN4 | □ | □ |
| 30 | CST4 | □ | □ |
| 31 | CZP4 | □ | □ |
| 32 | CFI4 | □ | □ |
| 33 | CUL5 | □ | □ |
| 34 | CUN5 | □ | □ |
| 35 | CST5 | □ | □ |
| 36 | CZP5 | □ | □ |
| 37 | CFI5 | □ | □ |
| 38 | CUL6 | □ | □ |
| 39 | CUN6 | □ | □ |
| 40 | CST6 | □ | □ |
| 41 | CZP6 | □ | □ |
| 42 | CFI6 | □ | □ |
| 43 | CUL7 | □ | □ |
| 44 | CUN7 | □ | □ |
| 45 | CST7 | □ | □ |
| 46 | CZP7 | □ | □ |
| 47 | CFI7 | □ | □ |
| 48 | CUL8 | □ | □ |
| 49 | CUN8 | □ | □ |
| 50 | CST8 | □ | □ |
| 51 | CZP8 | □ | □ |
| 52 | CFI8 | □ | □ |
| 53 | CUL9 | □ | □ |
| 54 | CUN9 | □ | □ |
| 55 | CST9 | □ | □ |
| 56 | CZP9 | □ | □ |
| 57 | CFI9 | □ | □ |
| 58 | CUL10 | □ | □ |
| 59 | CUN10 | □ | □ |
| 60 | CST10 | □ | □ |
| 61 | CZP10 | □ | □ |
| 62 | CFI10 | □ | □ |
| 63 | CUL11 | □ | □ |
| 64 | CUN11 | □ | □ |
| 65 | CST11 | □ | □ |
| 66 | CZP11 | □ | □ |
| 67 | CFI11 | □ | □ |
| 68 | CUL12 | □ | □ |
| 69 | CUN12 | □ | □ |
| 70 | CST12 | □ | □ |
| 71 | CZP12 | □ | □ |
| 72 | CFI12 | □ | □ |
| 73 | CUL13 | □ | □ |
| 74 | CUN13 | □ | □ |
| 75 | CST13 | □ | □ |
| 76 | CZP13 | □ | □ |
| 77 | CFI13 | □ | □ |
| 78 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MCUSINA — Customer Inquiry

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MCUSINA — Customer Inquiry

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the search key is accepted and the matching customer information is displayed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF7` | when pressed | page backward to the previous page of rows | stays on the screen |
| 3 | `PF8` | when pressed | page forward to the next page of rows | stays on the screen |
| 4 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| **Data entry section** | | | | |
| 5 | Key field(s) | on ENTER | the search key is accepted and the matching customer information is displayed | stays on `MCUSINA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Customer Inquiry screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| — | — | none | none |

