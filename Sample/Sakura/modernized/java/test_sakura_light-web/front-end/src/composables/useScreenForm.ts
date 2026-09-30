/**
 * useScreenForm — shared composable for form/popup-style COBOL screens.
 *
 * Eliminates ~150 LOC of boilerplate per SFC by centralizing:
 * - activeField + activeName reactive lookup
 * - submit() — sendMessage dispatch covering field/screen/endStatus modes,
 * updateBufferAt for ACCEPT confirmation, deferred endStatus handoff
 * - onKey() — keystroke dispatcher: Enter submits, PF keys submit with
 * aidKey, optional picture-aware keystroke lock (PIC 9 → numeric-only)
 * - windowKey listener — fires PF keys when no input is focused
 * - Focus management — auto-focus active input on field transition
 * - valueOf(scName) / descOf(scName) — coord-invariant field lookup via
 * state.fieldsByName (typed-by-name path)
 * - isFieldActive(scName) — boolean for v-if/v-else input vs read-only
 *
 * Per-SFC: only UI template + 1 config object.
 */

import {
  ref,
  computed,
  watch,
  nextTick,
  useTemplateRef,
  onMounted,
  onBeforeUnmount,
  type ComputedRef,
  type Ref,
} from 'vue';
import { resolveAidFromEvent } from './aidCodes';

/** Subset of TerminalScreen.vue's ScreenOverrideProps needed by the composable. */
export interface ScreenFormProps {
  state: {
    activeField: {
      fieldName: string;
      line: number;
      col: number;
      width: number;
      picture?: string;
      occursIdx?: number;
    } | null;
    screenFields: Array<{
      fieldName: string;
      line?: number;
      col?: number;
      width?: number;
      picture?: string;
    }>;
    waitingFor: 'none' | 'field' | 'screen' | 'endStatus';
    fieldsByName: Record<string, string>;
    fieldValues: Record<string, string>; // legacy coord-based; for backwards-compat valueOf fallback
    recordsByScreen: Record<string, Array<Record<string, unknown>>>; // Option 3 typed records
    charBuffer: string[][];
    statusMessage: string;
    finished: boolean;
    guideDisplay?: string | null;
  };
  sendMessage: (msg: object) => void;
  updateBufferAt: (line: number, col: number, width: number, value: string) => void;
}

/** Subset of TYPED_VALUES injection (from TerminalScreen.vue). Optional — when
 * provided, composable persists submitted values into the typed-values map so
 * they survive overlay remounts and feed snapshotRead caches. */
export interface TypedSink {
  values: Record<string, string>;
  setTyped: (fieldName: string, value: string) => void;
  snapshotRead: (key: string, live: string) => string;
}

export interface ScreenFormConfig {
  /** SC-input name → COBOL data fieldName for value lookup. */
  fieldDataMap?: Record<string, string>;
  /** SC-input name → COBOL description fieldName (e.g. KB-X-M). */
  fieldDescMap?: Record<string, string>;
  /** Per-screen PF override: F-key → aidKey. Defaults to F1→P1 … F12→PC. */
  pfOverrides?: Record<string, string>;
  /**
   * Picture-aware keystroke lock — when active field's `picture` matches,
   * block non-allowed keystrokes at keydown. Default: PIC 9(N) → digits only.
   */
  picKeyLock?: boolean;
  /**
   * Optional: callback that decides whether a field's input is numeric. Use
   * when the screen has a manually-curated Set<string> of numeric field names
   * (some screens override the picture-based heuristic). Falls back to
   * checking activeField.picture for `9`.
   */
  isNumericField?: (fieldName: string) => boolean;
  /**
   * Optional: callback that decides whether a numeric field accepts a leading
   * '-' (COBOL PIC S9(N) — signed). Default: no field allows negative input.
   */
  allowNegativeField?: (fieldName: string) => boolean;
  /**
   * Submit values payload builder for SCREEN mode (acceptScreen). When set,
   * `submit(value, aidKey)` packages values via this callback instead of just
   * `{ activeFieldName: value }`. Useful for multi-field popups (login, etc.).
   */
  buildScreenValues?: () => Record<string, string>;
  /** When true (default), register window keydown listener for unfocused PF dispatch. */
  windowPfDispatch?: boolean;
  /**
   * Optional getter for the active field's in-progress edit value (e.g. the
   * SFC's local `editValue` ref). windowKey() dispatches PF/Enter/AID keys
   * when focus has left the active control entirely (no INPUT/TEXTAREA
   * focused) — without this, submit() falls back to '' and clobbers whatever
   * the user picked/typed before clicking away (e.g. a Radio selection,
   * which has no single focused DOM input to read `.value` from).
   */
  getPendingValue?: () => string;
  /**
   * Optional typed-values sink (inject TYPED_VALUES_KEY from TerminalScreen.vue
   * in the SFC and pass the resulting object here). When provided, the
   * composable calls setTyped() on submit + input coercion to keep the typed
   * cache aligned with what the user has entered.
   */
  typed?: TypedSink;
  stripZeroDefaults?: boolean;
  isUseCached?: boolean;
}

