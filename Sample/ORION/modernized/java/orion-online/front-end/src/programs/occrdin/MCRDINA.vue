<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function val(name: string): string {
  return props.values[name] ?? ''
}
function setVal(name: string, v: string) {
  props.values[name] = v
  if (typed) typed.value[name] = v
}
function onFiltInput(e: Event) {
  setVal('FILT', (e.target as HTMLInputElement).value)
}

function fieldByName(name: string): Field | undefined {
  return allFields.value.find((f: Field) => f.name === name)
}
const filtField = computed<Field | undefined>(() => fieldByName('FILT'))
const filtLabel = computed<string>(() => (filtField.value ? labelFor(allFields.value, filtField.value) : 'Filter'))
const fdesc = computed<string>(() => val('FDESC'))

const list = computed(() => (props.screen?.recordLists?.[0] ?? { columns: [], headers: [], rows: [] }) as {
  columns: string[]
  headers?: string[]
  rows: Array<Record<string, string>>
})

const rows = computed(() =>
  list.value.rows
    .map((row) => ({
      cnm: val(row.CNM),
      cac: val(row.CAC),
      cna: val(row.CNA),
      cex: val(row.CEX),
      cst: val(row.CST),
    }))
    .filter((r) => r.cnm.trim() !== '' || r.cac.trim() !== '' || r.cna.trim() !== '')
)

function statusLabel(code: string): string {
  const c = code.trim().toUpperCase()
  if (c === 'A') return 'Active'
  if (c === 'I') return 'Inactive'
  if (c === 'E') return 'Expiring'
  return code || '—'
}
function statusClass(code: string): string {
  const c = code.trim().toUpperCase()
  if (c === 'A') return 'st-active'
  if (c === 'I') return 'st-inactive'
  if (c === 'E') return 'st-expiring'
  return 'st-unknown'
}

const filterOptions = ['ALL', 'ACTIVE', 'INACTIVE', 'EXPIRING-SOON']
function applyFilter(opt: string) {
  setVal('FILT', opt)
  props.send('ENTER')
}
</script>

<template>
  <section class="crd-page">
    <header class="crd-meta">
      <div class="crd-meta-left">
        <span class="crd-title">{{ title }}</span>
        <span class="crd-prog">{{ program }} · {{ screenName }}</span>
      </div>
      <div class="crd-meta-right">
        <span>{{ val('CURDATE') }}</span>
        <span>{{ val('CURTIME') }}</span>
      </div>
    </header>

    <fieldset class="crd-filter">
      <legend>Filter</legend>
      <div class="crd-filter-row">
        <div class="crd-filter-input">
          <label for="FILT">{{ filtLabel }}</label>
          <input
            id="FILT"
            type="text"
            :maxlength="filtField?.length"
            :value="val('FILT')"
            @input="onFiltInput"
            @keydown.enter.prevent="send('ENTER')"
          />
          <span v-if="fdesc" class="crd-fdesc">{{ fdesc }}</span>
        </div>
        <div class="crd-filter-chips">
          <button
            v-for="opt in filterOptions"
            :key="opt"
            type="button"
            class="crd-chip"
            @click="applyFilter(opt)"
          >
            {{ opt }}
          </button>
        </div>
      </div>
    </fieldset>

    <div class="crd-table-wrap">
      <table class="crd-table">
        <thead>
          <tr>
            <th>Card Number</th>
            <th>Acct ID</th>
            <th>Embossed Name</th>
            <th>Expiry</th>
            <th>St</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="rows.length === 0">
            <td colspan="5" class="crd-empty">No cards match the current filter.</td>
          </tr>
          <tr v-for="(r, idx) in rows" :key="idx">
            <td class="mono">{{ r.cnm }}</td>
            <td class="mono">{{ r.cac }}</td>
            <td>{{ r.cna }}</td>
            <td class="mono">{{ r.cex }}</td>
            <td>
              <span class="crd-status" :class="statusClass(r.cst)">{{ statusLabel(r.cst) }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="crd-summary">
      <span>Page: <strong class="mono">{{ val('PAGENO') }}</strong></span>
      <span>Active: <strong class="mono">{{ val('ACTCNT') }}</strong></span>
      <span>Inactive: <strong class="mono">{{ val('INACNT') }}</strong></span>
    </div>

    <p v-if="message" class="crd-msg">{{ message }}</p>
    <p v-else class="crd-msg-placeholder" aria-hidden="true">&nbsp;</p>

    <nav class="crd-pf">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="crd-pf-btn"
        @click="send(b.aidKey)"
        :title="b.aidKey"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.crd-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem;
  color: var(--color-text);
  font-family: inherit;
}

