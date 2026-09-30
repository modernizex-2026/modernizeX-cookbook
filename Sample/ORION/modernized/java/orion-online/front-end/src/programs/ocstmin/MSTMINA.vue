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

const fracctField = computed<Field | undefined>(() => fieldByName('FRACCT'))
const frcycField = computed<Field | undefined>(() => fieldByName('FRCYC'))
const fracctLabel = computed<string>(() => labelFor(allFields.value, fracctField.value as Field) || 'From Acct')
const frcycLabel = computed<string>(() => labelFor(allFields.value, frcycField.value as Field) || 'Cycle')

const list = computed(() => (props.screen?.recordLists ?? [])[0] as
  { recordName: string; columns: string[]; headers: string[]; rows: Array<Record<string, string>> } | undefined)

const rows = computed(() => (list.value?.rows ?? []).filter(row =>
  list.value!.columns.some((c: string) => val(row[c]).trim() !== '')))

function isNumericCol(colName: string): boolean {
  const f = fieldByName(colName)
  return f ? isNumericField(f) : false
}
</script>

<template>
  <section class="min-page">
    <header class="min-head">
      <div class="head-left">
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

    <fieldset class="query-panel">
      <legend>Search Criteria</legend>
      <div class="query-grid">
        <div class="field">
          <label for="FRACCT">{{ fracctLabel }}</label>
          <input
            id="FRACCT"
            type="text"
            :maxlength="fracctField?.length"
            :value="val('FRACCT')"
            @input="onInput('FRACCT', $event)"
          />
        </div>
        <div class="field">
          <label for="FRCYC">{{ frcycLabel }}</label>
          <input
            id="FRCYC"
            type="text"
            inputmode="numeric"
            :maxlength="frcycField?.length"
            :value="val('FRCYC')"
            @input="onInput('FRCYC', $event)"
          />
        </div>
      </div>
    </fieldset>

    <div class="table-wrap" v-if="list">
      <table class="min-table">
        <thead>
          <tr>
            <th v-for="(c, ci) in list.columns" :key="c" :class="{ num: isNumericCol(c + '1') }">
              {{ list.headers[ci] }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in rows" :key="idx">
            <td v-for="c in list.columns" :key="c" :class="{ num: isNumericCol(c + String(idx + 1)) }">
              {{ val(row[c]) }}
            </td>
          </tr>
          <tr v-if="!rows.length" class="empty-row">
            <td :colspan="list.columns.length">No records to display</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="msg-slot" :class="{ 'has-msg': message }">
      <span v-if="message">
        <svg class="msg-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.5" />
          <path d="M10 6v5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
          <circle cx="10" cy="13.5" r="0.9" fill="currentColor" />
        </svg>
        {{ message }}
      </span>
    </p>

    <nav class="pf">
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
.min-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.min-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
}
.head-left { display: flex; align-items: baseline; gap: .75rem; }
.min-head h1 { font-size: 1.4rem; margin: 0; }
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
.meta-item { display: flex; gap: .35rem; align-items: baseline; }
.meta-item dt { font-size: .75rem; color: var(--color-text-muted); font-weight: 600; }
.meta-item dd { margin: 0; font: 500 .85rem/1 monospace; color: var(--color-text); }

.query-panel {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0;
}
.query-panel legend {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
}
.query-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}
.field { display: flex; flex-direction: column; gap: .3rem; }
.field label { font-size: .8rem; font-weight: 600; color: var(--color-text); }
.field input {
  padding: .5rem .6rem;
  font: 1rem/1.2 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color .2s, box-shadow .2s;
}
.field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}
@media (prefers-reduced-motion: reduce) { .field input { transition: none; } }

.table-wrap { overflow-x: auto; border: 1px solid var(--color-border); border-radius: 8px; }
.min-table { width: 100%; border-collapse: collapse; font-size: .9rem; }
.min-table th, .min-table td {
  padding: .55rem .7rem;
  border-bottom: 1px solid var(--color-border);
  text-align: left;
  white-space: nowrap;
}
.min-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 600;
  position: sticky;
  top: 0;
}
.min-table td { font: 400 .9rem/1.3 monospace; color: var(--color-text); }
.min-table td.num, .min-table th.num { text-align: right; }
.min-table tbody tr:hover { background: var(--c-blue-50); }
.empty-row td { text-align: center; color: var(--color-text-muted); font-style: italic; white-space: normal; }

.msg-slot { min-height: 1.5rem; margin: 0; color: var(--color-danger); font-weight: 600; display: flex; align-items: center; }
.msg-slot.has-msg span { display: flex; align-items: center; gap: .4rem; }
.msg-icon { width: 1.1rem; height: 1.1rem; flex-shrink: 0; }

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}
.pf-btn {
  cursor: pointer;
  padding: .5rem 1rem;
  font: 500 .9rem/1 inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .18s, border-color .18s, box-shadow .18s;
}
.pf-btn:hover { border-color: var(--color-primary); background: var(--c-blue-50); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.pf-btn.primary:hover { filter: brightness(1.08); }
@media (prefers-reduced-motion: reduce) { .pf-btn { transition: none; } }

@media (max-width: 768px) {
  .min-page { padding: 1rem; }
  .min-head { flex-direction: column; align-items: flex-start; }
  .meta-strip { gap: .75rem 1rem; }
  .query-grid { grid-template-columns: 1fr; }
}
</style>
