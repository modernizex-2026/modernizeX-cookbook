<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const trnname = computed<string>(() => props.values['TRNNAME'] ?? '')
const pgmname = computed<string>(() => props.values['PGMNAME'] ?? '')
const curdate = computed<string>(() => props.values['CURDATE'] ?? '')
const curtime = computed<string>(() => props.values['CURTIME'] ?? '')

const items = computed(() => namedFields(allFields.value, new Set())
  .filter((f: Field) => f.input)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
</script>

<template>
  <section class="tr-page">
    <header class="tr-head">
      <h1>{{ title }}</h1>
      <dl class="tr-meta">
        <div><dt>Tran</dt><dd>{{ trnname }}</dd></div>
        <div><dt>Pgm</dt><dd>{{ pgmname }}</dd></div>
        <div><dt>Date</dt><dd>{{ curdate }}</dd></div>
        <div><dt>Time</dt><dd>{{ curtime }}</dd></div>
      </dl>
    </header>

    <fieldset class="tr-fieldset">
      <legend>Transaction Details</legend>
      <div class="tr-grid">
        <div v-for="it in items" :key="it.field.name" class="tr-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :class="{ 'tr-mono': it.numeric || it.field.name === 'CARDNUM' }"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
        </div>
      </div>
    </fieldset>

    <p class="tr-msg-slot" :class="{ 'tr-msg-visible': !!message }" role="alert">{{ message }}</p>

    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              :class="['pf-btn', b.aidKey === 'ENTER' ? 'pf-primary' : 'pf-secondary']"
              @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.tr-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.tr-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.tr-head h1 {
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: .01em;
}

.tr-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0 1.25rem;
  margin: 0;
  font-size: .8rem;
}

.tr-meta > div {
  display: flex;
  gap: .35rem;
  align-items: baseline;
}

.tr-meta dt {
  color: var(--color-text-muted, var(--color-text));
  font-weight: 600;
}

.tr-meta dd {
  margin: 0;
  font-family: monospace;
  color: var(--color-primary);
}

.tr-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.25rem 1rem;
  margin: 0 0 1.25rem;
}

.tr-fieldset legend {
  font-size: .85rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.tr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem 1.5rem;
}

.tr-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
}

.tr-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text);
}

.tr-field input {
  padding: .5rem .65rem;
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: border-color .2s, box-shadow .2s;
}

.tr-field input:hover {
  border-color: var(--color-primary);
}

.tr-field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}

.tr-mono {
  font-family: monospace;
  letter-spacing: .02em;
}

@media (prefers-reduced-motion: reduce) {
  .tr-field input {
    transition: none;
  }
}

.tr-msg-slot {
  min-height: 1.4rem;
  margin: 0 0 1rem;
  font-weight: 600;
  color: var(--color-danger);
  visibility: hidden;
}

.tr-msg-visible {
  visibility: visible;
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
  transition: background .2s, border-color .2s, transform .15s;
}

.pf-btn:hover {
  transform: translateY(-1px);
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

.pf-secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn {
    transition: none;
    transform: none;
  }
}

@media (max-width: 768px) {
  .tr-grid {
    grid-template-columns: 1fr;
  }
  .tr-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
