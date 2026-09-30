<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }
function fieldLen(name: string): number | undefined { return fieldByName(name)?.length }
function numeric(name: string): boolean { const f = fieldByName(name); return f ? isNumericField(f) : false }
function labelOf(name: string): string { const f = fieldByName(name); return f ? labelFor(allFields.value, f) : '' }

const filterFields = ['TMODE', 'FCARD', 'FMERCH', 'FRDATE', 'FTDATE', 'FTYPE', 'FCAT', 'FAMT']

const rows = computed(() => {
  const cols = ['TID', 'TCD', 'TTC', 'TMC', 'TAM', 'TDT'] as const
  const out: Array<{ idx: number; names: Record<string, string> }> = []
  for (let i = 1; i <= 6; i++) {
    const names: Record<string, string> = {}
    for (const c of cols) names[c] = `${c}${i}`
    out.push({ idx: i, names })
  }
  return out.filter(r => val(r.names.TID).trim() !== '')
})

const headers = ['Tran ID', 'Card', 'Ty/Cat', 'Merchant', 'Amount', 'Date']

const summaryRows = computed(() => ([
  { label: 'Matches', count: val('MCNT'), sum: null as string | null },
  { label: 'Purchases', count: val('PCNT'), sum: val('PSUM') },
  { label: 'Payments', count: val('YCNT'), sum: val('YSUM') },
  { label: 'Fees', count: val('FCNT'), sum: val('FSUM') },
  { label: 'Interest', count: val('ICNT'), sum: val('ISUM') },
]).filter(r => r.count.trim() !== '' || (r.sum ?? '').trim() !== ''))

const topTran = computed(() => ({ id: val('XID'), amt: val('XAMT') }))
const hasTopTran = computed(() => topTran.value.id.trim() !== '' || topTran.value.amt.trim() !== '')
const netTotal = computed(() => val('MTOT'))
</script>

<template>
  <section class="tx-page">
    <header class="tx-meta">
      <h1>{{ title }}</h1>
      <dl class="meta-strip">
        <div><dt>Tran</dt><dd>{{ val('TRNNAME') }}</dd></div>
        <div><dt>Pgm</dt><dd>{{ val('PGMNAME') }}</dd></div>
        <div><dt>Date</dt><dd>{{ val('CURDATE') }}</dd></div>
        <div><dt>Time</dt><dd>{{ val('CURTIME') }}</dd></div>
      </dl>
    </header>

    <fieldset class="tx-filters">
      <legend>Filter transactions</legend>
      <div class="filter-grid">
        <div class="tx-field mode-field">
          <label for="TMODE">Mode</label>
          <input id="TMODE" type="text" maxlength="1" :value="val('TMODE')" @input="onInput('TMODE', $event)" />
          <span class="hint">C=Card&nbsp;D=Date&nbsp;M=Merch&nbsp;T=Type&nbsp;A=Amt</span>
        </div>
        <div class="tx-field">
          <label for="FCARD">Card</label>
          <input id="FCARD" type="text" :maxlength="fieldLen('FCARD')" :value="val('FCARD')" @input="onInput('FCARD', $event)" />
        </div>
        <div class="tx-field">
          <label for="FMERCH">Merch ID</label>
          <input id="FMERCH" type="text" :maxlength="fieldLen('FMERCH')" :value="val('FMERCH')" @input="onInput('FMERCH', $event)" />
        </div>
        <div class="tx-field">
          <label for="FRDATE">Date From</label>
          <input id="FRDATE" type="text" :maxlength="fieldLen('FRDATE')" :value="val('FRDATE')" @input="onInput('FRDATE', $event)" />
        </div>
        <div class="tx-field">
          <label for="FTDATE">To</label>
          <input id="FTDATE" type="text" :maxlength="fieldLen('FTDATE')" :value="val('FTDATE')" @input="onInput('FTDATE', $event)" />
        </div>
        <div class="tx-field">
          <label for="FTYPE">Type</label>
          <input id="FTYPE" type="text" :maxlength="fieldLen('FTYPE')" :value="val('FTYPE')" @input="onInput('FTYPE', $event)" />
        </div>
        <div class="tx-field">
          <label for="FCAT">Cat</label>
          <input id="FCAT" type="text" :maxlength="fieldLen('FCAT')" :value="val('FCAT')" @input="onInput('FCAT', $event)" />
        </div>
        <div class="tx-field">
          <label for="FAMT">Amount &gt;=</label>
          <input id="FAMT" type="text" inputmode="numeric" :maxlength="fieldLen('FAMT')" :value="val('FAMT')" @input="onInput('FAMT', $event)" />
        </div>
      </div>
    </fieldset>

    <div class="tx-table-wrap">
      <table class="tx-table" v-if="rows.length">
        <caption class="sr-only">Transaction list</caption>
        <thead>
          <tr><th v-for="h in headers" :key="h">{{ h }}</th></tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.idx">
            <td class="mono">{{ val(r.names.TID) }}</td>
            <td class="mono">{{ val(r.names.TCD) }}</td>
            <td class="mono">{{ val(r.names.TTC) }}</td>
            <td>{{ val(r.names.TMC) }}</td>
            <td class="mono num">{{ val(r.names.TAM) }}</td>
            <td class="mono">{{ val(r.names.TDT) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-state">No transactions match the current filter.</p>
    </div>

    <div class="tx-summary" v-if="summaryRows.length || hasTopTran">
      <div class="summary-grid">
        <div class="summary-card" v-for="s in summaryRows" :key="s.label">
          <span class="s-label">{{ s.label }}</span>
          <span class="s-count mono">{{ s.count }}</span>
          <span v-if="s.sum" class="s-sum mono">{{ s.sum }}</span>
        </div>
        <div class="summary-card total" v-if="netTotal.trim() !== ''">
          <span class="s-label">Net Total</span>
          <span class="s-sum mono big">{{ netTotal }}</span>
        </div>
        <div class="summary-card" v-if="hasTopTran">
          <span class="s-label">Top Tran</span>
          <span class="s-count mono">{{ topTran.id }}</span>
          <span class="s-sum mono">{{ topTran.amt }}</span>
        </div>
      </div>
    </div>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>
    <p v-else class="msg-placeholder" aria-hidden="true">&nbsp;</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button" class="pf-btn" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.tx-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.25rem 1rem 2rem;
  color: var(--color-text);
}

.tx-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: 1.25rem;
}
.tx-meta h1 { font-size: 1.35rem; margin: 0; font-weight: 700; }

