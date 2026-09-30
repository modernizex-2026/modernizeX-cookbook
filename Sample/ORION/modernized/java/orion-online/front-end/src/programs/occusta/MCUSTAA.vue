<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const trnname = computed<string>(() => props.values['TRNNAME'] || '')
const curdate = computed<string>(() => props.values['CURDATE'] || '')
const curtime = computed<string>(() => props.values['CURTIME'] || '')
const pgmname = computed<string>(() => props.values['PGMNAME'] || props.program || '')

const items = computed(() => namedFields(allFields.value, cellNames.value)
  .filter((f: Field) => f.input)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f) })))

function val(name: string): string { return props.values[name] ?? '' }
function onInput(name: string, e: Event) {
  const v = (e.target as HTMLInputElement).value
  props.values[name] = v
  if (typed) typed.value[name] = v
}
</script>

<template>
  <section class="cust-page">
    <header class="meta-strip">
      <span class="meta-item"><span class="meta-label">Tran</span><span class="meta-value">{{ trnname }}</span></span>
      <h1 class="meta-title">{{ title }}</h1>
      <span class="meta-item"><span class="meta-label">Date</span><span class="meta-value">{{ curdate }}</span></span>
      <span class="meta-item"><span class="meta-label">Time</span><span class="meta-value">{{ curtime }}</span></span>
    </header>
    <p class="meta-pgm">Pgm: <span>{{ pgmname }}</span></p>

    <fieldset class="cust-fieldset">
      <legend>Customer Details</legend>
      <div class="cust-grid">
        <div v-for="it in items" :key="it.field.name" class="cust-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input :id="it.field.name"
                 type="text"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
        </div>
      </div>
    </fieldset>

    <p class="msg" role="alert" aria-live="polite">{{ message }}</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              :class="['pf-btn', b.aidKey === 'ENTER' ? 'pf-primary' : 'pf-secondary']"
              @click="send(b.aidKey)" :title="b.aidKey">
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

.meta-strip {
  display: flex;
  align-items: baseline;
  gap: 1.25rem;
  flex-wrap: wrap;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: .35rem;
}
.meta-title {
  flex: 1 1 auto;
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-text);
}
.meta-item {
  display: inline-flex;
  align-items: baseline;
  gap: .35rem;
  font-size: .8rem;
}
.meta-label {
  color: var(--color-text-muted);
  font-weight: 600;
}
.meta-value {
  font: 600 .85rem/1 monospace;
  color: var(--color-primary);
}
.meta-pgm {
  font-size: .8rem;
  color: var(--color-text-muted);
  margin: 0 0 1.5rem;
}
.meta-pgm span {
  font: 600 .85rem/1 monospace;
  color: var(--color-primary);
}

.cust-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin: 0 0 1.5rem;
}
.cust-fieldset legend {
  font-weight: 700;
  font-size: .95rem;
  color: var(--color-primary);
  padding: 0 .5rem;
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
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
}
.cust-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 150ms, box-shadow 150ms;
}
.cust-field input:hover {
  border-color: var(--color-primary);
}
.cust-field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}
@media (prefers-reduced-motion: reduce) {
  .cust-field input { transition: none; }
}

.msg {
  min-height: 1.25rem;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
  margin: 0 0 1rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}
.pf-btn {
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  border-radius: 6px;
  transition: background 150ms, border-color 150ms, transform 150ms;
}
.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
.pf-primary {
  background: var(--color-primary);
  color: var(--c-white);
  border: 1px solid var(--color-primary);
}
.pf-primary:hover {
  filter: brightness(1.08);
}
.pf-secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
.pf-secondary:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}
@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .cust-grid { grid-template-columns: 1fr; }
  .meta-strip { flex-direction: column; align-items: flex-start; gap: .4rem; }
  .meta-title { order: -1; }
}
</style>
