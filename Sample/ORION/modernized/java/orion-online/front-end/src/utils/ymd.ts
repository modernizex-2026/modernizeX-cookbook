/**
 * COBOL PIC 9(6) date helpers — wire format YYMMDD, display YYYY/MM/DD.
 *
 * Century pivot mirrors the backend (acceptMoveDate):
 *   value < 800000 → 20YY, else → 19YY.
 */

export function normalizeYymmdd(raw: string): string {
  const digits = raw.replace(/\D/g, '');
  if (!digits) return '';
  return digits.slice(0, 6).padStart(6, '0');
}

/** Expand PIC 9(6) YYMMDD to YYYYMMDD (pivot: YY < 80 → 20YY, else 19YY). */
export function yymmddToCenturyYear(yymmdd: number): number {
  if (!yymmdd) return 0;
  const yy = Math.floor(yymmdd / 10_000);
  const mm = Math.floor((yymmdd % 10_000) / 100);
  const dd = yymmdd % 100;
  const yyyy = yy < 80 ? 2000 + yy : 1900 + yy;
  return yyyy * 10_000 + mm * 100 + dd;
}

/** YYMMDD (or digits from BE) → YYYY/MM/DD for screen display. */
export function yymmddToDisplay(raw: string): string {
  const digits = raw.replace(/\D/g, '');
  if (digits.length < 6) return '';
  const yymmdd = parseInt(digits.slice(0, 6), 10);
  if (!yymmdd || /^0+$/.test(digits.slice(0, 6))) return '';
  const full = yymmddToCenturyYear(yymmdd);
  const yyyy = Math.floor(full / 10_000);
  const mm = Math.floor((full % 10_000) / 100);
  const dd = full % 100;
  if (mm < 1 || mm > 12 || dd < 1 || dd > 31) return '';
  return `${yyyy}/${String(mm).padStart(2, '0')}/${String(dd).padStart(2, '0')}`;
}

/**
 * Convert user input to YYMMDD wire value for BE.
 * Accepts:
 *   - "260215"       → 6 raw digits YYMMDD
 *   - "20260215"     → 8 raw digits YYYYMMDD
 *   - "26/02/15"     → YY/MM/DD
 *   - "2026/02/15"   → YYYY/MM/DD
 *   - dash variants  → same with "-" separator
 *
 * Deliberately does NOT reject out-of-range month/day (e.g. "19" for month):
 * an earlier version returned '' for those, which made the date-trio submit
 * chain silently falls back
 * to the field's last-committed value instead of forwarding what the user
 * actually typed — BE's own validation (e.g. EI007) never saw the bad value
 * and never fired, so the whole thing looked like it succeeded. Range
 * validation is BE's job; this only rejects genuinely unparsable formats.
 */
export function displayToYymmdd(display: string): string {
  const s = display.trim();

  if (/^\d{6}$/.test(s)) {
    return s;
  }

  if (/^\d{8}$/.test(s)) {
    const yyyy = parseInt(s.slice(0, 4), 10);
    const yy = yyyy % 100;
    return `${String(yy).padStart(2, '0')}${s.slice(4, 6)}${s.slice(6, 8)}`;
  }

  // YY/MM/DD, YY-MM-DD, YYYY/MM/DD, YYYY-MM-DD
  const m = s.match(/^(\d{2}|\d{4})[/-](\d{1,2})[/-](\d{1,2})$/);
  if (!m) return '';
  const yearPart = parseInt(m[1], 10);
  const yy = m[1].length === 4 ? yearPart % 100 : yearPart;
  const mm = m[2].padStart(2, '0');
  const dd = m[3].padStart(2, '0');
  return `${String(yy).padStart(2, '0')}${mm}${dd}`;
}

/** YYMMDD → YYYY-MM-DD for native date input value. */
export function yymmddToIso(raw: string): string {
  const display = yymmddToDisplay(raw);
  if (!display) return '';
  return display.replace(/\//g, '-');
}

/** YYYY-MM-DD (native picker) → YYMMDD wire value. */
export function isoToYymmdd(iso: string): string {
  const m = iso.trim().match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!m) return '';
  return displayToYymmdd(`${m[1]}/${m[2]}/${m[3]}`);
}

/**
 * True when a PIC 9(6) YYMMDD wire is a REAL calendar date — valid month (1-12)
 * AND valid day-of-month, leap-year aware (so 2026/02/30, 2026/02/31, 2026/13/01
 * are rejected). An empty/all-zero wire is treated as "not a date" → false.
 *
 * Kept separate from displayToYymmdd() ON PURPOSE: displayToYymmdd stays lenient so
 * a date-trio submit chain still forwards raw input to the backend; this
 * strict checker is only for call sites that want to block an invalid date before
 * submit (e.g. 出庫日 / 入庫日 fields).
 */
export function isValidYymmdd(wire: string): boolean {
  const digits = (wire ?? '').replace(/\D/g, '');
  if (digits.length < 6) return false;
  const yymmdd = parseInt(digits.slice(0, 6), 10);
  if (!yymmdd || /^0+$/.test(digits.slice(0, 6))) return false;
  const full = yymmddToCenturyYear(yymmdd);
  const yyyy = Math.floor(full / 10_000);
  const mm = Math.floor((full % 10_000) / 100);
  const dd = full % 100;
  if (mm < 1 || mm > 12 || dd < 1) return false;
  // Day 0 of month (mm+1) rolls back to the last day of month mm — leap-year aware.
  const lastDay = new Date(yyyy, mm, 0).getDate();
  return dd <= lastDay;
}
