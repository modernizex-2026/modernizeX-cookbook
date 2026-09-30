<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const items = computed(() => namedFields(allFields.value, cellNames.value)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input
    || (props.values[it.field.name] ?? '').trim() !== ''
    || (it.field.initial ?? '').trim() !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const acctField = computed(() => items.value.find(it => it.field.name === 'ACCTID'))
const cycField = computed(() => items.value.find(it => it.field.name === 'STCYC'))
const summaryFields = computed(() => items.value.filter(it =>
  ['STOPEN', 'STCLOSE', 'STMIN', 'STDUE'].includes(it.field.name)))

function submit(): void { props.send('ENTER') }
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <h1>{{ title }}</h1>
      <dl class="meta">
        <div class="meta-item"><dt>Tran</dt><dd>{{ values.TRNNAME }}</dd></div>
        <div class="meta-item"><dt>Pgm</dt><dd>{{ values.PGMNAME }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ values.CURDATE }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ values.CURTIME }}</dd></div>
      </dl>
    </header>

    <fieldset class="rec-card">
      <legend>Account Lookup</legend>
      <div class="rec-grid">
        <div v-if="acctField" class="rec-field">
          <label for="ACCTID">{{ acctField.label }}</label>
          <input id="ACCTID" type="text" class="mono"
                 :maxlength="acctField.field.length"
                 :value="val('ACCTID')"
                 @input="onInput('ACCTID', $event)"
                 @keydown.enter.prevent="submit" />
        </div>
        <div v-if="cycField" class="rec-field">
          <label for="STCYC">{{ cycField.label }}</label>
          <input id="STCYC" type="text" class="mono"
                 :inputmode="cycField.numeric ? 'numeric' : undefined"
                 :maxlength="cycField.field.length"
                 :value="val('STCYC')"
                 @input="onInput('STCYC', $event)"
                 @keydown.enter.prevent="submit" />
        </div>
      </div>
    </fieldset>

    <fieldset v-if="summaryFields.length" class="rec-card">
      <legend>Statement Summary</legend>
      <dl class="summary-grid">
        <template v-for="it in summaryFields" :key="it.field.name">
          <div class="summary-item">
            <dt>{{ it.label }}</dt>
            <dd class="mono">{{ val(it.field.name) }}</dd>
          </div>
        </template>
      </dl>
    </fieldset>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              class="pf-btn" :class="{ primary: b.aidKey === 'ENTER' }"
              @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 2rem auto;
  padding: 0 1.5rem;
  color: var(--color-text);
}

.am-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.am-head h1 {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  margin: 0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: .35rem;
  font-size: .8rem;
}

.meta-item dt {
  color: var(--color-text-muted);
  font-weight: 600;
}

.meta-item dd {
  margin: 0;
  font-family: monospace;
  color: var(--color-primary);
}

.rec-card {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin-bottom: 1.25rem;
  background: var(--color-surface);
}

.rec-card legend {
  padding: 0 .5rem;
  font-size: .85rem;
  font-weight: 700;
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: .03em;
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem 1.5rem;
}

.rec-field {
  display: flex;
  flex-direction: column;
  gap: .35rem;
}

.rec-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text);
}

.rec-field input {
  padding: .55rem .65rem;
  font: inherit;
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

.mono {
  font-family: monospace;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem 1.5rem;
  margin: 0;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: .3rem;
  padding: .75rem .9rem;
  border-radius: 8px;
  background: var(--c-blue-50);
}

.summary-item dt {
  font-size: .75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: .02em;
}

.summary-item dd {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--color-text);
}

.msg {
  min-height: 1.4em;
  color: var(--color-danger);
  font-weight: 600;
  margin: 1rem 0;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  margin-top: 1.25rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}

.pf-btn {
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
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

.pf-btn:active {
  transform: translateY(1px);
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
  filter: brightness(1.08);
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn, .rec-field input {
    transition: none;
  }
}

@media (max-width: 768px) {
  .am-page {
    padding: 0 1rem;
  }
  .rec-grid, .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
