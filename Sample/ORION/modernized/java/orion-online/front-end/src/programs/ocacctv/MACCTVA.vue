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

const inputItems = computed(() => items.value.filter(it => it.field.input))
const outputItems = computed(() => items.value.filter(it => !it.field.input && it.field.name !== 'ERRMSG'))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const meta = computed(() => ({
  trnname: val('TRNNAME'),
  pgmname: val('PGMNAME'),
  curdate: val('CURDATE'),
  curtime: val('CURTIME')
}))
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <div class="am-head-title">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item"><dt>Tran</dt><dd>{{ meta.trnname }}</dd></div>
        <div class="meta-item"><dt>Pgm</dt><dd>{{ meta.pgmname }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ meta.curdate }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ meta.curtime }}</dd></div>
      </dl>
    </header>

    <fieldset class="am-fieldset">
      <legend>Account Lookup</legend>
      <div class="rec-grid">
        <div v-for="it in inputItems" :key="it.field.name" class="rec-field">
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

    <fieldset v-if="outputItems.length" class="am-fieldset">
      <legend>Account Details</legend>
      <div class="rec-grid">
        <div v-for="it in outputItems" :key="it.field.name" class="rec-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <output :id="it.field.name" :class="{ mono: it.numeric }">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
        </div>
      </div>
    </fieldset>

    <p v-if="message" class="msg" role="alert">
      <svg v-if="message" class="msg-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <path d="M10 2a8 8 0 100 16 8 8 0 000-16zm0 5v4m0 3h.01" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      {{ message }}
    </p>
    <p v-else class="msg-placeholder" aria-hidden="true"></p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              class="pf-btn" :class="{ primary: b.aidKey === 'ENTER' }"
              @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem clamp(1rem, 3vw, 2.5rem);
  color: var(--color-text);
}

.am-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.am-head-title { display: flex; align-items: baseline; gap: .75rem; }
.am-head h1 { font-size: 1.4rem; margin: 0; font-weight: 700; }
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
  gap: 0 1.25rem;
  margin: 0;
  font-size: .8rem;
}
.meta-item { display: flex; gap: .35rem; align-items: baseline; }
.meta-item dt { color: var(--color-text-muted); font-weight: 600; }
.meta-item dd { margin: 0; font-family: monospace; color: var(--color-text); }

.am-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0 0 1.25rem;
}
.am-fieldset legend {
  font-size: .8rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: .9rem 1.5rem;
}
.rec-field { display: flex; flex-direction: column; gap: .3rem; }
.rec-field label { font-size: .8rem; font-weight: 600; color: var(--color-text-muted); }
.rec-field input {
  padding: .55rem .65rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 200ms, box-shadow 200ms;
}
.rec-field input:hover { border-color: var(--color-primary); }
.rec-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.rec-field output {
  padding: .55rem .1rem;
  font: inherit;
  color: var(--color-text);
  border-bottom: 1px dashed var(--color-border);
}
.rec-field output.mono { font-family: monospace; font-variant-numeric: tabular-nums; }

.msg, .msg-placeholder {
  min-height: 1.5rem;
  margin: 0 0 1rem;
  display: flex;
  align-items: center;
  gap: .4rem;
}
.msg {
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
}
.msg-icon { width: 1.1rem; height: 1.1rem; flex-shrink: 0; }

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
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, transform 150ms;
}
.pf-btn:hover { border-color: var(--color-primary); background: var(--c-blue-50); }
.pf-btn:active { transform: translateY(1px); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--color-surface);
  border-color: var(--color-primary);
}
.pf-btn.primary:hover { opacity: .9; background: var(--color-primary); }

@media (prefers-reduced-motion: reduce) {
  .rec-field input, .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .am-head { flex-direction: column; }
  .rec-grid { grid-template-columns: 1fr; }
}
</style>
