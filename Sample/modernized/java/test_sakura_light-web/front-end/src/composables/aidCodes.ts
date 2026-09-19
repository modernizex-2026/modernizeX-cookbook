/**
 * NEC COBOL85 Pro / WEBCOBOL key → AID code registry.
 *
 * Single source of truth for keyboard ↔ AID translations used by
 * useScreenForm.onKey, useTerminalSocket, TerminalScreen, and per-program SFCs.
 *
 * ============================================================================
 * Source of truth: NEC COBOL85 Pro 「プログラミング手引書」 Table 7-1
 *   「END STATUS データ項目に設定される値」 (page 7-9 ~ 7-10)
 * ============================================================================
 *
 * Table 7-1 defines the DEFAULT NEC keyboard → ESTS mapping (built-in to
 * COBOL85 Pro / WEBCOBOL runtime). Customer Win systems use this default
 * unless overridden via the .wcf customization file (rare).
 *
 * Modern Vue FE must implement this mapping to be faithful to legacy NEC UX.
 *
 * ┌──────────────────┬────────────────────┬─────────────────────────────────┐
 * │ Value            │ NEC Physical Key    │ ESTS name / meaning             │
 * ├──────────────────┼────────────────────┼─────────────────────────────────┤
 * │ '00'             │ (no end key)        │ NOP — digit count exhausted     │
 * │ '01'             │ [RETURN] (Enter)    │ HTB — next field                │
 * │ '02'             │ [SHIFT]+[f.8]       │ I  — approve (承認)             │
 * │ '03'             │ [SHIFT]+[f.9]       │ II — reject/delete (削除/取消)  │
 * │ '04'             │ [f.10], [vf.5]      │ ADV — advance/submit (登録)     │
 * │ '05'             │ [SHIFT]+[RETURN]    │ STB                             │
 * │ '06'             │ [TAB]               │ SKP — skip / next field         │
 * │ '07'             │ ↑                   │ ZEN — up                        │
 * │ '08'             │ ↓                   │ GO  — down                      │
 * │ '09'             │ [BS] (Backspace)    │ BTB — back-tab                  │
 * │ 'P0'             │ [HELP]              │ HELP                            │
 * │ 'P1'..'P9'       │ [f.1]..[f.9]        │ PF1..PF9                        │
 * │ 'PA'             │ [SHIFT]+[f.10]      │ PF10                            │
 * │ 'PB'..'PG'       │ [SHIFT]+[f.1]..[f.6]│ PF11..PF16                      │
 * │ 'FW'             │ [SHIFT]+[ROLL UP]   │ page forward                    │
 * │ 'BW'             │ [SHIFT]+[ROLL DOWN] │ page back                       │
 * │ 'FM'             │ [CTRL]+[f.10]       │ FORM                            │
 * │ 'UP'             │ [ROLL UP]           │                                 │
 * │ 'DW'             │ [ROLL DOWN]         │                                 │
 * └──────────────────┴────────────────────┴─────────────────────────────────┘
 *
 * ----------------------------------------------------------------------------
 * Modern keyboard adaptations (deliberate divergences from Table 7-1):
 * ----------------------------------------------------------------------------
 * 1. Enter ('Enter' key) → '00' (NOP) instead of '01' (HTB).
 *    Rationale: modern web UX expects Enter to "submit" on confirmation
 *    fields. BE WebSocketScreenRenderer.mapAidKey() resolves '00' → field's
 *    advanceEsts (usually '01' on data fields, '04' on SC-OK confirm), so
 *    Enter behavior is context-aware per field intent.
 *
 * 2. F7/F8 → 'BW'/'FW' (page back/forward) via AID_TRANSLATE.
 *    Rationale: ESTS-FW/BW are used for list pagination, but NEC
 *    keyboards have dedicated ROLL UP/DOWN keys that PC keyboards lack. So
 *    F7/F8 (which no program uses as PF7/PF8) are repurposed for pagination.
 *    Faithful alternative via Shift+PageUp/PageDown also supported.
 *
 * 3. Shift+Tab → '09' (BTB) — alias for Backspace.
 *    Rationale: modern PC users expect Shift+Tab = back-tab. Backspace is
 *    reserved for character deletion inside text inputs.
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────
// Single-key map (no modifier) — Browser KeyboardEvent.key → AID code
// ─────────────────────────────────────────────────────────────────────────
/**
 * Used by useScreenForm.onKey for keys without modifier.
 * Note: 'Enter' deliberately maps to '00' (NOP) to defer to BE smart resolver.
 */
