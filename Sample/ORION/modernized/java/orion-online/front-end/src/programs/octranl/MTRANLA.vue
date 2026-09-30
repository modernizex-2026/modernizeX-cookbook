<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }

const cardnumField = computed<Field | undefined>(() => fieldByName('CARDNUM'))
const cardnumLabel = computed<string>(() => labelFor(allFields.value, cardnumField.value as Field) || 'Card Num')

interface TranRow { trn: string; amt: string; typ: string }
const rowDefs = [
  { trn: 'TRN1', amt: 'AMT1', typ: 'TYP1' },
  { trn: 'TRN2', amt: 'AMT2', typ: 'TYP2' },
  { trn: 'TRN3', amt: 'AMT3', typ: 'TYP3' },
  { trn: 'TRN4', amt: 'AMT4', typ: 'TYP4' },
  { trn: 'TRN5', amt: 'AMT5', typ: 'TYP5' },
]
const rows = computed<TranRow[]>(() =>
  rowDefs.map(r => ({ trn: val(r.trn), amt: val(r.amt), typ: val(r.typ) }))
    .filter(r => r.trn.trim() !== '' || r.amt.trim() !== '' || r.typ.trim() !== ''))
</script>

<template>
  <section class="orn-page">
    <header class="orn-meta">
      <div class="orn-meta-left">
        <span class="orn-meta-item"><span class="orn-meta-label">Tran:</span>{{ props.values['TRNNAME'] || '' }}</span>
        <span class="orn-meta-item"><span class="orn-meta-label">Pgm:</span>{{ props.values['PGMNAME'] || '' }}</span>
      </div>
      <h1 class="orn-title">{{ title }}</h1>
      <div class="orn-meta-right">
        <span class="orn-meta-item"><span class="orn-meta-label">Date:</span>{{ props.values['CURDATE'] || '' }}</span>
        <span class="orn-meta-item"><span class="orn-meta-label">Time:</span>{{ props.values['CURTIME'] || '' }}</span>
      </div>
    </header>

    <fieldset class="orn-search">
      <legend>Lookup</legend>
      <div class="orn-field">
        <label for="CARDNUM">{{ cardnumLabel }}</label>
        <input
          id="CARDNUM"
          type="text"
          inputmode="numeric"
          :maxlength="cardnumField?.length"
          :value="val('CARDNUM')"
          @input="onInput('CARDNUM', $event)"
        />
      </div>
    </fieldset>

    <fieldset class="orn-list-section">
      <legend>Transactions</legend>
      <div class="orn-table-wrap">
        <table class="orn-table">
          <thead>
            <tr>
              <th>Tran ID</th>
              <th class="orn-num-col">Amount</th>
              <th>Type</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in rows" :key="idx">
              <td class="orn-mono">{{ row.trn }}</td>
              <td class="orn-mono orn-num-col">{{ row.amt }}</td>
              <td class="orn-mono">{{ row.typ }}</td>
            </tr>
            <tr v-if="!rows.length">
              <td colspan="3" class="orn-empty">No transactions to display.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </fieldset>

    <p class="orn-msg" role="status">{{ message }}</p>

    <nav class="orn-pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="orn-pf-btn"
        :class="{ 'orn-pf-primary': b.aidKey === 'ENTER' }"
        @click="send(b.aidKey)"
        :title="b.aidKey"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.orn-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.orn-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: 0.75rem;
}

.orn-meta-left,
.orn-meta-right {
  display: flex;
  gap: 1rem;
  font: 500 0.75rem/1.2 monospace;
  color: var(--color-text-muted);
}

.orn-meta-label {
  color: var(--color-primary);
  font-weight: 700;
  margin-right: 0.25rem;
}

.orn-title {
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-text);
  order: 2;
}

.orn-meta-left { order: 1; }
.orn-meta-right { order: 3; }

.orn-search {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem 1.25rem;
  background: var(--color-surface);
}

.orn-search legend,
.orn-list-section legend {
  padding: 0 0.4rem;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.orn-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  max-width: 320px;
}

.orn-field label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-text);
}

.orn-field input {
  padding: 0.55rem 0.65rem;
  font: 500 0.95rem/1.2 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 150ms, box-shadow 150ms;
}

.orn-field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}

@media (prefers-reduced-motion: reduce) {
  .orn-field input { transition: none; }
}

.orn-list-section {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem 1.25rem;
  background: var(--color-surface);
}

.orn-table-wrap {
  overflow-x: auto;
}

.orn-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.orn-table thead th {
  text-align: left;
  padding: 0.55rem 0.75rem;
  background: var(--c-blue-50);
  color: var(--color-primary);
  border-bottom: 2px solid var(--color-border);
  font-weight: 700;
}

.orn-table tbody td {
  padding: 0.55rem 0.75rem;
  border-bottom: 1px solid var(--color-border);
}

.orn-mono {
  font-family: monospace;
}

.orn-num-col {
  text-align: right;
}

.orn-empty {
  text-align: center;
  color: var(--color-text-muted);
  padding: 1.25rem 0.75rem;
}

.orn-msg {
  min-height: 1.2rem;
  margin: 0;
  color: var(--color-danger);
  font-weight: 600;
}

.orn-pf {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 0.9rem;
}

.orn-pf-btn {
  cursor: pointer;
  padding: 0.5rem 1.1rem;
  font: 600 0.9rem/1.2 inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms, color 150ms;
}

.orn-pf-btn:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.orn-pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.orn-pf-primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}

.orn-pf-primary:hover {
  filter: brightness(1.08);
}

@media (prefers-reduced-motion: reduce) {
  .orn-pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .orn-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  .orn-title { order: 0; }
  .orn-meta-left { order: 1; }
  .orn-meta-right { order: 2; }
  .orn-field { max-width: 100%; }
}
</style>
