// Cờ gỡ lỗi dùng CHUNG hai nhánh (R9/B7) — cùng quy ước localStorage.cobol_debug.
import { isDebugEnabled } from '../utils/debugLog'

// Layout + static attributes live in the manifest; the backend returns dynamic VALUES,
// dynamic attribute overrides, cursor field and navigation redirect (CICS semantics).
export interface ScreenData {
  screen: string
  fields: Record<string, string>
  attrs: Record<string, string>
  cursor: string
  message: string
  redirect: string
  erase: boolean
  // Per-screen PF/AID buttons from the backend (CICS getButtonDefs) — drives the dynamic PF bar.
  buttons: { aidKey: string, label: string }[]
}

function asStrings(o: Record<string, any> | null | undefined): Record<string, string> {
  const out: Record<string, string> = {}
  for (const k in (o || {})) { const v = (o as any)[k]; out[k] = v == null ? '' : String(v) }
  return out
}

// Per-tab virtual terminal id (CICS multi-terminal isolation), like the legacy terminal.js.
function tabId(): string {
  let t = sessionStorage.getItem('tabId')
  if (!t) { t = Date.now().toString(36) + Math.random().toString(36).slice(2, 6); sessionStorage.setItem('tabId', t) }
  return t
}

export async function loadManifest(program: string): Promise<any> {
  const r = await fetch(`/manifest/${program}.json`)
  // A missing manifest is answered by the dev/static server with index.html (200,
  // text/html) — guard so it surfaces as a clean error, not 'Unexpected token <'.
  const ct = r.headers.get('content-type') || ''
  if (!r.ok || !ct.includes('json')) throw new Error(`manifest not available: ${program}`)
  return r.json()
}

// SCREEN programs (those with a manifest) for the selector — lowercase slugs. The backend's
// /api/terminal/programs lists ALL programs incl. non-screen, so we use the manifest index.
export async function listPrograms(): Promise<string[]> {
  try { const r = await fetch('/manifest/index.json'); if (r.ok) return await r.json() } catch {}
  return []
}

// Transaction-ID -> program-name (uppercase), from EXEC CICS RETURN TRANSID. Drives the entry
// gateway: type a TransID (e.g. CC00) to start its program, like a real CICS terminal.
export async function listTransactions(): Promise<Record<string, string>> {
  try { const r = await fetch('/api/terminal/transactions'); if (r.ok) return await r.json() } catch {}
  return {}
}

// One CICS round-trip. aid = ENTER / PF1..PF24 / PA1..PA3 / CLEAR ; fields = field-name -> value.
export async function execute(programName: string, fields: Record<string, string>, aid = 'ENTER'): Promise<ScreenData> {
  const r = await fetch('/api/terminal/execute', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ programName, aidKey: aid, fields, tabId: tabId() })
  })
  if (!r.ok) throw new Error(`POST /api/terminal/execute -> ${r.status}`)
  const d = await r.json()
  // FE diagnostic logger — enable with localStorage.cobol_debug = '1' (zero overhead otherwise).
  if (isDebugEnabled()) console.log('[CICS]', programName, aid, '->', d.templateName || d.redirect || '(none)', d)
  return {
    screen: d.templateName || '',
    fields: asStrings(d.fields),
    attrs: asStrings(d.attrs),
    cursor: Object.keys(d.cursors || {})[0] || '',
    message: d.message || '',
    redirect: d.redirect || '',
    erase: d.eraseScreen !== false,
    buttons: (d.buttons || []).map((b: any) => ({ aidKey: b.aidKey, label: b.label || b.aidKey }))
  }
}
