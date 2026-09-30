<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick, provide, type CSSProperties } from 'vue'
import { loadManifest, execute, type ScreenData } from '../api/client'
import { screenOverrides } from '../programs'
import { TYPED_VALUES_KEY, SCREEN_CONTEXT_KEY, type ScreenContext, type ScreenOverrideProps } from '../types/screenOverride'

const props = defineProps<{ program: string }>()
// 'exit' = CICS trả về transaction gateway (redirect ENTRY_PATH) — App quay về Entry, không load manifest
const emit = defineEmits<{ (e: 'exit'): void }>()
const activeProgram = ref(props.program)   // changes on CICS XCTL / redirect
const manifest = ref<any>(null)
const screenName = ref<string>('')
const values = ref<Record<string, string>>({})
const dynAttrs = ref<Record<string, string>>({})  // backend attribute overrides (per round-trip)
const cursor = ref<string>('')
const message = ref<string>('')
const error = ref<string>('')
const busy = ref(false)

const sc = computed<any>(() => manifest.value?.screens?.[screenName.value] ?? null)
const fields = computed<any[]>(() => sc.value?.fields ?? [])
// Typed record-lists (geometry-detected in the manifest): each is rendered as an HTML
// table instead of loose positioned cells. Cells in a list are excluded from the grid.
const recordLists = computed<any[]>(() => sc.value?.recordLists ?? [])
const fieldByName = computed<Record<string, any>>(() =>
  Object.fromEntries(fields.value.map(f => [f.name, f])))
const listCellNames = computed<Set<string>>(() => {
  const s = new Set<string>()
  for (const rl of recordLists.value) for (const row of rl.rows) for (const k of rl.columns) {
    const n = row[k]; if (n) s.add(n)
  }
  return s
})
function bboxOf(rl: any) {
  let minR = 99, maxR = 0, minC = 99, maxC = 0
  for (const row of rl.rows) for (const k of rl.columns) {
    const f = fieldByName.value[row[k]]; if (!f) continue
    minR = Math.min(minR, f.row); maxR = Math.max(maxR, f.row)
    minC = Math.min(minC, f.col); maxC = Math.max(maxC, f.col + Math.max(f.length, 1))
  }
  return { minR, maxR, minC, maxC }
}
const regionBoxes = computed(() => recordLists.value.map(bboxOf))
// Regular grid fields = all EXCEPT (a) list cells and (b) the list's column-header LITERALS
// (unlabeled text on the 1-2 rows directly above a region, within its columns) — those are
// redundant with the table's own header row, so suppress them to avoid a duplicate header.
const regularFields = computed<any[]>(() => fields.value.filter(f => {
  if (listCellNames.value.has(f.name)) return false
  if (!f.name) for (const b of regionBoxes.value)
    if (f.row >= b.minR - 2 && f.row < b.minR && f.col >= b.minC - 2 && f.col < b.maxC) return false
  return true
}))
// Place the table over its region's bounding box in the 80x24 grid (+1 row for the header).
function tableStyle(rl: any): CSSProperties {
  const b = bboxOf(rl)
  return { gridRow: b.minR + ' / ' + (b.maxR + 2), gridColumn: b.minC + ' / ' + b.maxC }
}

function cell(f: any): CSSProperties {
  // 3270 grid placement (1-based row/col), matching the legacy 80x24 layout.
  return { gridRow: String(f.row), gridColumn: f.col + ' / span ' + Math.max(f.length, 1) }
}

// BMS attributes → CSS classes: static (manifest attrb/color/hilight) + dynamic (backend attrs).
function fieldClass(f: any): string {
  const c: string[] = []
  const a = (f.attrb || '').toUpperCase()
  if (a.includes('BRT')) c.push('attr-bright'); else if (a.includes('DRK')) c.push('attr-dark')
  const col = (f.color || '').toUpperCase()
  if (col && col !== 'DEFAULT' && col !== 'NEUTRAL') c.push('attr-' + col.toLowerCase())
  const hl = (f.hilight || '').toUpperCase()
  if (hl === 'REVERSE') c.push('attr-reverse'); else if (hl === 'BLINK') c.push('attr-blink'); else if (hl === 'UNDERLINE') c.push('attr-underline')
  if (dynAttrs.value[f.name]) c.push(dynAttrs.value[f.name])
  return c.join(' ')
}
function isDark(f: any) { return (f.attrb || '').toUpperCase().includes('DRK') }
// Numeric input field: BMS NUM attribute (numeric shift) or an all-9/Z input PICTURE.
// Drives client-side validation — digits only, mirroring the 3270 NUM field.
function isNumeric(f: any) {
  return (f.attrb || '').toUpperCase().includes('NUM') || (typeof f.picIn === 'string' && /^[9Z]+$/i.test(f.picIn))
}

