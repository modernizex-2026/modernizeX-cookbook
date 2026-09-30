import { reactive, onMounted, onUnmounted, watch, type Reactive } from 'vue';
import { translateAidKey } from './aidCodes';
import { DEBUG_ENABLED, dbg } from '../utils/debugLog';

// Logger chẩn đoán FE — cờ `localStorage.cobol_debug = '1'`, định dạng
// `[COBOL-CATEGORY] key=value …` (khớp log SLF4J của backend). Bản dùng CHUNG hai nhánh
// nằm ở core/src/utils/debugLog.ts (R9/B7) — định dạng và ngữ nghĩa giữ nguyên như trước.
import type {
  ServerMessage,
  WaitingFor,
  ActiveField,
  ActiveWindow,
  AcceptScreenField,
} from '../types/screen';

const ROWS = 24;
const COLS = 80;

/**
 * WebSocket endpoint. Set `VITE_WS_BASE_URL` to the remote BE HTTP origin
 * When unset, falls back to same-origin `/ws/...` (integrated deploy on one host).
 */
function buildWebSocketUrl(programId: string): string {
  const urlTicket = new URLSearchParams(window.location.search).get('ticket')?.trim() ?? '';
  let queryParam = '';
  if (urlTicket) {
    queryParam = `ticket=${encodeURIComponent(urlTicket)}`;
  } else {
    try {
      const stationName = sessionStorage.getItem('scr.stationName') ?? '';
      if (stationName) queryParam = `stationName=${encodeURIComponent(stationName)}`;
    } catch { /* SSR / test env */ }
  }
  const path = `/ws/${programId}${queryParam ? '?' + queryParam : ''}`;
  const base = import.meta.env.VITE_WS_BASE_URL?.trim().replace(/\/$/, '');
  if (base) {
    const wsBase = base.replace(/^http:\/\//i, 'ws://').replace(/^https:\/\//i, 'wss://');
    return `${wsBase}${path}`;
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}${path}`;
}

export interface ScreenState {
  /** 24x80 character buffer */
  charBuffer: string[][];
  /** 24x80 color buffer */
  colorBuffer: (string | null)[][];
  /** 24x80 attribute buffer (reverse, blink, etc.) */
  attrBuffer: (string | null)[][];
  /** What the server is waiting for */
  waitingFor: WaitingFor;
  /** Current active field (acceptField) */
  activeField: ActiveField | null;
  /** All fields for acceptScreen */
  screenFields: AcceptScreenField[];
  /** Active window overlay (top of windowStack, exposed for backward compat) */
  activeWindow: ActiveWindow | null;
  /** Full popup stack: COBOL can nest OPEN-WINDOW (popup-on-popup).
   * closeWindow pops the top and restores the cells it had covered.
   * Without this, two overlapping popups would corrupt each other's saved state. */
  windowStack: ActiveWindow[];
  /** Name of the currently displayed screen (from displayScreen.screenName) */
  currentScreenName: string;
  /** Status bar message — populated from DS-MESSAG displayScreen events */
  statusMessage: string;
  /** Contextual sub-region help text — populated from *-MSG help overlays that the COBOL
   * paints next to a field (e.g. DS-IODEN-MSG "※交換納入時の伝票№を入力のこと").
   * Kept SEPARATE from statusMessage: a blank DS-MESSAG (status line) frame often follows the
   * help overlay and would otherwise clear it. Cleared by a blank *-MSG or a full-screen change. */
  subRegionMessage: string;
  /** Status message code (e.g. 'GF001', 'EI001') — derived from statusMessage prefix */
  messageCode: string;
  /** Monotonic counter incremented on EVERY DS-MESSAG status emission. Vue 3
   * optimizes-away reactive writes that don't change the underlying value,
   * so when the backend re-sends the SAME status text (e.g. user types an
   * invalid № twice → two identical EI165 emissions) a watcher keyed only
   * on `statusMessage` won't re-fire — the toast that auto-hid never
   * reappears. SFC toast watchers should observe a tuple
   * `[statusMessage, messageNonce]` so the counter forces refire on every
   * fresh DS-MESSAG even when the text is identical. */
  messageNonce: number;
  /** COBOL STOP 'literal' / DISPLAY ... UPON GDD — text of the active blocking Guide Display
   *  dialog, or null when none. The BE program thread is blocked on waitForInput() until the
   *  client sends a guideDisplayAck (operator closes the dialog via OK/Esc). */
  guideDisplay: string | null;
  /** Reactive map of (line,col) → most-recent displayField value. Templates use this
   * to surface backend-resolved values (e.g. DS-TANTOMEI Tanaka name) without
   * reading charBuffer directly (avoids race with popup overlays at same coords). */
  fieldValues: Record<string, string>;
  /** Is connected */
  connected: boolean;
  /** Program finished */
  finished: boolean;
  /** Error message */
  error: string | null;
  /**
   * Option 3 — typed-record lists keyed by screen name (UPPERCASE). Populated when
   * the backend emits a {@code displayRecordList} WS frame; one entry per list-screen
   * that the server's RecordAccumulator has flushed. List SFCs prefer this typed path
   * over slicing charBuffer (see {@code columnExtraction.extractRecords}).
   */
  recordsByScreen: Record<string, Array<Record<string, unknown>>>;
  /**
   * Option 3 (forms/popups extension) — typed scalar values keyed by COBOL fieldName.
   * Populated from displayScreen / displayField frames that carry a `fieldName` attribute
   * (every `from=...` field in COBOL SCREEN SECTION). Eliminates hardcoded `slice(charBuffer
   * [line-1], col, width)` lookups in form/popup SFCs — read `state.fieldsByName['KB-TANTOMEI']`
   * instead. Coord-invariant: when backend shifts COBOL field positions, JSON keys stay the same.
   */
  fieldsByName: Record<string, string>;
  /** Set when BE paints DS-AREA-KAIZO (TAN-BUKACD = 1102 / SW-KAIZO mode). */
  isKaizoMode: boolean;
}

function createEmptyBuffer(): string[][] {
  return Array.from({ length: ROWS }, () => Array(COLS).fill(' '));
}
function createNullBuffer(): (string | null)[][] {
  return Array.from({ length: ROWS }, () => Array(COLS).fill(null));
}

// Detect Unicode East Asian full-width characters (2 COBOL columns wide)
function isFullWidth(ch: string): boolean {
  const cp = ch.codePointAt(0) ?? 0;
  return (
    (cp >= 0x1100 && cp <= 0x115f) ||
    (cp >= 0x2e80 && cp <= 0xa4cf) ||
    (cp >= 0xac00 && cp <= 0xd7af) ||
    (cp >= 0xf900 && cp <= 0xfaff) ||
    (cp >= 0xfe10 && cp <= 0xfe19) ||
    (cp >= 0xfe30 && cp <= 0xfe6f) ||
    (cp >= 0xff01 && cp <= 0xff60) ||
    (cp >= 0xffe0 && cp <= 0xffe6) ||
    (cp >= 0x20000 && cp <= 0x2fffd) ||
    (cp >= 0x30000 && cp <= 0x3fffd)
  );
}

// Returns the number of buffer cells a string occupies (full-width chars = 2 cells)
function cellWidth(text: string): number {
  let w = 0;
  for (const ch of text) w += isFullWidth(ch) ? 2 : 1;
  return w;
}

// Place text at position in buffer; full-width CJK chars occupy 2 cells
function placeText(buf: string[][], row: number, col: number, text: string) {
  const r = row - 1; // 1-based → 0-based
  let c = col - 1;
  if (r < 0 || r >= ROWS) return;
  for (let i = 0; i < text.length && c < COLS; i++) {
    const ch = text[i];
    buf[r][c] = ch;
    if (isFullWidth(ch)) {
      c++;
      if (c < COLS) buf[r][c] = '​'; // zero-width placeholder for double-width column
    }
    c++;
  }
}

function placeColor(
  buf: (string | null)[][],
  row: number,
  col: number,
  len: number,
  color: string | null,
) {
  const r = row - 1;
  const c = col - 1;
  if (r < 0 || r >= ROWS) return;
  for (let i = 0; i < len && c + i < COLS; i++) {
    buf[r][c + i] = color;
  }
}

function placeAttr(buf: (string | null)[][], row: number, col: number, len: number, attr: string) {
  const r = row - 1;
  const c = col - 1;
  if (r < 0 || r >= ROWS) return;
  for (let i = 0; i < len && c + i < COLS; i++) {
    buf[r][c + i] = attr;
  }
}


/**
 * Vue composable port of the React `useTerminalSocket` hook. Same WebSocket
 * lifecycle, same reconnection logic with exponential backoff, same buffer
 * mutation semantics. Implementation uses Vue's `reactive` for the screen
 * state and lifecycle hooks (onMounted/onUnmounted) instead of React's
 * `useEffect`, but the WebSocket message handling is structurally identical.
 */
export function useTerminalSocket(programIdGetter: () => string): {
  state: Reactive<ScreenState>;
  sendMessage: (msg: object) => void;
  updateBufferAt: (line: number, col: number, width: number, value: string) => void;
} {
  let ws: WebSocket | null = null;
  const messageQueue: ServerMessage[] = [];
  let processing = false;

  const state = reactive<ScreenState>({
    charBuffer: createEmptyBuffer(),
    colorBuffer: createNullBuffer(),
    attrBuffer: createNullBuffer(),
    waitingFor: 'none',
    activeField: null,
    screenFields: [],
    activeWindow: null,
    windowStack: [],
    currentScreenName: '',
    statusMessage: '',
    subRegionMessage: '',
    messageCode: '',
    messageNonce: 0,
    guideDisplay: null,
    fieldValues: {},
    connected: false,
    finished: false,
    error: null,
    recordsByScreen: {},
    fieldsByName: {},
    isKaizoMode: false,
  });

  // Hitachi 3270 ROLL key convention. Original terminals had dedicated
  // ROLL-DN / ROLL-UP keys (separate from PF7/PF8) that sent "BW" / "FW"
  // AID codes. Modern keyboards don't have those keys, so the convention is
  // to bind F7 → backward page (BW) and F8 → forward page (FW). The mapping
  // lives in `./aidCodes` (single source of truth across composable + SFCs).
  // If a specific screen needs F7/F8 to map to PF7/PF8 instead, the override's
  // submit handler can send the AID code directly to bypass this hook.

  function sendMessage(msg: object) {
    if (ws?.readyState === WebSocket.OPEN) {
      const m = msg as { aidKey?: string; type?: string };
      const rawAid = m.aidKey;
      if (m.aidKey) {
        m.aidKey = translateAidKey(m.aidKey);
      }
      if (DEBUG_ENABLED && rawAid && rawAid !== m.aidKey) {
        dbg('AID-TRANSLATE', { raw: rawAid, sent: m.aidKey });
      }
      dbg('SEND', { type: m.type ?? '?', aidKey: m.aidKey ?? '-' });
      ws.send(JSON.stringify(msg));
    } else {
      dbg('SEND-SKIP', { reason: 'ws-not-open', readyState: ws?.readyState });
    }
  }

  // Process a single server message — mutates `state` directly (Vue reactive)
  function processMessage(msg: ServerMessage) {
    const charBuf = state.charBuffer;
    const colorBuf = state.colorBuffer;
    const attrBuf = state.attrBuffer;

    // stationName is not in the ServerMessage union — handle before switch
    if ((msg as { type: string }).type === 'stationName') {
      try { sessionStorage.setItem('scr.stationName', (msg as { value?: string }).value ?? ''); } catch { /* ignore */ }
      return;
    }

    switch (msg.type) {
      case 'clearScreen':
        state.charBuffer = createEmptyBuffer();
        state.colorBuffer = createNullBuffer();
        state.attrBuffer = createNullBuffer();
        state.activeWindow = null;
        return;

      case 'displayScreen': {
        dbg('DISP', {
          screen: msg.screenName ?? '?',
          clearScreen: msg.clearScreen,
          clearRegion: msg.clearRegion
            ? `${msg.clearRegion.fromLine}..${msg.clearRegion.toLine}`
            : '-',
          rows: msg.rows?.length ?? 0,
        });
        // clearScreen = full-screen transition: reset buffer AND close any active window overlay
        const isFullScreen = msg.clearScreen;
        if (isFullScreen) {
          charBuf.forEach((r) => r.fill(' '));
          colorBuf.forEach((r) => r.fill(null));
          attrBuf.forEach((r) => r.fill(null));
        }
        // clearRegion = COBOL `CLEAR DATA TO N` row-range blank. Recovers
        // DSP-RTN-style "clear list area before re-paint" semantics that legacy
        // applet free → without this, F8 page transitions leave stale list rows.
        // Region is 1-based inclusive [fromLine..toLine].
        if (msg.clearRegion) {
          const { fromLine, toLine } = msg.clearRegion;
          const from = Math.max(1, fromLine) - 1;
          const to = Math.min(ROWS, toLine) - 1;
          for (let r = from; r <= to; r++) {
            charBuf[r].fill(' ');
            colorBuf[r].fill(null);
            attrBuf[r].fill(null);
          }
          // COBOL clears the list area
          // before painting a new page (dspZ000 begins with `DISPLAY SC-CLEAR`).
          // The typed `state.recordsByScreen` cache must also be reset for ALL
          // list screens — otherwise page 1's GREEN/YELLOW records bleed into
          // page 2 (cache stays populated because backend only re-emits the
          // screens that have rows on the new page; e.g. page 2 emits only
          // YELLOW so the page-1 GREEN cache lingers). Clearing here is safe
          // because the very next frames repopulate whatever the new page
          // contains; if no records arrive, the empty array correctly reflects
          // "no rows of this kind on this page".
          for (const key of Object.keys(state.recordsByScreen)) {
            state.recordsByScreen[key] = [];
          }
        }
        // SC-CLEAR is the COBOL utility screen that clears the list display area during
        // back-navigation (handleEstsBack09). It is NOT a real screen transition — do NOT
        // update waitingFor/activeField/currentScreenName. Setting waitingFor='none' would
        // make isFieldActive() return false, causing the active input to vanish briefly
        // (visible flicker) until the next acceptField restores it.
        if (msg.screenName === 'SC-CLEAR') return;
        // Kaizo dept (1102): INIT-025 paints DS-AREA-KAIZO when SW-KAIZO = 1.
        if (msg.screenName === 'DS-AREA-KAIZO') {
          state.isKaizoMode = true;
        }
        // Only apply window offset for popup (overlay) screens, not full-screen clears
        const lo = !isFullScreen && msg.window?.lineOffset ? msg.window.lineOffset : 0;
        const co = !isFullScreen && msg.window?.colOffset ? msg.window.colOffset : 0;
        // Two-pass paint:
        // 1) write every field's raw text + colors/attrs into the buffer in
        // arrival order so later fields (including literal '-' / ':' between
        // adjacent PIC-rendered date/time digits) properly overlay earlier
        // ones at the same cells.
        // 2) AFTER all paints, populate state.fieldValues from the now-merged
        // charBuffer at each "from" field's (line,col,width) so consumers
        // see the LITERAL-OVERLAID value (e.g. "26-05-19" not "26 05 19"
        // for PIC 99B99B99 with literal "-" at col+2 and col+5). Without
        // the second pass, fieldValues retains pre-merge field.text and
        // modernized SFCs displaying header date/time read raw PIC spaces.
        for (const row of msg.rows) {
          for (const field of row.fields) {
            const line = row.line + lo;
            const col = field.col + co;
            if (field.text) {
              placeText(charBuf, line, col, field.text);
              // When inside a window, also write at screen-relative coords so the UI
              // components (which read charBuffer at screen coords) get the value.
              if (lo !== 0 || co !== 0) {
                placeText(charBuf, row.line, field.col, field.text);
              }
              const cw = cellWidth(field.text);
              if (field.color) placeColor(colorBuf, line, col, cw, field.color);
              if (field.reverse) placeAttr(attrBuf, line, col, cw, 'reverse');
              if (field.blink) placeAttr(attrBuf, line, col, cw, 'blink');
              if (field.lowlight) placeAttr(attrBuf, line, col, cw, 'lowlight');
            }
          }
        }
        // Pass 2 — re-read merged values from charBuffer into fieldValues for
        // every "from" field. Done as a separate pass so it
        // captures POST-overlay state. The key remains screen-relative
        // (`${row.line}:${field.col}`) matching manifest "from" line/col.
        for (const row of msg.rows) {
          for (const field of row.fields) {
            if (field.type === 'from' && field.fieldName && field.text) {
              const r = row.line - 1;
              const startCol = field.col - 1;
              const width = cellWidth(field.text); // full-width CJK chars occupy 2 cells each
              if (r >= 0 && r < ROWS) {
                const merged = charBuf[r]
                  .slice(startCol, Math.min(startCol + width, COLS))
                  .join('');
                state.fieldValues[`${row.line}:${field.col}`] = merged;
                // Option 3 (forms extension) — index by fieldName too. Form/popup SFCs read
                // state.fieldsByName['KB-TANTOMEI'] instead of slice(charBuffer[1], 9, 7) —
                // coord-invariant when backend shifts COBOL field positions.
                state.fieldsByName[field.fieldName] = merged;
              }
            }
          }
        }
        // DS-MESSAG status capture for modernized SFCs that don't render
        // charBuffer row 24. Bare TerminalScreen still gets the text via buffer write above.
        // Skip *-SIZE / *-NORMAL companions which just carry size flags, not text.
        if (
          msg.screenName &&
          msg.screenName.startsWith('DS-MESSAG') &&
          !msg.screenName.includes('-SIZE') &&
          !msg.screenName.includes('-NORMAL')
        ) {
          let messageText = '';
          for (const row of msg.rows ?? []) {
            for (const field of row.fields ?? []) {
              const t = (field as { text?: string }).text;
              if (t) messageText += t;
            }
          }
          messageText = messageText.trim();
          if (messageText) {
            const codeMatch = messageText.match(/^([GE][FI]\d{3})/);
            const newCode = codeMatch ? codeMatch[1] : '';
            // DS-MESSAG with non-error (G*) should NOT override existing error (E*) messages
            // from showMessage. Only error DS-MESSAG or when no error exists should update.
            if (newCode.startsWith('E') || !state.messageCode.startsWith('E')) {
              state.messageCode = newCode;
              state.statusMessage = messageText;
            }
          } else {
            // Empty DS-MESSAG clears the status (COBOL convention: blank to dismiss)
            // BUT preserve error messages (E*) from showMessage — they should persist
            // until explicitly replaced. Only clear general/info messages.
            if (!state.messageCode.startsWith('E')) {
              state.messageCode = '';
              state.statusMessage = '';
            }
          }
          // always bump the nonce so SFC watchers
          // observing [statusMessage, messageNonce] re-fire even when the
          // backend re-emits an identical status (e.g. two consecutive EI165
          // attempts). Vue 3's `state.x = x` short-circuit otherwise hides
          // the second event from any single-key watch on statusMessage,
          // which is why repeat-error toasts silently fail to re-popup.
          state.messageNonce = (state.messageNonce + 1) | 0;
        }
        // Contextual help sub-region (e.g. DS-IODEN-MSG). Captured into its OWN channel —
        // NOT statusMessage — because a blank DS-MESSAG (status line) frame usually follows the
        // help overlay and would clear statusMessage. Concatenate the screen's literal text; a
        // blank variant (e.g. DS-IODEN-MSG1) clears the hint. The standalone *-MSG SFC never
        // stays mounted (the form keeps currentScreenName), so the form renders this inline.
        if (
          msg.screenName &&
          msg.screenName.includes('-MSG') &&
          !msg.screenName.startsWith('DS-MESSAG') &&
          !msg.screenName.includes('-SIZE') &&
          !msg.screenName.includes('-NORMAL')
        ) {
          let hint = '';
          for (const row of msg.rows ?? []) {
            for (const field of row.fields ?? []) {
              const t = (field as { text?: string }).text;
              if (t) hint += t;
            }
          }
          state.subRegionMessage = hint.trim();
        }
        // FLICKER/FOCUS FIX (FORM-REMOUNT-001): distinguish a top-level frame (full-screen
        // form OR popup content — a window is open) from a TRANSIENT sub-screen paint
        // (clearScreen=false, no window — e.g. DS-NO/SD-IDOYMD/DS-HANBAIMEI/DS-MESSAG painted
        // around the field the operator is entering). A transient paint must NOT clobber
        // currentScreenName nor clear the active-field context: doing so flips the
        // <component :is> override to a no-override screen and back on EVERY field advance,
        // remounting the form SFC (= full-screen flash + input element recreation + focus
        // loss → operator "can't type"). Capture window presence BEFORE the full-screen
        // reset below nulls it.
        //
        // DS-MESSAG* (incl. -SIZE / -NORMAL companions) are always transient when
        // clearScreen=false — even inside an OPEN-WINDOW popup. Treating them as top-level
        // when activeWindow != null cleared activeField mid-ACCEPT (e.g. EI007 on
        // SC-TBUKACD-KOU unmounted the Koubai overlay before acceptField restored focus).
        const isTransientSubScreen =
          !isFullScreen &&
          !!msg.screenName &&
          (msg.screenName.startsWith('DS-MESSAG') ||
            // Sub-screen repaint during field acceptance inside an open window (e.g. DS-KIBAN-UN
            // status update between SC-KIBAN-UN acceptField calls). Must NOT clear activeField or
            // change currentScreenName — doing so unmounts the transparent overlay
            // and loses localCellIdx / cellValues state, making the cell appear cleared.
            (state.activeWindow != null && state.activeField != null));
        const isTopLevelFrame =
          !isTransientSubScreen && (isFullScreen || state.activeWindow != null);
        // Close window overlay when full screen is drawn
        if (isFullScreen) {
          state.activeWindow = null;
          state.windowStack = [];
          state.subRegionMessage = ''; // help hint is per-screen; reset on full-screen change
        }
        // Only reset waitingFor / activeField / currentScreenName for real screen transitions
        // (top-level frames). Sub-screen repaints (DS-NO, SC-AREA1-MEISAI, DS-HANBAIMEI …
        // during handleEstsBack09 or normal field-advance) must NOT flip waitingFor to 'none':
        // isFieldActive() checks waitingFor==='field', so a transient 'none' makes the active
        // input box vanish for one paint frame — the visible flicker operators complain about.
        if (isTopLevelFrame) {
          state.waitingFor = 'none';
          state.activeField = null;
          // Defensive: server sometimes emits displayScreen with screenName=null for
          // unnamed/anonymous sub-screens. Mutating currentScreenName to null would
          // briefly drop the active override (key=null misses the lookup map),
          // remounting the override and re-triggering its focus-on-mount watcher.
          // Preserve last-known name when null arrives.
          state.currentScreenName = msg.screenName ?? state.currentScreenName;
        }
        return;
      }

      case 'displayField':
        placeText(charBuf, msg.line, msg.col, msg.value);
        // Also record per-(line,col) value so templates can surface
        // backend-resolved data (e.g. DS-TANTOMEI Tanaka name) without re-reading
        // charBuffer at risk of popup-overlay race conditions.
        state.fieldValues[`${msg.line}:${msg.col}`] = msg.value;
        // Option 3 (forms extension) — also index by fieldName for coord-invariant lookup.
        if (msg.fieldName) state.fieldsByName[msg.fieldName] = msg.value;
        return;

      case 'acceptField':
        dbg('ACCEPT-FIELD', {
          name: msg.fieldName,
          line: msg.line,
          col: msg.col,
          width: msg.width,
          pic: msg.picture,
        });
        state.waitingFor = 'field';
        state.activeField = {
          fieldName: msg.fieldName,
          line: msg.line,
          col: msg.col,
          width: msg.width,
          picture: msg.picture,
          occursIdx: msg.occursIdx,
        };
        state.screenFields = [];
        return;

      case 'acceptScreen':
        dbg('ACCEPT-SCREEN', {
          fieldCount: msg.fields?.length ?? 0,
          fields: msg.fields?.map((f) => f.fieldName).join(','),
        });
        state.waitingFor = 'screen';
        state.activeField = null;
        state.screenFields = msg.fields;
        return;

      case 'readEndStatus':
        dbg('READ-ENDSTATUS', {});
        state.waitingFor = 'endStatus';
        return;

      case 'showMessage': {
        const messageText = msg.text?.trim() ?? '';
        if (messageText) {
          const codeMatch = messageText.match(/^([GE][FI]\d{3})/);
          state.messageCode = codeMatch ? codeMatch[1] : '';
          state.statusMessage = messageText;
        } else {
          state.messageCode = '';
          state.statusMessage = '';
        }
        // bump nonce so SFC watchers on
        // [statusMessage, messageNonce] re-fire even when text is identical
        // (e.g. EI165 fires twice for consecutive drilldowns into same row).
        state.messageNonce = (state.messageNonce + 1) | 0;
        return;
      }

      case 'guideDisplay':
        // COBOL STOP 'literal' / DISPLAY ... UPON GDD — blocking Guide Display Window.
        // BE is blocked on waitForInput(); TerminalScreen shows a modal and sends
        // guideDisplayAck on OK/Esc so the program resumes at the next statement.
        state.guideDisplay = msg.text ?? '';
        return;

      case 'openWindow': {
        // SNAPSHOT cells the window covers BEFORE clearing, so closeWindow can
        // restore them. Without this, after a popup closes its text stays in the
        // buffer and gets read as "list data" by table renderers that scan rows
        // directly.
        const win: ActiveWindow = {
          name: msg.name,
          height: msg.height,
          width: msg.width,
          line: msg.line,
          col: msg.col,
          border: msg.border,
          savedChar: [],
          savedColor: [],
          savedAttr: [],
        };
        const r0 = win.line - 1,
          c0 = win.col - 1;
        const rEnd = Math.min(r0 + win.height, ROWS);
        const cEnd = Math.min(c0 + win.width, COLS);
        for (let r = r0; r < rEnd; r++) {
          win.savedChar!.push(charBuf[r].slice(c0, cEnd));
          win.savedColor!.push(colorBuf[r].slice(c0, cEnd));
          win.savedAttr!.push(attrBuf[r].slice(c0, cEnd));
          for (let c = c0; c < cEnd; c++) {
            charBuf[r][c] = ' ';
            colorBuf[r][c] = null;
            attrBuf[r][c] = null;
          }
        }
        // Push to stack — COBOL can nest OPEN-WINDOW (popup-on-popup).
        state.windowStack.push(win);
        state.activeWindow = win;
        return;
      }

      case 'closeWindow': {
        // Pop the topmost window + restore the cells IT covered.
        // Multi-level: nested popups close in LIFO order, each restoring its own region.
        const win = state.windowStack.pop();
        if (win?.savedChar) {
          const r0 = win.line - 1,
            c0 = win.col - 1;
          for (let i = 0; i < win.savedChar.length; i++) {
            const r = r0 + i;
            if (r >= ROWS) break;
            for (let j = 0; j < win.savedChar[i].length; j++) {
              const c = c0 + j;
              if (c >= COLS) break;
              charBuf[r][c] = win.savedChar[i][j];
              colorBuf[r][c] = win.savedColor![i][j];
              attrBuf[r][c] = win.savedAttr![i][j];
            }
          }
        }
        // activeWindow exposes the new top (or null if stack empty).
        state.activeWindow = state.windowStack[state.windowStack.length - 1] ?? null;
        return;
      }

      case 'programFinished':
        state.finished = true;
        state.waitingFor = 'none';
        state.statusMessage = `Program finished (${msg.reason ?? 'normal'})`;
        state.messageNonce = (state.messageNonce + 1) | 0;
        return;

      case 'error':
        state.error = msg.message;
        state.waitingFor = 'none';
        return;

      case 'displayRecordList': {
        // Option 3 — backend RecordAccumulator flushed a typed record list for this screen.
        // Key by UPPERCASE screen name so SFC lookup matches manifest convention.
        const sn = msg.screenName?.toUpperCase();
        if (!sn) return;
        dbg('RECORD-LIST', {
          screen: sn,
          count: msg.records?.length ?? 0,
          recordName: msg.recordName,
        });
        // tag each record with its source screen name so
        // SFC list templates can apply visual differentiation (e.g. a screen
        // renders SC-DISP-GREEN as light-green pending rows and SC-DISP-YELLOW
        // as light-yellow approved rows). Without this tag, the merged record
        // list from extractRecordsFromState loses the GREEN/YELLOW distinction
        // and all rows render identically — losing the COBOL color attribute
        // intent that the XML AST already captured (color="GREEN"/"YELLOW")
        // and that the manifest correctly carries forward. The __source key
        // is non-conflicting with any COBOL field name (no field starts with
        // double underscore in any schema).
        const records = (msg.records ?? []).map((r) => ({
          ...r,
          __source: sn,
        }));
        state.recordsByScreen[sn] = records;
        return;
      }

      case 'idleWarning':
        state.statusMessage = `Session idle — timeout in ${msg.minutesLeft} minute(s)`;
        state.messageNonce = (state.messageNonce + 1) | 0;
        return;
    }
  }

  // Sequential message processing (H-6)
  function processQueue() {
    if (processing) return;
    processing = true;
    while (messageQueue.length > 0) {
      const msg = messageQueue.shift()!;
      processMessage(msg);
    }
    processing = false;
  }

  let retryCount = 0;
  let retryTimer: ReturnType<typeof setTimeout> | null = null;
  let connectTimer: ReturnType<typeof setTimeout> | null = null;
  let intentionalClose = false;

  function connect() {
    const programId = programIdGetter();
    const wsUrl = buildWebSocketUrl(programId);
    ws = new WebSocket(wsUrl);

    // Spring Security returns 302 → /login when session is missing; browsers may hang
    // in CONNECTING without firing onerror. Surface a actionable message after 10s.
    connectTimer = setTimeout(() => {
      if (ws?.readyState === WebSocket.CONNECTING) {
        ws.close();
        state.error = 'WebSocket timed out — log in first at /login (admin/admin), then retry.';
      }
    }, 100000);

    ws.onopen = () => {
      if (connectTimer) clearTimeout(connectTimer);
      retryCount = 0;
      state.connected = true;
      state.error = null;
    };

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data) as ServerMessage;
        messageQueue.push(msg);
        processQueue();
      } catch (e) {
        console.error('Failed to parse message:', e);
      }
    };

    ws.onclose = (event) => {
      if (connectTimer) clearTimeout(connectTimer);
      state.connected = false;
      // Option 3 — drop accumulated typed records on disconnect so reconnects start fresh.
      state.recordsByScreen = {};
      state.fieldsByName = {};
      state.isKaizoMode = false;
      // Reconnect on abnormal close (not program finish or intentional disconnect)
      if (!intentionalClose && !event.wasClean && retryCount < 5) {
        const delay = Math.min(1000 * Math.pow(2, retryCount), 30000);
        retryCount++;
        state.statusMessage = `Reconnecting in ${delay / 1000}s...`;
        state.messageNonce = (state.messageNonce + 1) | 0;
        retryTimer = setTimeout(connect, delay);
      } else if (!intentionalClose && !state.finished) {
        // Permanent failure: auth rejection (clean close) or retry limit reached.
        // Signal parent via state.error so TerminalScreen can emit auth-error.
        state.error = 'auth_failed';
      }
    };

    ws.onerror = () => {
      state.error =
        'WebSocket connection error — open /login (admin/admin) on this host, then retry.';
    };
  }

  onMounted(() => {
    connect();
  });

  onUnmounted(() => {
    intentionalClose = true;
    if (retryTimer) clearTimeout(retryTimer);
    if (connectTimer) clearTimeout(connectTimer);
    ws?.close();
  });

  // Reconnect when programId changes (parity with React useEffect dep)
  watch(programIdGetter, () => {
    intentionalClose = true;
    if (retryTimer) clearTimeout(retryTimer);
    ws?.close();
    intentionalClose = false;
    connect();
  });

  /**
   * Mirror a typed field value back into the char buffer at the given
   * screen position, padded to the declared field width. Called after the
   * user submits an acceptField so the display keeps the value on screen
   * once the input overlay is removed.
   */
  function updateBufferAt(line: number, col: number, width: number, value: string) {
    const padded = (value || '').padEnd(width, ' ').slice(0, width);
    const r = line - 1;
    let c = col - 1;
    if (r >= 0 && r < ROWS) {
      for (let i = 0; i < padded.length && c < COLS; i++) {
        state.charBuffer[r][c] = padded[i];
        c++;
      }
    }
  }

  return { state, sendMessage, updateBufferAt };
}
