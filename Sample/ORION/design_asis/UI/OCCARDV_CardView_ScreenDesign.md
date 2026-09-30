# Screen Design Document — OCCARDV_CardView

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Card View |
| Function ID | OCCARDV |
| CICS transaction | ORCV |
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
    OP["Operator"] --> OCCARDV["OCCARDV<br>Card View"]
    OCCARDV -- "R" --> T_WS_CARDFILE[("Card master<br>WS-CARDFILE")]
    OCCARDV -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Look up the card by its key.
2. Display the current details of the card in read-only form.
3. Return to the calling screen when the operator is finished.

**Roles:** authenticated operator (signed on through the Sign On screen).

**Function keys**

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=Process` | validate the entry and process the current step |
| `PF3` | `PF3=Back` | return to the main menu (OCMENU) |
| `PF4` | `PF4=Clear` | clear the entered values and start again |

### 1.3 Databases used

| № | Logical table name | Physical table/file name | Create(C) | Read(R) | Update(U) | Delete(D) | Notes |
|---|---|---|---|---|---|---|---|
| 1 | Card master | WS-CARDFILE | - | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORCV / selection" --> S["MCARDVA<br>Card View"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MCARDVA"]
```

### 2.1 MCARDVA — Card View (OCCARDV)

**Layout**

![AS-IS mockup of MCARDVA](../Image/MCARDV_MCARDVA.png)

*This screen appears when the operator selects the view function and enters a key.* Physical size 24×80 (mapset `MCARDV`, map `MCARDVA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 10 | BLUE | — | — | Card Num : | field header |
| 11 | CARDNUM | CARDNUMO | input | UNPROT/IC/FSET | 6 | 17 | 16 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 10 | BLUE | — | — | Acct ID  : | field header |
| 13 | CDACCT | CDACCTO | display | ASKIP/NORM | 8 | 17 | 11 | TURQUOISE | — | — | — | program-supplied display |
| 14 | (literal) | — | caption | ASKIP/NORM | 9 | 5 | 10 | BLUE | — | — | Name     : | field header |
| 15 | CDNAME | CDNAMEO | display | ASKIP/NORM | 9 | 17 | 50 | TURQUOISE | — | — | — | program-supplied display |
| 16 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 10 | BLUE | — | — | Expiry   : | field header |
| 17 | CDEXP | CDEXPO | display | ASKIP/NORM | 10 | 17 | 10 | TURQUOISE | — | — | — | program-supplied display |
| 18 | (literal) | — | caption | ASKIP/NORM | 11 | 5 | 10 | BLUE | — | — | Status   : | field header |
| 19 | CDSTAT | CDSTATO | display | ASKIP/NORM | 11 | 17 | 1 | TURQUOISE | — | — | — | program-supplied display |
| 20 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 21 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display (key prompt) | Record displayed |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | CARDNUM | ○ | □ |
| 7 | CDACCT | □ | □ |
| 8 | CDNAME | □ | □ |
| 9 | CDEXP | □ | □ |
| 10 | CDSTAT | □ | □ |
| 11 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MCARDVA — Card View

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | processing state | I | A (message line) | Enter card number and press ENTER. | on-screen ERRMSG line |
| 2 | ENTER / validation | processing state | C | A (message line) | Press ENTER to view the selected card. | on-screen ERRMSG line |
| 3 | ENTER / validation | processing state | I | A (message line) | Card displayed. | on-screen ERRMSG line |
| 4 | ENTER / validation | the entered value failed a field rule | E | A (message line) | Card number must be exactly 16 digits. | on-screen ERRMSG line |
| 5 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |
| 6 | ENTER / validation | the keyed record does not exist | E | A (message line) | Record not found. | on-screen ERRMSG line |
| 7 | ENTER / validation | a required field was left blank | E | A (message line) | Please enter all required fields. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MCARDVA — Card View

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the keyed card is read and its details are displayed | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 4 | `PF12` | when pressed | cancel the current action and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 5 | `CLEAR` | when pressed | clear the screen and redisplay it | stays on the screen |
| **Data entry section** | | | | |
| 6 | Key field(s) | on ENTER | the keyed card is read and its details are displayed | stays on `MCARDVA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Card View screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| Card master | WS-CARDFILE | whole record | R |

