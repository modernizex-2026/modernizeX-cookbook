<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const meta = computed(() => ({
  tran: props.values['TRNNAME'] ?? '',
  pgm: props.values['PGMNAME'] ?? '',
  date: props.values['CURDATE'] ?? '',
  time: props.values['CURTIME'] ?? '',
}))

const items = computed(() => namedFields(allFields.value, new Set())
  .filter((f: Field) => f.name !== 'TRNNAME' && f.name !== 'PGMNAME' && f.name !== 'CURDATE' && f.name !== 'CURTIME' && f.name !== 'ERRMSG')
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input
    || (props.values[it.field.name] ?? '').trim() !== ''
    || (it.field.initial ?? '').trim() !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
</script>

<template>
  <section class="cust-page">
    <header class="cust-head">
      <h1>{{ title }}</h1>
      <dl class="meta-strip">
        <div><dt>Tran</dt><dd>{{ meta.tran }}</dd></div>
        <div><dt>Pgm</dt><dd>{{ meta.pgm }}</dd></div>
        <div><dt>Date</dt><dd>{{ meta.date }}</dd></div>
        <div><dt>Time</dt><dd>{{ meta.time }}</dd></div>
      </dl>
    </header>

    <fieldset class="cust-form">
      <legend>Customer Lookup</legend>
      <div class="cust-grid">
        <div v-for="it in items" :key="it.field.name" class="cust-field" :class="{ 'is-numeric': it.numeric }">
          <label v-if="it.label" :for="it.field.name">{{ it.label }}</label>
          <input v-if="it.field.input" :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)"
                 autofocus />
          <output v-else :id="it.field.name" class="cust-value">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
        </div>
      </div>
    </fieldset>

    <p class="msg" role="alert" :class="{ 'msg--empty': !message }">{{ message || '\u00A0' }}</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              class="pf-btn" :class="{ primary: b.aidKey === 'ENTER' }"
              @click="send(b.aidKey)" :title="b.aidKey">
        <svg v-if="b.aidKey === 'ENTER'" aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20 6 9 17 4 12" />
        </svg>
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.cust-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.cust-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.cust-head h1 {
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: .01em;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 0 1.25rem;
  margin: 0;
  font-size: .8rem;
}

.meta-strip > div {
  display: flex;
  gap: .35rem;
  align-items: baseline;
}

.meta-strip dt {
  color: var(--color-text-muted);
  font-weight: 600;
}

.meta-strip dd {
  margin: 0;
  font-family: monospace;
  color: var(--color-text);
}

.cust-form {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin: 0 0 1.25rem;
  background: var(--color-surface);
}

.cust-form legend {
  font-size: .85rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.cust-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem 1.5rem;
}

.cust-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
}

.cust-field label {
  font-size: .78rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: .03em;
}

.cust-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 200ms, box-shadow 200ms;
}

.cust-field input:hover {
  border-color: var(--color-primary);
}

.cust-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.cust-field.is-numeric input {
  text-align: right;
}

.cust-value {
  padding: .55rem 0;
  font-family: monospace;
  font-size: .95rem;
  color: var(--color-text);
  min-height: 1.4rem;
}

@media (prefers-reduced-motion: reduce) {
  .cust-field input { transition: none; }
}

.msg {
  min-height: 1.4rem;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
  margin: 0 0 1rem;
}

.msg--empty {
  visibility: hidden;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}

.pf-btn {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  font-size: .9rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, color 200ms;
}

.pf-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
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
  color: var(--c-white);
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .cust-grid { grid-template-columns: 1fr; }
  .cust-head { flex-direction: column; align-items: flex-start; }
}
</style>
