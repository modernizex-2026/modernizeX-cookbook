# Screen Design Document — OCANLIN_AnalyticsInquiry

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Analytics Inquiry |
| Function ID | OCANLIN |
| CICS transaction | ORAN |
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
    OP["Operator"] --> OCANLIN["OCANLIN<br>Analytics Inquiry"]
    OCANLIN -- "EXEC CICS LINK" --> S_OUANLIN["OUANLIN<br>Analytics compute subroutine"]
    OCANLIN -- "PF3 returns" --> X_OCRPTMN["OCRPTMN<br>Reports Menu"]
```

### 1.2 Function overview

1. Accept the search key for the analytics inquiry.
2. Retrieve and display the matching analytics information for review.
3. Let the operator refine the key and inquire again.

**Roles:** authenticated operator (signed on through the Sign On screen).

**Function keys**

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=Process` | validate the entry and process the current step |
| `PF3` | `PF3=Back` | return to the reports menu (OCRPTMN) |
| `PF4` | `PF4=Clear` | clear the entered values and start again |

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
    ENTRY["Calling menu / transaction"] -- "ORAN / selection" --> S["MANLINA<br>Analytics Inquiry"]
    S -- "PF3 (return)" --> X_OCRPTMN["MRPTMNA<br>Reports Menu"]
    S -- "EXEC CICS LINK" --> L_OUANLIN["OUANLIN<br>Analytics compute subroutine"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MANLINA"]
```

### 2.1 MANLINA — Analytics Inquiry (OCANLIN)

**Layout**

![AS-IS mockup of MANLINA](../Image/MANLIN_MANLINA.png)

*This screen appears when the operator opens the inquiry function.* Physical size 24×80 (mapset `MANLIN`, map `MANLINA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 5 | 5 | 5 | BLUE | — | — | Mode: | field header |
| 11 | ANMODE | ANMODEO | input | UNPROT/IC/FSET | 5 | 11 | 7 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 5 | 25 | 34 | TURQUOISE | — | — | RW=Rewards FR=Fraud GL=GL RC=Recon | field header |
| 13 | ANHEAD | ANHEADO | display | ASKIP/NORM | 6 | 2 | 60 | YELLOW | — | — | — | program-supplied display |
| 14 | (literal) | — | caption | ASKIP/NORM | 7 | 2 | 3 | BLUE | — | — | Key | field header |
| 15 | (literal) | — | caption | ASKIP/NORM | 7 | 19 | 4 | BLUE | — | — | Info | field header |
| 16 | (literal) | — | caption | ASKIP/NORM | 7 | 31 | 5 | BLUE | — | — | Count | field header |
| 17 | (literal) | — | caption | ASKIP/NORM | 7 | 44 | 6 | BLUE | — | — | Amount | field header |
| 18 | (literal) | — | caption | ASKIP/NORM | 7 | 60 | 5 | BLUE | — | — | Value | field header |
| 19 | ANK1 | ANK1O | display | ASKIP/NORM | 8 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 20 | ANI1 | ANI1O | display | ASKIP/NORM | 8 | 19 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 21 | ANC1 | ANC1O | display | ASKIP/NORM | 8 | 30 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 22 | ANA1 | ANA1O | display | ASKIP/NORM | 8 | 40 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 23 | ANV1 | ANV1O | display | ASKIP/NORM | 8 | 56 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 24 | ANK2 | ANK2O | display | ASKIP/NORM | 9 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 25 | ANI2 | ANI2O | display | ASKIP/NORM | 9 | 19 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 26 | ANC2 | ANC2O | display | ASKIP/NORM | 9 | 30 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 27 | ANA2 | ANA2O | display | ASKIP/NORM | 9 | 40 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 28 | ANV2 | ANV2O | display | ASKIP/NORM | 9 | 56 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 29 | ANK3 | ANK3O | display | ASKIP/NORM | 10 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 30 | ANI3 | ANI3O | display | ASKIP/NORM | 10 | 19 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 31 | ANC3 | ANC3O | display | ASKIP/NORM | 10 | 30 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 32 | ANA3 | ANA3O | display | ASKIP/NORM | 10 | 40 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 33 | ANV3 | ANV3O | display | ASKIP/NORM | 10 | 56 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 34 | ANK4 | ANK4O | display | ASKIP/NORM | 11 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 35 | ANI4 | ANI4O | display | ASKIP/NORM | 11 | 19 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 36 | ANC4 | ANC4O | display | ASKIP/NORM | 11 | 30 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 37 | ANA4 | ANA4O | display | ASKIP/NORM | 11 | 40 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 38 | ANV4 | ANV4O | display | ASKIP/NORM | 11 | 56 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 39 | ANK5 | ANK5O | display | ASKIP/NORM | 12 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 40 | ANI5 | ANI5O | display | ASKIP/NORM | 12 | 19 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 41 | ANC5 | ANC5O | display | ASKIP/NORM | 12 | 30 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 42 | ANA5 | ANA5O | display | ASKIP/NORM | 12 | 40 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 43 | ANV5 | ANV5O | display | ASKIP/NORM | 12 | 56 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 44 | ANK6 | ANK6O | display | ASKIP/NORM | 13 | 2 | 16 | TURQUOISE | — | — | — | program-supplied display |
| 45 | ANI6 | ANI6O | display | ASKIP/NORM | 13 | 19 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 46 | ANC6 | ANC6O | display | ASKIP/NORM | 13 | 30 | 9 | TURQUOISE | — | — | — | program-supplied display |
| 47 | ANA6 | ANA6O | display | ASKIP/NORM | 13 | 40 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 48 | ANV6 | ANV6O | display | ASKIP/NORM | 13 | 56 | 14 | TURQUOISE | — | — | — | program-supplied display |
| 49 | ANTOT1 | ANTOT1O | display | ASKIP/NORM | 15 | 2 | 78 | GREEN | — | — | — | program-supplied display |
| 50 | ANTOT2 | ANTOT2O | display | ASKIP/NORM | 16 | 2 | 78 | GREEN | — | — | — | program-supplied display |
| 51 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 52 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (key prompt) | Result displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | ANMODE | ○ | □ |
| 7 | ANHEAD | □ | □ |
| 8 | ANK1 | □ | □ |
| 9 | ANI1 | □ | □ |
| 10 | ANC1 | □ | □ |
| 11 | ANA1 | □ | □ |
| 12 | ANV1 | □ | □ |
| 13 | ANK2 | □ | □ |
| 14 | ANI2 | □ | □ |
| 15 | ANC2 | □ | □ |
| 16 | ANA2 | □ | □ |
| 17 | ANV2 | □ | □ |
| 18 | ANK3 | □ | □ |
| 19 | ANI3 | □ | □ |
| 20 | ANC3 | □ | □ |
| 21 | ANA3 | □ | □ |
| 22 | ANV3 | □ | □ |
| 23 | ANK4 | □ | □ |
| 24 | ANI4 | □ | □ |
| 25 | ANC4 | □ | □ |
| 26 | ANA4 | □ | □ |
| 27 | ANV4 | □ | □ |
| 28 | ANK5 | □ | □ |
| 29 | ANI5 | □ | □ |
| 30 | ANC5 | □ | □ |
| 31 | ANA5 | □ | □ |
| 32 | ANV5 | □ | □ |
| 33 | ANK6 | □ | □ |
| 34 | ANI6 | □ | □ |
| 35 | ANC6 | □ | □ |
| 36 | ANA6 | □ | □ |
| 37 | ANV6 | □ | □ |
| 38 | ANTOT1 | □ | □ |
| 39 | ANTOT2 | □ | □ |
| 40 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MANLINA — Analytics Inquiry

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MANLINA — Analytics Inquiry

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the search key is accepted and the matching analytics information is displayed | stays on the screen, or continues to the reports menu (OCRPTMN) |
| 2 | `PF3` | when pressed | cancel and return to the reports menu (OCRPTMN) | the reports menu (OCRPTMN) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 4 | `PF7` | when pressed | page backward to the previous page of rows | stays on the screen |
| 5 | `PF8` | when pressed | page forward to the next page of rows | stays on the screen |
| **Data entry section** | | | | |
| 6 | Key field(s) | on ENTER | the search key is accepted and the matching analytics information is displayed | stays on `MANLINA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Analytics Inquiry screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| — | — | none | none |