.meta-strip {
  display: flex;
  gap: 1.25rem;
  margin: 0;
  font-size: .8rem;
}
.meta-strip > div { display: flex; gap: .35rem; align-items: baseline; }
.meta-strip dt { color: var(--color-text-muted); font-weight: 600; }
.meta-strip dd { margin: 0; font-family: monospace; color: var(--color-text); }

.tx-filters {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.1rem 1.1rem;
  margin-bottom: 1.5rem;
  background: var(--color-surface);
}
.tx-filters legend {
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
  font-size: .85rem;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: .85rem 1rem;
}
.tx-field { display: flex; flex-direction: column; gap: .3rem; }
.tx-field label { font-size: .78rem; font-weight: 600; color: var(--color-text-muted); }
.tx-field input {
  padding: .5rem .6rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color .15s, box-shadow .15s;
}
.tx-field input:hover { border-color: var(--color-primary); }
.tx-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.mode-field .hint { font-size: .68rem; color: var(--color-text-muted); }
@media (prefers-reduced-motion: reduce) {
  .tx-field input { transition: none; }
}

.tx-table-wrap { overflow-x: auto; margin-bottom: 1.5rem; }
.tx-table { width: 100%; border-collapse: collapse; font-size: .9rem; }
.tx-table th, .tx-table td { border: 1px solid var(--color-border); padding: .5rem .6rem; text-align: left; }
.tx-table thead th { background: var(--c-blue-50); color: var(--color-primary); font-weight: 600; }
.tx-table tbody tr:hover { background: var(--c-blue-50); }
.mono { font-family: monospace; }
.num { text-align: right; }

.empty-state {
  color: var(--color-text-muted);
  padding: 1.5rem;
  text-align: center;
  border: 1px dashed var(--color-border);
  border-radius: 8px;
}

.tx-summary { margin-bottom: 1.25rem; }
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: .75rem;
}
.summary-card {
  display: flex;
  flex-direction: column;
  gap: .25rem;
  padding: .75rem .9rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
}
.summary-card.total { border-color: var(--color-primary); }
.s-label { font-size: .75rem; font-weight: 600; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: .02em; }
.s-count { font-size: 1rem; font-weight: 600; }
.s-sum { font-size: .95rem; color: var(--color-success); }
.s-sum.big { font-size: 1.2rem; font-weight: 700; }

.msg {
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem;
  min-height: 1.2em;
}
.msg-placeholder { min-height: 1.2em; margin: 0 0 1rem; }

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: .9rem;
}
.pf-btn {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  font-weight: 500;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .18s, border-color .18s, color .18s;
}
.pf-btn:first-child {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.pf-btn:hover { border-color: var(--color-primary); }
.pf-btn:first-child:hover { opacity: .9; }
.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
}

.sr-only {
  position: absolute;
  width: 1px; height: 1px;
  padding: 0; margin: -1px;
  overflow: hidden;
  clip: rect(0,0,0,0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 768px) {
  .tx-meta { flex-direction: column; align-items: flex-start; }
  .filter-grid { grid-template-columns: 1fr; }
  .summary-grid { grid-template-columns: 1fr; }
}
</style>
