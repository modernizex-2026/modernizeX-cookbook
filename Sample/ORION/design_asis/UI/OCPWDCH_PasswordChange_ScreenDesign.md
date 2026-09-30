# Screen Design Document — OCPWDCH_PasswordChange

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Password Change |
| Function ID | OCPWDCH |
| CICS transaction | ORPW |
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
    OP["Operator"] --> OCPWDCH["OCPWDCH<br>Password Change"]
    OCPWDCH -- "RU" --> T_WS_USRSEC[("User security<br>WS-USRSEC")]
    OCPWDCH -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Prompt the operator for the current and new password.
2. Validate the new password against the password rules.
3. Update the stored password on confirmation.

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
| 1 | User security | WS-USRSEC | - | 〇 | 〇 | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORPW / selection" --> S["MPWDCHA<br>Password Change"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MPWDCHA"]
```

### 2.1 MPWDCHA — Password Change (OCPWDCH)

**Layout**

![AS-IS mockup of MPWDCHA](../Image/MPWDCH_MPWDCHA.png)

*This screen appears when the operator selects the password-change function.* Physical size 24×80 (mapset `MPWDCH`, map `MPWDCHA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 9 | BLUE | — | — | User ID : | field header |
| 11 | USERID | USERIDO | input | UNPROT/IC/FSET | 6 | 17 | 8 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 9 | BLUE | — | — | Old Pass: | field header |
| 13 | OLDPWD | OLDPWDO | input | UNPROT/IC/FSET | 8 | 17 | 8 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 9 | 5 | 9 | BLUE | — | — | New Pass: | field header |
| 15 | NEWPWD | NEWPWDO | input | UNPROT/IC/FSET | 9 | 17 | 8 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 9 | BLUE | — | — | Confirm : | field header |
| 17 | CFMPWD | CFMPWDO | input | UNPROT/IC/FSET | 10 | 17 | 8 | GREEN | — | — | — | operator input |
| 18 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 19 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display | New password entered |
|---|---|---|---|
| 1 | TRNNAME | □ | □ |
| 2 | TITLE | □ | □ |
| 3 | CURDATE | □ | □ |
| 4 | PGMNAME | □ | □ |
| 5 | CURTIME | □ | □ |
| 6 | USERID | ○ | ○ |
| 7 | OLDPWD | ○ | ○ |
| 8 | NEWPWD | ○ | ○ |
| 9 | CFMPWD | ○ | ○ |
| 10 | ERRMSG | □ | □ |

## 3. Check specifications

### 3.1 MPWDCHA — Password Change

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | processing state | I | A (message line) | Enter user id, old and new password. | on-screen ERRMSG line |
| 2 | ENTER / validation | the entry passed validation | C | A (message line) | New password and confirm do not match. | on-screen ERRMSG line |
| 3 | ENTER / validation | processing state | I | A (message line) | Current password is incorrect. | on-screen ERRMSG line |
| 4 | ENTER / validation | the action completed | I | A (message line) | Password changed successfully. | on-screen ERRMSG line |
| 5 | ENTER / validation | processing state | E | A (message line) | Password change failed. | on-screen ERRMSG line |
| 6 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |
| 7 | ENTER / validation | a required field was left blank | E | A (message line) | Please enter all required fields. | on-screen ERRMSG line |
| 8 | ENTER / validation | the keyed record does not exist | E | A (message line) | Record not found. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MPWDCHA — Password Change

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the new password is validated and stored | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| **Data entry section** | | | | |
| 4 | Key field(s) | on ENTER | the new password is validated and stored | stays on `MPWDCHA` (or shows a message) |

## 5. DB CRUD

**Update spec**

### WS-USRSEC — User security

| No. | PK | Item name | Field | On ENTER — read |
|---|---|---|---|---|
| 1 | ✓ | Id | US-ID | read |
| 2 |  | First Name | US-FIRST-NAME | read |
| 3 |  | Last Name | US-LAST-NAME | read |
| 4 |  | Password | US-PASSWORD | read |
| 5 |  | Type | US-TYPE | read |
| 6 |  | Filler | FILLER | read |

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On ENTER — read |
|---|---|---|---|
| User security | WS-USRSEC | whole record | R/U |

