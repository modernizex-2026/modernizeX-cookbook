<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const named = computed(() => namedFields(allFields.value, cellNames.value))

const META_NAMES = ['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME']
const SEARCH_NAMES = ['CARDNUM', 'FRAMT', 'TOAMT']
const RESULT_NAMES = ['SR1', 'SR2', 'SR3', 'SR4', 'SR5']

function byName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }
function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const metaItems = computed(() => META_NAMES
  .map((n: string) => byName(n))
  .filter((f): f is Field => !!f)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f) })))

const searchItems = computed(() => SEARCH_NAMES
  .map((n: string) => named.value.find((f: Field) => f.name === n))
  .filter((f): f is Field => !!f)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

const resultLines = computed(() => RESULT_NAMES
  .map((n: string) => byName(n))
  .filter((f): f is Field => !!f)
  .map((f: Field) => val(f.name))
  .filter((v: string) => v.trim() !== ''))

const errMsg = computed<string>(() => val('ERRMSG'))
</script>

<template>
  <section class="srch-page">
    <header class="srch-head">
      <div class="srch-titles">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="srch-meta">
        <template v-for="mi in metaItems" :key="mi.field.name">
          <div v-if="val(mi.field.name)" class="meta-pair">
            <dt v-if="mi.label">{{ mi.label }}</dt>
            <dd>{{ val(mi.field.name) }}</dd>
          </div>
        </template>
      </dl>
    </header>

    <fieldset class="srch-form">
      <legend>Search Criteria</legend>
      <div class="srch-grid">
        <div v-for="it in searchItems" :key="it.field.name" class="srch-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 class="mono-input"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
        </div>
      </div>
      <div class="srch-actions">
        <button type="button" class="btn-primary" @click="send('ENTER')">
          <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true" focusable="false">
            <path fill="currentColor" d="M15.5 14h-.79l-.28-.27a6.47 6.47 0 0 0 1.57-4.23 6.5 6.5 0 1 0-6.5 6.5 6.47 6.47 0 0 0 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0A4.5 4.5 0 1 1 14 9.5 4.5 4.5 0 0 1 9.5 14z" />
          </svg>
          <span>Search</span>
        </button>
      </div>
    </fieldset>

    <section v-if="resultLines.length" class="srch-results" aria-label="Search results">
      <p v-for="(line, idx) in resultLines" :key="'sr' + idx" class="result-line">{{ line }}</p>
    </section>

    <p v-if="errMsg" class="err-banner" role="alert">{{ errMsg }}</p>
    <p v-if="message" class="msg" role="status">{{ message }}</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button" class="pf-btn" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.srch-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.srch-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: 0.75rem;
  margin-bottom: 1.5rem;
}

.srch-titles {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
}

.srch-titles h1 {
  font-size: 1.4rem;
  margin: 0;
}

.prog-tag {
  font: 600 0.75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: 0.3rem 0.6rem;
}

.srch-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1.5rem;
  margin: 0;
}

.meta-pair {
  display: flex;
  gap: 0.35rem;
  align-items: baseline;
  font-size: 0.85rem;
}

.meta-pair dt {
  color: var(--color-text-muted);
  font-weight: 600;
}

.meta-pair dd {
  margin: 0;
  font-family: monospace;
  color: var(--color-text);
}

.srch-form {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
}

.srch-form legend {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 0.4rem;
}

.srch-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem 1.25rem;
}

.srch-field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.srch-field label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-primary);
}

.mono-input {
  padding: 0.55rem 0.65rem;
  font: 0.95rem/1.2 monospace;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: border-color 200ms, box-shadow 200ms;
}

.mono-input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.srch-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.25rem;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.55rem 1.1rem;
  font: 600 0.9rem/1 inherit;
  color: var(--c-white);
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: 8px;
  transition: filter 200ms, transform 150ms;
}

.btn-primary:hover {
  filter: brightness(1.08);
}

.btn-primary:active {
  transform: translateY(1px);
}

.btn-primary:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 3px;
}

.srch-results {
  border: 1px solid var(--color-border);
  border-left: 4px solid var(--color-primary);
  border-radius: 8px;
  padding: 0.9rem 1.1rem;
  margin-bottom: 1.25rem;
  background: var(--color-surface);
  min-height: 2.5rem;
}

.result-line {
  margin: 0.2rem 0;
  font: 0.9rem/1.4 monospace;
  color: var(--color-text);
}

.err-banner {
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 0.75rem;
  min-height: 1.2rem;
}

.msg {
  color: var(--color-warning);
  font-weight: 600;
  margin: 0 0 0.75rem;
  min-height: 1.2rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 1rem;
  border-top: 1px solid var(--color-border);
  padding-top: 0.85rem;
}

.pf-btn {
  cursor: pointer;
  padding: 0.5rem 1rem;
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms;
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
  .mono-input,
  .btn-primary,
  .pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .srch-head {
    flex-direction: column;
  }

  .srch-grid {
    grid-template-columns: 1fr;
  }

  .srch-actions {
    justify-content: stretch;
  }

  .btn-primary {
    justify-content: center;
    width: 100%;
  }
}
</style>
