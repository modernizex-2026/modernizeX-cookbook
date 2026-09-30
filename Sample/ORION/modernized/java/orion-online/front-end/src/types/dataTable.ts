/** Text alignment for table header and body cells. */
export type DataTableAlign = 'left' | 'center' | 'right';

/** Built-in cell render mode. Use slots to override per column. */
export type DataTableColumnKind = 'index' | 'display' | 'input' | 'select';

export interface DataTableSelectOption {
  value: string;
  label: string;
}

export interface DataTableColumn {
  /** Unique column id; used for slot names (`cell-{key}`) and value lookup. */
  key: string;
  label: string;
  kind?: DataTableColumnKind;
  align?: DataTableAlign;
  width?: string;
  headerClass?: string;
  cellClass?: string;
  /** COBOL screen field name for editable input/select cells. */
  fieldName?: string;
  inputMode?: 'text' | 'numeric';
  /** Show custom up/down spinner buttons for numeric input cells (e.g. quantity fields). */
  spinner?: boolean;
  maxlength?: number;
  options?: DataTableSelectOption[];
  /** Display-only formatting for the non-editing cell value. 'number' adds thousands separators (e.g. "1,000"). */
  format?: 'number';
}

/** Wired from useScreenForm screens for OCCURS table editing. */
export interface DataTableEditableState {
  isEditing: (rowIndex: number, fieldName: string) => boolean;
  editValue: string;
  activeWidth?: number;
  /**
   * useScreenForm.bindActiveInput — required for focus after Enter when inputs
   * live inside DataTable (child SFC). Do not use inputRef here.
   */
  bindActiveInput: (el: Element | object | null) => void;
  onInput: (e: Event) => void;
  onKeydown: (e: KeyboardEvent) => void;
  /** Optional: called when the active input loses focus (click-out / Tab). */
  onBlur?: (e: FocusEvent) => void;
  /**
   * Optional: called when a display cell (non-editing row) is clicked.
   * Screen component uses this to implement click-to-edit previous rows.
   */
  onRowClick?: (rowIndex: number, fieldName?: string) => void;
}
