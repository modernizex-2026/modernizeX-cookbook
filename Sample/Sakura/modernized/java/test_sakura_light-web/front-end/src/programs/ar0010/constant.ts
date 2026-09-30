// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'RE-DATE': 'RE-DATE',
  'RE-CUST': 'RE-CUST',
  'RE-METHOD': 'RE-METHOD',
  'RE-AMOUNT': 'RE-AMOUNT',
  'RE-BANK-CODE': 'RE-BANK-CODE',
  'RE-REMARK': 'RE-REMARK',
  'SC-CONF': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'RE-CUST': 'WK-CUST-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['RE-DATE', 'RE-CUST', 'RE-METHOD', 'RE-AMOUNT', 'RE-BANK-CODE']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'RE-DATE': 8,
  'RE-CUST': 6,
  'RE-METHOD': 1,
  'RE-AMOUNT': 11,
  'RE-BANK-CODE': 4,
  'RE-REMARK': 30,
  'SC-CONF': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'RE-DATE': 'Date',
  'RE-CUST': 'Customer',
  'RE-METHOD': 'Method',
  'RE-AMOUNT': 'Amount',
  'RE-BANK-CODE': 'Bank Code',
  'RE-REMARK': 'Remark',
  'SC-CONF': 'Save receipt Y/N',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'RE-METHOD': '1:cash 2:transfer 3:bill 4:offset',
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