function programFromRedirect(r: string): string | null {
  const m = /\/terminal\/([A-Za-z0-9]+)/.exec(r || '')
  return m ? m[1].toLowerCase() : null
}

function applyScreen(d: ScreenData) {
  // CICS XCTL / navigation: backend asks to switch program → load it fresh.
  // CICS exit về transaction gateway (TerminalController.ENTRY_PATH — sentinel, KHÔNG phải program:
  // không có manifest 'entry'; xử lý như program sẽ lỗi "manifest not available: entry" — bug PF3).
  if (d.redirect === '/terminal/entry') { emit('exit'); return }
  const target = programFromRedirect(d.redirect)
  if (target && target !== activeProgram.value) { activeProgram.value = target; loadProgram(target); return }
  const keys = Object.keys(manifest.value?.screens ?? {})
  const t = (d.screen || '').toUpperCase()
  const match = keys.find(k => k.toUpperCase() === t) || keys.find(k => t && t.includes(k.toUpperCase()))
  const prevScreen = screenName.value
  if (match) screenName.value = match
  // SEND MAP DATAONLY (no ERASE) keeps the prior 3270 buffer: merge the fields the program
  // re-sent over the displayed ones, so fields it did not touch this turn (e.g. the list
  // rows behind a PF7/PF8 boundary message) are retained instead of blanking out.
  if (!d.erase && match && match === prevScreen) {
    values.value = { ...values.value, ...(d.fields || {}) }
  } else {
    values.value = d.fields || {}
  }
  dynAttrs.value = d.attrs || {}
  cursor.value = d.cursor || ''
  message.value = d.message || ''
  // Per-screen dynamic PF bar (CICS: each program declares its own AID keys via
  // getButtonDefs) — adopt the backend's list; keep the previous bar when absent.
  if (d.buttons && d.buttons.length) pfKeys.value = d.buttons
  focusCursor()
}

async function focusCursor() {
  await nextTick()
  const root = document.getElementById('screen'); if (!root) return
  let el = cursor.value ? root.querySelector<HTMLInputElement>(`[name="${cursor.value}"]`) : null
  if (!el) el = root.querySelector<HTMLInputElement>('input')
  el?.focus()
}

async function loadProgram(slug: string) {
  error.value = ''; message.value = ''
  try {
    manifest.value = await loadManifest(slug)
    screenName.value = Object.keys(manifest.value.screens)[0] || ''
    const d = await execute(manifest.value.programId, {}, 'ENTER').catch(() => null as ScreenData | null)
    if (d) applyScreen(d)
  } catch (e: any) { error.value = String(e?.message ?? e) }
}

async function send(aid = 'ENTER') {
  if (busy.value) return
  busy.value = true
  const inputs: Record<string, string> = {}
  for (const f of fields.value) if (f.input) inputs[f.name] = values.value[f.name] ?? ''
  try { applyScreen(await execute(manifest.value.programId, inputs, aid)) }
  catch (e: any) { error.value = String(e?.message ?? e) }
  finally { busy.value = false }
}

// AID key map (CICS): Enter→ENTER, F1..F12→PF1..PF12, Esc→CLEAR.
// B3 (R9): thêm tổ hợp Shift+F1..F12 → PF13..PF24 — đúng quy ước bàn phím 3270 (24 phím PF
// trên 12 phím vật lý). Trước đây nhánh online chỉ nhận 12 phím nên chương trình dùng PF13+
// không có cách bấm; nhánh web đã có bảng modifier từ lâu (aidCodes.MODIFIER_KEY_AID_MAP).
const AID: Record<string, string> = { Enter: 'ENTER', Escape: 'CLEAR' }
for (let i = 1; i <= 12; i++) AID['F' + i] = 'PF' + i
for (let i = 1; i <= 12; i++) AID['Shift+F' + i] = 'PF' + (i + 12)
function onKey(e: KeyboardEvent) {
  // Shift+Fn phải tra trước Fn, nếu không Shift+F3 sẽ bị hiểu thành PF3.
  const aid = (e.shiftKey ? AID['Shift+' + e.key] : undefined) ?? AID[e.key]
  if (aid) { e.preventDefault(); send(aid) }
}
// Autoskip: jump to next input when a field is filled to its length.
function onInput(e: Event, f: any) {
  const t = e.target as HTMLInputElement
  // Numeric field: strip any non-digit the user typed or pasted (3270 NUM shift).
  if (isNumeric(f)) {
    const clean = t.value.replace(/[^0-9]/g, '')
    if (clean !== t.value) { t.value = clean; values.value[f.name] = clean }
  }
  // Autoskip: jump to next input when filled to its length.
  if (f.length && t.value.length >= f.length) {
    const all = Array.from(document.querySelectorAll<HTMLInputElement>('#screen input'))
    const i = all.indexOf(t); if (i >= 0 && i < all.length - 1) all[i + 1].focus()
  }
}

