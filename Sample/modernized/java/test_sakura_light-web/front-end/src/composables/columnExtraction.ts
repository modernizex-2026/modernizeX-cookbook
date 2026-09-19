/**
 * Declarative column metadata + extraction helper for OCCURS-style list screens.
 *
 * Replaces the per-screen pattern of:
 * - hardcoded `slice(row1, col, width)` calls scattered through the SFC
 * - hand-crafted `<table>` markup with hardcoded `<th>` / `<td>` order
 *
 * With a single source of truth:
 * - COLUMNS: ColumnDef[]  — declared once at the top of the SFC
 * - rows = extractRecords(buffer, COLUMNS, opts)  — generic helper
 * - <ColumnCell v-for="col in COLUMNS"> — generic renderer
 *
 * Benefits:
 * - Reorder column = swap entries in COLUMNS array (1 edit)
 * - Add column = add 1 entry (no template touch)
 * - Hide column = `visible: () => ...` per entry
 * - Derived column = `compute: (record) => ...`
 * - Type-safe key access — no magic numbers in render code
 * - Coord changes confined to ONE place per column (vs scattered slice() calls)
 */

import type { Ref, ComputedRef } from 'vue';

/* ── Types ─────────────────────────────────────────────────────────────── */

/** Where in the per-record byte window this value comes from. */
export interface ColumnSource {
  /** 0-based offset from the record's first row. Most COBOL list screens use 0 (data on
   * first row of the record) or 1 (continuation on second row, e.g. 品名 under 品目CD). */
  rowOffset: number;
  /** 1-based COBOL column (matches manifest). Will be converted to 0-based internally. */
  col: number;
  /** Field width in characters (matches manifest picture width). */
  width: number;
}

/** Render mode for a column cell. ColumnCell.vue dispatches on this. */
export type ColumnRender =
  /** Plain text — single source value. */
  | 'plain'
  /** Numeric — same as plain but with .scr-numeric class (right-align + mono font). */
  | 'numeric'
  /** Stacked — render each source on its own line with NBSP placeholder when empty. */
  | 'stacked'
  /** Stamps — render each source as approval-seal box (filled / empty=—). */
  | 'stamps'
  /** Computed — value comes from `compute()` callback, not buffer slice. */
  | 'computed';

/** Declarative column definition. */
export interface ColumnDef<R = Record<string, unknown>> {
  /** Semantic key — used as :key + ARIA + debug identifier. Avoid magic numbers in templates. */
  key: string;
  /** Header label (Japanese, displayed in <th>). For multi-line stacked headers, use `\n`. */
  label: string;
  /** 1+ source coords. Stamps + stacked use multiple sources; plain/numeric use 1. */
  sources?: ColumnSource[];
  /** Group sub-labels (e.g. ['部長', 'マネージャ', '担当']). When set, the column has a
   * parent <th colspan=N> + N sub-<th> children. `sources.length` MUST match `group.length`. */
  group?: string[];
  /** Render dispatch — picks the cell component. Default 'plain'. */
  render?: ColumnRender;
  /** Conditional visibility (e.g. role-based). Called per render; cheap. */
  visible?: () => boolean;
  /** Derived value — when render='computed' OR for multi-source renders
   * (stamps/stacked) the value comes from here instead of slicing buffer.
   * `r` is the partially-extracted record so far. Returns string for plain/
   * numeric/computed renders; string[] for stamps/stacked; null to defer
   * to the buffer-slice values. */
  compute?: (r: R) => string | string[] | null;
  /** Optional CSS class name applied to <td>. */
  cellClass?: string;
}

/** A single extracted record. Keys match COLUMNS[*].key. Values are:
 * - string when sources.length === 1 (plain/numeric/computed)
 * - string[] when sources.length > 1 (stacked/stamps) */
export type ExtractedRecord = Record<string, string | string[]>;

/* ── Extraction helper ─────────────────────────────────────────────────── */

export interface ExtractOptions {
  /** 1-based COBOL line where the first record starts. */
  startLine: number;
  /** Number of records to extract (e.g. OCCURS 8). */
  recordCount: number;
  /** Number of COBOL rows per record (data row + name row = 2). */
  rowsPerRecord: number;
  /** Optional callback: skip a record if the given line range is covered by an
   * active popup window (avoids reading overlay text as record data). */
  isCoveredByPopup?: (line: number) => boolean;
  /** Optional callback: drop a record after extraction if it's deemed empty
   * (e.g. all sources blank). Defaults to dropping when EVERY non-computed source is blank. */
  isEmpty?: (record: ExtractedRecord, columns: ColumnDef[]) => boolean;
}

