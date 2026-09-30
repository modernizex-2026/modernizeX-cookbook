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

const custIdField = computed<Field | undefined>(() => fieldByName('CUSTID'))
const custIdLabel = computed<string>(() => custIdField.value ? labelFor(allFields.value, custIdField.value) : 'Cust ID')
const custIdNumeric = computed<boolean>(() => custIdField.value ? isNumericField(custIdField.value) : false)

interface DisplayRow { name: string; label: string }
const displayRows: DisplayRow[] = [
  { name: 'CUNAME', label: 'Name' },
  { name: 'CUADDR', label: 'Address' },
  { name: 'CUCITY', label: 'City' },
  { name: 'CUPHONE', label: 'Phone' },
  { name: 'CUFICO', label: 'FICO' },
]
const visibleRows = computed(() => displayRows.filter(r => val(r.name).trim() !== ''))

function submit(): void { props.send('ENTER') }
</script>

<template>
  <section class="cust-page">
    <header class="cust-head">
      <div class="cust-head-main">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item"><dt>Tran</dt><dd>{{ values.TRNNAME }}</dd></div>
        <div class="meta-item"><dt>Pgm</dt><dd>{{ values.PGMNAME }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ values.CURDATE }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ values.CURTIME }}</dd></div>
      </dl>
    </header>

    <fieldset class="lookup-panel">
      <legend>Customer Lookup</legend>
      <div class="lookup-row">
        <label for="CUSTID">{{ custIdLabel }}</label>
        <input
          id="CUSTID"
          type="text"
          :inputmode="custIdNumeric ? 'numeric' : undefined"
          :maxlength="custIdField?.length"
          :value="val('CUSTID')"
          @input="onInput('CUSTID', $event)"
        />
        <button type="button" class="btn-primary" @click="submit">
          <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          Look Up
        </button>
      </div>
    </fieldset>

    <fieldset v-if="visibleRows.length" class="details-panel">
      <legend>Customer Details</legend>
      <dl class="details-grid">
        <div v-for="row in visibleRows" :key="row.name" class="detail-item" :class="{ mono: row.name === 'CUFICO' || row.name === 'CUPHONE' }">
          <dt>{{ row.label }}</dt>
          <dd>{{ val(row.name) }}</dd>
        </div>
      </dl>
    </fieldset>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>
    <p v-else class="msg-placeholder" aria-hidden="true"></p>

    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" class="pf-btn" :title="b.aidKey" @click="send(b.aidKey)">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.cust-page {
  max-width: var(--screen-max-width, 900px);
  margin: 2rem auto;
  padding: 0 1rem;
  color: var(--color-text);
}

.cust-head {
  display: flex;
  flex-direction: column;
  gap: .6rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.cust-head-main {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
}
.cust-head-main h1 { font-size: 1.4rem; margin: 0; }
.prog-tag {
  font: 600 .75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .3rem .6rem;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  margin: 0;
}
.meta-item { display: flex; align-items: baseline; gap: .35rem; }
.meta-item dt {
  font-size: .75rem;
  font-weight: 600;
  color: var(--color-text-muted);
}
.meta-item dd {
  margin: 0;
  font: 500 .85rem/1 monospace;
  color: var(--color-text);
}

fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0 0 1.25rem;
}
legend {
  font-size: .8rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.lookup-row {
  display: flex;
  align-items: flex-end;
  gap: .75rem;
  flex-wrap: wrap;
}
.lookup-row label {
  display: block;
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: .3rem;
}
.lookup-row input {
  padding: .5rem .6rem;
  font: 500 .95rem/1 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  width: 12rem;
  transition: border-color 150ms, box-shadow 150ms;
}
.lookup-row input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: .45rem;
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: 600 .9rem/1 inherit;
  color: var(--c-white);
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  transition: filter 150ms, box-shadow 150ms;
}
.btn-primary:hover { filter: brightness(0.92); }
.btn-primary:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem 1.5rem;
  margin: 0;
}
.detail-item { display: flex; flex-direction: column; gap: .25rem; }
.detail-item dt {
  font-size: .78rem;
  font-weight: 600;
  color: var(--color-text-muted);
}
.detail-item dd {
  margin: 0;
  font-size: .95rem;
  color: var(--color-text);
}
.detail-item.mono dd { font-family: monospace; }

.msg {
  color: var(--color-danger);
  font-weight: 600;
  min-height: 1.2rem;
  margin: 0 0 1rem;
}
.msg-placeholder {
  min-height: 1.2rem;
  margin: 0 0 1rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  border-top: 1px solid var(--color-border);
  padding-top: .9rem;
}
.pf-btn {
  cursor: pointer;
  padding: .5rem 1rem;
  font: 500 .9rem/1 inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms;
}
.pf-btn:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .lookup-row input, .btn-primary, .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .cust-head-main { flex-direction: column; align-items: flex-start; gap: .3rem; }
  .lookup-row { flex-direction: column; align-items: stretch; }
  .lookup-row input { width: 100%; }
  .btn-primary { justify-content: center; }
}
</style>
