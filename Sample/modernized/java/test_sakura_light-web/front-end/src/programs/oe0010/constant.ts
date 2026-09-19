// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'OH-DATE': 'OH-DATE',
  'OH-CUST': 'OH-CUST',
  'OH-STAFF': 'OH-STAFF',
  'OH-WHSE': 'OH-WHSE',
  'OH-DUE-DATE': 'OH-DUE-DATE',
  'OH-CUST-PO': 'OH-CUST-PO',
  'OH-TAX-TYPE': 'OH-TAX-TYPE',
  'WK-D-PROD': 'WK-D-PROD',
  'WK-D-WHSE': 'WK-D-WHSE',
  'WK-D-QTY': 'WK-D-QTY',
  'WK-D-PRICE': 'WK-D-PRICE',
  'WK-CONFIRM': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'OH-CUST': 'WK-CUST-NAME',
  'WK-D-PROD': 'WK-D-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['OH-DATE', 'OH-CUST', 'OH-STAFF', 'OH-WHSE', 'OH-DUE-DATE', 'OH-TAX-TYPE', 'WK-D-PROD', 'WK-D-WHSE', 'WK-D-QTY', 'WK-D-PRICE']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'OH-DATE': 8,
  'OH-CUST': 6,
  'OH-STAFF': 4,
  'OH-WHSE': 3,
  'OH-DUE-DATE': 8,
  'OH-CUST-PO': 20,
  'OH-TAX-TYPE': 1,
  'WK-D-PROD': 8,
  'WK-D-WHSE': 3,
  'WK-D-QTY': 9,
  'WK-D-PRICE': 9,
  'WK-CONFIRM': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'OH-DATE': 'Order Date',
  'OH-CUST': 'Customer',
  'OH-STAFF': 'Sales Rep',
  'OH-WHSE': 'Warehouse',
  'OH-DUE-DATE': 'Due Date',
  'OH-CUST-PO': 'Customer PO',
  'OH-TAX-TYPE': 'Tax Type',
  'WK-D-PROD': 'Product',
  'WK-D-WHSE': 'Warehouse',
  'WK-D-QTY': 'Quantity',
  'WK-D-PRICE': 'Unit Price',
  'WK-CONFIRM': 'Save order (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'OH-TAX-TYPE': '1:excl 2:incl 3:exempt',
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
