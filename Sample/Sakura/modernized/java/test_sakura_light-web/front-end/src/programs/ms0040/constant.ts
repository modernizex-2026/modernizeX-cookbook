// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'SC-KEY': 'WH-CODE',
  'WH-NAME': 'WH-NAME',
  'WH-ZIP': 'WH-ZIP',
  'WH-ADDR': 'WH-ADDR',
  'WH-TEL': 'WH-TEL',
  'WH-TYPE': 'WH-TYPE',
  'WH-MANAGER': 'WH-MANAGER',
  'SC-CONF': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'WH-MANAGER': 'WK-MGR-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['SC-KEY', 'WH-TYPE', 'WH-MANAGER']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'SC-KEY': 3,
  'WH-NAME': 30,
  'WH-ZIP': 8,
  'WH-ADDR': 40,
  'WH-TEL': 15,
  'WH-TYPE': 1,
  'WH-MANAGER': 4,
  'SC-CONF': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'SC-KEY': 'Warehouse Code',
  'WH-NAME': 'Name',
  'WH-ZIP': 'Post Code',
  'WH-ADDR': 'Address',
  'WH-TEL': 'Telephone',
  'WH-TYPE': 'Type',
  'WH-MANAGER': 'Manager',
  'SC-CONF': 'Confirm (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'WH-TYPE': '1:normal 2:transit 3:defect',
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
