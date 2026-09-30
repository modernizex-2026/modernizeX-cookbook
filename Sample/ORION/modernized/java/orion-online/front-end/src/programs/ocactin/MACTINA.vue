<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const trnName = computed<string>(() => props.values.TRNNAME ?? '')
const pgmName = computed<string>(() => props.values.PGMNAME ?? '')
const curDate = computed<string>(() => props.values.CURDATE ?? '')
const curTime = computed<string>(() => props.values.CURTIME ?? '')

const filtField = computed<Field | undefined>(() => allFields.value.find((f: Field) => f.name === 'FILT'))
const filtLabel = computed<string>(() => labelFor(allFields.value, filtField.value ?? ({} as Field)) || 'Filter')
const filtDesc = computed<string>(() => props.values.FDESC ?? '')
const filtNumeric = computed<boolean>(() => (filtField.value ? isNumericField(filtField.value) : false))

const filterHints = ['ALL', 'DELINQUENT', 'OVER-LIMIT', 'DORMANT', 'CLOSED', 'NEW', 'HIGH-UTIL']

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onFiltInput(e: Event) { setVal('FILT', (e.target as HTMLInputElement).value) }
function applyHint(h: string) { setVal('FILT', h); props.send('ENTER') }
function applyFilter() { props.send('ENTER') }

const columns = ['AID', 'AST', 'ABL', 'ALM', 'AAV', 'AUT'] as const
const headers = ['Acct ID', 'St', 'Balance', 'Credit Lim', 'Available', 'Util%']
const rowSuffixes = Array.from({ length: 13 }, (_: unknown, i: number) => String(i + 1))

const rows = computed(() => rowSuffixes
  .map((suffix: string) => ({
    id: val('AID' + suffix),
    st: val('AST' + suffix),
    bal: val('ABL' + suffix),
    lim: val('ALM' + suffix),
    avl: val('AAV' + suffix),
    util: val('AUT' + suffix),
  }))
  .filter(r => r.id.trim() !== ''))

const pageNo = computed<string>(() => props.values.PAGENO ?? '')
const runTot = computed<string>(() => props.values.RUNTOT ?? '')
const pgBal = computed<string>(() => props.values.PGBAL ?? '')
const pgAvl = computed<string>(() => props.values.PGAVL ?? '')

function statusClass(st: string): string {
  const s = st.trim().toUpperCase()
  if (s === 'D' || s === 'X') return 'st-danger'
  if (s === 'O') return 'st-warning'
  return 'st-neutral'
}
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <div class="am-head-title">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item"><dt>Tran</dt><dd>{{ trnName }}</dd></div>
        <div class="meta-item"><dt>Pgm</dt><dd>{{ pgmName }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ curDate }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ curTime }}</dd></div>
      </dl>
    </header>

    <fieldset class="filter-panel">
      <legend>Filter accounts</legend>
      <div class="filter-row">
        <div class="filter-input">
          <label for="FILT">{{ filtLabel }}</label>
          <input
            id="FILT"
            type="text"
            :inputmode="filtNumeric ? 'numeric' : undefined"
            :maxlength="filtField?.length"
            :value="val('FILT')"
            @input="onFiltInput"
            placeholder="e.g. DELINQUENT"
          />
        </div>
        <output v-if="filtDesc.trim()" class="filt-desc">{{ filtDesc }}</output>
        <button type="button" class="btn-primary" @click="applyFilter">
          <svg aria-hidden="true" viewBox="0 0 20 20" width="16" height="16" fill="none">
            <path d="M4 4h12l-4.5 6v5l-3 1.5v-6.5L4 4z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
          </svg>
          Apply
        </button>
      </div>
      <div class="filter-hints" role="group" aria-label="Quick filters">
        <button v-for="h in filterHints" :key="h" type="button" class="hint-chip" @click="applyHint(h)">{{ h }}</button>
      </div>
    </fieldset>

    <div class="table-wrap">
      <table class="acct-table">
        <thead>
          <tr>
            <th v-for="h in headers" :key="h">{{ h }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(r, idx) in rows" :key="idx">
            <td class="mono">{{ r.id }}</td>
            <td><span class="status-badge" :class="statusClass(r.st)">{{ r.st }}</span></td>
            <td class="mono num">{{ r.bal }}</td>
            <td class="mono num">{{ r.lim }}</td>
            <td class="mono num">{{ r.avl }}</td>
            <td class="mono num">{{ r.util }}</td>
          </tr>
          <tr v-if="!rows.length" class="empty-row">
            <td :colspan="columns.length">No accounts match this filter.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="summary-bar">
      <div class="summary-item"><span class="summary-label">Page</span><span class="mono summary-value">{{ pageNo }}</span></div>
      <div class="summary-item"><span class="summary-label">Matched</span><span class="mono summary-value">{{ runTot }}</span></div>
      <div class="summary-item"><span class="summary-label">Portfolio Balance</span><span class="mono summary-value">{{ pgBal }}</span></div>
      <div class="summary-item"><span class="summary-label">Portfolio Available</span><span class="mono summary-value">{{ pgAvl }}</span></div>
    </div>

    <p v-if="message" class="msg" role="status">{{ message }}</p>
    <p v-else class="msg-placeholder" aria-hidden="true"></p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem;
  color: var(--color-text);
}

