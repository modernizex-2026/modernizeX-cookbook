// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'SC-KEY': 'SP-CODE',
  'SP-NAME': 'SP-NAME',
  'SP-KANA': 'SP-KANA',
  'SP-ZIP': 'SP-ZIP',
  'SP-ADDR1': 'SP-ADDR1',
  'SP-ADDR2': 'SP-ADDR2',
  'SP-TEL': 'SP-TEL',
  'SP-FAX': 'SP-FAX',
  'SP-CLOSE-DAY': 'SP-CLOSE-DAY',
  'SP-PAY-MONTH': 'SP-PAY-MONTH',
  'SP-PAY-DAY': 'SP-PAY-DAY',
  'SP-PAY-METHOD': 'SP-PAY-METHOD',
  'SP-TAX-TYPE': 'SP-TAX-TYPE',
  'SP-BANK-CODE': 'SP-BANK-CODE',
  'SP-BANK-ACCT': 'SP-BANK-ACCT',
  'SC-CONF': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'SP-BANK-CODE': 'WK-BANK-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['SC-KEY', 'SP-CLOSE-DAY', 'SP-PAY-MONTH', 'SP-PAY-DAY', 'SP-PAY-METHOD', 'SP-TAX-TYPE', 'SP-BANK-CODE']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'SC-KEY': 6,
  'SP-NAME': 40,
  'SP-KANA': 40,
  'SP-ZIP': 8,
  'SP-ADDR1': 40,
  'SP-ADDR2': 40,
  'SP-TEL': 15,
  'SP-FAX': 15,
  'SP-CLOSE-DAY': 2,
  'SP-PAY-MONTH': 1,
  'SP-PAY-DAY': 2,
  'SP-PAY-METHOD': 1,
  'SP-TAX-TYPE': 1,
  'SP-BANK-CODE': 4,
  'SP-BANK-ACCT': 20,
  'SC-CONF': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'SC-KEY': 'Supplier Code',
  'SP-NAME': 'Name',
  'SP-KANA': 'Search Name',
  'SP-ZIP': 'Post Code',
  'SP-ADDR1': 'Address 1',
  'SP-ADDR2': 'Address 2',
  'SP-TEL': 'Telephone',
  'SP-FAX': 'Facsimile',
  'SP-CLOSE-DAY': 'Closing Day',
  'SP-PAY-MONTH': 'Pay Month',
  'SP-PAY-DAY': 'Pay Day',
  'SP-PAY-METHOD': 'Pay Method',
  'SP-TAX-TYPE': 'Tax Type',
  'SP-BANK-CODE': 'Bank Code',
  'SP-BANK-ACCT': 'Bank Account',
  'SC-CONF': 'Confirm (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'SP-CLOSE-DAY': '(99=month end)',
  'SP-PAY-MONTH': '0:same 1:next 2:+2',
  'SP-PAY-DAY': '(99=month end)',
  'SP-PAY-METHOD': '1:cash 2:transfer 3:bill',
  'SP-TAX-TYPE': '1:excl 2:incl 3:exempt',
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
