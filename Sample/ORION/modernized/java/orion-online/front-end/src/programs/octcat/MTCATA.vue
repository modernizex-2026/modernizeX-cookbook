<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const inputNames = ['TCTYPE', 'TCCD', 'TCDESC']
const inputItems = computed(() => inputNames
  .map((n: string) => allFields.value.find((f: Field) => f.name === n))
  .filter((f): f is Field => !!f)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const trnName = computed<string>(() => val('TRNNAME'))
const pgmName = computed<string>(() => val('PGMNAME'))
const curDate = computed<string>(() => val('CURDATE'))
const curTime = computed<string>(() => val('CURTIME'))
</script>

<template>
  <section class="cat-page">
    <header class="cat-meta">
      <div class="cat-meta-group">
        <span class="cat-meta-item"><span class="cat-meta-label">Tran</span><span class="cat-meta-value">{{ trnName }}</span></span>
        <span class="cat-meta-item"><span class="cat-meta-label">Pgm</span><span class="cat-meta-value">{{ pgmName }}</span></span>
      </div>
      <div class="cat-meta-group">
        <span class="cat-meta-item"><span class="cat-meta-label">Date</span><span class="cat-meta-value">{{ curDate }}</span></span>
        <span class="cat-meta-item"><span class="cat-meta-label">Time</span><span class="cat-meta-value">{{ curTime }}</span></span>
      </div>
    </header>

    <div class="cat-title-row">
      <h1>{{ title }}</h1>
    </div>

    <fieldset class="cat-card">
      <legend>Category Details</legend>
      <div class="cat-grid">
        <div v-for="it in inputItems" :key="it.field.name" class="cat-field" :class="{ 'cat-field-wide': it.field.name === 'TCDESC' }">
          <label :for="it.field.name">{{ it.label }}</label>
          <input :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
        </div>
      </div>
    </fieldset>

    <p class="cat-msg" role="alert">{{ message }}&nbsp;</p>

    <nav class="cat-pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" class="cat-pf-btn" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.cat-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.5rem 1rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cat-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: .5rem 1.5rem;
  font-size: .8rem;
  color: var(--color-text-muted);
  padding-bottom: .6rem;
  border-bottom: 1px solid var(--color-border);
}

.cat-meta-group {
  display: flex;
  gap: 1.25rem;
}

.cat-meta-item {
  display: inline-flex;
  gap: .35rem;
  align-items: baseline;
}

.cat-meta-label {
  font-weight: 600;
  color: var(--color-primary);
}

.cat-meta-value {
  font-family: monospace;
}

.cat-title-row h1 {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--color-text);
}

.cat-card {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.25rem 1.5rem;
  margin: 0;
  background: var(--color-surface);
}

.cat-card legend {
  font-size: .8rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
  text-transform: uppercase;
  letter-spacing: .03em;
}

.cat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem 1.25rem;
  margin-top: .5rem;
}

.cat-field {
  display: flex;
  flex-direction: column;
  gap: .35rem;
}

.cat-field-wide {
  grid-column: 1 / -1;
}

.cat-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text);
}

.cat-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 200ms, box-shadow 200ms;
}

.cat-field input:hover {
  border-color: var(--color-primary);
}

.cat-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

@media (prefers-reduced-motion: reduce) {
  .cat-field input {
    transition: none;
  }
}

.cat-msg {
  min-height: 1.2em;
  margin: 0;
  font-size: .85rem;
  font-weight: 600;
  color: var(--color-danger);
}

.cat-pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  padding-top: .75rem;
  border-top: 1px solid var(--color-border);
}

.cat-pf-btn {
  cursor: pointer;
  padding: .5rem 1.1rem;
  font: inherit;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, color 200ms;
}

.cat-pf-btn:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.cat-pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.cat-pf-btn:first-child {
  color: var(--c-white);
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.cat-pf-btn:first-child:hover {
  opacity: .92;
}

@media (prefers-reduced-motion: reduce) {
  .cat-pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .cat-page {
    padding: 1rem;
  }

  .cat-meta {
    flex-direction: column;
    gap: .35rem;
  }

  .cat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
