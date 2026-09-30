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

const cardFields = computed(() => items.value.filter(it => it.field.input))
const metaFields = computed(() => ['TRNNAME', 'PGMNAME', 'CURDATE', 'CURTIME']
  .map(n => ({ name: n, value: val(n) }))
  .filter(m => m.value !== ''))
</script>

<template>
  <section class="occarda-page">
    <header class="page-head">
      <div class="head-title">
        <svg class="head-icon" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="2" y="5" width="20" height="14" rx="2" />
          <line x1="2" y1="10" x2="22" y2="10" />
        </svg>
        <h1>{{ title }}</h1>
      </div>
      <dl class="meta-strip">
        <div class="meta-item" v-for="m in metaFields" :key="m.name">
          <dt>{{ m.name === 'TRNNAME' ? 'Tran' : m.name === 'PGMNAME' ? 'Pgm' : m.name === 'CURDATE' ? 'Date' : 'Time' }}</dt>
          <dd>{{ m.value }}</dd>
        </div>
      </dl>
    </header>

    <fieldset class="card-fieldset">
      <legend>Card Details</legend>
      <div class="card-grid">
        <div v-for="it in cardFields" :key="it.field.name" class="card-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input
            :id="it.field.name"
            :type="it.dark ? 'password' : 'text'"
            :inputmode="it.numeric ? 'numeric' : undefined"
            :maxlength="it.field.length"
            :value="val(it.field.name)"
            :class="{ mono: it.numeric || it.field.name === 'CARDNUM' || it.field.name === 'CDACCT' }"
            @input="onInput(it.field.name, $event)"
          />
        </div>
      </div>
    </fieldset>

    <p v-if="message" class="msg" role="alert">
      <svg class="msg-icon" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <span>{{ message }}</span>
    </p>
    <p v-else class="msg-placeholder" aria-hidden="true"></p>

    <nav class="pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        @click="send(b.aidKey)"
        :title="b.aidKey"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.occarda-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.page-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.head-title {
  display: flex;
  align-items: center;
  gap: .6rem;
}

.head-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: var(--color-primary);
  flex-shrink: 0;
}

.head-title h1 {
  font-size: 1.35rem;
  margin: 0;
  font-weight: 700;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem 1.5rem;
  margin: 0;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: .4rem;
  font-size: .8rem;
}

.meta-item dt {
  color: var(--color-text-muted);
  font-weight: 600;
}

.meta-item dd {
  margin: 0;
  font-family: monospace;
  color: var(--color-text);
}

.card-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin: 0 0 1.5rem;
}

.card-fieldset legend {
  font-size: .85rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .5rem;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem 1.5rem;
}

.card-field {
  display: flex;
  flex-direction: column;
  gap: .35rem;
}

.card-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
}

.card-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-size: .95rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color .2s, box-shadow .2s;
}

.card-field input.mono {
  font-family: monospace;
  letter-spacing: .03em;
}

.card-field input:hover {
  border-color: var(--color-primary);
}

.card-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

@media (prefers-reduced-motion: reduce) {
  .card-field input {
    transition: none;
  }
}

.msg,
.msg-placeholder {
  min-height: 1.6rem;
  margin: 0 0 1rem;
  display: flex;
  align-items: center;
  gap: .5rem;
}

.msg {
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
}

.msg-icon {
  width: 1.1rem;
  height: 1.1rem;
  flex-shrink: 0;
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
  font-size: .9rem;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .2s, border-color .2s, color .2s;
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
  opacity: .9;
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }

  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
