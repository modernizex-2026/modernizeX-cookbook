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

const metaNames = new Set(['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME'])
const metaItems = computed(() => items.value.filter(it => metaNames.has(it.field.name)))
const gridItems = computed(() => items.value.filter(it => !metaNames.has(it.field.name)))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
</script>

<template>
  <section class="cust-page">
    <header class="cust-head">
      <h1>{{ title }}</h1>
      <ul class="meta-strip">
        <li v-for="it in metaItems" :key="it.field.name">
          <span class="meta-label">{{ it.label }}</span>
          <span class="meta-value">{{ val(it.field.name) }}</span>
        </li>
      </ul>
    </header>

    <fieldset class="cust-fieldset">
      <legend>Customer details</legend>
      <div class="cust-grid">
        <div v-for="it in gridItems" :key="it.field.name" class="cust-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input
            :id="it.field.name"
            :type="it.dark ? 'password' : 'text'"
            :inputmode="it.numeric ? 'numeric' : undefined"
            :maxlength="it.field.length"
            :value="val(it.field.name)"
            @input="onInput(it.field.name, $event)"
          />
        </div>
      </div>
    </fieldset>

    <p class="msg" role="alert">{{ message }}&nbsp;</p>

    <nav class="pf">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        :title="b.aidKey"
        @click="send(b.aidKey)"
      >
        <svg v-if="b.aidKey === 'ENTER'" viewBox="0 0 20 20" width="14" height="14" aria-hidden="true">
          <path d="M4 10l4 4 8-8" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.cust-page {
  max-width: var(--screen-max-width, 960px);
  margin: 2rem auto;
  padding: 0 1rem;
  color: var(--color-text);
}

.cust-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: 1.5rem;
}

.cust-head h1 {
  font-size: 1.4rem;
  margin: 0;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: .8rem;
}

.meta-strip li {
  display: flex;
  gap: .35rem;
  color: var(--color-text-muted);
}

.meta-label {
  font-weight: 600;
}

.meta-value {
  font-family: monospace;
  color: var(--color-text);
}

.cust-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.25rem 1.25rem 1.5rem;
  margin: 0 0 1.25rem;
}

.cust-fieldset legend {
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
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
}

.cust-field input {
  padding: .5rem .6rem;
  font: inherit;
  font-family: monospace;
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 200ms, box-shadow 200ms;
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
  .cust-field input {
    transition: none;
  }
}

.msg {
  min-height: 1.4em;
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: .9rem;
}

.pf-btn {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  font-weight: 600;
  color: var(--color-text);
  background: var(--color-surface);
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
  color: var(--color-surface);
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.pf-btn.primary:hover {
  filter: brightness(0.92);
  color: var(--color-surface);
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .cust-grid {
    grid-template-columns: 1fr;
  }
  .cust-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