export interface ScreenFormApi {
  /** Active field reactive — null when not waiting for a field. */
  activeField: ComputedRef<ScreenFormProps['state']['activeField']>;
  /** Active field name reactive — '' when not waiting. */
  activeFieldName: ComputedRef<string>;
  /** True when state.waitingFor is 'field' (activeName match) or 'screen' (member of screenFields). */
  isFieldActive: (scName: string) => boolean;
  /** Picture string for active field, or ''. */
  activePicture: ComputedRef<string>;
  /** Width for active field, or undefined. */
  activeWidth: ComputedRef<number | undefined>;
  /**
   * COBOL data value for an SC- input name. Lookup precedence:
   * 1. config.typed.values[scName]      — local typed cache (user's edits persist)
   * 2. state.fieldsByName[dataFieldName] — backend-resolved typed value
   * 3. state.fieldsByName[scName]        — direct SC-name lookup fallback
   * Coord-invariant — never reads charBuffer by line/col.
   */
  valueOf: (scName: string) => string;
  /** Description label (KB-X-M-style) for an SC- input name. */
  descOf: (scName: string) => string;
  /**
   * Submit single field value + optional aidKey. Handles all 3 ACCEPT modes:
   * - 'field'     → fieldInput frame + optimistic updateBufferAt + typed.setTyped
   * - 'screen'    → screenInput frame (values from config.buildScreenValues, or
   * {[activeFieldName]: value} fallback) + arm pendingEndStatus
   * - 'endStatus' → endStatusInput frame
   */
  submit: (value: string, aidKey?: string) => void;
  /** Keystroke handler — bind to input's @keydown. Picture-aware lock + PF dispatch. */
  onKey: (e: KeyboardEvent) => void;
  /**
   * Input-event handler — bind to input's @input. Performs full-width-digit
   * normalization (０-９ → 0-9) + non-digit strip when numeric. Persists into
   * typed cache if config.typed is provided.
   */
  onInput: (e: Event, fieldName?: string) => void;
  /** Template ref for the currently active input — auto-focused on field change. */
  inputRef: Ref<HTMLInputElement | null>;
  /**
   * Function-ref binding for inputs INSIDE `v-for`. Vue assigns Array<Element>
   * to string-refs in v-for; this captures the active element only. Bind as
   * `:ref="bindActiveInput"` on each input; only the currently-`data-active`
   * one will register.
   */
  bindActiveInput: (el: Element | object | null) => void;
  /** True when state.waitingFor is 'field' OR 'screen' OR 'endStatus' (any input mode). */
  isAccepting: ComputedRef<boolean>;
}

// Key→AID resolution lives in aidCodes.ts (KEY_AID_MAP + MODIFIER_KEY_AID_MAP +
// resolveAidFromEvent) per NEC COBOL85 Pro Programming Guide Table 7-1. Each
// keystroke flows through resolveAidFromEvent in onKey/windowKey below.

const ALLOWED_NAV_KEYS = new Set([
  'Backspace',
  'Delete',
  'Tab',
  'ArrowLeft',
  'ArrowRight',
  'ArrowUp',
  'ArrowDown',
  'Home',
  'End',
  'Enter',
]);