/**
 * Extract per-record values from a 25×80 char buffer using declarative metadata.
 *
 * Returns an array of `ExtractedRecord`s (length ≤ `recordCount`) — records whose entire
 * data area is covered by a popup OR detected as empty are filtered out.
 */
export function extractRecords<R extends ExtractedRecord = ExtractedRecord>(
  charBuffer: string[][],
  columns: ColumnDef[],
  opts: ExtractOptions,
): R[] {
  const out: R[] = [];
  for (let i = 0; i < opts.recordCount; i++) {
    const baseRow = opts.startLine + i * opts.rowsPerRecord;
    if (opts.isCoveredByPopup) {
      let covered = false;
      for (let off = 0; off < opts.rowsPerRecord; off++) {
        if (opts.isCoveredByPopup(baseRow + off)) { covered = true; break; }
      }
      if (covered) continue;
    }

    const record: ExtractedRecord = {};
    for (const col of columns) {
      if (col.render === 'computed' || !col.sources?.length) continue;
      if (col.sources.length === 1) {
        const src = col.sources[0];
        record[col.key] = sliceBuffer(charBuffer, baseRow + src.rowOffset, src.col, src.width);
      } else {
        record[col.key] = col.sources.map((src) =>
          sliceBuffer(charBuffer, baseRow + src.rowOffset, src.col, src.width),
        );
      }
    }
    // Second pass — fill computed columns now that all source-extracted values are present.
    for (const col of columns) {
      if (col.render === 'computed' && col.compute) {
        const v = col.compute(record as R);
        if (v !== null) record[col.key] = v;
      }
    }

    const empty = opts.isEmpty
      ? opts.isEmpty(record, columns)
      : isRecordEmpty(record, columns);
    if (empty) continue;

    out.push(record as R);
  }
  return out;
}

/** Slice a 1-based (line, col, width) window from charBuffer, returning trimmed text. */
function sliceBuffer(
  charBuffer: string[][],
  line: number,
  col: number,
  width: number,
): string {
  const row = charBuffer[line - 1];
  if (!row) return '';
  return row.slice(col - 1, col - 1 + width).join('').trim();
}

/** Default empty-record detector: returns true when EVERY non-computed extracted value is blank.
 * Treats stamp arrays as "all empty" when every element is blank after ZWSP+full-width-space strip. */
function isRecordEmpty(record: ExtractedRecord, columns: ColumnDef[]): boolean {
  for (const col of columns) {
    if (col.render === 'computed') continue;
    const v = record[col.key];
    if (typeof v === 'string') {
      if (!isBlankCell(v)) return false;
    } else if (Array.isArray(v)) {
      if (v.some((s) => !isBlankCell(s))) return false;
    }
  }
  return true;
}

/** Whitespace check that strips ASCII whitespace + zero-width space (U+200B) + full-width
 * space (U+3000) — COBOL `MOVE ALL NC"　"` fills with full-width spaces that JS `.trim()`
 * does NOT strip (depending on engine), so a "blank" stamp would otherwise count as filled. */
export function isBlankCell(s: string | undefined | null): boolean {
  return !s || !s.replace(/[\s​　]/g, '');
}

/* ── Option 3 typed-path extraction (preferred when records available) ──── */

/**
 * Option 3 — preferred extraction path. Returns backend-resolved typed records
 * (state.recordsByScreen[screenName]) when populated; falls back to charBuffer
 * slicing (Option 1) otherwise.
 *
 * <p>Migration semantics: coexist with Option 1 indefinitely. A screen "opts in" to
 * the typed path by virtue of the backend generator emitting recordSchema in
 * manifest.json — runtime RecordAccumulator then auto-flushes records on transition.
 *
 * <p>Output shape: typed records are keyed by SCHEMA field names (e.g. `sinseino`,
 * `hanbaicd`), NOT ColumnDef keys. SFC templates must read via the schema key —
 * either by aligning ColumnDef.key = schema key, or by adding a `sourceKey` mapping.
 */
