// AUTO-GENERATED per-program config (source-faithful, manifest-derived).
// Do NOT hand-edit — regenerate via modernize-ui. Consumed by the deterministic
// form archetype (imported as './constant'). Text is verbatim from the manifest.

// SC-input → COBOL data field (coord-invariant typed lookup).
export const FIELD_DATA_MAP: Record<string, string> = {
  'SC-KEY': 'PR-CODE',
  'PR-NAME': 'PR-NAME',
  'PR-KANA': 'PR-KANA',
  'PR-SPEC': 'PR-SPEC',
  'PR-CATEGORY': 'PR-CATEGORY',
  'PR-BARCODE': 'PR-BARCODE',
  'PR-UNIT': 'PR-UNIT',
  'PR-STD-COST': 'PR-STD-COST',
  'PR-LIST-PRICE': 'PR-LIST-PRICE',
  'PR-LAST-COST': 'PR-LAST-COST',
  'PR-TAX-CATEGORY': 'PR-TAX-CATEGORY',
  'PR-RANK-PRICE(1)': 'PR-RANK-PRICE(1)',
  'PR-RANK-PRICE(2)': 'PR-RANK-PRICE(2)',
  'PR-RANK-PRICE(3)': 'PR-RANK-PRICE(3)',
  'PR-RANK-PRICE(4)': 'PR-RANK-PRICE(4)',
  'PR-RANK-PRICE(5)': 'PR-RANK-PRICE(5)',
  'PR-SAFETY-STOCK': 'PR-SAFETY-STOCK',
  'PR-REORDER-POINT': 'PR-REORDER-POINT',
  'PR-REORDER-QTY': 'PR-REORDER-QTY',
  'PR-LEAD-DAYS': 'PR-LEAD-DAYS',
  'PR-DFLT-SUPP': 'PR-DFLT-SUPP',
  'PR-DFLT-WHSE': 'PR-DFLT-WHSE',
  'PR-STOCK-MNG': 'PR-STOCK-MNG',
  'SC-CONF': 'WK-CONFIRM',
};

// Code fields whose adjacent read-only display field (name/description) is shown
// beside the value.
export const FIELD_DESC_MAP: Record<string, string> = {
  'PR-CATEGORY': 'WK-CATG-NAME',
  'PR-DFLT-SUPP': 'WK-SUPP-NAME',
  'PR-DFLT-WHSE': 'WK-WHSE-NAME',
};

// PIC 9 / S9 / Z numeric fields — right-aligned + numeric inputmode.
export const NUMERIC_FIELDS = new Set<string>(['SC-KEY', 'PR-CATEGORY', 'PR-STD-COST', 'PR-LIST-PRICE', 'PR-LAST-COST', 'PR-TAX-CATEGORY', 'PR-RANK-PRICE(1)', 'PR-RANK-PRICE(2)', 'PR-RANK-PRICE(3)', 'PR-RANK-PRICE(4)', 'PR-RANK-PRICE(5)', 'PR-SAFETY-STOCK', 'PR-REORDER-POINT', 'PR-REORDER-QTY', 'PR-LEAD-DAYS', 'PR-DFLT-SUPP', 'PR-DFLT-WHSE', 'PR-STOCK-MNG']);

// Per-field rendered widths (character units), from the manifest PIC/width.
export const WIDTHS: Record<string, number> = {
  'SC-KEY': 8,
  'PR-NAME': 40,
  'PR-KANA': 40,
  'PR-SPEC': 30,
  'PR-CATEGORY': 4,
  'PR-BARCODE': 13,
  'PR-UNIT': 6,
  'PR-STD-COST': 9,
  'PR-LIST-PRICE': 9,
  'PR-LAST-COST': 9,
  'PR-TAX-CATEGORY': 1,
  'PR-RANK-PRICE(1)': 9,
  'PR-RANK-PRICE(2)': 9,
  'PR-RANK-PRICE(3)': 9,
  'PR-RANK-PRICE(4)': 9,
  'PR-RANK-PRICE(5)': 9,
  'PR-SAFETY-STOCK': 9,
  'PR-REORDER-POINT': 9,
  'PR-REORDER-QTY': 9,
  'PR-LEAD-DAYS': 3,
  'PR-DFLT-SUPP': 6,
  'PR-DFLT-WHSE': 3,
  'PR-STOCK-MNG': 1,
  'SC-CONF': 1,
};

// Field captions taken verbatim from the COBOL screen literals.
export const LABELS: Record<string, string> = {
  'SC-KEY': 'Product Code',
  'PR-NAME': 'Name',
  'PR-KANA': 'Search Name',
  'PR-SPEC': 'Spec / Model',
  'PR-CATEGORY': 'Category',
  'PR-BARCODE': 'Barcode',
  'PR-UNIT': 'Unit',
  'PR-STD-COST': 'Std Cost',
  'PR-LIST-PRICE': 'List Price',
  'PR-LAST-COST': 'Last Cost',
  'PR-TAX-CATEGORY': 'Tax',
  'PR-RANK-PRICE(1)': 'Rank1',
  'PR-RANK-PRICE(2)': 'Rank2',
  'PR-RANK-PRICE(3)': 'Rank3',
  'PR-RANK-PRICE(4)': 'Rank4',
  'PR-RANK-PRICE(5)': 'Rank5',
  'PR-SAFETY-STOCK': 'Safety Stock',
  'PR-REORDER-POINT': 'Reorder Pt',
  'PR-REORDER-QTY': 'Reorder Qty',
  'PR-LEAD-DAYS': 'Lead Days',
  'PR-DFLT-SUPP': 'Dflt Supplier',
  'PR-DFLT-WHSE': 'Dflt Warehouse',
  'PR-STOCK-MNG': 'Stock Mng',
  'SC-CONF': 'Confirm (Y/N)',
};

// Enum-guide / note literals beside each input (verbatim from the screen).
export const HINTS: Record<string, string> = {
  'PR-BARCODE': 'Unit:',
  'PR-STD-COST': 'List Price:',
  'PR-LAST-COST': 'Tax: 1:std 2:reduced 3:exempt',
  'PR-TAX-CATEGORY': '1:std 2:reduced 3:exempt',
  'PR-RANK-PRICE(1)': 'Rank2: Rank3:',
  'PR-RANK-PRICE(2)': 'Rank3:',
  'PR-RANK-PRICE(4)': 'Rank5:',
  'PR-SAFETY-STOCK': 'Reorder Pt:',
  'PR-REORDER-QTY': 'Lead Days:',
  'PR-STOCK-MNG': '0:no 1:managed',
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
