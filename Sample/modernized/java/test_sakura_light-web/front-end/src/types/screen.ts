// ── Server → Client messages ──

export interface FieldData {
  col: number;
  type: string;
  text?: string;
  fieldName?: string;
  color?: string;
  reverse?: boolean;
  blink?: boolean;
  lowlight?: boolean;
  overLineTo?: number;
  underLineTo?: number;
  verticalLines?: number[];
}

export interface RowData {
  line: number;
  fields: FieldData[];
}

export interface DisplayScreenMsg {
  type: 'displayScreen';
  screenName: string;
  clearScreen: boolean;
  rows: RowData[];
  window?: { lineOffset: number; colOffset: number };
  /** COBOL `CLEAR DATA TO N` directive — inclusive row range to blank before paint. */
  clearRegion?: { fromLine: number; toLine: number };
}

export interface AcceptFieldMsg {
  type: 'acceptField';
  fieldName: string;
  line: number;
  col: number;
  width: number;
  picture?: string;
  /** OCCURS subscript (0-based) for table fields — drives click-to-edit row sync. */
  occursIdx?: number;
}

export interface AcceptScreenField {
  fieldName: string;
  line?: number;
  col?: number;
  width?: number;
  picture?: string;
}

export interface AcceptScreenMsg {
  type: 'acceptScreen';
  fields: AcceptScreenField[];
}

export interface ReadEndStatusMsg {
  type: 'readEndStatus';
}

export interface DisplayFieldMsg {
  type: 'displayField';
  fieldName: string;
  line: number;
  col: number;
  value: string;
}

export interface ShowMessageMsg {
  type: 'showMessage';
  text: string;
}

/** COBOL STOP 'literal' / DISPLAY ... UPON GDD → Guide Display Window (blocking modal).
 *  BE blocks until the client replies with a guideDisplayAck (OK/Esc). */
export interface GuideDisplayMsg {
  type: 'guideDisplay';
  text: string;
}

export interface OpenWindowMsg {
  type: 'openWindow';
  name: string;
  height: number;
  width: number;
  line: number;
  col: number;
  border: boolean;
}

export interface CloseWindowMsg {
  type: 'closeWindow';
}

export interface ClearScreenMsg {
  type: 'clearScreen';
}

export interface ProgramFinishedMsg {
  type: 'programFinished';
  code: number;
  reason?: string;
}

export interface ErrorMsg {
  type: 'error';
  message: string;
}

export interface IdleWarningMsg {
  type: 'idleWarning';
  minutesLeft: number;
}

/**
 * Option 3 — typed-record list frame. Emitted by backend RecordAccumulator after
 * one or more displayScreen() calls on a list/OCCURS screen, just before the
 * renderer transitions to a non-list screen (or accepts user input).
 *
 * Generic value type: keep loose at the transport level. SFCs cast to their
 * generated per-program record interface (see schemas.d.ts).
 */
export interface DisplayRecordListMsg {
  type: 'displayRecordList';
  screenName: string;
  recordName: string;
  records: Array<Record<string, unknown>>;
}

export type ServerMessage =
  | DisplayScreenMsg | AcceptFieldMsg | AcceptScreenMsg
  | ReadEndStatusMsg | DisplayFieldMsg | ShowMessageMsg
  | GuideDisplayMsg
  | OpenWindowMsg | CloseWindowMsg | ClearScreenMsg
  | ProgramFinishedMsg | ErrorMsg | IdleWarningMsg
  | DisplayRecordListMsg;

// ── Client → Server messages ──

export interface FieldInputMsg {
  type: 'fieldInput';
  value: string;
  aidKey?: string;
}

export interface ScreenInputMsg {
  type: 'screenInput';
  values: Record<string, string>;
  aidKey: string;
}

export interface EndStatusInputMsg {
  type: 'endStatusInput';
  aidKey: string;
}

// ── Screen state ──

export type WaitingFor = 'none' | 'field' | 'screen' | 'endStatus';

export interface ActiveField {
  fieldName: string;
  line: number;
  col: number;
  width: number;
  picture?: string;
  /** OCCURS subscript (0-based) for table fields — drives click-to-edit row sync. */
  occursIdx?: number;
}

export interface ActiveWindow {
  name: string;
  height: number;
  width: number;
  line: number;
  col: number;
  border: boolean;
  /** Snapshot of buffer cells the window covers, captured at openWindow.
   * closeWindow restores these so the cells under the popup don't bleed
   * through as "data" to readers that scan charBuffer directly. */
  savedChar?: string[][];
  savedColor?: (string | null)[][];
  savedAttr?: (string | null)[][];
}
