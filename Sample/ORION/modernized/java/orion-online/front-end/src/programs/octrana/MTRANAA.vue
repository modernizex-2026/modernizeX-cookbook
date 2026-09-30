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
  time: props.values['CURTIME'] ?? ''
}))

const items = computed(() => namedFields(allFields.value, new Set())
  .filter((f: Field) => f.name !== 'TRNNAME' && f.name !== 'PGMNAME' && f.name !== 'CURDATE'
    && f.name !== 'CURTIME' && f.name !== 'ERRMSG')
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

function val(name: string): string { return props.values[name] ?? '' }
function onInput(name: string, e: Event) {
  const v = (e.target as HTMLInputElement).value
  props.values[name] = v
  if (typed) typed.value[name] = v
}
</script>

<template>
  <section class="tx-page">
    <header class="tx-head">
      <div class="tx-title-row">
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

    <fieldset class="tx-fieldset">
      <legend>Transaction Details</legend>
      <div class="tx-grid">
        <div v-for="it in items" :key="it.field.name" class="tx-field">
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

    <p v-if="message" class="msg" role="alert">
      <svg class="msg-icon" viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
        <path fill="currentColor" d="M10 1.5a8.5 8.5 0 1 0 0 17 8.5 8.5 0 0 0 0-17Zm.75 12.25h-1.5v-1.5h1.5v1.5Zm0-3h-1.5V5.25h1.5v5.5Z" />
      </svg>
      <span>{{ message }}</span>
    </p>
    <p v-else class="msg-placeholder" aria-hidden="true">&nbsp;</p>

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
.tx-page {
  max-width: var(--screen-max-width, 960px);
  margin: 2rem auto;
  padding: 0 1rem;
  color: var(--color-text);
}

.tx-head {
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.tx-title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: .5rem;
}
.tx-title-row h1 {
  font-size: 1.4rem;
  margin: 0;
}
.prog-tag {
  font: 600 .75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .3rem .6rem;
  white-space: nowrap;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  margin: 0;
  font-size: .85rem;
}
.meta-item {
  display: flex;
  gap: .35rem;
  align-items: baseline;
}
.meta-item dt {
  font-weight: 600;
  color: var(--color-text-muted);
}
.meta-item dd {
  margin: 0;
  font-family: monospace;
  color: var(--color-text);
}

.tx-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.25rem 1.25rem .5rem;
  margin: 0 0 1.25rem;
}
.tx-fieldset legend {
  font-size: .85rem;
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.tx-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: .9rem 1.5rem;
  margin-bottom: .75rem;
}

.tx-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
}
.tx-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
}
.tx-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color .2s, box-shadow .2s;
}
.tx-field input:hover {
  border-color: var(--color-primary);
}
.tx-field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}
@media (prefers-reduced-motion: reduce) {
  .tx-field input { transition: none; }
}

.msg,
.msg-placeholder {
  min-height: 1.6rem;
  display: flex;
  align-items: center;
  gap: .4rem;
  margin: 0 0 1rem;
}
.msg {
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
}
.msg-icon {
  flex-shrink: 0;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: .9rem;
}
.pf-btn {
  cursor: pointer;
  padding: .5rem 1.1rem;
  font: inherit;
  font-weight: 500;
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
  color: var(--color-surface);
  border-color: var(--color-primary);
}
.pf-btn.primary:hover {
  opacity: .9;
  color: var(--color-surface);
}
@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .tx-grid { grid-template-columns: 1fr; }
  .tx-title-row { flex-direction: column; align-items: flex-start; gap: .3rem; }
}
</style>
