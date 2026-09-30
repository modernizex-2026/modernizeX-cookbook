<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const metaNames = new Set(['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME'])

const filterItems = computed(() => namedFields(allFields.value, cellNames.value)
  .filter((f: Field) => f.input || metaNames.has(f.name) === false)
  .filter((f: Field) => !metaNames.has(f.name))
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input))

const list = computed(() => (props.screen?.recordLists ?? [])[0] as
  { columns: string[]; headers?: string[]; rows: Array<Record<string, string>> } | undefined)

const visibleRows = computed(() => (list.value?.rows ?? []).filter(row => {
  const nameField = row['CUN']
  return nameField && val(nameField).trim() !== ''
}))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
</script>

<template>
  <section class="scr-page">
    <header class="scr-meta">
      <span class="scr-meta-item"><span class="scr-meta-label">Tran</span><span class="scr-meta-val">{{ val('TRNNAME') }}</span></span>
      <h1 class="scr-title">{{ title }}</h1>
      <span class="scr-meta-item"><span class="scr-meta-label">Date</span><span class="scr-meta-val">{{ val('CURDATE') }}</span></span>
      <span class="scr-meta-item"><span class="scr-meta-label">Pgm</span><span class="scr-meta-val">{{ val('PGMNAME') }}</span></span>
      <span class="scr-meta-item"><span class="scr-meta-label">Time</span><span class="scr-meta-val">{{ val('CURTIME') }}</span></span>
    </header>

    <fieldset class="scr-filters">
      <legend>Search customers</legend>
      <div class="scr-filter-grid">
        <div v-for="it in filterItems" :key="it.field.name" class="scr-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
        </div>
      </div>
      <div class="scr-filter-actions">
        <button type="button" class="btn-primary" @click="send('ENTER')">
          <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" /><path d="m21 21-4.3-4.3" />
          </svg>
          Filter
        </button>
      </div>
    </fieldset>

    <div class="scr-table-wrap" v-if="list">
      <table class="scr-table">
        <thead>
          <tr>
            <th v-for="(c, ci) in list.columns" :key="c" :class="{ num: c === 'CFI' }">
              {{ (list.headers && list.headers[ci]) || c }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="visibleRows.length === 0">
            <td :colspan="list.columns.length" class="scr-empty">No customers match the current filter.</td>
          </tr>
          <tr v-for="(row, idx) in visibleRows" :key="idx">
            <td class="mono">{{ val(row['CUL']) }}</td>
            <td>{{ val(row['CUN']) }}</td>
            <td>{{ val(row['CST']) }}</td>
            <td class="mono">{{ val(row['CZP']) }}</td>
            <td class="mono num">{{ val(row['CFI']) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="scr-msg" role="status" :class="{ 'is-empty': !message }">{{ message }}</p>

    <nav class="pf" aria-label="Screen actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.scr-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
  color: var(--color-text);
}

.scr-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 1.25rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: 1.5rem;
}
.scr-title {
  font-size: 1.4rem;
  margin: 0;
  margin-right: auto;
  color: var(--color-text);
}
.scr-meta-item {
  display: flex;
  gap: .35rem;
  align-items: baseline;
  font-size: .8rem;
}
.scr-meta-label {
  color: var(--color-text-muted);
  font-weight: 600;
}
.scr-meta-val {
  font-family: monospace;
  color: var(--color-primary);
}

.scr-filters {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1rem 1.25rem 1.25rem;
  margin-bottom: 1.5rem;
}
.scr-filters legend {
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
}
.scr-filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: .85rem 1.25rem;
}
.scr-field {
  display: flex;
  flex-direction: column;
  gap: .25rem;
}
.scr-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text);
}
.scr-field input {
  padding: .5rem .6rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 200ms, box-shadow 200ms;
}
.scr-field input:hover {
  border-color: var(--color-primary);
}
.scr-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
@media (prefers-reduced-motion: reduce) {
  .scr-field input { transition: none; }
}

.scr-filter-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: .5rem;
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  color: var(--c-white);
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  transition: opacity 200ms, transform 150ms;
}
.btn-primary:hover {
  opacity: .9;
}
.btn-primary:active {
  transform: translateY(1px);
}
.btn-primary:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
@media (prefers-reduced-motion: reduce) {
  .btn-primary { transition: none; }
  .btn-primary:active { transform: none; }
}

.scr-table-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  margin-bottom: 1.25rem;
}
.scr-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 640px;
}
.scr-table th,
.scr-table td {
  padding: .55rem .75rem;
  text-align: left;
  font-size: .9rem;
  border-bottom: 1px solid var(--color-border);
}
.scr-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 600;
  position: sticky;
  top: 0;
}
.scr-table th.num,
.scr-table td.num {
  text-align: right;
}
.scr-table tbody tr:hover {
  background: var(--c-blue-50);
}
.scr-table tbody tr:last-child td {
  border-bottom: none;
}
.mono {
  font-family: monospace;
}
.scr-empty {
  text-align: center;
  color: var(--color-text-muted);
  padding: 1.5rem .75rem;
}

.scr-msg {
  min-height: 1.25rem;
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem;
}
.scr-msg.is-empty {
  visibility: hidden;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  border-top: 1px solid var(--color-border);
  padding-top: .85rem;
}
.pf button {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: border-color 200ms, background 200ms;
}
.pf button:hover {
  border-color: var(--color-primary);
  background: var(--c-blue-50);
}
.pf button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
@media (prefers-reduced-motion: reduce) {
  .pf button { transition: none; }
}

@media (max-width: 768px) {
  .scr-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: .4rem;
  }
  .scr-title {
    order: -1;
    margin-right: 0;
  }
  .scr-filter-grid {
    grid-template-columns: 1fr;
  }
  .scr-table {
    min-width: 100%;
  }
}
</style>
