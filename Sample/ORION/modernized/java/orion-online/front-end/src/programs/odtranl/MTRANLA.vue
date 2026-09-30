<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const metaField = (name: string): Field | undefined => allFields.value.find((f: Field) => f.name === name)
function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const items = computed(() => namedFields(allFields.value, cellNames.value)
  .filter((f: Field) => !['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME', 'ERRMSG'].includes(f.name))
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input
    || (props.values[it.field.name] ?? '').trim() !== ''
    || (it.field.initial ?? '').trim() !== ''))

const lists = computed<Array<{ columns: string[]; headers?: string[]; rows: Array<Record<string, string>> }>>(
  () => (props.screen?.recordLists ?? []))

function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }

function rowHasData(row: Record<string, string>, columns: string[]): boolean {
  return columns.some(c => (val(row[c]) || '').trim() !== '')
}
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <div class="am-head-left">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item">
          <dt>Pgm</dt>
          <dd>{{ val('PGMNAME') }}</dd>
        </div>
        <div class="meta-item">
          <dt>Date</dt>
          <dd>{{ val('CURDATE') }}</dd>
        </div>
        <div class="meta-item">
          <dt>Time</dt>
          <dd>{{ val('CURTIME') }}</dd>
        </div>
      </dl>
    </header>

    <fieldset v-if="items.length" class="rec-fieldset">
      <legend>Search</legend>
      <div class="rec-grid">
        <div v-for="it in items" :key="it.field.name" class="rec-field">
          <label v-if="it.label" :for="it.field.name">{{ it.label }}</label>
          <input v-if="it.field.input" :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
          <output v-else :id="it.field.name">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
        </div>
      </div>
    </fieldset>

    <div v-for="(rl, ri) in lists" :key="'rl' + ri" class="table-wrap">
      <table class="rec-table">
        <thead>
          <tr>
            <th v-for="(c, ci) in rl.columns" :key="c">{{ (rl.headers && rl.headers[ci]) || c }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in rl.rows" :key="idx" v-show="rowHasData(row, rl.columns)">
            <td v-for="c in rl.columns" :key="c" :class="{ numeric: c === 'AMT' }">
              <input v-if="fieldByName(row[c])?.input"
                     :value="val(row[c])" :maxlength="fieldByName(row[c])?.length"
                     @input="onInput(row[c], $event)" />
              <span v-else>{{ val(row[c]) }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>
    <p v-else class="msg-reserve" aria-hidden="true">&nbsp;</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button" class="pf-btn" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.am-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.am-head-left {
  display: flex;
  align-items: baseline;
  gap: .75rem;
}

.am-head h1 {
  font-size: 1.4rem;
  margin: 0;
}

.prog-tag {
  font: 600 .75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .3rem .6rem;
}

.meta-strip {
  display: flex;
  gap: 1.25rem;
  margin: 0;
  font-size: .8rem;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: .1rem;
  align-items: flex-end;
}

.meta-item dt {
  color: var(--color-text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .03em;
  font-size: .68rem;
}

.meta-item dd {
  margin: 0;
  font-family: monospace;
}

.rec-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0 0 1.5rem;
}

.rec-fieldset legend {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: .75rem 1.25rem;
}

.rec-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
}

.rec-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
}

.rec-field input,
.rec-field output {
  padding: .5rem .6rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color .2s, box-shadow .2s;
}

.rec-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

@media (prefers-reduced-motion: reduce) {
  .rec-field input {
    transition: none;
  }
}

.table-wrap {
  overflow-x: auto;
  margin-bottom: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.rec-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 480px;
}

.rec-table th,
.rec-table td {
  border-bottom: 1px solid var(--color-border);
  padding: .55rem .75rem;
  text-align: left;
  font-size: .9rem;
  font-family: monospace;
}

.rec-table td.numeric {
  text-align: right;
}

.rec-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-family: inherit;
  font-weight: 600;
  border-bottom: 2px solid var(--color-border);
}

.rec-table tbody tr:last-child td {
  border-bottom: none;
}

.rec-table tbody tr:hover {
  background: var(--c-blue-50);
}

.rec-table input {
  width: 100%;
  box-sizing: border-box;
  padding: .35rem .5rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-surface);
}

.msg,
.msg-reserve {
  min-height: 1.4rem;
  margin: 0 0 1rem;
}

.msg {
  color: var(--color-danger);
  font-weight: 600;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  margin-top: .5rem;
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
  transition: background .2s, border-color .2s, transform .15s;
}

.pf-btn:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .am-head {
    flex-direction: column;
  }

  .meta-strip {
    width: 100%;
    justify-content: space-between;
  }

  .meta-item {
    align-items: flex-start;
  }

  .rec-grid {
    grid-template-columns: 1fr;
  }
}
</style>
