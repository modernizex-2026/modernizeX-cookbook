<script setup lang="ts">
import { computed, inject, ref } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function val(name: string): string { return props.values[name] ?? '' }
function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const named = computed(() => namedFields(allFields.value, cellNames.value)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

function pick(name: string) { return named.value.find(it => it.field.name === name) }

const optionItem = computed(() => pick('OPTION'))
const parmItem = computed(() => pick('PARM'))

const statusFields = ['RSTAT', 'RMSG'].map(n => pick(n)).filter((x): x is NonNullable<typeof x> => !!x)
const errItem = pick('ERRMSG')

const resultNames = ['RCREAD', 'RCSEL', 'RCPOST', 'RCUPD', 'RCREJ', 'RCTRAN', 'RAMT1', 'RAMT2']
const resultItems = computed(() => resultNames
  .map(n => pick(n))
  .filter((x): x is NonNullable<typeof x> => !!x)
  .filter(it => val(it.field.name).trim() !== ''))

const OPT_RE = /^\s*(\d+)\s*[.．)）]\s+(.+)/
const menuOptions = computed(() => allFields.value
  .filter((f: Field) => f.name === '' && OPT_RE.test((f.initial ?? '')))
  .map((f: Field) => { const m = OPT_RE.exec(f.initial ?? '')!; return { code: m[1], text: m[2].trim() } }))

const helpNote = computed<string>(() => {
  const f = allFields.value.find((f: Field) => f.name === '' && f.row === 16)
  const raw = (f?.initial ?? '').trim()
  const paren = /\([^)]*\)/.exec(raw)
  return paren ? paren[0] : raw
})

const parmInputEl = ref<HTMLInputElement | null>(null)
function chooseOption(code: string) {
  setVal('OPTION', code)
  parmInputEl.value?.focus()
}

function runScreen() { props.send('ENTER') }
</script>

<template>
  <section class="ops-page">
    <header class="ops-meta">
      <span class="meta-item"><span class="meta-key">Tran</span><span class="meta-val">{{ val('TRNNAME') }}</span></span>
      <span class="meta-item"><span class="meta-key">Pgm</span><span class="meta-val">{{ val('PGMNAME') }}</span></span>
      <span class="meta-item"><span class="meta-key">Date</span><span class="meta-val">{{ val('CURDATE') }}</span></span>
      <span class="meta-item"><span class="meta-key">Time</span><span class="meta-val">{{ val('CURTIME') }}</span></span>
    </header>

    <h1 class="ops-title">{{ title }}</h1>

    <div class="ops-layout">
      <fieldset class="ops-menu-panel">
        <legend>Batch Operations</legend>
        <ul class="ops-menu">
          <li v-for="o in menuOptions" :key="o.code">
            <button
              type="button"
              class="ops-opt"
              :class="{ 'is-selected': val('OPTION').trim() === o.code }"
              @click="chooseOption(o.code)"
            >
              <span class="ops-opt-num">{{ o.code }}</span>
              <span class="ops-opt-text">{{ o.text }}</span>
              <svg class="ops-opt-icon" viewBox="0 0 20 20" width="16" height="16" aria-hidden="true" focusable="false">
                <path d="M7 4l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </button>
          </li>
        </ul>
      </fieldset>

      <fieldset class="ops-run-panel">
        <legend>Run</legend>

        <div class="ops-run-fields">
          <div class="ops-field" v-if="optionItem">
            <label for="OPTION">{{ optionItem.label || 'Option' }}</label>
            <input
              id="OPTION"
              type="text"
              inputmode="numeric"
              :maxlength="optionItem.field.length"
              :value="val('OPTION')"
              @input="onInput('OPTION', $event)"
            />
          </div>

          <div class="ops-field ops-field--wide" v-if="parmItem">
            <label for="PARM">{{ parmItem.label || 'Param' }}</label>
            <input
              id="PARM"
              ref="parmInputEl"
              type="text"
              :maxlength="parmItem.field.length"
              :value="val('PARM')"
              @input="onInput('PARM', $event)"
            />
          </div>
        </div>

        <p v-if="helpNote" class="ops-note">{{ helpNote }}</p>

        <button type="button" class="ops-run-btn" @click="runScreen">Run</button>

        <div v-if="statusFields.length && statusFields.some(it => val(it.field.name).trim() !== '')" class="ops-status">
          <span v-for="it in statusFields" :key="it.field.name" class="ops-status-part">{{ val(it.field.name) }}</span>
        </div>

        <p v-if="errItem && val('ERRMSG').trim() !== ''" class="ops-error" role="alert">{{ val('ERRMSG') }}</p>
      </fieldset>
    </div>

    <fieldset v-if="resultItems.length" class="ops-results-panel">
      <legend>Results</legend>
      <div class="ops-results-grid">
        <div v-for="it in resultItems" :key="it.field.name" class="ops-result">
          <span class="ops-result-label">{{ it.label }}</span>
          <output class="ops-result-value">{{ val(it.field.name) }}</output>
        </div>
      </div>
    </fieldset>

    <p v-if="message" class="ops-message">{{ message }}</p>

    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.ops-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.25rem 1.5rem 1.75rem;
  color: var(--color-text);
}

