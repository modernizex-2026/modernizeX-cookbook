<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const trnName = computed(() => props.values.TRNNAME ?? '')
const curDate = computed(() => props.values.CURDATE ?? '')
const pgmName = computed(() => props.values.PGMNAME ?? '')
const curTime = computed(() => props.values.CURTIME ?? '')

const items = computed(() => namedFields(allFields.value, new Set())
  .filter((f: Field) => !['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME', 'ERRMSG'].includes(f.name))
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input
    || (props.values[it.field.name] ?? '').trim() !== ''
    || (it.field.initial ?? '').trim() !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function onInput(name: string, e: Event) {
  const v = (e.target as HTMLInputElement).value
  props.values[name] = v
  if (typed) typed.value[name] = v
}
</script>

<template>
  <section class="am-page">
    <header class="am-meta">
      <div class="am-meta-left">
        <span class="meta-pair"><span class="meta-label">Tran</span><span class="meta-val">{{ trnName }}</span></span>
        <span class="meta-pair"><span class="meta-label">Pgm</span><span class="meta-val">{{ pgmName }}</span></span>
      </div>
      <h1 class="am-title">{{ title }}</h1>
      <div class="am-meta-right">
        <span class="meta-pair"><span class="meta-label">Date</span><span class="meta-val">{{ curDate }}</span></span>
        <span class="meta-pair"><span class="meta-label">Time</span><span class="meta-val">{{ curTime }}</span></span>
      </div>
    </header>

    <fieldset class="rec-fieldset">
      <legend>Account Details</legend>
      <div class="rec-grid">
        <div v-for="it in items" :key="it.field.name" class="rec-field">
          <label v-if="it.label" :for="it.field.name">{{ it.label }}</label>
          <input v-if="it.field.input" :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
          <output v-else :id="it.field.name">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
        </div>
      </div>
    </fieldset>

    <p v-if="message" class="msg" role="alert">
      <svg class="msg-icon" viewBox="0 0 20 20" aria-hidden="true" focusable="false">
        <path fill="currentColor" d="M10 1a9 9 0 1 0 0 18 9 9 0 0 0 0-18Zm.75 13.25h-1.5v-1.5h1.5v1.5Zm0-3h-1.5V5.75h1.5v5.5Z" />
      </svg>
      {{ message }}
    </p>
    <p v-else class="msg msg-placeholder" aria-hidden="true">&nbsp;</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              class="pf-btn" :class="{ primary: b.aidKey === 'ENTER' }"
              @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.am-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.am-meta-left,
.am-meta-right {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.meta-pair {
  display: inline-flex;
  align-items: baseline;
  gap: .35rem;
  font-size: .8rem;
}

.meta-label {
  color: var(--color-text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .03em;
}

.meta-val {
  font-family: monospace;
  color: var(--color-primary);
}

.am-title {
  font-size: 1.4rem;
  margin: 0;
  order: -1;
  flex-basis: 100%;
  text-align: center;
}

@media (min-width: 600px) {
  .am-title { order: 0; flex-basis: auto; }
}

.rec-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.25rem 1rem;
  margin: 0 0 1.25rem;
}

.rec-fieldset legend {
  padding: 0 .5rem;
  font-weight: 600;
  color: var(--color-primary);
  font-size: .9rem;
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem 1.5rem;
}

.rec-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
}

.rec-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
}

.rec-field input,
.rec-field output {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 200ms, box-shadow 200ms;
}

.rec-field input:hover {
  border-color: var(--color-primary);
}

.rec-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.rec-field output {
  background: transparent;
  border-color: transparent;
  padding-left: 0;
  color: var(--color-text);
}

@media (prefers-reduced-motion: reduce) {
  .rec-field input { transition: none; }
}

.msg {
  display: flex;
  align-items: center;
  gap: .5rem;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
  min-height: 1.5rem;
  margin: 0 0 1rem;
}

.msg-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.msg-placeholder {
  color: transparent;
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
  font-weight: 500;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, transform 150ms;
}

.pf-btn:hover {
  border-color: var(--color-primary);
  background: var(--c-blue-50);
}

.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.pf-btn:active {
  transform: scale(0.98);
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
  .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .am-page { padding: 1rem .85rem 1.5rem; }
  .rec-grid { grid-template-columns: 1fr; }
  .am-meta { flex-direction: column; align-items: flex-start; gap: .5rem; }
}
</style>