.am-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.25rem;
}

.am-head-title {
  display: flex;
  align-items: baseline;
  gap: .75rem;
}

.am-head h1 {
  font-size: 1.35rem;
  margin: 0;
}

.prog-tag {
  font: 600 .72rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .3rem .6rem;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin: 0;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: .35rem;
  font-size: .82rem;
}

.meta-item dt {
  color: var(--color-text-muted);
  font-weight: 600;
}

.meta-item dd {
  margin: 0;
  font-family: monospace;
}

.filter-panel {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem 1.1rem;
  margin: 0 0 1.5rem;
}

.filter-panel legend {
  font-size: .85rem;
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 1rem;
}

.filter-input {
  display: flex;
  flex-direction: column;
  gap: .3rem;
  min-width: 220px;
}

.filter-input label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
}

.filter-input input {
  padding: .5rem .6rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 150ms, box-shadow 150ms;
}

.filter-input input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.filt-desc {
  align-self: center;
  color: var(--color-text-muted);
  font-style: italic;
  padding-bottom: .5rem;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  background: var(--color-primary);
  color: var(--c-white);
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  transition: opacity 150ms, transform 150ms;
}

.btn-primary:hover {
  opacity: .9;
}

.btn-primary:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.filter-hints {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  margin-top: .85rem;
}

.hint-chip {
  cursor: pointer;
  padding: .3rem .7rem;
  font-size: .78rem;
  font-weight: 600;
  letter-spacing: .02em;
  color: var(--color-text-muted);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  transition: background 150ms, color 150ms, border-color 150ms;
}

.hint-chip:hover {
  background: var(--c-blue-50);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.hint-chip:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  margin-bottom: 1.25rem;
}

.acct-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 640px;
}

.acct-table th,
.acct-table td {
  padding: .55rem .75rem;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
  font-size: .88rem;
}

.acct-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 600;
  position: sticky;
  top: 0;
}

.acct-table tbody tr:hover {
  background: var(--c-blue-50);
}

.acct-table tbody tr:last-child td {
  border-bottom: none;
}

.mono {
  font-family: monospace;
}

.num {
  text-align: right;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.6rem;
  padding: .1rem .4rem;
  border-radius: 4px;
  font-family: monospace;
  font-weight: 700;
  font-size: .78rem;
}

.st-danger {
  background: var(--color-danger);
  color: var(--c-white);
}

.st-warning {
  background: var(--color-warning);
  color: var(--c-white);
}

.st-neutral {
  background: var(--color-border);
  color: var(--color-text);
}

.empty-row td {
  text-align: center;
  color: var(--color-text-muted);
  padding: 1.5rem;
}

.summary-bar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: .75rem 1.5rem;
  padding: .9rem 1.1rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  margin-bottom: 1rem;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: .2rem;
}

.summary-label {
  font-size: .72rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .04em;
  color: var(--color-text-muted);
}

.summary-value {
  font-size: 1rem;
  font-weight: 600;
}

.msg {
  color: var(--color-danger);
  font-weight: 600;
  min-height: 1.2rem;
  margin: 0 0 .75rem;
}

.msg-placeholder {
  min-height: 1.2rem;
  margin: 0 0 .75rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  border-top: 1px solid var(--color-border);
  padding-top: .9rem;
}

.pf button {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms;
}

.pf button:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.pf button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .filter-input input,
  .btn-primary,
  .hint-chip,
  .pf button {
    transition: none;
  }
}

@media (max-width: 768px) {
  .am-page {
    padding: 1rem;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-primary {
    justify-content: center;
  }
}
</style>
