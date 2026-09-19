// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'PH-DATE': 'PH-DATE',
  'PH-SUPP': 'PH-SUPP',
  'PH-WHSE': 'PH-WHSE',
  'PH-STAFF': 'PH-STAFF',
  'PH-DUE-DATE': 'PH-DUE-DATE',
  'PH-TAX-TYPE': 'PH-TAX-TYPE',
  'PH-REMARK': 'PH-REMARK',
  'WK-D-PROD': 'WK-D-PROD',
  'WK-D-WHSE': 'WK-D-WHSE',
  'WK-D-QTY': 'WK-D-QTY',
  'WK-D-COST': 'WK-D-COST',
  'WK-CONFIRM': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'PH-SUPP': 'WK-SUPP-NAME',
  'WK-D-PROD': 'WK-D-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['PH-DATE', 'PH-SUPP', 'PH-WHSE', 'PH-STAFF', 'PH-DUE-DATE', 'PH-TAX-TYPE', 'WK-D-PROD', 'WK-D-WHSE', 'WK-D-QTY', 'WK-D-COST']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'PH-DATE': 8,
  'PH-SUPP': 6,
  'PH-WHSE': 3,
  'PH-STAFF': 4,
  'PH-DUE-DATE': 8,
  'PH-TAX-TYPE': 1,
  'PH-REMARK': 40,
  'WK-D-PROD': 8,
  'WK-D-WHSE': 3,
  'WK-D-QTY': 9,
  'WK-D-COST': 9,
  'WK-CONFIRM': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'PH-DATE': 'PO Date',
  'PH-SUPP': 'Supplier',
  'PH-WHSE': 'Warehouse',
  'PH-STAFF': 'Buyer/Staff',
  'PH-DUE-DATE': 'Due Date',
  'PH-TAX-TYPE': 'Tax Type',
  'PH-REMARK': 'Remark',
  'WK-D-PROD': 'Product',
  'WK-D-WHSE': 'Warehouse',
  'WK-D-QTY': 'Quantity',
  'WK-D-COST': 'Unit Cost',
  'WK-CONFIRM': 'Save PO ? (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'PH-TAX-TYPE': '1:excl 2:incl 3:exempt',
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