.crd-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: 0.75rem;
  margin-bottom: 1.25rem;
}
.crd-meta-left {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.crd-title {
  font-size: 1.35rem;
  font-weight: 700;
}
.crd-prog {
  font: 600 0.75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: 0.3rem 0.6rem;
}
.crd-meta-right {
  display: flex;
  gap: 1rem;
  font: 0.85rem/1 monospace;
  color: var(--color-text-muted);
}

.crd-filter {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem;
  margin-bottom: 1.25rem;
}
.crd-filter legend {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 0.4rem;
}
.crd-filter-row {
  display: flex;
  align-items: flex-end;
  gap: 1.5rem;
  flex-wrap: wrap;
}
.crd-filter-input {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 200px;
}
.crd-filter-input label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-primary);
}
.crd-filter-input input {
  padding: 0.5rem 0.6rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 200ms, box-shadow 200ms;
}
.crd-filter-input input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.crd-fdesc {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}
.crd-filter-chips {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.crd-chip {
  cursor: pointer;
  padding: 0.45rem 0.9rem;
  font: inherit;
  font-size: 0.85rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  transition: background 200ms, border-color 200ms, color 200ms;
}
.crd-chip:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.crd-chip:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.crd-table-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  margin-bottom: 1.25rem;
}
.crd-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 640px;
}
.crd-table th,
.crd-table td {
  padding: 0.55rem 0.75rem;
  text-align: left;
  font-size: 0.9rem;
  border-bottom: 1px solid var(--color-border);
}
.crd-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 700;
  position: sticky;
  top: 0;
}
.crd-table tbody tr:last-child td {
  border-bottom: none;
}
.crd-table tbody tr:hover {
  background: var(--c-blue-50);
}
.crd-empty {
  text-align: center;
  color: var(--color-text-muted);
  padding: 1.25rem;
}
.mono {
  font-family: monospace;
}

.crd-status {
  display: inline-block;
  padding: 0.15rem 0.55rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}
.st-active {
  background: var(--color-success);
  color: var(--color-surface);
}
.st-inactive {
  background: var(--color-border);
  color: var(--color-text);
}
.st-expiring {
  background: var(--color-warning);
  color: var(--color-surface);
}
.st-unknown {
  background: var(--color-border);
  color: var(--color-text-muted);
}

.crd-summary {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin-bottom: 1rem;
}
.crd-summary strong {
  color: var(--color-text);
}

.crd-msg,
.crd-msg-placeholder {
  min-height: 1.25rem;
  margin: 0 0 1rem;
  color: var(--color-danger);
  font-weight: 600;
}

.crd-pf {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  border-top: 1px solid var(--color-border);
  padding-top: 0.85rem;
}
.crd-pf-btn {
  cursor: pointer;
  padding: 0.5rem 1rem;
  font: inherit;
  font-size: 0.9rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms;
}
.crd-pf-btn:first-child {
  background: var(--color-primary);
  color: var(--color-surface);
  border-color: var(--color-primary);
  font-weight: 600;
}
.crd-pf-btn:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}
.crd-pf-btn:first-child:hover {
  filter: brightness(1.08);
}
.crd-pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .crd-filter-input input,
  .crd-chip,
  .crd-pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .crd-page {
    padding: 1rem;
  }
  .crd-filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  .crd-summary {
    flex-direction: column;
    gap: 0.4rem;
  }
}
</style>
