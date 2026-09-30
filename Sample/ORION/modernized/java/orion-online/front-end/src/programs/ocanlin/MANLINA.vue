<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }
function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const modeField = computed<Field | undefined>(() => fieldByName('ANMODE'))
const modeLabel = computed<string>(() => modeField.value ? labelFor(allFields.value, modeField.value) : 'Mode')

const heading = computed<string>(() => val('ANHEAD'))
const total1 = computed<string>(() => val('ANTOT1'))
const total2 = computed<string>(() => val('ANTOT2'))

const headers = computed<string[]>(() => props.screen?.recordLists?.[0]?.headers ?? ['Key', 'Info', 'Count', 'Amount', 'Value'])
const columns = computed<string[]>(() => props.screen?.recordLists?.[0]?.columns ?? ['ANK', 'ANI', 'ANC', 'ANA', 'ANV'])
const rawRows = computed<Array<Record<string, string>>>(() => props.screen?.recordLists?.[0]?.rows ?? [])

const rows = computed(() => rawRows.value
  .map((r: Record<string, string>) => ({
    key: val(r.ANK),
    info: val(r.ANI),
    count: val(r.ANC),
    amount: val(r.ANA),
    value: val(r.ANV),
  }))
  .filter((r) => r.key.trim() !== '' || r.info.trim() !== ''))

function isMoneyColumn(idx: number): boolean {
  return columns.value[idx] === 'ANA' || columns.value[idx] === 'ANV'
}
</script>

<template>
  <section class="manlina">
    <header class="meta-strip">
      <span class="meta-item"><span class="meta-label">Tran</span><span class="meta-value">{{ val('TRNNAME') }}</span></span>
      <span class="meta-item"><span class="meta-label">Pgm</span><span class="meta-value">{{ val('PGMNAME') }}</span></span>
      <span class="meta-item meta-spacer" aria-hidden="true"></span>
      <span class="meta-item"><span class="meta-label">Date</span><span class="meta-value">{{ val('CURDATE') }}</span></span>
      <span class="meta-item"><span class="meta-label">Time</span><span class="meta-value">{{ val('CURTIME') }}</span></span>
    </header>

    <div class="title-row">
      <h1>{{ title }}</h1>
      <span class="prog-tag">{{ program }}</span>
    </div>

    <fieldset class="mode-panel">
      <legend>Line selection</legend>
      <div class="mode-row">
        <label v-if="modeField" for="ANMODE">{{ modeLabel }}</label>
        <input
          v-if="modeField"
          id="ANMODE"
          type="text"
          :maxlength="modeField.length"
          :value="val('ANMODE')"
          @input="onInput('ANMODE', $event)"
        />
        <span class="mode-hint">RW=Rewards&nbsp;&nbsp;FR=Fraud&nbsp;&nbsp;GL=GL&nbsp;&nbsp;RC=Recon</span>
      </div>
    </fieldset>

    <p v-if="heading" class="section-heading">{{ heading }}</p>

    <div class="table-wrap">
      <table class="lines-table">
        <thead>
          <tr>
            <th v-for="(h, i) in headers" :key="'h' + i" :class="{ 'num-col': isMoneyColumn(i) || columns[i] === 'ANC' }">{{ h }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in rows" :key="idx">
            <td>{{ row.key }}</td>
            <td>{{ row.info }}</td>
            <td class="num-col">{{ row.count }}</td>
            <td class="num-col mono">{{ row.amount }}</td>
            <td class="num-col mono">{{ row.value }}</td>
          </tr>
          <tr v-if="!rows.length" class="empty-row">
            <td colspan="5">No lines to display.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="totals" v-if="total1 || total2">
      <p v-if="total1" class="total-line">{{ total1 }}</p>
      <p v-if="total2" class="total-line">{{ total2 }}</p>
    </div>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>

    <nav class="pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        @click="send(b.aidKey)"
        :title="b.aidKey"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.manlina {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.25rem 1.5rem 2rem;
  color: var(--color-text);
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  align-items: baseline;
  font-size: 0.8rem;
  color: var(--color-text-muted);
  padding-bottom: 0.6rem;
  margin-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}
.meta-spacer {
  flex: 1;
}
.meta-item {
  display: inline-flex;
  gap: 0.35rem;
}
.meta-label {
  font-weight: 600;
  color: var(--color-primary);
}
.meta-value {
  font-family: monospace;
}

.title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
}
.title-row h1 {
  font-size: 1.4rem;
  margin: 0;
  color: var(--color-text);
}
.prog-tag {
  font: 600 0.75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: 0.3rem 0.6rem;
  white-space: nowrap;
}

.mode-panel {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.9rem 1rem 1rem;
  margin-bottom: 1.25rem;
}
.mode-panel legend {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 0.35rem;
}
.mode-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}
.mode-row label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text);
}
.mode-row input {
  width: 8rem;
  padding: 0.45rem 0.55rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 150ms, box-shadow 150ms;
}
.mode-row input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}
.mode-hint {
  font-size: 0.78rem;
  color: var(--color-text-muted);
}

.section-heading {
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 0.75rem;
}

.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  margin-bottom: 1.25rem;
}
.lines-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 640px;
}
.lines-table th,
.lines-table td {
  padding: 0.55rem 0.75rem;
  text-align: left;
  font-size: 0.9rem;
  border-bottom: 1px solid var(--color-border);
}
.lines-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 600;
  white-space: nowrap;
}
.lines-table tbody tr:hover {
  background: var(--c-blue-50);
}
.lines-table tbody tr:last-child td {
  border-bottom: none;
}
.num-col {
  text-align: right;
}
.mono {
  font-family: monospace;
}
.empty-row td {
  text-align: center;
  color: var(--color-text-muted);
  font-style: italic;
}

.totals {
  margin-bottom: 1.25rem;
}
.total-line {
  font-weight: 600;
  color: var(--color-success);
  font-family: monospace;
  margin: 0.2rem 0;
}

.msg {
  color: var(--color-danger);
  font-weight: 600;
  min-height: 1.2rem;
  margin-bottom: 1rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  border-top: 1px solid var(--color-border);
  padding-top: 0.9rem;
}
.pf-btn {
  cursor: pointer;
  padding: 0.5rem 1rem;
  font: inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms;
}
.pf-btn:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}
.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.pf-btn.primary:hover {
  opacity: 0.9;
}

@media (prefers-reduced-motion: reduce) {
  .mode-row input,
  .pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .manlina {
    padding: 1rem;
  }
  .meta-strip {
    gap: 0.75rem;
  }
  .meta-spacer {
    display: none;
  }
  .title-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .lines-table {
    min-width: 560px;
  }
}
</style>
