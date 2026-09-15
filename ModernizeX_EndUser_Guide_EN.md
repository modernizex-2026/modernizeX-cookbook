# ModernizeX — End-User Guide

**Product:** ModernizeX — COBOL → Java Modernization Platform
**Document version:** 2.1 · **Date:** 2026-09-15

> This guide reflects the actual UI of the running ModernizeX application (build `2.0-refactored`). Every screenshot is captured directly from the live app.

---

## Table of Contents

1. [Overview & Navigation](#1-overview--navigation)
2. [Dashboard (Workspace)](#2-dashboard-workspace)
3. [Creating a New Project](#3-creating-a-new-project)
4. [Project Overview](#4-project-overview)
5. [Assessment — Database](#5-assessment--database)
6. [Assessment — Files (VSAM/QSAM)](#6-assessment--files-vsamqsam)
7. [Assessment — Analysis (Graph)](#7-assessment--analysis-graph)
8. [AS-IS — Input Source](#8-as-is--input-source)
9. [AS-IS — AS-IS Design](#9-as-is--as-is-design)
10. [TO-BE — Conversion Results](#10-to-be--conversion-results)
11. [TO-BE — Class & Schema Mapping](#11-to-be--class--schema-mapping)
12. [TO-BE — Java Output](#12-to-be--java-output)
13. [TO-BE — Refactor Code](#13-to-be--refactor-code)
14. [TO-BE — Modernize UI](#14-to-be--modernize-ui)
15. [TO-BE — TO-BE Design](#15-to-be--to-be-design)
16. [Data Migration — Gen Schema](#16-data-migration--gen-schema)
17. [Data Migration — Migration Files](#17-data-migration--migration-files)
18. [Validation — Gen Unit Test](#18-validation--gen-unit-test)
19. [Validation — Integration Test](#19-validation--integration-test)
20. [Settings](#20-settings)
21. [Help](#21-help)
22. [Delivery & Project Closeout](#22-delivery--project-closeout)
- [Appendix A — Glossary](#appendix-a--glossary)
- [Appendix B — Keyboard Shortcuts](#appendix-b--keyboard-shortcuts)
- [Appendix C — Deployment & AI Provider](#appendix-c--deployment--ai-provider)

---

## 1. Overview & Navigation

### 1.1. What is ModernizeX

ModernizeX is a web platform for modernizing COBOL/CICS/JCL systems (including DB2/SQL, DL/I, MQ, VSAM/QSAM) to Java (Spring Boot). Unlike naive LLM code translation, ModernizeX **statically analyzes the entire codebase first** (AST, call graph, data flow, dependency graphs) and only then uses AI to generate documentation and refactor code — grounded in deterministically recovered facts. As a result:

- No hallucinated variable/program names; the original business logic is preserved.
- Every Java component is **traceable** back to its original COBOL program.
- The new system's behavior is validated through Unit Tests / Integration Tests.
- Supports **on-premise** deployment — customer source code never leaves their infrastructure (see Appendix C).

### 1.2. Who Uses ModernizeX

| Role | Main activities on ModernizeX |
|---|---|
| **Project Manager** | Create projects, monitor the Dashboard, export reports |
| **Migration Architect** | Configure stack & AI provider, review and approve AS-IS/TO-BE docs |
| **Developer / Migration Engineer** | Run pipeline steps, review generated Java, resolve conversion issues |
| **QA Engineer** | Generate and run Unit Tests / Integration Tests, analyze differences |
| **Stakeholder / Customer** | View the Dashboard, download deliverables |

### 1.3. End-to-End Workflow

All work is organized into sequential functional groups:

```
Import Source
        ↓
Assessment (system evaluation & analysis)
        ↓
AS-IS (browse original source · AS-IS design docs)
        ↓
TO-BE (COBOL → Java conversion · refactor · UI modernization · TO-BE design docs)
        ↓
Data Migration (schema generation & VSAM data migration)
        ↓
Validation (generate & run Unit / Integration Tests)
        ↓
Delivery
```

### 1.4. Sidebar Structure

The left sidebar has three areas:

- **WORKSPACE**
  - **Dashboard** — overview of all projects.
- **PROJECT** — the items below appear only when a project is open. The panel shows the project name and its status pill; click **CLOSE** to close the current project.
  - **ASSESSMENT:** Overview · Database · Files (VSAM/QSAM) · Analysis (Graph)
  - **AS-IS:** Input Source · AS-IS Design
  - **TO-BE:** Conversion Results · Java Output · Refactor Code · Modernize UI · TO-BE Design
  - **DATA MIGRATION:** Gen Schema · Migration Files
  - **VALIDATION:** Gen Unit Test · Integration Test
- **MANAGE**
  - **Settings** — configure the AI Provider and application.
  - **Help**

### 1.5. Top Bar & Status Colors

The top bar contains: the current-location **breadcrumb**, the **Search projects** box (shortcut `Ctrl+K`), the **language switcher** (EN ⇄ JA — switches the whole UI between English and Japanese), the **theme toggle** (dark/light), and the user menu.

Status pills use consistent colors throughout the app:

| Color | Meaning |
|---|---|
| 🟢 Green | Completed / passed (`COMPLETED`, `MIGRATED`, `SUCCESS`) |
| 🔵 Blue | Assessment done, migration in progress (`ASSESSED`) |
| 🟡 Amber | Running / needs review (`RUNNING`, `WARN`) |
| 🔴 Red | Error / blocked (`FAILED`) |
| ⚪ Gray | Not started / not applicable (`NOT STARTED`, `SKIPPED`, `PENDING`) |

---

## 2. Dashboard (Workspace)

**Navigation:** Sidebar → **Dashboard**

![Dashboard — multi-project overview](ModernizeX_EndUser_Guide_assets/01-dashboard.png)

The Dashboard is the *Multi-project migration overview* screen, showing:

- **Summary tiles:** **Active Projects** and **Total LoC Migrated**.
- **All projects** — the project list with two view modes, **Grid** / **Table** (toggle at the top right).
- Each **project card** shows:
  - Project name, project code, and **status** (e.g. `ASSESSED`, `MIGRATED`).
  - The pipeline progress bar: **IMPORT → ASSESS → REVERSE → MIGRATE → TEST** (completed steps marked ✓).
  - Quick figures: **LoC**, **Programs**, **Copybooks**, **Miss refs**.
  - A trash icon to delete the project.
- A **+ New project** button.

Click a project card to open that project and switch to the **Project Overview** screen.

---

## 3. Creating a New Project

**Navigation:** Click **+ New project** (top of the sidebar or on the Dashboard)

![Create new project dialog](ModernizeX_EndUser_Guide_assets/02-new-project-modal.png)

A **Create new project** dialog (*Configure source stack and project settings*) opens. Fill in the fields (those marked `*` are required):

| Field | Required | Description / Values |
|---|---|---|
| **Project name** | ✔ | Project name, e.g. `AWS CardDemo`. |
| **Package name** | ✔ | Root Java package, e.g. `com.example.modernizex`. |
| **Description** | — | Optional description. |
| **Database target** | ✔ | Target database: **PostgreSQL** / **Oracle**. |
| **COBOL dialect** | — | COBOL dialect: **ANS-85** (default), IBM, HP, Fujitsu, ADABAS, Micro Focus, COBOL 2002, ACOS-77. |
| **Source format** | ✔ | Source format: **Fixed** / **Tandem** / **Variable** / **Free**. |
| **File encoding** | — | Encoding: **Auto** (default), UTF-8, Shift-JIS, MS-932, CP1252. |
| **Document language** | ✔ | Generated-document language: **English** / **Japanese**. |

Click **Create** to create the project (or **Cancel** to abort). After creation, the app opens the project and shows the **Project Overview** screen.

---

## 4. Project Overview

**Navigation:** Project → Sidebar `ASSESSMENT` → **Overview**

![Project Overview — OVERVIEW tab](ModernizeX_EndUser_Guide_assets/03-project-overview.png)

This is the central screen that orchestrates the whole project pipeline. The project header shows the project name, code, and status pill, plus two buttons: **⚙️ Edit Settings** (change the project configuration) and **Export** (export the project report).

### 4.1. Pipeline Cards

The four cards at the top represent the main stages. Each has a status label (`COMPLETED` / `NOT STARTED` / `RUNNING`…) and a corresponding action button:

| Card | Sample status | Primary action | Secondary link |
|---|---|---|---|
| **Import Source** | COMPLETED | **Upload ZIP** | View File |
| **Assessment** | COMPLETED | **Re-run** | — |
| **Reverse Engineering** | COMPLETED | **Re-run** | View Documents |
| **Source Code** | COMPLETED | **Re-run** | View Mapping |

### 4.2. Summary Metrics Bar

Just below the pipeline cards is a metrics strip: **Source Files**, **Total LOC**, **Java LOC**, **Converted**, **Unresolved Refs**, **External Refs**.

### 4.3. Detail Tabs

The lower area has five tabs: **OVERVIEW · COBOL · JAVA · RISKS · METADATA**.

**OVERVIEW tab** — codebase asset breakdown: Total, Programs, Copybooks, BMS Maps, JCL Jobs, BAT Scripts.

**COBOL tab** — Lines-of-Code statistics:

![Project Overview — COBOL tab](ModernizeX_EndUser_Guide_assets/04-overview-tab-cobol.png)

- **Lines of Code:** Total, Programs, Copybooks, BMS Maps, JCL Jobs, BAT Scripts.
- **By Program Type:** split into `batch` / `screen`.
- **Programs:** classified as Online / Interactive / Batch.

**JAVA tab** — Java generation results:

![Project Overview — JAVA tab](ModernizeX_EndUser_Guide_assets/05-overview-tab-java.png)

- **Java Generation:** Total Java LOC, COBOL LOC covered, Programs converted.
- **By Java Layer:** lines and files per architectural layer (SERVICE, INFRASTRUCTURE, FIELD_ACCESSOR, MODEL, LINKAGE…).

**RISKS tab** — reference-risk lists:

![Project Overview — RISKS tab](ModernizeX_EndUser_Guide_assets/06-overview-tab-risks.png)

- **Unresolved Refs** — references that could not be resolved (need attention).
- **External Refs** — references to external components.

**METADATA tab** — technical details of the snapshot:

![Project Overview — METADATA tab](ModernizeX_EndUser_Guide_assets/07-overview-tab-metadata.png)

- Tool Version, Created At, Source Directory, Input Format, Encoding.

---

## 5. Assessment — Database

**Navigation:** Project → Sidebar `ASSESSMENT` → **Database**

![Database Schema & Tables](ModernizeX_EndUser_Guide_assets/09-database.png)

The **Database Schema & Tables** screen analyzes how COBOL programs interact with data via SQL/DB2, DL/I, and MQ.

- **DB Stats:** summary figures — **SQL Programs**, **Distinct Tables**, **DL/I Programs**, **MQ Programs**.
- **SQL:** two expandable relationship tables — **Tables → Programs** (which programs access each table) and **Programs → Tables** (which tables each program uses).
- **DL/I:** list of programs that use DL/I (IMS).
- **MQ:** list of programs that use MQ queues.

Use this screen to scope data access before generating the target schema and migrating data. For purely file-based systems (VSAM/QSAM only), these figures can legitimately be zero.

---

## 6. Assessment — Files (VSAM/QSAM)

**Navigation:** Project → Sidebar `ASSESSMENT` → **Files (VSAM/QSAM)**

![Project Files — VSAM/QSAM analysis](ModernizeX_EndUser_Guide_assets/10-files-vsam-qsam.png)

The **Project Files** screen (*VSAM / QSAM data file analysis*) lists all sequential/indexed data files of the legacy system.

- **File Stats:** total files and counts of **VSAM**, **QSAM**, **Orphan** (files not referenced by any program).
- A **search box** to filter the list by file name, DSN, or program.
- **File detail table** with columns: **Name**, **DSN**, **Organization** (INDEXED/SEQUENTIAL/…), **Access Mode** (DYNAMIC/SEQUENTIAL/RANDOM/…), **Record Key**, **Programs** (programs using the file), **Readers**, **Writers**.

Use this screen to scope the VSAM data migration (see section 17).

---

## 7. Assessment — Analysis (Graph)

**Navigation:** Project → Sidebar `ASSESSMENT` → **Analysis (Graph)**

![Call Graph Analysis — Tree mode](ModernizeX_EndUser_Guide_assets/11-call-graph-tree.png)

The **Call Graph Analysis** screen visualizes call relationships between programs.

**How to use:**

1. Filter by **Program Type**: **All**, **batch**, or **screen** (with counts).
2. Select a program in the **Program** dropdown (e.g. `MENU00`).
3. Choose the analysis direction:
   - **Calls made by** — programs *called by* the selected program.
   - **Calls made to** — programs that *call* the selected program.
4. Toggle between **Tree** and **Graph** display modes. In Tree mode, each edge is labeled with its call type (`CALL`), and recursive call chains are flagged with a **CYCLE** badge.

In **Graph** mode, the diagram is rendered as nodes and edges with layout options:

![Call Graph Analysis — Graph mode](ModernizeX_EndUser_Guide_assets/12-call-graph-visual.png)

- **Layout** (e.g. *Dagre top-down*), plus **Fit** and **Re-layout** buttons.
- **Hide externals** / **Hide unresolved** options to reduce clutter.
- A node/edge counter (e.g. *66 nodes · 202 edges*) at the top right of the toolbar.
- **Legend:** Program, Shared, Also-root, External, Unresolved.
- **Click a node** to see its details in the right-hand panel (*Click a node to see details*).

Use this screen to understand dependencies and prioritize migration order.

---

## 8. AS-IS — Input Source

**Navigation:** Project → Sidebar `AS-IS` → **Input Source**

![Input Source — file tree and source viewer](ModernizeX_EndUser_Guide_assets/08-input-source.png)

The **Input Source** screen (*Original COBOL source files*) lets you browse all ingested original COBOL source, plus the analysis artifacts produced by the Assessment step (`analysis_output/` — parsed program JSON, control-flow graphs, …).

**How to use:**

1. In the left file tree, expand folders (click the ▶/▼ icons) to browse the source structure.
2. Click a file (`.cob`, `.cbl`, `.cpy`, `.jcl`…) to view its content in the source viewer on the right (with line numbers).
3. If a file displays garbled characters, change the **Encoding** dropdown at the top of the viewer (default **Auto**).
4. Use this screen to reference the original source throughout migration.

> Source is ingested via the **Upload ZIP** button on the *Import Source* card in Project Overview.

---

## 9. AS-IS — AS-IS Design

**Navigation:** Project → Sidebar `AS-IS` → **AS-IS Design**

![Reverse Engineering Specs — AS-IS](ModernizeX_EndUser_Guide_assets/13-reverse-as-is.png)

The **Reverse Engineering Specs** screen (*AS-IS documentation generated from source + graph*) produces documentation describing the current COBOL system, based on the source and graph analysis.

**Steps:**

1. Click **Generate** to produce the AS-IS documents (or click **Re-run** on the *Reverse Engineering* card in Project Overview).
2. When generation finishes, the document tree appears in the left column, organized into folders (e.g. `design_asis/Batch`, `Image`, `UI`) — select a document to read its content in the right pane. Documents are written in the **Document language** chosen at project creation (English or Japanese).
3. Export documents with **Export .md**.

> While Reverse Engineering is `NOT STARTED`, the document column is empty until you click **Generate**.

---

## 10. TO-BE — Conversion Results

**Navigation:** Project → Sidebar `TO-BE` → **Conversion Results**

![Conversion Results — metrics and results table](ModernizeX_EndUser_Guide_assets/15-conversion-results.png)

The **Conversion Results** screen summarizes the COBOL → Java conversion.

- **Conversion Metrics:** **Total Assets**, **Converted**, **Accuracy**, **Total Java LOC**.
- **File Details:** a status filter dropdown (**All** / **SUCCESS** / **FAILED** / **UNSUPPORTED** / **SKIPPED** / **PENDING**) and a per-file results table with columns **File Name**, **LOC**, **Status**, **Error Message**. Non-COBOL assets (docs, tools, scripts) are marked `SKIPPED`.

**How to use:**

1. Click **Re-run** on the *Source Code* card in Project Overview to (re)run the conversion.
2. Filter and review files with `FAILED` or `UNSUPPORTED` status; read the **Error Message** column to resolve them.
3. Verify the **Accuracy** metric meets your target before moving on.

---

## 11. TO-BE — Class & Schema Mapping

**Navigation:** Project Overview → *Source Code* card → **View Mapping**

![Class & Schema Mapping — COBOL ↔ Java](ModernizeX_EndUser_Guide_assets/16-conversion-mapping.png)

The **Class & Schema Mapping** screen (*COBOL to Java source and schema translation*) shows a direct side-by-side comparison between the original source and the generated output, in three panes:

1. **COBOL Source** — the COBOL source tree.
2. **COBOL code pane** — the content of the selected COBOL file (with an **Encoding** selector).
3. **Java Output** — the Java files generated from the selected COBOL program; select a Java file to view the corresponding content.

The **File Details** button (top right) shows detailed information about the compared file pair. Use this screen to confirm that COBOL logic was translated correctly into Java.

---

## 12. TO-BE — Java Output

**Navigation:** Project → Sidebar `TO-BE` → **Java Output**

![Java Output — file tree and Java viewer](ModernizeX_EndUser_Guide_assets/17-java-output.png)

The **Java Output** screen (*Source code generate from the migration process*) lets you browse the entire generated Java project.

**How to use:**

1. Click **Generate Java** (top right) to run the COBOL → Java generation if it has not been run yet.
2. Expand the left file tree to browse the project structure — the output is split into modules such as `output/<project>-batch` (batch programs), `output/<project>-web` (online/screen programs), and `unittest-report`.
3. Click a `.java` file (or `.xml`, `.yml`…) to view its content in the code viewer on the right.
4. Click **Download Zip** (top right) to download the full Java project.

---

## 13. TO-BE — Refactor Code

**Navigation:** Project → Sidebar `TO-BE` → **Refactor Code**

![Refactor Code](ModernizeX_EndUser_Guide_assets/20-refactor-code.png)

The **Refactor Code** screen (*Improve code quality, readability, and maintainability without changing application behavior*) refactors the generated Java to raise quality without changing behavior.

**Steps:**

1. Click **Refactor Code** (top right) to run the refactoring process.
2. Browse the results in the `Refactor/modernized/` tree on the left.
3. Select a file to view the refactored code in the right pane.

> Before a refactor is run, the results tree is empty.

---

## 14. TO-BE — Modernize UI

**Navigation:** Project → Sidebar `TO-BE` → **Modernize UI**

![Modernize UI — BEFORE / AFTER screen preview](ModernizeX_EndUser_Guide_assets/21-modernize-ui.png)

The **Modernize UI** screen (*Preview generated screens and modernize each one*) converts legacy COBOL terminal screens into modern web interfaces.

**How to use:**

1. The left tree lists all screen-type programs (e.g. `MENU00`, `AP0010`…); expand a program to see its individual screens (`DS-…`). Screens already modernized are marked ✓.
2. Tick the checkboxes (or **Select all**) to choose the screens to process, then click **Modernize UI** to generate their modern web versions; use **Regenerate** to redo previously generated screens.
3. Click a screen to open the **UI Modernization Preview**: for each screen it shows the **BEFORE** rendering (legacy COBOL 3270 24×80 terminal) side by side with the **AFTER** design (modernized web UI) so you can review the proposed interface.

---

## 15. TO-BE — TO-BE Design

**Navigation:** Project → Sidebar `TO-BE` → **TO-BE Design**

![To-Be Target Architecture](ModernizeX_EndUser_Guide_assets/14-reverse-to-be.png)

The **To-Be Target Architecture** screen (*TO-BE design documentation generated from migration analysis*) produces documentation describing the target Java architecture.

**Steps:**

1. Click **Generate** to produce the TO-BE documents.
2. Select a TO-BE document in the left tree (organized like the AS-IS docs, e.g. `design_tobe/Batch`, `Image`, `UI`) to view the target architecture, detailed service design, and migration decisions.
3. Export documents with **Export .md**.

> It is best to complete and review the AS-IS documentation before working on TO-BE.

---

## 16. Data Migration — Gen Schema

**Navigation:** Project → Sidebar `DATA MIGRATION` → **Gen Schema**

![Gen Schema — DDL SQL generation](ModernizeX_EndUser_Guide_assets/18-gen-schema.png)

The **Gen Schema** screen (*Generate source schema for the project based on the current configuration*) generates the target database schema from the legacy data definitions.

**Steps:**

1. Click **Gen Schema** (top right) to generate the schema.
2. Browse the results in the `schema/` tree — e.g. the consolidated `all_tables.sql` file and the `sql/` folder with per-table DDL.
3. Select a file to view its DDL content (`CREATE TABLE …`) in the viewer on the right.
4. Click **Download** to download the schema scripts.

---

## 17. Data Migration — Migration Files

**Navigation:** Project → Sidebar `DATA MIGRATION` → **Migration Files**

![Migration Files](ModernizeX_EndUser_Guide_assets/19-migration-files.png)

The **Migration Files** screen (*Output files generated by VSAM data migration*) manages the data files produced by VSAM migration.

**Steps:**

1. Click **Start VSAM Migration** (top right) to begin migrating VSAM data.
2. Browse the generated CSV files in the `csv/` tree in the left column.
3. Select a file to preview its data in the right pane.

> Before the migration is run, the `csv/` folder is empty.

---

## 18. Validation — Gen Unit Test

**Navigation:** Project → Sidebar `VALIDATION` → **Gen Unit Test**

![Gen Unit Test — C0 coverage report](ModernizeX_EndUser_Guide_assets/22-gen-unit-test.png)

The **Gen Unit Test** screen (*Generate unit test scripts for the original Java output*) generates and manages unit tests for the Java code.

**How to use:**

1. Click **Gen Unit Test** (top right) to generate and run the unit tests.
2. Select the source scope using the **Source Before Refactor** / **Source After Refactor** tabs.
3. Review the **Unit Test Report**: statement coverage (**C0 Coverage**) per program with an overall total, plus notes on uncovered code (framework I/O branches, DAO error paths, interactive-terminal groups, …).
4. Click **Download** to download the test report/scripts.

> Before generation, the screen shows no report.

---

## 19. Validation — Integration Test

**Navigation:** Project → Sidebar `VALIDATION` → **Integration Test**

![Integration Test](ModernizeX_EndUser_Guide_assets/23-integration-test.png)

The **Integration Test** screen (*Generate integration test cases for the project based on the current design*) generates and manages integration test scenarios.

**How to use:**

1. Click **Generate** (top right) to generate integration test cases based on the current design.
2. Browse the test cases in the `test_cases/` tree on the left — one folder per program (e.g. `MENU00`, `INITDB`) — and select a file to view its details.
3. Click **Download** to download the test cases.

> Design Docs (AS-IS/TO-BE) must be generated first — the screen prompts you to generate them if the design documents are not ready.

---

## 20. Settings

**Navigation:** Sidebar `MANAGE` → **Settings**

![Settings — AI Provider](ModernizeX_EndUser_Guide_assets/24-settings.png)

The **Settings** screen has two tabs: **AI Provider** and **App Settings**.

**AI Provider tab** — configure the AI model used by the pipeline:

- **Provider** — the model provider: **Claude** / **Gemini** / **Codex**.
- **Auth Mode** — **API Key** or **Subscription**.
- **Auth Token** — the API key / token (stored in backend settings).
- **Base URL** *(optional)* — override the default endpoint (e.g. `https://api.anthropic.com/v1` for Claude); useful for proxies or self-hosted models.
- **Model** — the model name (e.g. `claude-sonnet-5`).

Click **Save** to save, or **Reset** to restore defaults.

---

## 21. Help

**Navigation:** Sidebar `MANAGE` → **Help**

![Help](ModernizeX_EndUser_Guide_assets/25-help.png)

The **Help** screen provides in-app documentation and support. *(Currently under construction — "Help — coming soon".)*

---

## 22. Delivery & Project Closeout

As the pipeline progresses, the project status advances (e.g. `ASSESSED` after assessment) and reaches **MIGRATED** when all steps are complete (shown on the Dashboard and at the top of the Project Overview screen).

**Exportable / deliverable items:**

| Item | Location | How to export |
|---|---|---|
| AS-IS documents | AS-IS → AS-IS Design | **Export .md** |
| TO-BE documents | TO-BE → TO-BE Design | **Export .md** |
| Java source (project) | TO-BE → Java Output | **Download Zip** |
| Database schema (DDL) | Data Migration → Gen Schema | **Download** |
| Unit test report / scripts | Validation → Gen Unit Test | **Download** |
| Integration test cases | Validation → Integration Test | **Download** |
| Project report | Project Overview | **Export** |

**Pre-delivery checklist:**

- [ ] Source ingested and snapshot stable (Import Source `COMPLETED`).
- [ ] Assessment complete; no **Unresolved Refs** left to handle (RISKS tab).
- [ ] AS-IS and TO-BE documents generated and reviewed.
- [ ] Conversion meets the required **Accuracy**; `FAILED`/`UNSUPPORTED` files resolved.
- [ ] Database schema and data files generated and verified.
- [ ] Unit Tests and Integration Tests generated and passing to target (C0 coverage reviewed).
- [ ] All deliverable documents and source exported.

---

## Appendix A — Glossary

| Term | Meaning |
|---|---|
| **AS-IS** | Documentation of the legacy system in its current state (reverse-engineered) |
| **TO-BE** | Target-state design of the modernized system (forward design) |
| **SAD / SDD** | Software Architecture Document / Software Design Document |
| **Copybook** | Shared COBOL data structure (`.cpy`) |
| **JCL** | Job Control Language — mainframe job control scripts |
| **CICS / BMS** | IBM online transaction system / 3270 screen definitions |
| **VSAM / QSAM** | Indexed / sequential mainframe file formats |
| **DL/I** | Access interface for the IMS hierarchical database |
| **MQ** | Message queue |
| **COMP-3** | COBOL packed-decimal numeric type |
| **DDL** | Data Definition Language — statements that create database structures |
| **C0 Coverage** | Statement coverage — % of executable statements exercised by unit tests |
| **Snapshot** | A versioned capture of the source at a point in time |

---

## Appendix B — Keyboard Shortcuts

| Key | Function |
|---|---|
| `Ctrl+K` | Open the project search box (Search projects) |

---

## Appendix C — Deployment & AI Provider

- **On-premise deployment:** ModernizeX is installed within your internal infrastructure (packaged installer — see *ModernizeX_Installation_Guide*), suitable for environments that require source code to stay inside the customer's systems.
- **AI configuration (Settings → AI Provider):**
  - **Provider** — choose the model provider (**Claude**, **Gemini**, or **Codex**).
  - **Auth Mode** — **API Key** (BYOK: use the customer's own key) or **Subscription**.
  - **Base URL** *(optional)* — point to a custom endpoint, for internal proxies or **self-hosted models** to limit outbound access.
  - **Model** — specify the model name used by the pipeline.

---

*— End of document —*
