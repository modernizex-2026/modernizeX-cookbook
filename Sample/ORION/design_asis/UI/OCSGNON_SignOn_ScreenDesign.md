# Screen Design Document — OCSGNON_SignOn

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Sign On |
| Function ID | OCSGNON |
| CICS transaction | ORSN |
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
    OP["Operator"] --> OCSGNON["OCSGNON<br>Sign On"]
    OCSGNON -- "R" --> T_WS_USRSEC[("User security<br>WS-USRSEC")]
    OCSGNON -- "PF3 returns" --> X_OCADMEN["OCADMEN<br>Administrator Menu"]
    OCSGNON -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Prompt the operator for a user id and password.
2. Validate the credentials against the user security file and, when they match, sign the operator on.
3. Route an administrator to the administrator menu and a normal user to the main menu.

**Roles:** authenticated operator (signed on through the Sign On screen).

**Function keys**

| Key | Label as rendered (verbatim) | What it does |
|---|---|---|
| `ENTER` | `ENTER=Process` | validate the entry and process the current step |
| `PF3` | `PF3=Back` | return to the administrator menu (OCADMEN) |
| `PF4` | `PF4=Clear` | clear the entered values and start again |

### 1.3 Databases used

| № | Logical table name | Physical table/file name | Create(C) | Read(R) | Update(U) | Delete(D) | Notes |
|---|---|---|---|---|---|---|---|
| 1 | User security | WS-USRSEC | - | 〇 | - | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORSN / selection" --> S["MSGNONA<br>Sign On"]
    S -- "PF3 (return)" --> X_OCADMEN["MADMENA<br>Administrator Menu"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MSGNONA"]
```

### 2.1 MSGNONA — Sign On (OCSGNON)

**Layout**

![AS-IS mockup of MSGNONA](../Image/MSGNON_MSGNONA.png)

*This screen appears first when the operator starts the ORSN entry transaction.* Physical size 24×80 (mapset `MSGNON`, map `MSGNONA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 10 | 20 | 9 | BLUE | — | — | User ID : | field header |
| 11 | USERID | USERIDO | input | UNPROT/IC/FSET | 10 | 31 | 8 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 12 | 20 | 9 | BLUE | — | — | Password: | field header |
| 13 | PASSWD | PASSWDO | input | UNPROT/IC/FSET | 12 | 31 | 8 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 14 | 20 | 38 | TURQUOISE | — | — | Type your credentials and press ENTER. | field header |
| 15 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 16 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display | Credentials entered |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | USERID | ○ | ○ |
| 7 | PASSWD | ○ | ○ |
| 8 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MSGNONA — Sign On

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | processing state | I | A (message line) | Please sign on. | on-screen ERRMSG line |
| 2 | ENTER / validation | the keyed record does not exist | E | A (message line) | User not found. | on-screen ERRMSG line |
| 3 | ENTER / validation | the entered value failed a field rule | E | A (message line) | Invalid password. | on-screen ERRMSG line |
| 4 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |
| 5 | ENTER / validation | a required field was left blank | E | A (message line) | Please enter all required fields. | on-screen ERRMSG line |
| 6 | ENTER / validation | processing state | I | A (message line) | Thank you for using ORION-CCMS. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MSGNONA — Sign On

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the user id and password are validated against the user security file; a match signs the operator on | stays on the screen, or continues to the administrator menu (OCADMEN) |
| 2 | `PF3` | when pressed | cancel and return to the administrator menu (OCADMEN) | the administrator menu (OCADMEN) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the user id and password are validated against the user security file; a match signs the operator on | stays on `MSGNONA` (or shows a message) |

## 5. DB CRUD

**Update spec**

Not applicable — the Sign On screen only reads data; no table row is created, updated or deleted.

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| User security | WS-USRSEC | whole record | R |