.ops-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  font-size: .8rem;
  color: var(--color-text-muted);
  padding-bottom: .6rem;
  margin-bottom: .75rem;
  border-bottom: 1px solid var(--color-border);
}
.meta-item { display: inline-flex; gap: .4rem; align-items: baseline; }
.meta-key { font-weight: 600; color: var(--color-primary); }
.meta-val { font-family: var(--font-mono, monospace); }

.ops-title {
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0 0 1.25rem;
  color: var(--color-text);
}

.ops-layout {
  display: grid;
  grid-template-columns: minmax(260px, 1.1fr) minmax(280px, 1fr);
  gap: 1.25rem;
  margin-bottom: 1.25rem;
}

fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1rem 1.1rem 1.15rem;
  margin: 0;
}
legend {
  font-size: .78rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .04em;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.ops-menu { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: .45rem; }
.ops-opt {
  width: 100%;
  display: flex;
  align-items: center;
  gap: .65rem;
  padding: .6rem .75rem;
  font: inherit;
  text-align: left;
  cursor: pointer;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: background .18s, border-color .18s, transform .18s;
}
.ops-opt-num {
  flex: 0 0 auto;
  font-family: var(--font-mono, monospace);
  font-weight: 700;
  color: var(--color-primary);
  min-width: 1.3rem;
}
.ops-opt-text { flex: 1 1 auto; }
.ops-opt-icon { flex: 0 0 auto; color: var(--color-text-muted); }
.ops-opt:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.ops-opt:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.ops-opt.is-selected {
  border-color: var(--color-primary);
  background: var(--c-blue-50);
}
.ops-opt.is-selected .ops-opt-icon { color: var(--color-primary); }
@media (prefers-reduced-motion: reduce) { .ops-opt { transition: none; } }

.ops-run-fields { display: flex; flex-wrap: wrap; gap: .85rem; margin-bottom: .75rem; }
.ops-field { display: flex; flex-direction: column; gap: .3rem; flex: 0 0 110px; }
.ops-field--wide { flex: 1 1 200px; }
.ops-field label { font-size: .78rem; font-weight: 600; color: var(--color-primary); }
.ops-field input {
  padding: .5rem .6rem;
  font: inherit;
  font-family: var(--font-mono, monospace);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color .18s, box-shadow .18s;
}
.ops-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
@media (prefers-reduced-motion: reduce) { .ops-field input { transition: none; } }

.ops-note { font-size: .8rem; color: var(--color-text-muted); margin: 0 0 .85rem; }

.ops-run-btn {
  cursor: pointer;
  padding: .55rem 1.4rem;
  font: inherit;
  font-weight: 600;
  color: var(--c-white);
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  transition: opacity .18s, transform .18s;
}
.ops-run-btn:hover { opacity: .9; }
.ops-run-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .ops-run-btn { transition: none; } }

.ops-status {
  margin-top: .9rem;
  padding: .5rem .7rem;
  background: var(--c-blue-50);
  border-radius: 6px;
  color: var(--color-success);
  font-family: var(--font-mono, monospace);
  font-size: .85rem;
  display: flex;
  gap: .6rem;
  flex-wrap: wrap;
}

.ops-error {
  margin-top: .75rem;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .88rem;
}

.ops-results-panel { margin-bottom: 1.1rem; }
.ops-results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: .75rem 1.25rem;
}
.ops-result { display: flex; flex-direction: column; gap: .2rem; }
.ops-result-label { font-size: .75rem; font-weight: 600; color: var(--color-text-muted); }
.ops-result-value {
  font-family: var(--font-mono, monospace);
  font-size: .95rem;
  color: var(--color-success);
}

.ops-message {
  color: var(--color-warning);
  font-weight: 600;
  min-height: 1.2em;
  margin: .75rem 0;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  margin-top: 1rem;
  border-top: 1px solid var(--color-border);
  padding-top: .85rem;
}
.pf button {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .18s, border-color .18s;
}
.pf button:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf button:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .pf button { transition: none; } }

@media (max-width: 768px) {
  .ops-layout { grid-template-columns: 1fr; }
  .ops-meta { gap: .75rem; }
  .ops-field { flex: 1 1 100%; }
}
</style>