export const KEY_AID_MAP: Record<string, string> = {
  // Function keys F1-F9 → PF1-PF9 (Table 7-1 default)
  F1: 'P1',
  F2: 'P2',
  F3: 'P3',
  F4: 'P4',          // PF4 — used for delete-row
  F5: 'P5',
  F6: 'P6',          // PF6 — cancel/back navigation
  F7: 'P7',          // PF7 — see AID_TRANSLATE (POC pagination convention)
  F8: 'P8',          // PF8 — see AID_TRANSLATE (POC pagination convention)
  F9: 'P9',          // PF9 — exit program
  F10: '04',         // ⭐ ADV (submit/登録) — Table 7-1 default, NOT 'PA'
  End: 'P0',         // PF0 — used for "end"

  // Non-PF terminate keys (Table 7-1 通常の入力終了キー)
  // Enter → '00' defers to BE smart resolver (intentional divergence — see header docs)
  Enter: '00',
  Backspace: '09',   // BTB — back-tab (intercepted only outside text inputs; see useScreenForm.onKey)

  // Navigation keys — modern PC adaptation for COBOL ROLL UP/DOWN
  PageUp: 'UP',      // ROLL UP
  PageDown: 'DW',    // ROLL DOWN

  // INTENTIONALLY NOT MAPPED (Table 7-1 codes exist but we defer to browser default):
  //   - Tab ('06' SKP): modern web users expect Tab = focus next form field
  //     (FE-only nav). Intercepting forces server round-trip per Tab. If
  //     ESTS-SKP semantic needed, add per-SFC pfOverrides.
  //   - ArrowUp ('07' ZEN), ArrowDown ('08' GO): common for dropdown / select
  //     navigation. Intercepting breaks dropdown UX. Per-SFC override if needed.
};

// ─────────────────────────────────────────────────────────────────────────
// Modifier-combination map — "Ctrl+Shift+Key" → AID code
// Format keys: alphabetical modifier order (Alt < Ctrl < Shift) joined by '+'
// then '+' + KeyboardEvent.key (e.g. 'Shift+F1', 'Ctrl+F10').
// ─────────────────────────────────────────────────────────────────────────
export const MODIFIER_KEY_AID_MAP: Record<string, string> = {
  // Shift + Function key → Extended PF codes (Table 7-1 PF10-PF16)
  'Shift+F1':  'PB',     // PF11
  'Shift+F2':  'PC',     // PF12
  'Shift+F3':  'PD',     // PF13
  'Shift+F4':  'PE',     // PF14
  'Shift+F5':  'PF',     // PF15
  'Shift+F6':  'PG',     // PF16
  'Shift+F8':  '02',     // ESTS-I (承認 / approve)
  'Shift+F9':  '03',     // ESTS-II (削除/取消/否認)
  'Shift+F10': 'PA',     // ESTS-PF10 (raw PF10 — for programs that bind ESTS-PF10)

  // Shift + Other keys
  'Shift+Enter': '05',   // STB (Shift+Return)
  'Shift+Tab':   '09',   // BTB (modern alias for Backspace back-tab)
  'Shift+PageUp':   'FW', // page forward (Shift+ROLL UP)
  'Shift+PageDown': 'BW', // page back (Shift+ROLL DOWN)

  // Ctrl + Function keys
  'Ctrl+F10': 'FM',      // ESTS-FORM
};

