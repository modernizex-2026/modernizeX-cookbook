# Screen Design Document — OCDGRP_DisclosureGroupMaint

## 0. Cover

| Item | Content |
|---|---|
| System | ORION-CCMS (ORION Credit Card Management System) |
| Subsystem | On-line (CICS/BMS) |
| Function name | Disclosure Group Maintenance |
| Function ID | OCDGRP |
| CICS transaction | ORDG |
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
    OP["Operator"] --> OCDGRP["OCDGRP<br>Disclosure Group Maintenance"]
    OCDGRP -- "CRU" --> T_WS_DGRPFILE[("Disclosure group<br>WS-DGRPFILE")]
    OCDGRP -- "PF3 returns" --> X_OCMENU["OCMENU<br>Main Menu"]
```

### 1.2 Function overview

1. Look up the disclosure group code and display its current definition.
2. Let the operator add, change or remove the disclosure group definition.
3. Validate and apply the maintenance action on confirmation.

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
| 1 | Disclosure group | WS-DGRPFILE | 〇 | 〇 | 〇 | - | VSAM KSDS (EXEC CICS file control) |

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
    ENTRY["Calling menu / transaction"] -- "ORDG / selection" --> S["MDGRPA<br>Disclosure Group Maintenance"]
    S -- "PF3 (return)" --> X_OCMENU["MMENUA<br>Main Menu"]
    S -- "invalid key / error" --> MSG["Message line (ERRMSG) on MDGRPA"]
```

### 2.1 MDGRPA — Disclosure Group Maintenance (OCDGRP)

**Layout**

![AS-IS mockup of MDGRPA](../Image/MDGRP_MDGRPA.png)

*This screen appears when the operator selects the maintenance function.* Physical size 24×80 (mapset `MDGRP`, map `MDGRPA`).

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
| 10 | (literal) | — | caption | ASKIP/NORM | 6 | 5 | 11 | BLUE | — | — | Acct Group: | field header |
| 11 | DGGRP | DGGRPO | input | UNPROT/IC/FSET | 6 | 18 | 10 | GREEN | — | — | — | operator input |
| 12 | (literal) | — | caption | ASKIP/NORM | 7 | 5 | 11 | BLUE | — | — | Type Code : | field header |
| 13 | DGTYPE | DGTYPEO | input | UNPROT/IC/FSET | 7 | 18 | 2 | GREEN | — | — | — | operator input |
| 14 | (literal) | — | caption | ASKIP/NORM | 8 | 5 | 11 | BLUE | — | — | Cat Code  : | field header |
| 15 | DGCAT | DGCATO | input | UNPROT/IC/FSET | 8 | 18 | 4 | GREEN | — | — | — | operator input |
| 16 | (literal) | — | caption | ASKIP/NORM | 10 | 5 | 10 | BLUE | — | — | Int Rate : | field header |
| 17 | DGRATE | DGRATEO | input | UNPROT/IC/FSET | 10 | 18 | 7 | GREEN | — | — | — | operator input |
| 18 | ERRMSG | ERRMSGO | display | ASKIP/NORM | 23 | 1 | 78 | RED | — | — | — | program-supplied display |
| 19 | (literal) | — | caption | ASKIP/NORM | 24 | 1 | 34 | TURQUOISE | — | — | ENTER=Process  PF3=Back  PF4=Clear | field header |

**Item states**

> Legend: `○`=enabled ｜ `必`=enabled (required) ｜ `□`=display only ｜ `×`=hidden ｜ `レ`=initial focus

| № | Field (BMS) | Initial display | Record loaded | Confirm action |
|---|---|---|---|---|
| 1 | TRNNAME | □ | □ | □ |
| 2 | TITLE | □ | □ | □ |
| 3 | CURDATE | □ | □ | □ |
| 4 | PGMNAME | □ | □ | □ |
| 5 | CURTIME | □ | □ | □ |
| 6 | DGGRP | ○ | ○ | □ |
| 7 | DGTYPE | ○ | ○ | □ |
| 8 | DGCAT | ○ | ○ | □ |
| 9 | DGRATE | ○ | ○ | □ |
| 10 | ERRMSG | □ | □ | □ |

## 3. Check specifications

### 3.1 MDGRPA — Disclosure Group Maintenance

All messages below are the literal strings the program sends to the on-screen message line, extracted from the source. Type: E=Error / W=Warning / C=Confirm / I=Information.

| No. | Timing | Condition | Type | Display | Message content (verbatim) | Notes |
|---|---|---|---|---|---|---|
| 1 | ENTER / validation | an unsupported key was pressed | E | A (message line) | Invalid key pressed. Please try again. | on-screen ERRMSG line |

## 4. Event specifications

### 4.1 MDGRPA — Disclosure Group Maintenance

Per-key and per-field behaviour, written as operator steps.

| No. | Item | Timing / condition | Action | Next screen |
|---|---|---|---|---|
| **Function key section** | | | | |
| 1 | `ENTER` | when pressed | the entered disclosure group definition is validated and the maintenance action is applied | stays on the screen, or continues to the main menu (OCMENU) |
| 2 | `PF3` | when pressed | cancel and return to the main menu (OCMENU) | the main menu (OCMENU) |
| 3 | `PF4` | when pressed | clear the entered values so the operator can start again | stays on the screen |
| 4 | `PF5` | when pressed | confirm and save the change to the file | stays on the screen with a result message |
| **Data entry section** | | | | |
| 5 | Key field(s) | on ENTER | the entered disclosure group definition is validated and the maintenance action is applied | stays on `MDGRPA` (or shows a message) |

## 5. DB CRUD

**Update spec**

### WS-DGRPFILE — Disclosure group

| No. | PK | Item name | Field | On add | On change | On delete |
|---|---|---|---|---|---|---|
| 1 | ✓ | Acct Group | DG-ACCT-GROUP | read | read | delete record |
| 2 |  | Type Cd | DG-TYPE-CD | read | read | — |
| 3 |  | Cat Cd | DG-CAT-CD | read | read | — |
| 4 |  | Int Rate | DG-INT-RATE | read | read | — |
| 5 |  | Filler | FILLER | read | read | — |

**Column-level CRUD (per event / business type)**

| Screen / table | Physical name | Field group | On add | On change | On delete |
|---|---|---|---|---|---|
| Disclosure group | WS-DGRPFILE | whole record | C/R/U | C/R/U | C/R/U |