export function extractRecordsFromState<R extends ExtractedRecord = ExtractedRecord>(
  args: {
    /** Composable state: must expose charBuffer + recordsByScreen (provided by useTerminalSocket). */
    state: {
      charBuffer: string[][];
      recordsByScreen: Record<string, Array<Record<string, unknown>>>;
    };
    /** Screen name as it appears in manifest (UPPERCASE keys are auto-applied).
     * Pass an ARRAY when records may arrive under any of several sub-screens
     * depending on runtime state (e.g. pending=SC-DISP-GREEN vs
     * approved=SC-DISP-YELLOW); the first key with a populated entry wins,
     * falling back to the first key's empty array if all are empty. */
    screenName: string | string[];
    /** ColumnDef list — used by charBuffer fallback when typed records absent. */
    columns: ColumnDef[];
    startLine: number;
    recordCount: number;
    rowsPerRecord: number;
    isCoveredByPopup?: (line: number) => boolean;
  },
): R[] {
  // once backend has emitted ANY displayRecordList
  // frame for this screen (even an empty array), TRUST the typed path. Do
  // NOT fall back to charBuffer just because typed.length === 0 — that
  // re-introduces the popup-bleed-through bug (popup overlay paint shows
  // through into list cells when COBOL clears typed records on transition
  // to popup). The charBuffer fallback is ONLY for screens that have never
  // emitted typed records (no Option 3 backend support).
  //
  // accept array of screenName candidates. Records may be emitted
  // under SC-DISP-GREEN (pending) AND/OR SC-DISP-YELLOW
  // (approved) depending on KB-DATAKB runtime state. On a SINGLE page COBOL
  // may flush BOTH (e.g. 3 pending + 5 approved when wkMax > pending count),
  // so we MERGE all populated entries in the order the candidates were
  // declared. The order matches the COBOL paragraph emission order
  // (dspZ000 calls displayScreen GREEN for KB-PRN==0 entries first, then
  // YELLOW for KB-PRN==1) — preserving it keeps the row indices aligned
  // with the № column. If all candidates are empty, fall through to the
  // first one's empty array (preserves "list was populated then cleared"
  // intent). Merging (not first-non-empty) ensures YELLOW records aren't
  // dropped when GREEN also has entries.
  const candidates = Array.isArray(args.screenName)
    ? args.screenName.map(s => s.toUpperCase())
    : [args.screenName.toUpperCase()];
  let firstSeen: Array<Record<string, unknown>> | undefined;
  let merged: Array<Record<string, unknown>> | undefined;
  for (const key of candidates) {
    const entry = args.state.recordsByScreen[key];
    if (entry === undefined) continue;
    if (firstSeen === undefined) firstSeen = entry;
    if (entry.length === 0) continue;
    if (merged === undefined) merged = [...entry];
    else merged.push(...entry);
  }
  const picked = merged ?? firstSeen;
  if (picked !== undefined) {
    // typed records arrive with backend's COBOL field
    // names (hanbaicd, kikaku1, syoninmei[]), but ColumnDef may declare
    // DERIVED columns (e.g. 'item' = hanbaicd+kikaku1+hanbaimei1, 'approvers'
    // = syoninmei.slice(0,3)) whose value comes from compute(). Without
    // running compute, derived columns render blank. Apply compute callbacks
    // on each record so derived keys are populated for ColumnCell render.
    const enriched = picked.map((rec) => {
      const out = { ...rec } as Record<string, unknown>;
      for (const col of args.columns) {
        if (col.compute && !(col.key in out)) {
          const v = col.compute(out as R);
          if (v !== null) out[col.key] = v;
        } else if (col.compute && col.render && col.render !== 'plain' && col.render !== 'numeric') {
          // For multi-source renders (stacked/stamps) where compute provides the
          // array form, prefer compute over the raw value to keep render uniform.
          const v = col.compute(out as R);
          if (v !== null) out[col.key] = v;
        }
      }
      return out;
    });
    return enriched as unknown as R[];
  }
  // Fallback — Option 1 declarative metadata over charBuffer (legacy screens
  // without recordSchema; backend never emits displayRecordList for them).
  return extractRecords<R>(args.state.charBuffer, args.columns, {
    startLine: args.startLine,
    recordCount: args.recordCount,
    rowsPerRecord: args.rowsPerRecord,
    isCoveredByPopup: args.isCoveredByPopup,
  });
}

/* ── Header helpers (template uses these to render <thead>) ───────────── */

/** Total number of visible cells per data row (for empty-state `<td colspan>`). */
export function totalCellCount(columns: ColumnDef[]): number {
  let n = 0;
  for (const col of columns) {
    if (col.visible && !col.visible()) continue;
    n += col.group?.length ?? 1;
  }
  return n;
}

/** Visible columns only (filter by visible() callback). */
export function visibleColumns(columns: ColumnDef[]): ColumnDef[] {
  return columns.filter((c) => !c.visible || c.visible());
}