// PF bar: per-screen dynamic buttons from the backend (getButtonDefs per program).
// Generic fallback list is only shown until the first response arrives.
const pfKeys = ref(['ENTER', 'PF1', 'PF3', 'PF4', 'PF7', 'PF8', 'PF12'].map(k => ({ aidKey: k, label: k })))

// ── Modern override dispatch ──────────────────────────────────────────────
// Nếu screen hiện tại có SFC modern đăng ký trong screenOverrides → render nó thay grid faithful.
// Registry rỗng (mặc định) → override = undefined → luôn dùng grid (hành vi KHÔNG đổi).
provide(TYPED_VALUES_KEY, values)
// R9.2 — hợp đồng CHUNG hai nhánh (core/types/screenContract.ts): SFC hiện đại đọc qua
// useScreenContext() nên viết một lần chạy được cả web lẫn online. Getter để giữ phản ứng;
// props riêng của nhánh (overrideCtx bên dưới) GIỮ NGUYÊN cho SFC đã bàn giao.
provide(SCREEN_CONTEXT_KEY, {
  branch: 'online',
  get program() { return activeProgram.value },
  get screenName() { return screenName.value },
  get screen() { return sc.value },
  get values() { return values.value },
  get message() { return message.value },
  get buttons() { return pfKeys.value },
  send,
  // Dùng lại đúng bảng AID của listener bàn phím bên dưới (Enter→ENTER, F3→PF3…).
  sendKey: (key: string) => { const aid = AID[key]; if (aid) send(aid) },
  setValue: (name: string, value: string) => { values.value[name] = value },
  isActive: (name: string) => !!fieldByName.value[name]?.input,
  // Lối thoát cho đặc thù CICS: attribute override + cursor của vòng round-trip hiện tại.
  get native() { return { attrs: dynAttrs.value, cursor: cursor.value } },
} as ScreenContext)
const override = computed<any>(() => screenOverrides[(screenName.value || '').toUpperCase()])
const overrideCtx = computed<ScreenOverrideProps>(() => ({
  program: props.program, screenName: screenName.value, screen: sc.value,
  values: values.value, buttons: pfKeys.value, message: message.value, send,
}))
watch(() => props.program, p => { activeProgram.value = p; loadProgram(p) })
onMounted(() => { window.addEventListener('keydown', onKey); loadProgram(props.program) })
onUnmounted(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <div class="screen-wrap">
    <div v-if="error" class="err">{{ error }}</div>
    <!-- Modern override SFC (nếu screen được modernize); ngược lại → grid faithful bên dưới. -->
    <component v-if="override" :is="override" v-bind="overrideCtx" />
    <template v-else>
    <div id="screen" class="screen">
      <template v-for="f in regularFields" :key="f.name + '_' + f.row + '_' + f.col">
        <input v-if="f.input" :type="isDark(f) ? 'password' : 'text'" :name="f.name" :class="['fld','in',fieldClass(f)]"
               :style="cell(f)" :maxlength="f.length" :inputmode="isNumeric(f) ? 'numeric' : undefined"
               v-model="values[f.name]" :title="f.name" @input="onInput($event, f)" />
        <span v-else :class="['fld','out',fieldClass(f)]" :style="cell(f)" :title="f.name">{{ values[f.name] ?? f.initial ?? '' }}</span>
      </template>
      <table v-for="(rl, ri) in recordLists" :key="'rl' + ri" class="rec-list" :style="tableStyle(rl)">
        <thead><tr><th v-for="(k, ci) in rl.columns" :key="k">{{ (rl.headers && rl.headers[ci]) || k }}</th></tr></thead>
        <tbody>
          <tr v-for="(row, idx) in rl.rows" :key="idx">
            <td v-for="k in rl.columns" :key="k">
              <input v-if="fieldByName[row[k]] && fieldByName[row[k]].input" :name="row[k]"
                     :maxlength="fieldByName[row[k]].length" v-model="values[row[k]]" class="rec-in" />
              <span v-else>{{ values[row[k]] ?? '' }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="msg" v-if="message">{{ message }}</div>
    <div class="pf">
      <button v-for="b in pfKeys" :key="b.aidKey" @click="send(b.aidKey)" :disabled="busy" :title="b.aidKey">{{ b.label }}</button>
    </div>
    </template>
  </div>
</template>
