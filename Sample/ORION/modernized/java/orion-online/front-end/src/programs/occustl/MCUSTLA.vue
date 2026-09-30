<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function fieldByName(name: string): Field | undefined {
  return allFields.value.find((f: Field) => f.name === name)
}

function val(name: string): string {
  return props.values[name] ?? ''
}
function setVal(name: string, v: string) {
  props.values[name] = v
  if (typed) typed.value[name] = v
}
function onInput(name: string, e: Event) {
  setVal(name, (e.target as HTMLInputElement).value)
}

const frcustField = computed<Field | undefined>(() => fieldByName('FRCUST'))
const frcustLabel = computed<string>(() => (frcustField.value ? labelFor(allFields.value, frcustField.value) : 'From Cust'))
const frcustNumeric = computed<boolean>(() => (frcustField.value ? isNumericField(frcustField.value) : false))
const frcustDark = computed<boolean>(() => (frcustField.value ? isDarkField(frcustField.value) : false))

const rowDefs = [
  { l: 'CUL1', n: 'CUN1', f: 'CUF1' },
  { l: 'CUL2', n: 'CUN2', f: 'CUF2' },
  { l: 'CUL3', n: 'CUN3', f: 'CUF3' },
  { l: 'CUL4', n: 'CUN4', f: 'CUF4' },
]

const rows = computed(() => rowDefs
  .map(r => ({ id: val(r.l), name: val(r.n), fico: val(r.f) }))
  .filter(r => r.id.trim() !== '' || r.name.trim() !== '' || r.fico.trim() !== ''))

const metaLine = computed<string>(() => {
  const parts: string[] = []
  if (val('TRNNAME')) parts.push(`Tran: ${val('TRNNAME')}`)
  if (val('PGMNAME')) parts.push(`Pgm: ${val('PGMNAME')}`)
  if (val('CURDATE')) parts.push(`Date: ${val('CURDATE')}`)
  if (val('CURTIME')) parts.push(`Time: ${val('CURTIME')}`)
  return parts.join('   ')
})
</script>

<template>
  <section class="cust-page">
    <header class="cust-head">
      <h1>{{ title }}</h1>
      <span class="meta-strip">{{ metaLine }}</span>
    </header>

    <fieldset class="search-box">
      <legend>Search</legend>
      <div class="search-row">
        <label for="FRCUST">{{ frcustLabel }}</label>
        <input
          id="FRCUST"
          type="text"
          :inputmode="frcustNumeric ? 'numeric' : undefined"
          :maxlength="frcustField?.length"
          :value="val('FRCUST')"
          :class="{ mono: frcustNumeric }"
          @input="onInput('FRCUST', $event)"
        />
        <button type="button" class="btn-primary" @click="send('ENTER')">
          <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true" focusable="false">
            <path fill="currentColor" d="M15.5 14h-.79l-.28-.27a6.5 6.5 0 1 0-.7.7l.27.28v.79l5 4.99L20.49 19zm-6 0A4.5 4.5 0 1 1 14 9.5A4.5 4.5 0 0 1 9.5 14"/>
          </svg>
          Search
        </button>
      </div>
    </fieldset>

    <div class="table-wrap">
      <table class="cust-table">
        <thead>
          <tr>
            <th class="col-id">Cust ID</th>
            <th class="col-name">Name</th>
            <th class="col-fico">FICO</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!rows.length">
            <td colspan="3" class="empty-row">No customers found.</td>
          </tr>
          <tr v-for="(r, idx) in rows" :key="idx">
            <td class="col-id mono">{{ r.id }}</td>
            <td class="col-name">{{ r.name }}</td>
            <td class="col-fico mono">{{ r.fico }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>
    <p v-else class="msg-placeholder" aria-hidden="true"></p>

    <nav class="pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        :title="b.aidKey"
        @click="send(b.aidKey)"
      >{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.cust-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
  color: var(--color-text);
}

.cust-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: .5rem 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: 1.5rem;
}
.cust-head h1 {
  font-size: 1.4rem;
  margin: 0;
  font-weight: 700;
}
.meta-strip {
  font: 500 .8rem/1 monospace;
  color: var(--color-text-muted);
}

.search-box {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0 0 1.5rem;
}
.search-box legend {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
}
.search-row {
  display: flex;
  align-items: flex-end;
  gap: .75rem;
  flex-wrap: wrap;
}
.search-row label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text);
  display: block;
  margin-bottom: .3rem;
}
.search-row input {
  padding: .5rem .6rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  min-width: 180px;
  transition: border-color .2s, box-shadow .2s;
}
.search-row input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.search-row input.mono {
  font-family: monospace;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  cursor: pointer;
  padding: .5rem 1rem;
  font: 600 .9rem/1 inherit;
  color: var(--c-white);
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  transition: filter .15s;
}
.btn-primary:hover { filter: brightness(0.92); }
.btn-primary:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
@media (prefers-reduced-motion: reduce) {
  .btn-primary, .search-row input { transition: none; }
}

.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  margin-bottom: 1.25rem;
}
.cust-table {
  width: 100%;
  border-collapse: collapse;
}
.cust-table th, .cust-table td {
  padding: .6rem .75rem;
  text-align: left;
  font-size: .9rem;
  border-bottom: 1px solid var(--color-border);
}
.cust-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 700;
  font-size: .78rem;
  text-transform: uppercase;
  letter-spacing: .03em;
}
.cust-table tbody tr:last-child td { border-bottom: none; }
.cust-table tbody tr:hover { background: var(--c-blue-50); }
.col-id { width: 15%; }
.col-name { width: 55%; }
.col-fico { width: 12%; }
.mono { font-family: monospace; }
.empty-row {
  text-align: center;
  color: var(--color-text-muted);
  padding: 1.25rem;
}

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
  font: inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .15s, border-color .15s;
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
.pf-btn.primary:hover { filter: brightness(0.92); }
@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .search-row { flex-direction: column; align-items: stretch; }
  .search-row input { min-width: 0; }
  .cust-head { flex-direction: column; align-items: flex-start; }
}
</style>
