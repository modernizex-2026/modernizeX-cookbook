# ModernizeX — COBOL → Java Migration Effort Evaluation

**Subject:** SAKURA-SMS (sales management).

This evaluation measures the actual effort required to convert a complete COBOL codebase to a modern Java/Vue stack using the **ModernizeX** pipeline combined with our **Cobol2Java generator**. Unlike a projection, the per-stage hours below were **measured on a real run** of the pipeline; source-code statistics were measured directly from the repository.

---

## 1. The Pipeline — 7 Stages

ModernizeX executes a migration as seven tool-driven stages. Each stage is automated; the hours measured include operator time to launch, monitor, and review each stage.

| # | Stage | What the tool does |
| :-- | :---- | :---- |
| 1 | **Assessment** | Ingest the source tree; inventory programs/copybooks/screens/JCL; classify each program (batch / screen / online); count LOC; map CALL dependencies; flag unsupported constructs. |
| 2 | **Reverse Engineering** | Parse every COBOL program into a full XML AST (divisions, data items, screen sections, statements); resolve copybooks (incl. REPLACING); build the symbol table / IR; emit the application manifest and reverse-design documents. |
| 3 | **Code Generation** | Generate a complete multi-module Maven project (Spring Boot / Spring Batch) from the AST: one module per program, shared runtime (`common`), file-I/O layer with dual text/SQL mode, screen manifest, and per-screen Vue 3 SFC scaffolding. Output compiles out of the box. |
| 4 | **Code Refactoring** (Modernization) | AI-driven structural refactor of the generated services (dedup near-identical paragraphs, decompose long methods, rename to intent-revealing names) with behavioral-fingerprint preservation; runs programs **in parallel** (auto job sizing); emits per-program refactor reports + diffs. |
| 5 | **UI/UX Modernization** | Replace the 24×80 terminal emulation with per-screen modern Vue components — labels harvested from the COBOL screen captions, faithful field/validation behavior, optional design-system theming. |
| 6 | **Data Migration** | Derive relational DDL from the COBOL record layouts (ISAM/VSAM → PostgreSQL tables), generate column mappings for divergent customer schemas, load provided sample/master data. |
| 7 | **Validation** | Generate JUnit tests per module (parallel, with JaCoCo coverage aggregation) and produce/execute the integration-test specification against the running system. |

---

## 2. Source Profile (measured)

### 2.1 SAKURA-SMS — Sales management (interactive screen + batch)

| Metric | Value |
| :---- | :---- |
| COBOL programs | **68** (.cob) — 60 main + 1 menu + 7 sub |
| COBOL LOC | **30,635** |
| Copybooks | **72** (1,106 LOC) |
| Screens | **46 programs with SCREEN SECTION** (terminal UI) |
| Batch / callable subs | 22 programs |
| Database | Indexed/sequential files (ISAM); no CICS, no embedded SQL |

---

## 3. Measured Effort per Stage

Hours below are **actual measured effort** using ModernizeX + the Cobol2Java generator (operator + pipeline time per stage).

| Stage | SAKURA-SMS |
| :---- | ---: |
| 1. Assessment | 0.2 h |
| 2. Reverse Engineering | 2 h |
| 3. Code Generation | 0.3 h |
| 4. Code Refactoring | 10 h |
| 5. UI/UX Modernization | 0.5 h |
| 6. Data Migration | 1 h |
| 7a. Validation — Unit testing | 12 h |
| 7b. Validation — IT spec | 12 h |
| **Total** | **38 h** |
| Repository | *(link)* |

Reading the numbers:

- **The deterministic stages are near-free.** Assessment, reverse engineering, generation, and data migration together take **≤ 3.5 h** for this ~30K-LOC codebase, because they are fully automated (parser + generator); the output of Stage 3 compiles without manual patching.
- **Effort concentrates where verification lives.** Refactoring (10 h) and validation (24 h) dominate — these stages run AI agents in parallel but retain human review of refactor reports, test results, and IT-spec sign-off. This is by design: the hours are spent *proving* equivalence, not *writing* code.
- **Designed to scale flat.** Parallel per-program execution is intended to keep this effort profile flat as codebases grow; quantifying that scaling on larger estates is future work.

---

## 4. What Each Stage Produced (verified results)

### SAKURA-SMS

- **Generation:** 68 programs → Maven reactors `batch-app-web` (**66 program modules** + shared `common`) and `batch-app-batch` (**2 batch modules**). Full build: **compiles clean**.
- **UI:** screen programs regenerated as per-program Vue 3 SFC folders (36 program UI folders); labels taken from the COBOL screens' own literal captions.
- **Validation:** unit-test campaign completed for **68/68 modules**, with per-module reports and an aggregated JaCoCo coverage report.

---

## 5. Manual Baseline & Comparison

No team manually re-migrated SAKURA-SMS in parallel, so the comparison below is between measured pipeline effort and an *estimated* manual baseline (owner-adjustable), and should be read as indicative rather than a controlled result.

| Codebase | Size | Fully manual (estimated) | ModernizeX pipeline (measured) |
| :---- | :---- | :---- | :---- |
| SAKURA-SMS | 30,635 LOC | **> 12 man-months** (≈ 4 people × 3 months) | **38 h** |

Notes:

- The 38 h figure covers the **pipeline stages through validation** (generated UT executed, IT spec run). Downstream acceptance/enhancement effort depends on each project's UAT and change-request scope and was not part of this measurement.
- Man-month conversion: 1 MM ≈ 21 working days × 8 h = 168 h.

---

## 6. Cost Summary

*(Unit rates intentionally left blank — to be filled in by the project owner.)*

### Assumptions

| Parameter | Value |
| :---- | :---- |
| Pipeline/compute rate | $ ___ / hour |
| Engineer rate (operate + review) | $ ___ / hour |
| Exchange rate reference | 1 USD ≈ ___ VND |

### Per-source cost (ModernizeX pipeline)

| Codebase | Measured hours | Pipeline/compute cost | Human cost | Total |
| :---- | ---: | :---- | :---- | :---- |
| SAKURA-SMS | 38 h | $ ___ | $ ___ | **$ ___** |

### Manual comparison (for the same codebase)

| Codebase | Fully manual effort | Manual cost | Tool-assisted cost | Saving |
| :---- | :---- | :---- | :---- | :---- |
| SAKURA-SMS | > 12 man-months (est.) | $ ___ | $ ___ | ___ % |

---

## 7. Notes & Provenance

- **Measured, not projected:** per-stage hours come from an actual pipeline run recorded by the application team; source statistics were measured directly on the repository (program counts, LOC, copybooks, screens).
- **Generated results verified:** SAKURA build status (66 + 2 modules compile clean) and the 68/68 unit-test campaign are verified from the generated repository in this workspace.
- **Baseline:** no team manually migrated SAKURA-SMS in parallel; the "> 12 man-months" fully-manual figure is a rough, owner-adjustable estimate, marked for owner adjustment.
