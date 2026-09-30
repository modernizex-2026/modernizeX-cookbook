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

const acctField = computed(() => items.value.find(it => it.field.name === 'ACCTID'))
const detailFields = computed(() => items.value.filter(it => it.field.name !== 'ACCTID' && it.label))

const trnname = computed(() => val('TRNNAME'))
const pgmname = computed(() => val('PGMNAME'))
const curdate = computed(() => val('CURDATE'))
const curtime = computed(() => val('CURTIME'))
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <h1>{{ title }}</h1>
      <span class="prog-tag">{{ program }}</span>
    </header>

    <div class="meta-strip">
      <span class="meta-item"><span class="meta-label">Tran</span><span class="meta-val">{{ trnname }}</span></span>
      <span class="meta-item"><span class="meta-label">Pgm</span><span class="meta-val">{{ pgmname }}</span></span>
      <span class="meta-item"><span class="meta-label">Date</span><span class="meta-val">{{ curdate }}</span></span>
      <span class="meta-item"><span class="meta-label">Time</span><span class="meta-val">{{ curtime }}</span></span>
    </div>

    <fieldset v-if="acctField" class="lookup-group">
      <legend>Account Lookup</legend>
      <div class="lookup-row">
        <label for="ACCTID">{{ acctField.label }}</label>
        <input
          id="ACCTID"
          type="text"
          :inputmode="acctField.numeric ? 'numeric' : undefined"
          :maxlength="acctField.field.length"
          :value="val('ACCTID')"
          autofocus
          placeholder="Enter account ID"
          @input="onInput('ACCTID', $event)"
        />
      </div>
    </fieldset>

    <fieldset v-if="detailFields.length" class="detail-group">
      <legend>Account Details</legend>
      <div class="rec-grid">
        <div v-for="it in detailFields" :key="it.field.name" class="rec-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <output :id="it.field.name" class="mono-val">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
        </div>
      </div>
    </fieldset>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>

    <nav class="pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        :title="b.aidKey"
        @click="send(b.aidKey)"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
  color: var(--color-text);
}

.am-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: 1rem;
}
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
  gap: 1.25rem;
  font-size: .82rem;
  color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.meta-item { display: inline-flex; gap: .35rem; align-items: baseline; }
.meta-label { font-weight: 600; color: var(--color-primary); }
.meta-val { font-family: monospace; }

fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0 0 1.25rem;
}
legend {
  font-size: .78rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .04em;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.lookup-row { display: flex; flex-wrap: wrap; align-items: center; gap: .75rem; }
.lookup-row label { font-size: .9rem; font-weight: 600; color: var(--color-text); min-width: 90px; }
.lookup-row input {
  flex: 1 1 220px;
  max-width: 320px;
  padding: .55rem .7rem;
  font: 500 1rem/1.2 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color .18s, box-shadow .18s;
}
.lookup-row input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem 1.5rem;
}
.rec-field { display: flex; flex-direction: column; gap: .25rem; }
.rec-field label { font-size: .78rem; font-weight: 600; color: var(--color-text-muted); }
.mono-val {
  font: 500 .95rem/1.3 monospace;
  color: var(--color-text);
  padding: .3rem 0;
  border-bottom: 1px dashed var(--color-border);
}

.msg {
  color: var(--color-danger);
  font-weight: 600;
  min-height: 1.3em;
  margin: 0 0 1rem;
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
  padding: .5rem 1.1rem;
  font: 600 .9rem/1 inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .18s, border-color .18s, transform .12s;
}
.pf-btn:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf-btn:active { transform: translateY(1px); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.pf-btn.primary:hover { filter: brightness(1.08); }

@media (prefers-reduced-motion: reduce) {
  .pf-btn, .lookup-row input { transition: none; }
}

@media (max-width: 768px) {
  .am-head { flex-direction: column; align-items: flex-start; gap: .35rem; }
  .lookup-row { flex-direction: column; align-items: stretch; }
  .lookup-row label { min-width: 0; }
  .lookup-row input { max-width: none; }
}
</style>
