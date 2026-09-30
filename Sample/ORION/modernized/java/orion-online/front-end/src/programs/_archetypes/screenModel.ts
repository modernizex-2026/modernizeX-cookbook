// screenModel.ts — pure helpers for deterministic archetypes (emitted by
// bms-screen-modernize). No Vue, no reference runtime: input is the manifest
// field shape only. Labels are derived from BMS geometry, not name-guessing.

export interface Field {
  name: string
  row?: number
  col?: number
  length?: number
  input?: boolean
  initial?: string | null
  attrb?: string | null
  picIn?: string | null
  picOut?: string | null
}

// "1. XXX" / "2）YYY" — numbered menu-option literal (full-width punctuation ok).
export const MENU_OPTION_RE = /^\s*(\d+)\s*[.．)）、]\s*(.+?)\s*$/

/**
 * Label for `f` = the nearest non-input literal to its LEFT on the same row —
 * but ONLY if no OTHER named field sits between them (so a shared/far caption is
 * not stolen by the wrong field). No left caption ⇒ '' (never the raw field name).
 */
export function labelFor(fields: Field[], f: Field): string {
  const sameRow = fields.filter(x => x.row === f.row)
  // a label is an UNNAMED static caption (name==='') — never another named data field.
  const nearest = sameRow
    .filter(x => !x.input && !x.name && typeof x.initial === 'string' && (x.col ?? 0) < (f.col ?? 0))
    .sort((a, b) => (b.col ?? 0) - (a.col ?? 0))[0]
  if (!nearest) return ''
  const blocked = sameRow.some(x => x.name && x !== f
    && (x.col ?? 0) > (nearest.col ?? 0) && (x.col ?? 0) < (f.col ?? 0))
  return blocked ? '' : String(nearest.initial).replace(/[:：]\s*$/, '').trim()
}

/** 3270 non-display input (password shift). */
export function isDarkField(f: Field): boolean {
  return /(^|,)\s*DRK\b/i.test(f.attrb ?? '')
}

/** 3270 numeric shift, or an all-9/Z input picture. */
export function isNumericField(f: Field): boolean {
  return /(^|,)\s*NUM\b/i.test(f.attrb ?? '')
    || (typeof f.picIn === 'string' && /^[9Z]+$/i.test(f.picIn))
}

/** Named fields (input or named output) in reading order, excluding record cells. */
export function namedFields(fields: Field[], exclude: Set<string>): Field[] {
  return fields
    .filter(f => f.name && !exclude.has(f.name))
    .sort((a, b) => (a.row ?? 0) - (b.row ?? 0) || (a.col ?? 0) - (b.col ?? 0))
}

/**
 * Topmost real title literal: not a numbered option, not a "PFx" hint, not a
 * field caption (ends with ':'), and reasonably word-like (≥ 4 chars). Falls back
 * to the screen name so a stray caption like "Tran:" never becomes the title.
 */
export function titleOf(fields: Field[], fallback: string): string {
  const lits = fields.filter(f => !f.input && !f.name && typeof f.initial === 'string'
    && !MENU_OPTION_RE.test(f.initial as string) && !/PF\d/i.test(f.initial as string)
    && !/[:：]\s*$/.test(f.initial as string) && (f.initial as string).trim().length >= 4)
  lits.sort((a, b) => (a.row ?? 0) - (b.row ?? 0))
  return String(lits[0]?.initial ?? fallback).trim()
}

/** Field names that are cells of a record region (excluded from the flat grid). */
export function listCellNames(screen: { recordLists?: Array<{ columns?: string[]; rows?: Array<Record<string, string>> }> }): Set<string> {
  const s = new Set<string>()
  for (const rl of (screen?.recordLists ?? [])) {
    for (const row of (rl.rows ?? [])) for (const c of (rl.columns ?? [])) {
      if (row[c]) s.add(row[c])
    }
  }
  return s
}
