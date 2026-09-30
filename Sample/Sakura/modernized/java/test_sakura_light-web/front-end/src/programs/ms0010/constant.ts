// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'SC-KEY': 'CU-CODE',
  'CU-NAME': 'CU-NAME',
  'CU-KANA': 'CU-KANA',
  'CU-ZIP': 'CU-ZIP',
  'CU-ADDR1': 'CU-ADDR1',
  'CU-ADDR2': 'CU-ADDR2',
  'CU-TEL': 'CU-TEL',
  'CU-FAX': 'CU-FAX',
  'CU-REGION': 'CU-REGION',
  'CU-STAFF': 'CU-STAFF',
  'CU-CLOSE-DAY': 'CU-CLOSE-DAY',
  'CU-PAY-METHOD': 'CU-PAY-METHOD',
  'CU-TAX-TYPE': 'CU-TAX-TYPE',
  'CU-CREDIT-LIMIT': 'CU-CREDIT-LIMIT',
  'CU-PRICE-RANK': 'CU-PRICE-RANK',
  'CU-BANK-CODE': 'CU-BANK-CODE',
  'SC-CONF': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'CU-REGION': 'WK-REGION-NAME',
  'CU-STAFF': 'WK-STAFF-NAME',
  'CU-BANK-CODE': 'WK-BANK-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['SC-KEY', 'CU-REGION', 'CU-STAFF', 'CU-CLOSE-DAY', 'CU-PAY-METHOD', 'CU-TAX-TYPE', 'CU-CREDIT-LIMIT', 'CU-PRICE-RANK', 'CU-BANK-CODE']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'SC-KEY': 6,
  'CU-NAME': 40,
  'CU-KANA': 40,
  'CU-ZIP': 8,
  'CU-ADDR1': 40,
  'CU-ADDR2': 40,
  'CU-TEL': 15,
  'CU-FAX': 15,
  'CU-REGION': 3,
  'CU-STAFF': 4,
  'CU-CLOSE-DAY': 2,
  'CU-PAY-METHOD': 1,
  'CU-TAX-TYPE': 1,
  'CU-CREDIT-LIMIT': 11,
  'CU-PRICE-RANK': 1,
  'CU-BANK-CODE': 4,
  'SC-CONF': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'SC-KEY': 'Customer Code',
  'CU-NAME': 'Name',
  'CU-KANA': 'Search Name',
  'CU-ZIP': 'Post Code',
  'CU-ADDR1': 'Address 1',
  'CU-ADDR2': 'Address 2',
  'CU-TEL': 'Telephone',
  'CU-FAX': 'Facsimile',
  'CU-REGION': 'Region Code',
  'CU-STAFF': 'Sales Rep',
  'CU-CLOSE-DAY': 'Closing Day',
  'CU-PAY-METHOD': 'Pay Method',
  'CU-TAX-TYPE': 'Tax Type',
  'CU-CREDIT-LIMIT': 'Credit Limit',
  'CU-PRICE-RANK': 'Price Rank',
  'CU-BANK-CODE': 'Bank Code',
  'SC-CONF': 'Confirm (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'CU-PAY-METHOD': '1:cash 2:transfer 3:bill',
  'CU-TAX-TYPE': '1:excl 2:incl 3:exempt',
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
