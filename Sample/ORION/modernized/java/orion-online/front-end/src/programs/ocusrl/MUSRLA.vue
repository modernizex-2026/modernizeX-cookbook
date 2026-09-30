<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const trnName = computed<string>(() => props.values['TRNNAME'] ?? '')
const pgmName = computed<string>(() => props.values['PGMNAME'] ?? '')
const curDate = computed<string>(() => props.values['CURDATE'] ?? '')
const curTime = computed<string>(() => props.values['CURTIME'] ?? '')

const items = computed(() => namedFields(allFields.value, cellNames.value)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input
    || (props.values[it.field.name] ?? '').trim() !== ''
    || (it.field.initial ?? '').trim() !== ''))

const lists = computed<Array<{ recordName?: string; columns: string[]; headers?: string[]; rows: Array<Record<string, string>> }>>(
  () => (props.screen?.recordLists ?? []))

function val(name: string): string { return props.values[name] ?? '' }
function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }

const listRowNames = computed<Set<string>>(() => {
  const s = new Set<string>()
  for (const rl of lists.value) for (const row of rl.rows) for (const c of rl.columns) if (row[c]) s.add(row[c])
  return s
})

const looseOut = computed(() => items.value.filter(it => !it.field.input && !it.label && !listRowNames.value.has(it.field.name)))
const textLines = computed(() => looseOut.value.filter(it => val(it.field.name) !== ''))

const errMsg = computed<string>(() => val('ERRMSG'))

function userRows(rl: { columns: string[]; rows: Array<Record<string, string>> }) {
  return rl.rows.filter(row => val(row[rl.columns[0]]).trim() !== '')
}
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <div class="am-head-main">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item">
          <dt>Tran</dt>
          <dd>{{ trnName }}</dd>
        </div>
        <div class="meta-item">
          <dt>Pgm</dt>
          <dd>{{ pgmName }}</dd>
        </div>
        <div class="meta-item">
          <dt>Date</dt>
          <dd>{{ curDate }}</dd>
        </div>
        <div class="meta-item">
          <dt>Time</dt>
          <dd>{{ curTime }}</dd>
        </div>
      </dl>
    </header>

    <p v-for="it in textLines" :key="'t_' + it.field.name" class="rec-note">{{ val(it.field.name) }}</p>

    <fieldset v-for="(rl, ri) in lists" :key="'rl' + ri" class="list-fieldset">
      <legend>Users</legend>
      <div class="table-scroll">
        <table class="rec-table">
          <thead>
            <tr>
              <th v-for="(c, ci) in rl.columns" :key="c">{{ (rl.headers && rl.headers[ci]) || c }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in userRows(rl)" :key="idx">
              <td v-for="(c, ci) in rl.columns" :key="c" :class="{ mono: ci === 0 || ci === 2 }">
                {{ val(row[c]) }}
              </td>
            </tr>
            <tr v-if="userRows(rl).length === 0" class="empty-row">
              <td :colspan="rl.columns.length">No users found</td>
            </tr>
          </tbody>
        </table>
      </div>
    </fieldset>

    <p v-if="errMsg" class="msg" role="alert">
      <svg class="msg-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.5" />
        <path d="M10 6v5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
        <circle cx="10" cy="13.5" r="0.9" fill="currentColor" />
      </svg>
      {{ errMsg }}
    </p>
    <p v-else-if="message" class="msg">{{ message }}</p>
    <div v-else class="msg-placeholder" aria-hidden="true"></div>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
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
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.am-head-main {
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
  flex-wrap: wrap;
  gap: 1.25rem;
  margin: 0;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: .35rem;
}

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

.rec-note {
  color: var(--color-text-muted);
  margin: .15rem 0 1rem;
}

.list-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem;
  margin: 0 0 1.25rem;
}

.list-fieldset legend {
  padding: 0 .5rem;
  font-weight: 600;
  color: var(--color-primary);
  font-size: .85rem;
}

.table-scroll {
  overflow-x: auto;
}

.rec-table {
  width: 100%;
  border-collapse: collapse;
}

.rec-table th,
.rec-table td {
  border: 1px solid var(--color-border);
  padding: .55rem .65rem;
  text-align: left;
  font-size: .9rem;
}

.rec-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 600;
}

.rec-table td.mono {
  font-family: monospace;
}

.rec-table tbody tr:hover {
  background: var(--c-blue-50);
}

.empty-row td {
  text-align: center;
  color: var(--color-text-muted);
  font-style: italic;
}

.msg {
  display: flex;
  align-items: center;
  gap: .4rem;
  color: var(--color-danger);
  font-weight: 600;
  min-height: 1.4rem;
  margin: 0 0 1rem;
}

.msg-icon {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
}

.msg-placeholder {
  min-height: 1.4rem;
  margin: 0 0 1rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  margin-top: 1rem;
  border-top: 1px solid var(--color-border);
  padding-top: .75rem;
}

.pf button {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .18s, border-color .18s, transform .12s;
}

.pf button:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.pf button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.pf button:active {
  transform: translateY(1px);
}

@media (prefers-reduced-motion: reduce) {
  .pf button {
    transition: none;
  }
}

@media (max-width: 768px) {
  .am-page {
    padding: 1rem;
  }

  .am-head {
    flex-direction: column;
  }

  .meta-strip {
    gap: .75rem 1.25rem;
  }

  .rec-table th,
  .rec-table td {
    padding: .45rem .5rem;
    font-size: .82rem;
  }
}
</style>
