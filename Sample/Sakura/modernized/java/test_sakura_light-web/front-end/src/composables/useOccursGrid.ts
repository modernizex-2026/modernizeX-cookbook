/**
 * useOccursGrid — the OCCURS row-entry protocol, consolidating the known
 * failure modes of hand-rolled OCCURS grids:
 *   - rows >10 not rendering, lookup by array index, page-turn wiping rows
 *     (pendingListRefresh)
 *   - occursIdx filing values under
 *     the wrong row; WS burst coalescing under Vue's default watch flush)
 *
 * Every OCCURS grid an SFC renders MUST go through this composable instead of
 * hand-rolled watchers, which repeatedly reinvent
 * (and re-break) each of these behaviors:
 *
 *  1. RECORD LOOKUP BY `no`, NEVER BY ARRAY POSITION — after a page-turn the
 *     backend only emits records for rows that actually have data, so the
 *     array is not padded to the absolute row index.
 *  2. ACTIVE ROW = LAST element of the row-signal batch (DS-NO), watched with
 *     flush:'sync' — a page-turn streams many rows in ONE synchronous WS
 *     burst; Vue's default flush coalesces those mutations into a single
 *     callback carrying only the final value, and the FIRST element is just
 *     whichever row happened to repaint earliest.
 *  3. NEVER trust `activeField.occursIdx` for filing values — it resets while
 *     a child popup is active even though the backend keeps pushing records
 *     for the row underneath. File under the snapshot taken when the OCCURS
 *     field became active (`activeRowIdx`), captured BEFORE the row bumps.
 *  4. enteredRows cache persists the operator's typed values per row (typed
 *     cache is keyed by field NAME only — one slot shared by all 60 rows —
 *     so per-row values MUST live here, and seeds must bypass the shared
 *     typed cache for OCCURS fields).
 *  5. pendingListRefresh — leaving the OCCURS section does NOT wipe the
 *     cache; when re-entering, clear it only if the backend's own row-0 echo
 *     is empty too (a header re-prompt loop keeps BE working storage intact).
 *  6. rowCount grows with data AND the active row — a row's input can only
 *     mount if its index is actually rendered.
 */
import { computed, reactive, ref, watch, type ComputedRef, type Ref } from 'vue';

type StateSlice = {
  recordsByScreen?: Record<string, unknown[] | undefined>;
  fieldsByName?: Record<string, string | undefined>;
};

export interface OccursGridConfig {
  /** SC- input field names cycled per row (e.g. SC-HANBAICD / SC-KIKAKU / SC-IDOSU). */
  occursFields: string[];
  /** Typed-record sources in priority order (first non-empty wins), e.g. ['SC-DISP','SC-AREA']. */
  recordScreens: string[];
  /** Screens whose record batches carry the active row number (default ['DS-NO']). */
  rowSignalScreens?: string[];
  /** Record key holding the 1-based row number (default 'no'). */
  rowKey?: string;
  /** Base visible row count (static number or a growable ref, e.g. useOccursRows().displayRowCount). */
  baseRows: number | Ref<number>;
  /** SC-field → record key overrides; default converts SC-KIKAI_NO → kikaiNo. */
  fieldToKey?: Record<string, string>;
  /** Confirm-anchor names excluded from the "left the section" reset (SC-OK…). */
  anchorFields?: string[];
  /** Live bindings from useScreenForm. */
  form: { activeFieldName: ComputedRef<string> | Ref<string>; editValue: Ref<string> };
}

const ZWSP = /​/g;

function defaultKey(scName: string): string {
  const body = scName.replace(/^(SC|DS|KB)-/, '').toLowerCase();
  return body.replace(/[-_](\w)/g, (_, c: string) => c.toUpperCase());
}

