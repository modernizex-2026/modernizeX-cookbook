// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'SC-KEY': 'SF-CODE',
  'SF-NAME': 'SF-NAME',
  'SF-KANA': 'SF-KANA',
  'SF-DEPT': 'SF-DEPT',
  'SF-TEL': 'SF-TEL',
  'SF-EMAIL': 'SF-EMAIL',
  'SF-TITLE': 'SF-TITLE',
  'SC-CONF': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'SF-DEPT': 'WK-DEPT-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['SC-KEY', 'SF-DEPT']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'SC-KEY': 4,
  'SF-NAME': 30,
  'SF-KANA': 30,
  'SF-DEPT': 4,
  'SF-TEL': 15,
  'SF-EMAIL': 40,
  'SF-TITLE': 20,
  'SC-CONF': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'SC-KEY': 'Staff Code',
  'SF-NAME': 'Name',
  'SF-KANA': 'Search Name',
  'SF-DEPT': 'Department',
  'SF-TEL': 'Telephone',
  'SF-EMAIL': 'E-mail',
  'SF-TITLE': 'Title',
  'SC-CONF': 'Confirm (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {

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