export function useScreenForm(props: ScreenFormProps, config: ScreenFormConfig = { isUseCached: true }): ScreenFormApi {
  const inputRef = useTemplateRef<HTMLInputElement>('inputRef');
  const activeInputEl = ref<HTMLInputElement | HTMLTextAreaElement | null>(null);

  const activeField = computed(() => props.state.activeField);
  const activeFieldName = computed(() => activeField.value?.fieldName ?? '');
  const activePicture = computed(() => activeField.value?.picture ?? '');
  const activeWidth = computed(() => {
    const w = activeField.value?.width;
    if (w === undefined) return undefined;
  
    return config.allowNegativeField?.(activeFieldName.value) ? w + 1 : w;
  });
  const isAccepting = computed(
    () =>
      props.state.waitingFor === 'field' ||
      props.state.waitingFor === 'screen' ||
      props.state.waitingFor === 'endStatus',
  );

  const screenFieldSet = computed(() => {
    const set = new Set<string>();
    for (const f of props.state.screenFields ?? []) {
      const name = typeof f === 'string' ? f : (f as { fieldName: string }).fieldName;
      if (name) set.add(name);
    }
    return set;
  });

  function isFieldActive(scName: string): boolean {
    if (props.state.waitingFor === 'field') return activeFieldName.value === scName;
    if (props.state.waitingFor === 'screen') return screenFieldSet.value.has(scName);
    return false;
  }

  // ── Option 3 typed-by-name lookups (3-tier precedence) ──────────────────
  //
  // stripPaintArtifacts: when a `from`-field points at an empty PIC N(n) working
  // storage value, COBOL initializes the cell with IDEOGRAPHIC SPACE (U+3000),
  // and the FE `placeText` paints a ZWSP (U+200B) placeholder in the second
  // cell of every full-width glyph. The merged charBuffer slice then contains
  // an alternating ZWSP/U+3000 pattern. JS `.trim()` removes U+3000 but NOT
  // U+200B — so a "blank" field looks truthy on first paint, snapshotRead
  // caches it, and later real-value updates are ignored. Strip ZWSP first,
  // then trim (which handles U+3000) — matches `columnExtraction.isBlankCell`.
  function stripPaintArtifacts(s: string): string {
    return s.replace(/​/g, '').trim();
  }

  function valueOf(scName: string): string {
    // 1) Local typed cache (user's in-progress edits, persists across remounts).
    // Typed values reflect deliberate user input — including an explicit blank
    // value after the user clears a field. Returning an empty string here keeps
    // the cleared value authoritative instead of falling back to stale backend data.
    const cached = config.typed?.values?.[scName];
    if (cached !== undefined) return cached;
    // 2) Backend-resolved typed value via mapped fieldName.
    let raw = '';
    const dataField = config.fieldDataMap?.[scName];
    if (dataField) {
      raw = stripPaintArtifacts(props.state.fieldsByName?.[dataField] ?? '');
    }
    // 3) Direct SC-name lookup fallback (some screens store value by SC- key).
    if (!raw) raw = stripPaintArtifacts(props.state.fieldsByName?.[scName] ?? '');
    if (!raw) return '';

    // Optional zero-default suppression — see stripZeroDefaults in config.
    if (config.stripZeroDefaults !== false && /^0+$/.test(raw)) return '';

    return raw;
  }

  function descOf(scName: string): string {
    const descField = config.fieldDescMap?.[scName];
    if (!descField) return '';
    // Tie description visibility to the underlying value:
    // when valueOf() returns empty because the code is the zero default
    // (stripped by /^0+$/ rule), the lookup description alone ("商品",
    // "対象外", "HQ Dept", etc.) is meaningless and visually misleading.
    // Hide it so the field reads as "not yet entered".
    //
    // only hide when the code IS present
    // in fieldsByName but is the zero-default. When the code is ABSENT
    // entirely (SSO auto-login skipped the displayField for SC-TANTOCD,
    // but the descriptor sub-screen DS-TANTOMEI did push WK-TANTOMEI), showing
    // the descriptor is the correct legacy-faithful behavior — operator sees
    // their name even though their code slot stays blank.
    const dataField = config.fieldDataMap?.[scName];
    const codeEntryPresent =
      (dataField !== undefined && props.state.fieldsByName?.[dataField] !== undefined) ||
      props.state.fieldsByName?.[scName] !== undefined;
    const raw = stripPaintArtifacts(props.state.fieldsByName?.[descField] ?? '');
    // Suppress only when BOTH code is zero-default AND desc is also empty.
    // If BE painted a desc despite code=0 (e.g. "客先", "仕入先" for mokuteki=2/5),
    // trust the BE and show it — don't treat it as "stale zero".
    if (codeEntryPresent && !valueOf(scName) && !raw) return '';
    return raw;
  }

  // ── Numeric detection (picture-based heuristic, with override callback) ──
  function isNumericForField(fieldName: string, picture: string): boolean {
    if (config.isNumericField) return config.isNumericField(fieldName);
    if (!picture) return false;
    return /^[Z9*+,.-]/.test(picture) && /9/.test(picture);
  }

  // ── Submit semantics ────────────────────────────────────────────────────
  let pendingEndStatus: string | null = null;

  function submit(value: string, aidKey: string = '00'): void {
    const mode = props.state.waitingFor;
    if (mode === 'screen') {
      const values = config.buildScreenValues
        ? config.buildScreenValues()
        : { [activeFieldName.value]: value };
      props.sendMessage({ type: 'screenInput', values, aidKey });
      pendingEndStatus = aidKey;
      // Persist all screen-mode values into typed cache.
      if (config.typed) {
        for (const [k, v] of Object.entries(values)) config.typed.setTyped(k, v);
      }
      return;
    }
    if (mode === 'endStatus') {
      props.sendMessage({ type: 'endStatusInput', aidKey });
      return;
    }
    if (mode === 'field' && activeField.value && aidKey === '00') {
      // Confirm field — optimistic local buffer update + typed cache persistence.
      props.updateBufferAt(
        activeField.value.line,
        activeField.value.col,
        activeField.value.width,
        value,
      );
      config.typed?.setTyped(activeFieldName.value, value);
    }
    props.sendMessage({ type: 'fieldInput', value, aidKey });
  }

  // After a 'screen' submit, server transitions to 'endStatus' to read the aidKey.
  watch(
    () => props.state.waitingFor,
    (now) => {
      if (now === 'endStatus' && pendingEndStatus !== null) {
        const aid = pendingEndStatus;
        pendingEndStatus = null;
        props.sendMessage({ type: 'endStatusInput', aidKey: aid });
      }
    },
  );

  // ── Keystroke dispatcher ────────────────────────────────────────────────
  //
  // Key→AID resolution per NEC COBOL85 Pro 「プログラミング手引書」Table 7-1.
  // See aidCodes.ts for full mapping. Lookup order:
  //   1. Enter — special: send '00' (BE smart resolver picks per field intent)
  //   2. SFC explicit pfOverrides (kept for backward compat)
  //   3. Modifier combinations (Shift+F8, Ctrl+F10, etc.) via resolveAidFromEvent
  //   4. Single keys (F1-F10, Tab, Backspace*, Arrows, PageUp/Down) via resolveAidFromEvent
  //   5. Picture-aware numeric lock (fall-through)
  //
  // * Backspace only intercepted when target is NOT an INPUT/TEXTAREA, to
  //   preserve native character-deletion inside text fields.
  function onKey(e: KeyboardEvent): void {
    e.stopPropagation();

    if (props.state.guideDisplay) return; // Block PF keys while guideDisplay is open (BE parked on waitForInput()).

    // (1) Enter or Tab — defer aidKey to BE smart resolver via '00' (NOP). On data
    // fields BE resolves to advanceEsts='01' (HTB next field); on confirm
    // fields BE resolves to '04' (ADV submit). Preserves modern web UX.
    const isEnter = e.key === 'Enter' && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey;
    if (isEnter && activeFieldName.value === 'SC-OK') {
      e.preventDefault();
      return;
    }
  
    const isTab = e.key === 'Tab' && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey;
    if (isEnter || isTab) {
      e.preventDefault();
      const target = e.target as HTMLInputElement | null;
      // Date inputs expose the COBOL PIC 9(6) YYMMDD wire via data-wire-value; their
      // visible .value is the display text (YYYY/MM/DD), which must NEVER be submitted to
      // the backend. Prefer the wire when present (also '' for an invalid/blank date, so
      // an impossible date like 2026/02/30 can't be registered). Non-date fields have no
      // such attribute and fall back to .value unchanged.
      const raw = target?.dataset?.wireValue ?? target?.value ?? '';
      submit(raw, '00');
      return;
    }

    // (2) SFC explicit override (legacy backward-compat — new code should
    // rely on Table 7-1 default in aidCodes.ts instead).
    const sfcOverride = config.pfOverrides?.[e.key];
    if (sfcOverride && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey) {
      e.preventDefault();
      const target = e.target as HTMLInputElement | null;
      submit(target?.value ?? '', sfcOverride);
      return;
    }

    // (3) + (4) Resolve via Table 7-1 (modifier combos first, then single key).
    const aidKey = resolveAidFromEvent(e);
    if (aidKey !== undefined) {
      // Guard: don't intercept Backspace inside text inputs — Backspace has
      // native semantic (delete char) that must be preserved. Backspace only
      // emits BTB ('09') when focus is on a button or outside any input.
      // (ArrowUp/ArrowDown/Tab intentionally not in KEY_AID_MAP to avoid
      //  breaking dropdown nav + browser Tab focus — see aidCodes.ts.)
      const tag = (e.target as HTMLElement | null)?.tagName;
      const isBackspaceInInput =
        e.key === 'Backspace' && (tag === 'INPUT' || tag === 'TEXTAREA');
      if (isBackspaceInInput) {
        return;
      }
      e.preventDefault();
      const target = e.target as HTMLInputElement | null;
      submit(target?.value ?? '', aidKey);
      return;
    }

    // (5) Picture-aware keystroke lock for numeric fields.
    if (
      config.picKeyLock !== false &&
      isNumericForField(activeFieldName.value, activePicture.value)
    ) {
      if (e.ctrlKey || e.metaKey || e.altKey || e.isComposing) return;
      if (ALLOWED_NAV_KEYS.has(e.key)) return;
      if (e.key.length !== 1) return;
      // Allow full-width digit (FF10..FF19) — Japanese IME may emit these;
      // onInput will normalize to ASCII.
      const cp = e.key.codePointAt(0)!;
      if (cp >= 0xff10 && cp <= 0xff19) return;
      // Signed fields (PIC S9(N)) allow a leading '-' too; onInput() strips
      // any '-' that ends up somewhere other than position 0.
      if (config.allowNegativeField?.(activeFieldName.value) && e.key === '-') return;
      if (!/^[0-9]$/.test(e.key)) e.preventDefault();
    }
  }

  // ── Input-event handler — paste/IME normalization + typed cache persist ──
  function onInput(e: Event, fieldName?: string): void {
    const inp = e.target as HTMLInputElement;
    const name = fieldName ?? activeFieldName.value;
    const picture = name === activeFieldName.value ? activePicture.value : '';
    const negativeAllowed = config.allowNegativeField?.(name) ?? false;
    if (isNumericForField(name, picture)) {
      // Convert full-width digits (０-９ U+FF10..FF19) to ASCII.
      let v = inp.value.replace(/[０-９]/g, (s) => String.fromCharCode(s.charCodeAt(0) - 0xfee0));
      if (negativeAllowed) {
        // At most ONE leading '-': record whether the value opens with a sign,
        // strip every non-digit (including any other '-' typed elsewhere or a
        // repeated leading '-'), then re-prepend a single sign if it had one.
        const neg = v.startsWith('-');
        v = (neg ? '-' : '') + v.replace(/[^0-9]/g, '');
      } else {
        // Strip non-digits (catches paste of mixed content).
        v = v.replace(/[^0-9]/g, '');
      }
      if (v !== inp.value) inp.value = v;
    }
    const width = activeWidth.value;
    // A signed field's '-' occupies a display slot beyond its PIC S9(N) digit
    // count (mirrors NumericInput.vue's picture-width calc, which counts 'S'
    // as +1) — otherwise the leading sign would silently eat into the digits.
    const effectiveWidth = width && negativeAllowed && inp.value.startsWith('-') ? width + 1 : width;
    const value = effectiveWidth ? inp.value.slice(0, effectiveWidth) : inp.value;
    if (value !== inp.value) inp.value = value;
    // Persist into typed cache for snapshotRead + reload survival.
    config.typed?.setTyped(name, value);
  }

  // ── v-for function-ref binding (string-ref inside v-for is broken in Vue 3) ──
  function bindActiveInput(el: Element | object | null): void {
    if (!el) {
      if (activeInputEl.value && !document.body.contains(activeInputEl.value)) {
        activeInputEl.value = null;
      }
      return;
    }
    // instanceof on the wide `el` type keeps every TS 5.x happy (casting to
    // HTMLInputElement first makes newer TS narrow the second check to `never`).
    if (!(el instanceof HTMLInputElement) && !(el instanceof HTMLTextAreaElement)) return;
    const inp = el;
    if (inp.disabled) return;

    const fieldName = inp.dataset.fieldName;
    if (fieldName) {
      if (props.state.waitingFor === 'field' && fieldName !== activeFieldName.value) return;
      if (props.state.waitingFor === 'screen' && !screenFieldSet.value.has(fieldName)) return;
    }

    activeInputEl.value = inp;
  }

  // ── Window-level PF dispatch ────────────────────────────────────────────
  //
  // Fires when no INPUT/TEXTAREA is focused (typical: PF key press on a list
  // or button-only screen). Uses same Table 7-1 resolver as onKey().
  function windowKey(e: KeyboardEvent): void {
    if (props.state.guideDisplay) return; // Block PF keys while guideDisplay is open (BE parked on waitForInput()).
    if (e.key === 'Enter' && activeFieldName.value === 'SC-OK') {
      e.preventDefault();
      return;
    }

    const tag = (e.target as HTMLElement)?.tagName;
    // Escape is universal cancel — find and click the dialog's own cancel
    // button (each dialog wires its cancel onClick to the correct ESTS code:
    // 'P9' for popup, '09' for ESTS-BTB confirmation, etc.). Bypasses
    // INPUT guard so dialogs with focused inputs can still be dismissed.
    if (e.key === 'Escape') {
      const cancelBtn = findCancelButton();
      if (cancelBtn) {
        e.preventDefault();
        cancelBtn.click();
        return;
      }
      // No cancel button visible (likely on a non-dialog screen) — fall through
      // to standard PF dispatch path; resolveAidFromEvent returns undefined, no-op.
    }
    if (tag === 'INPUT' || tag === 'TEXTAREA') return;

    // Resolve via Table 7-1 (modifier combos + single keys + SFC overrides).
    let aidKey: string | undefined;
    const sfcOverride = config.pfOverrides?.[e.key];
    if (sfcOverride && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey) {
      aidKey = sfcOverride;
    } else {
      aidKey = resolveAidFromEvent(e);
    }
    if (!aidKey) return;

    const mode = props.state.waitingFor;
    if (mode !== 'field' && mode !== 'screen' && mode !== 'endStatus') return;
    e.preventDefault();
    submit(config.getPendingValue?.() ?? '', aidKey);
  }

  /** Locate the dialog's cancel button by its キャンセル label (works for all
   * dialog SFCs that follow the convention <kbd>F9</kbd>キャンセル. */
  function findCancelButton(): HTMLButtonElement | null {
    const buttons = document.querySelectorAll<HTMLButtonElement>('.scr-dialog-actions button');
    for (const b of buttons) {
      const text = (b.textContent || '').trim();
      if (/^キャンセル/.test(text) || /キャンセル/.test(text)) return b;
    }
    return null;
  }

  if (config.windowPfDispatch !== false) {
    onMounted(() => window.addEventListener('keydown', windowKey));
    onBeforeUnmount(() => window.removeEventListener('keydown', windowKey));
  }

  // ── Focus management ────────────────────────────────────────────────────
  // 3-tier focus target priority:
  // 1. activeInputEl (bound via bindActiveInput in v-for — most reliable)
  // 2. inputRef (static template ref — for non-v-for inputs)
  // 3. document.querySelector('input[data-active="true"]') (fallback)
  //
  // Vue 3 collects `ref="inputRef"` used INSIDE v-for into
  // an Array<Element>, not a single ref. That made the `?? inputRef.value`
  // short-circuit at an array (truthy even when empty), bypassing the
  // querySelector fallback. Calling `array.focus()` silently no-ops because
  // arrays have no focus method. As a result form SFCs that use `ref="inputRef"`
  // inside v-for lost both auto-focus on entry AND
  // auto-advance after Enter. Resolve the array case explicitly + ALWAYS try
  // querySelector as the final guarantee.
  function resolveFocusTarget(): HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement | null {
    // Validate activeInputEl is still the current active control — avoids focusing
    // a stale input when the single-component (:readonly/:disabled) pattern is used
    // and bindActiveInput was only called at mount (before the field became active).
    if (activeInputEl.value?.dataset.active === 'true') return activeInputEl.value;
    const r = inputRef.value as unknown;
    if (Array.isArray(r)) {
      // v-for collected refs — prefer the one currently marked data-active
      const active = (r as HTMLInputElement[]).find((el) => el?.dataset?.active === 'true');
      if (active) return active;
    } else if (r) {
      return r as HTMLInputElement;
    }
    // Broader fallback: handles composite controls (radio groups, confirm anchors,
    // native <select> dropdowns) where data-active is on a wrapper element, not the
    // control itself.
    const activeEl = document.querySelector<HTMLElement>('[data-active="true"]');
    if (!activeEl) return null;
    if (
      activeEl instanceof HTMLInputElement ||
      activeEl instanceof HTMLTextAreaElement ||
      activeEl instanceof HTMLSelectElement
    ) {
      return activeEl;
    }
    // Radio groups (Radio.vue) render one <input type="radio"> per option —
    // `querySelector('input,textarea,select')` alone always returns the FIRST option
    // in DOM order regardless of which is actually checked (e.g. BTB back
    // into a group where option 3 of 8 is selected focused option 1 instead).
    // Prefer the checked one; only fall back to "first input/select" for controls
    // where nothing can be checked (plain text inputs, native <select> dropdowns —
    // e.g. a 発注種別 field wraps a <Select> in a data-active div since Select.vue's
    // multi-root template can't forward attrs to the underlying <select> itself).
    const nested =
      activeEl.querySelector<HTMLInputElement>('input:checked') ??
      activeEl.querySelector<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>(
        'input,textarea,select',
      );
    if (nested) return nested;
    return activeEl as unknown as HTMLInputElement;
  }

  function focusActiveInput(): void {
    const ae = document.activeElement as HTMLElement | null;
    // Only skip when the COBOL-active control already has focus (not any INPUT on page).
    if (ae?.getAttribute?.('data-active') === 'true') return;
    const target = resolveFocusTarget();
    target?.focus();
    // Textareas manage their own caret (e.g. multi-line COBOL fields); select-all breaks line focus.
    if (target instanceof HTMLInputElement) target.select?.();
  }

  watch(
    () => [activeFieldName.value, props.state.waitingFor, activeField.value?.occursIdx],
    async () => {
      if (props.state.waitingFor !== 'field' && props.state.waitingFor !== 'screen') return;
      // nextTick: v-if input mounted in parent template.
      // Short delay: child SFCs (e.g. DataTable) may mount one frame later.
      await nextTick();
      focusActiveInput();
      setTimeout(focusActiveInput, 30);
      // Extra attempt at 80ms: reclaims focus if TerminalScreen's 30ms endStatus
      // terminalRef.focus() fired between our first attempt and now.
      setTimeout(focusActiveInput, 80);
    },
    { immediate: true, flush: 'post' },
  );

  return {
    activeField,
    activeFieldName,
    isFieldActive,
    activePicture,
    activeWidth,
    isAccepting,
    valueOf,
    descOf,
    submit,
    onKey,
    onInput,
    inputRef,
    bindActiveInput,
  };
}