export function useOccursGrid(
  props: { state: StateSlice },
  cfg: OccursGridConfig,
) {
  const occursSet = new Set(cfg.occursFields);
  const anchorSet = new Set(cfg.anchorFields ?? []);
  const rowKey = cfg.rowKey ?? 'no';
  const signals = cfg.rowSignalScreens ?? ['DS-NO'];
  const keyOf = (n: string) => cfg.fieldToKey?.[n] ?? defaultKey(n);

  const enteredRows = ref<Map<number, Record<string, string>>>(new Map());
  const localRowIdx = ref(0);
  /** Row snapshot taken when an OCCURS field became active — values are FILED here (rule 3). */
  let activeRowIdx = 0;
  let pendingListRefresh = false;

  const records = computed<Record<string, unknown>[]>(() => {
    for (const s of cfg.recordScreens) {
      const r = props.state.recordsByScreen?.[s];
      if (Array.isArray(r) && r.length) return r as Record<string, unknown>[];
    }
    return [];
  });

  const rowCount = computed(() => {
    const base = typeof cfg.baseRows === 'number' ? cfg.baseRows : cfg.baseRows.value;
    return Math.max(base, records.value.length, localRowIdx.value + 1); // rule 6
  });

  function captureActive(value?: string) {
    const name = cfg.form.activeFieldName.value;
    if (!name || !occursSet.has(name)) return;
    const rec = enteredRows.value.get(activeRowIdx) ?? {};
    rec[name] = value ?? cfg.form.editValue.value;
    enteredRows.value.set(activeRowIdx, rec);
  }

  const clean = (v: unknown): string => {
    const s = String(v ?? '').replace(ZWSP, '').trim();
    return s === '' || /^0+$/.test(s) ? '' : s;
  };

  /** Cell value: operator cache first, then the record found BY `no` (rule 1). */
  function cellValue(rowIdx: number, scName: string): string {
    const local = enteredRows.value.get(rowIdx)?.[scName];
    if (local) return local;
    const rec = records.value.find((r) => Number(r[rowKey]) === rowIdx + 1);
    return rec ? clean(rec[keyOf(scName)]) : '';
  }

  /** Seed for the ACTIVE OCCURS input — per-row cache, else the backend's raw
   * per-row repaint. Deliberately bypasses the shared typed cache (rule 4). */
  function seedFor(scName: string): string {
    const cached = enteredRows.value.get(localRowIdx.value)?.[scName];
    if (cached) return cached;
    return clean(props.state.fieldsByName?.[scName]);
  }

  // Rule 2: active row = LAST element of each row-signal batch, flush:'sync'.
  for (const sig of signals) {
    watch(
      () => props.state.recordsByScreen?.[sig],
      (recs) => {
        const list = (recs ?? []) as Record<string, unknown>[];
        const last = list[list.length - 1];
        const no = last ? Number(last[rowKey]) : NaN;
        if (!Number.isFinite(no) || no < 1) return;
        const newIdx = no - 1;
        if (newIdx !== localRowIdx.value) {
          captureActive(); // file the row we're LEAVING under its own snapshot
          localRowIdx.value = newIdx;
        }
      },
      { flush: 'sync' },
    );
  }

  // Rules 3 + 5: snapshot the row on entry; reset/reconcile when leaving.
  watch(
    () => cfg.form.activeFieldName.value,
    (now, prev) => {
      if (prev && occursSet.has(prev)) captureActive(cfg.form.editValue.value);
      if (now && !occursSet.has(now) && !anchorSet.has(now)) {
        localRowIdx.value = 0;
        pendingListRefresh = true;
        return;
      }
      if (now && occursSet.has(now)) {
        activeRowIdx = localRowIdx.value;
        if (pendingListRefresh) {
          pendingListRefresh = false;
          // Clear only when the backend's own first-row echo is empty too —
          // a header re-prompt loop keeps BE working storage (and therefore
          // its raw echo) intact, and the cache must survive it.
          const first = cfg.occursFields[0];
          if (!clean(props.state.fieldsByName?.[first])) enteredRows.value.clear();
        }
      }
    },
  );

  function clearRow(rowIdx: number) {
    enteredRows.value.delete(rowIdx);
  }
  function clearCache() {
    enteredRows.value.clear();
  }

  // reactive() so `grid.rowCount` / `grid.localRowIdx` auto-unwrap to plain
  // numbers in BOTH <script> and <template> (nested refs on a plain object do
  // not auto-unwrap in templates); the methods close over the live refs.
  return reactive({
    records,
    rowCount,
    localRowIdx,
    cellValue,
    seedFor,
    captureActive,
    clearRow,
    clearCache,
  });
}
