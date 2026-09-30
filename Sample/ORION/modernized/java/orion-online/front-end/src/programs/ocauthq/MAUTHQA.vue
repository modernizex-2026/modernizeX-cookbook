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

const inputNames = ['CARDNUM', 'AMOUNT', 'MERCH']
const outputNames = ['DECISN', 'REASON', 'AVAIL']

const inputItems = computed(() => inputNames
  .map((n: string) => allFields.value.find((f: Field) => f.name === n))
  .filter((f): f is Field => !!f)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

const outputItems = computed(() => outputNames
  .map((n: string) => allFields.value.find((f: Field) => f.name === n))
  .filter((f): f is Field => !!f)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f) }))
  .filter(it => (props.values[it.field.name] ?? '').trim() !== '' || (it.field.initial ?? '').trim() !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

function submit() { props.send('ENTER') }
</script>

<template>
  <section class="auth-page">
    <header class="auth-head">
      <div class="head-left">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item" v-if="meta.tran"><dt>Tran</dt><dd>{{ meta.tran }}</dd></div>
        <div class="meta-item" v-if="meta.pgm"><dt>Pgm</dt><dd>{{ meta.pgm }}</dd></div>
        <div class="meta-item" v-if="meta.date"><dt>Date</dt><dd>{{ meta.date }}</dd></div>
        <div class="meta-item" v-if="meta.time"><dt>Time</dt><dd>{{ meta.time }}</dd></div>
      </dl>
    </header>

    <fieldset class="auth-fieldset">
      <legend>Transaction Details</legend>
      <div class="auth-grid">
        <div v-for="it in inputItems" :key="it.field.name" class="auth-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input
            :id="it.field.name"
            :type="it.dark ? 'password' : 'text'"
            :inputmode="it.numeric ? 'numeric' : undefined"
            :maxlength="it.field.length"
            :value="val(it.field.name)"
            class="mono-field"
            @input="onInput(it.field.name, $event)"
          />
        </div>
      </div>
    </fieldset>

    <fieldset v-if="outputItems.length" class="auth-fieldset result-set">
      <legend>Authorization Result</legend>
      <div class="auth-grid">
        <div v-for="it in outputItems" :key="it.field.name" class="auth-field">
          <span class="out-label">{{ it.label }}</span>
          <output :id="it.field.name" class="mono-field out-value">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
        </div>
      </div>
    </fieldset>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>
    <p v-else class="msg msg-placeholder" aria-hidden="true">&nbsp;</p>

    <nav class="pf">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        :title="b.aidKey"
        @click="b.aidKey === 'ENTER' ? submit() : send(b.aidKey)"
      >
        <svg v-if="b.aidKey === 'ENTER'" class="pf-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M4 10h10m0 0-4-4m4 4-4 4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.auth-page {
  max-width: var(--screen-max-width, 960px);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
  color: var(--color-text);
}

.auth-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.head-left { display: flex; align-items: baseline; gap: .75rem; }
.auth-head h1 { font-size: 1.4rem; margin: 0; font-weight: 700; }
.prog-tag {
  font: 600 .75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .3rem .6rem;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem 1.5rem;
  margin: 0;
  font-size: .8rem;
}
.meta-item { display: flex; gap: .35rem; align-items: baseline; }
.meta-item dt { color: var(--color-text-muted); font-weight: 600; }
.meta-item dd { margin: 0; font-family: monospace; }

.auth-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.1rem 1.25rem 1.25rem;
  margin: 0 0 1.25rem;
}
.auth-fieldset legend {
  padding: 0 .5rem;
  font-size: .85rem;
  font-weight: 700;
  color: var(--color-primary);
}
.result-set { background: var(--c-blue-50); }

.auth-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: .85rem 1.25rem;
}
.auth-field { display: flex; flex-direction: column; gap: .3rem; }
.auth-field label,
.out-label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
}

.mono-field {
  font-family: monospace;
  font-size: .95rem;
  padding: .5rem .6rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color .2s, box-shadow .2s;
}
.auth-field input.mono-field:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.out-value {
  background: transparent;
  border-color: transparent;
  padding-left: 0;
  font-weight: 600;
  color: var(--color-success);
}

.msg {
  min-height: 1.4rem;
  color: var(--color-danger);
  font-weight: 600;
  margin: .25rem 0 1rem;
}
.msg-placeholder { color: transparent; }

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
  padding: .5rem 1rem;
  font: inherit;
  font-weight: 600;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .2s, border-color .2s, color .2s;
}
.pf-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--c-white);
}
.pf-btn.primary:hover { opacity: .9; }
.pf-icon { width: 1rem; height: 1rem; }

@media (prefers-reduced-motion: reduce) {
  .mono-field, .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .auth-grid { grid-template-columns: 1fr; }
  .meta-strip { gap: .5rem 1rem; }
}
</style>
