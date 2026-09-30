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

const metaFields = computed(() => items.value.filter(it =>
  ['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME'].includes(it.field.name)))
const detailFields = computed(() => items.value.filter(it =>
  !['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME'].includes(it.field.name)))
</script>

<template>
  <section class="tv-page">
    <header class="tv-head">
      <h1>{{ title }}</h1>
      <dl class="tv-meta">
        <template v-for="it in metaFields" :key="it.field.name">
          <dt>{{ it.label }}</dt>
          <dd>{{ val(it.field.name) }}</dd>
        </template>
      </dl>
    </header>

    <fieldset class="tv-card">
      <legend>Transaction Details</legend>
      <div class="tv-grid">
        <div v-for="it in detailFields" :key="it.field.name" class="tv-field">
          <label v-if="it.label" :for="it.field.name">{{ it.label }}</label>
          <input v-if="it.field.input"
                 :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
          <output v-else :id="it.field.name" class="tv-value">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
        </div>
      </div>
    </fieldset>

    <p v-if="message" class="tv-msg" role="alert">
      <svg class="tv-msg-icon" viewBox="0 0 20 20" width="16" height="16" aria-hidden="true" fill="none">
        <circle cx="10" cy="10" r="9" stroke="currentColor" stroke-width="1.5" />
        <path d="M10 6v5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
        <circle cx="10" cy="13.5" r="1" fill="currentColor" />
      </svg>
      {{ message }}
    </p>
    <p v-else class="tv-msg tv-msg-placeholder" aria-hidden="true">&nbsp;</p>

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
.tv-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.tv-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.tv-head h1 {
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0;
}

.tv-meta {
  display: flex;
  flex-wrap: wrap;
  gap: .35rem 1.25rem;
  margin: 0;
  font-size: .8rem;
}

.tv-meta dt {
  color: var(--color-text-muted);
  font-weight: 600;
  display: inline;
}

.tv-meta dd {
  display: inline;
  margin: 0 0 0 .35rem;
  font-family: monospace;
  color: var(--color-text);
}

.tv-meta dt::after {
  content: '';
}

.tv-meta dt + dd {
  margin-right: 0;
}

.tv-meta dt:not(:first-child) {
  margin-left: 0;
}

.tv-card {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin: 0 0 1.25rem;
  background: var(--color-surface);
}

.tv-card legend {
  font-size: .85rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.tv-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem 1.5rem;
}

.tv-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
}

.tv-field label {
  font-size: .78rem;
  font-weight: 600;
  color: var(--color-text-muted);
}

.tv-field input {
  padding: .5rem .6rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 150ms, box-shadow 150ms;
}

.tv-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.tv-value {
  padding: .5rem 0;
  font-family: monospace;
  color: var(--color-text);
  word-break: break-word;
}

@media (prefers-reduced-motion: reduce) {
  .tv-field input {
    transition: none;
  }
}

.tv-msg {
  display: flex;
  align-items: center;
  gap: .4rem;
  min-height: 1.5rem;
  margin: 0 0 1rem;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
}

.tv-msg-icon {
  flex-shrink: 0;
}

.tv-msg-placeholder {
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
  transform: translateY(-1px);
  opacity: .92;
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
  .pf-btn {
    transition: none;
  }
  .pf-primary:hover {
    transform: none;
  }
}

@media (max-width: 768px) {
  .tv-head {
    flex-direction: column;
    align-items: flex-start;
  }
  .tv-grid {
    grid-template-columns: 1fr;
  }
  .pf {
    flex-direction: column;
  }
  .pf-btn {
    width: 100%;
    text-align: center;
  }
}
</style>
