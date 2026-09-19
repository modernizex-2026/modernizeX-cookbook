/**
 * useScreenList — shared composable for list/OCCURS-style COBOL screens.
 *
 * Composes useScreenForm (for the row-picker input + PF dispatch) with the
 * Option-3 typed-record extraction (state.recordsByScreen) and Option-1
 * declarative COLUMNS metadata (charBuffer fallback). Eliminates ~150 LOC
 * of boilerplate per list SFC.
 *
 * Per-SFC config: only UI template + COLUMNS config +
 * RECORD_LAYOUT config. No PF map, no submit handlers, no focus watchers.
 *
 * Defaults match the standard list convention:
 * - F7 → ROLL-BW ('BW') for page back
 * - F8 → ROLL-FW ('FW') for page forward
 * - F9 → P9 for exit
 * - F3 → P3 for cancel/back (popup re-open in some screens)
 *
 * @see composables/columnExtraction.ts extractRecordsFromState
 */

import { computed, type ComputedRef } from 'vue';
import {
  extractRecordsFromState,
  visibleColumns as filterVisible,
  totalCellCount,
  type ColumnDef,
  type ExtractedRecord,
} from './columnExtraction';
import { useScreenForm, type ScreenFormProps, type ScreenFormConfig, type ScreenFormApi } from './useScreenForm';

export interface ScreenListConfig extends ScreenFormConfig {
  /** Manifest screen name of the SFC root (parent DS-*). Drives `screenOverride`
   * routing in App.vue. For a typical list this is 'DS-AREA'. */
  screenName: string;
  /** Optional override for typed-record lookup key. When
   * the SFC's `screenName` is a parent DS-* but typed records live under a
   * sub-screen with recordSchema (e.g. SC-DISP-GREEN), pass that
   * sub-screen's name here. Pass an ARRAY when records may arrive under any
   * of several sub-screens depending on runtime state (e.g. SC-DISP-GREEN
   * for pending vs SC-DISP-YELLOW for approved); composable
   * picks the first non-empty hit. Falls back to `screenName` when omitted.
   *
   * Why: COBOL may paint list rows under an internal sub-screen (e.g.
   * SC-DISP-GREEN, `_topLevel:false`) while the user-routed SFC is DS-AREA.
   * Without this, `state.recordsByScreen['DS-AREA']` is empty → composable
   * silently falls back to charBuffer slice → popup paint over the same
   * coords bleeds into list cells. With this set, lookup hits the populated
   * sub-screen records. */
  recordSchemaScreenName?: string | string[];
  /** COLUMNS metadata for table layout + charBuffer fallback when typed records absent. */
  columns: ColumnDef[];
  /** 1-based COBOL line where the first record's primary row starts. */
  startLine: number;
  /** Number of records per page (e.g. 8). */
  recordCount: number;
  /** Buffer rows per record (1 for single-line, 2 for stacked data + continuation). */
  rowsPerRecord: number;
  /** Optional: filter out rows whose buffer area is covered by a popup overlay. */
  isCoveredByPopup?: (line: number) => boolean;
}

export interface ScreenListApi extends ScreenFormApi {
  /** Computed list of records — prefers typed path (state.recordsByScreen) over charBuffer slice. */
  rows: ComputedRef<ExtractedRecord[]>;
  /** Visible columns only (filters via ColumnDef.visible callback). */
  visibleCols: ComputedRef<ColumnDef[]>;
  /** Total cell count for empty-state colspan. */
  totalCells: ComputedRef<number>;
}

export function useScreenList(props: ScreenFormProps, config: ScreenListConfig): ScreenListApi {
  // useScreenForm provides submit, onKey, focus mgmt, valueOf/descOf for the picker.
  // PF defaults override Hitachi/NEC ROLL convention: F7=BW, F8=FW.
  const form = useScreenForm(props, {
    ...config,
    pfOverrides: { F7: 'BW', F8: 'FW', ...(config.pfOverrides ?? {}) },
  });

  const rows = computed(() =>
    extractRecordsFromState({
      state: props.state,
      // Prefer dedicated typed-lookup key when provided (sub-screen
      // with recordSchema like SC-DISP-GREEN); fall back to the SFC's own
      // screenName for backward compat with screens whose typed key matches.
      screenName: config.recordSchemaScreenName ?? config.screenName,
      columns: config.columns,
      startLine: config.startLine,
      recordCount: config.recordCount,
      rowsPerRecord: config.rowsPerRecord,
      isCoveredByPopup: config.isCoveredByPopup,
    }),
  );
  const visibleCols = computed(() => filterVisible(config.columns));
  const totalCells = computed(() => totalCellCount(config.columns));

  return { ...form, rows, visibleCols, totalCells };
}