// ─────────────────────────────────────────────────────────────────────────
// AID_TRANSLATE — raw PF code → pagination convention
// ─────────────────────────────────────────────────────────────────────────
/**
 * Translate raw PF code → NEC ROLL convention code.
 *
 * ESTS-FW/BW are used for list pagination. Since modern PC keyboards
 * don't have dedicated ROLL UP/DOWN keys, F7/F8 PF codes are repurposed
 * via this translation:
 *
 *   F7 → 'P7' → 'BW' (page back / ROLL-DOWN equivalent)
 *   F8 → 'P8' → 'FW' (page forward / ROLL-UP equivalent)
 *
 * Verified safe: no program binds ESTS-PF7 or ESTS-PF8.
 *
 * Alternative faithful path: users may press Shift+PageDown / Shift+PageUp
 * which directly emit 'BW' / 'FW' via MODIFIER_KEY_AID_MAP without translation.
 *
 * If a future program legitimately uses ESTS-PF7 or ESTS-PF8, remove the
 * corresponding entry below.
 */
export const AID_TRANSLATE: Record<string, string> = {
  P7: 'BW',
  P8: 'FW',
};

// ─────────────────────────────────────────────────────────────────────────
// Button-label → AID — bypass PF translation
// ─────────────────────────────────────────────────────────────────────────
/**
 * Button labels mapped to their COBOL ESTS code.
 *
 * Used by SFC button onClick handlers — those bypass keyboard→AID lookup
 * entirely and send the action's AID code directly per Table 7-1 + CWK001.
 *
 * `labels` are Japanese button strings observed in the legacy + modernize FE.
 * Add new labels when discovered; verify by grep'ing COBOL for ESTS-X
 * binding.
 */
export const SCREEN_ACTION_AID: Record<
  'ADV' | 'I' | 'II' | 'BTB' | 'STB' | 'HELP' | 'FORM',
  { code: string; ests: string; labels: string[] }
> = {
  ADV:  { code: '04', ests: 'ESTS-ADV',  labels: ['一括印刷', '確定', '登録', '実行'] },
  I:    { code: '02', ests: 'ESTS-I',    labels: ['承認'] },
  II:   { code: '03', ests: 'ESTS-II',   labels: ['否認', '取消', '削除'] },
  BTB:  { code: '09', ests: 'ESTS-BTB',  labels: ['戻る', 'キャンセル'] },
  STB:  { code: '05', ests: 'ESTS-STB',  labels: [] },
  HELP: { code: 'P0', ests: 'ESTS-HELP', labels: ['ヘルプ'] },
  FORM: { code: 'FM', ests: 'ESTS-FORM', labels: [] },
};

// ─────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────

/**
 * Resolve a KeyboardEvent to an AID code per Table 7-1.
 * Returns undefined if key is not mapped (caller falls through to browser default).
 *
 * Lookup order:
 *   1. Modifier combination (e.g. 'Shift+F8') via MODIFIER_KEY_AID_MAP
 *   2. Single key (no modifier) via KEY_AID_MAP
 *
 * Modifier prefix is built in alphabetical order: 'Alt+Ctrl+Shift+<key>'.
 */
export function resolveAidFromEvent(e: KeyboardEvent): string | undefined {
  // Build modifier prefix (alphabetical to ensure deterministic lookup)
  const mods: string[] = [];
  if (e.altKey) mods.push('Alt');
  if (e.ctrlKey || e.metaKey) mods.push('Ctrl');
  if (e.shiftKey) mods.push('Shift');

  if (mods.length > 0) {
    const lookupKey = mods.join('+') + '+' + e.key;
    if (MODIFIER_KEY_AID_MAP[lookupKey]) {
      return MODIFIER_KEY_AID_MAP[lookupKey];
    }
    // Modifier combo not mapped — fall through to single-key lookup
    // (some keys like Shift+Letter or Ctrl+C should NOT match the no-modifier map)
    return undefined;
  }

  return KEY_AID_MAP[e.key];
}

/**
 * Apply NEC ROLL translation (pagination convention) if applicable.
 * Called by useTerminalSocket.sendMessage before every WS emit.
 */
export function translateAidKey(raw: string): string {
  return AID_TRANSLATE[raw] ?? raw;
}

