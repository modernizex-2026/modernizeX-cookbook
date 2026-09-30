// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'SC-KEY': 'US-CODE',
  'US-LOGIN': 'US-LOGIN',
  'US-PASSWORD': 'US-PASSWORD',
  'US-NAME': 'US-NAME',
  'US-ROLE': 'US-ROLE',
  'US-AUTH-MASTER': 'US-AUTH-MASTER',
  'US-AUTH-ORDER': 'US-AUTH-ORDER',
  'US-AUTH-SALES': 'US-AUTH-SALES',
  'US-AUTH-PURCH': 'US-AUTH-PURCH',
  'US-AUTH-CLOSE': 'US-AUTH-CLOSE',
  'SC-CONF': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {

};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['SC-KEY', 'US-ROLE', 'US-AUTH-MASTER', 'US-AUTH-ORDER', 'US-AUTH-SALES', 'US-AUTH-PURCH', 'US-AUTH-CLOSE']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'SC-KEY': 6,
  'US-LOGIN': 12,
  'US-PASSWORD': 16,
  'US-NAME': 30,
  'US-ROLE': 1,
  'US-AUTH-MASTER': 1,
  'US-AUTH-ORDER': 1,
  'US-AUTH-SALES': 1,
  'US-AUTH-PURCH': 1,
  'US-AUTH-CLOSE': 1,
  'SC-CONF': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'SC-KEY': 'User Code',
  'US-LOGIN': 'Login ID',
  'US-PASSWORD': 'Password',
  'US-NAME': 'User Name',
  'US-ROLE': 'Role',
  'US-AUTH-MASTER': 'Auth Master',
  'US-AUTH-ORDER': 'Auth Order',
  'US-AUTH-SALES': 'Auth Sales',
  'US-AUTH-PURCH': 'Auth Purchase',
  'US-AUTH-CLOSE': 'Auth Close',
  'SC-CONF': 'Confirm (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'US-ROLE': '1:admin 2:manager 3:clerk',
  'US-AUTH-MASTER': '0:no 1:yes',
};

// Program-wide keyboard Fn → COBOL ESTS code (manifest.pfKeys).
export const PF_OVERRIDES: Record<string, string> = {
  F1: '01',
  F2: '02',
  F3: '03',
  F4: '04',
  F5: '05',
  F6: '06',
  F7: '07',
  F8: '08',
  F9: '09',
  F10: 'PA',
  F11: 'PB',
  F12: 'PC',
};

export const SECTION_TYPES = { FORM: 'form', TABLE: 'table' } as const;
