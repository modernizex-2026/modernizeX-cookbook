<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const meta = computed(() => ({
  tran: props.values['TRNNAME'] ?? '',
  pgm: props.values['PGMNAME'] ?? '',
  date: props.values['CURDATE'] ?? '',
  time: props.values['CURTIME'] ?? '',
}))

const acctField = computed<Field | undefined>(() => allFields.value.find((f: Field) => f.name === 'ACCTID'))
const acctLabel = computed<string>(() => acctField.value ? labelFor(allFields.value, acctField.value) : '')
const acctNumeric = computed<boolean>(() => acctField.value ? isNumericField(acctField.value) : false)
const acctDark = computed<boolean>(() => acctField.value ? isDarkField(acctField.value) : false)

const lists = computed<Array<{ recordName: string; columns: string[]; headers?: string[]; rows: Array<Record<string, string>> }>>(
  () => (props.screen?.recordLists ?? []))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const cardRows = computed(() => {
  const rl = lists.value[0]
  if (!rl) return []
  return rl.rows
    .map((row: Record<string, string>) => ({ card: val(row.CARD), stat: val(row.STAT) }))
    .filter((r: { card: string; stat: string }) => r.card.trim() !== '')
})

const message = computed<string>(() => props.message || val('ERRMSG'))

function submit(): void { props.send('ENTER') }
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <div class="am-head-left">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item"><dt>Tran</dt><dd>{{ meta.tran }}</dd></div>
        <div class="meta-item"><dt>Pgm</dt><dd>{{ meta.pgm }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ meta.date }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ meta.time }}</dd></div>
      </dl>
    </header>

    <fieldset v-if="acctField" class="am-search">
      <legend>Search</legend>
      <div class="search-row">
        <label v-if="acctLabel" for="ACCTID">{{ acctLabel }}</label>
        <input id="ACCTID"
               :type="acctDark ? 'password' : 'text'"
               :inputmode="acctNumeric ? 'numeric' : undefined"
               :maxlength="acctField.length"
               :value="val('ACCTID')"
               @input="onInput('ACCTID', $event)"
               @keydown.enter.prevent="submit" />
        <button type="button" class="btn-primary" @click="submit">
          <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="9" r="6" />
            <line x1="14" y1="14" x2="18" y2="18" />
          </svg>
          Search
        </button>
      </div>
    </fieldset>

    <fieldset class="am-cards">
      <legend>Cards on account</legend>
      <table class="rec-table">
        <thead>
          <tr>
            <th>Card Number</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in cardRows" :key="idx">
            <td class="mono">{{ row.card }}</td>
            <td>{{ row.stat }}</td>
          </tr>
          <tr v-if="!cardRows.length">
            <td colspan="2" class="empty-row">No cards found for this account.</td>
          </tr>
        </tbody>
      </table>
    </fieldset>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>
    <p v-else class="msg-placeholder" aria-hidden="true"></p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              :class="b.aidKey === 'ENTER' ? 'btn-primary' : 'btn-secondary'"
              @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
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
.am-head-left { display: flex; align-items: baseline; gap: .75rem; }
.am-head h1 { font-size: 1.4rem; margin: 0; }
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
  text-transform: uppercase;
  letter-spacing: .03em;
}
.meta-item dd { margin: 0; font: 500 .85rem/1 monospace; color: var(--color-text); }

fieldset {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0 0 1.25rem;
}
legend {
  font-size: .85rem;
  font-weight: 700;
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
  display: block;
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: .3rem;
}
.search-row input {
  padding: .5rem .6rem;
  font: 500 .95rem/1 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  min-width: 180px;
  transition: border-color .2s, box-shadow .2s;
}
.search-row input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.btn-primary, .btn-secondary {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  padding: .5rem 1rem;
  font: 600 .9rem/1 inherit;
  border-radius: 6px;
  transition: background .15s, border-color .15s, transform .15s;
}
.btn-primary {
  background: var(--color-primary);
  color: var(--c-white);
  border: 1px solid var(--color-primary);
}
.btn-primary:hover { filter: brightness(0.92); }
.btn-secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
.btn-secondary:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
@media (prefers-reduced-motion: reduce) {
  .btn-primary, .btn-secondary, .search-row input { transition: none; }
}

.rec-table {
  width: 100%;
  border-collapse: collapse;
}
.rec-table th, .rec-table td {
  border: 1px solid var(--color-border);
  padding: .55rem .7rem;
  text-align: left;
  font-size: .9rem;
}
.rec-table thead th {
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 700;
}
.rec-table .mono { font-family: monospace; letter-spacing: .03em; }
.empty-row {
  text-align: center;
  color: var(--color-text-muted);
  padding: 1rem;
}

.msg {
  color: var(--color-danger);
  font-weight: 600;
  min-height: 1.2rem;
  margin: 0 0 .75rem;
}
.msg-placeholder { min-height: 1.2rem; margin: 0 0 .75rem; }

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}

@media (max-width: 768px) {
  .am-head { flex-direction: column; }
  .search-row { flex-direction: column; align-items: stretch; }
  .search-row input { width: 100%; box-sizing: border-box; }
  .rec-table th, .rec-table td { font-size: .85rem; padding: .45rem .5rem; }
}
</style>
