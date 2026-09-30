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

const OPT_RE = /^\s*(\d+)\s*[.．)）]\s+(.+)/
const optionField = computed<string>(() => 'UTOPT')

const looseOut = computed(() => items.value.filter(it => !it.field.input && !it.label))
const options = computed(() => looseOut.value
  .map(it => { const v = val(it.field.name); const m = OPT_RE.exec(v || (it.field.initial ?? '')); return m ? { code: m[1], text: (v || it.field.initial || '').trim(), name: it.field.name } : null })
  .filter((o): o is { code: string; text: string; name: string } => o !== null))

const inputFields = computed(() => items.value.filter(it => it.field.input))
const resultLines = computed(() => allFields.value
  .filter((f: Field) => ['RLINE1', 'RLINE2', 'RLINE3', 'RLINE4'].includes(f.name))
  .map((f: Field) => val(f.name))
  .filter(v => v.trim() !== ''))

const trnName = computed(() => val('TRNNAME'))
const pgmName = computed(() => val('PGMNAME'))
const curDate = computed(() => val('CURDATE'))
const curTime = computed(() => val('CURTIME'))
const rstat = computed(() => val('RSTAT'))
const rmsg = computed(() => val('RMSG'))
const errMsg = computed(() => val('ERRMSG'))

function choose(code: string) { setVal(optionField.value, code); props.send('ENTER') }
</script>

<template>
  <section class="util-page">
    <header class="util-head">
      <div class="head-left">
        <h1>{{ title }}</h1>
        <span class="prog-tag" v-if="trnName || pgmName">{{ trnName }}<span v-if="pgmName"> · {{ pgmName }}</span></span>
      </div>
      <div class="head-meta" v-if="curDate || curTime">
        <span v-if="curDate">{{ curDate }}</span>
        <span v-if="curTime">{{ curTime }}</span>
      </div>
    </header>

    <div class="util-body">
      <fieldset class="menu-panel">
        <legend>Available Utilities</legend>
        <div class="rec-menu">
          <button
            v-for="o in options"
            :key="o.name"
            type="button"
            class="rec-opt"
            :class="{ active: val('UTOPT') === o.code }"
            @click="choose(o.code)"
          >
            <span class="opt-num" aria-hidden="true">{{ o.code }}</span>
            <span class="opt-text">{{ o.text.replace(/^\s*\d+\s*[.．)）]\s*/, '') }}</span>
          </button>
        </div>
      </fieldset>

      <fieldset class="params-panel">
        <legend>Run Parameters</legend>
        <div class="rec-grid">
          <div v-for="it in inputFields" :key="it.field.name" class="rec-field">
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

      <div v-if="rstat || rmsg" class="status-strip" :class="{ ok: rstat?.trim().toUpperCase() === 'OK' }">
        <svg v-if="rstat" class="status-icon" viewBox="0 0 20 20" aria-hidden="true">
          <circle cx="10" cy="10" r="9" fill="none" stroke="currentColor" stroke-width="1.5" />
          <path d="M6 10l2.5 2.5L14 7" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        <span v-if="rstat" class="status-code">{{ rstat }}</span>
        <span v-if="rmsg" class="status-text">{{ rmsg }}</span>
      </div>

      <div v-if="resultLines.length" class="result-panel">
        <p v-for="(line, i) in resultLines" :key="i" class="result-line">{{ line }}</p>
      </div>
    </div>

    <p v-if="errMsg" class="err-msg" role="alert">{{ errMsg }}</p>
    <p v-if="message" class="msg">{{ message }}</p>

    <nav class="pf">
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
.util-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.util-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.head-left { display: flex; align-items: baseline; gap: .75rem; flex-wrap: wrap; }
.util-head h1 { font-size: 1.4rem; margin: 0; letter-spacing: .01em; }
.prog-tag {
  font: 600 .75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .3rem .6rem;
}
.head-meta {
  display: flex;
  gap: 1rem;
  font: 500 .8rem/1 monospace;
  color: var(--color-text-muted);
}

.util-body { display: flex; flex-direction: column; gap: 1.5rem; }

fieldset { border: 1px solid var(--color-border); border-radius: 10px; padding: 1rem 1.1rem 1.15rem; margin: 0; }
legend { font-size: .85rem; font-weight: 700; color: var(--color-primary); padding: 0 .4rem; }

.rec-menu { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: .6rem; }
.rec-opt {
  display: flex;
  align-items: center;
  gap: .75rem;
  text-align: left;
  padding: .65rem .9rem;
  font: inherit;
  cursor: pointer;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: background 200ms, border-color 200ms, transform 150ms;
}
.rec-opt:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.rec-opt:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.rec-opt.active { border-color: var(--color-primary); background: var(--c-blue-50); }
.opt-num {
  font: 700 .8rem/1 monospace;
  color: var(--color-primary);
  background: var(--color-surface);
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  min-width: 1.6rem;
  height: 1.6rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.opt-text { font-size: .92rem; }

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: .85rem 1.25rem;
}
.rec-field { display: flex; flex-direction: column; gap: .3rem; }
.rec-field label { font-size: .8rem; font-weight: 600; color: var(--color-primary); }
.rec-field input {
  padding: .5rem .6rem;
  font: 500 .95rem/1.2 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 150ms, box-shadow 150ms;
}
.rec-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.status-strip {
  display: flex;
  align-items: center;
  gap: .6rem;
  padding: .6rem .9rem;
  border-radius: 8px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-primary);
  min-height: 2.5rem;
}
.status-strip.ok { color: var(--color-success); border-color: var(--color-success); }
.status-icon { width: 1.1rem; height: 1.1rem; flex-shrink: 0; }
.status-code { font: 700 .8rem/1 monospace; }
.status-text { font-size: .9rem; }

.result-panel {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: .85rem 1rem;
  background: var(--color-surface);
}
.result-line {
  font: 500 .88rem/1.5 monospace;
  color: var(--color-text);
  margin: 0;
}
.result-line + .result-line { margin-top: .3rem; }

.err-msg {
  color: var(--color-danger);
  font-weight: 600;
  margin: 1rem 0 0;
  min-height: 1.2rem;
}
.msg {
  color: var(--color-warning);
  font-weight: 600;
  margin: .5rem 0 0;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  margin-top: 1.5rem;
  border-top: 1px solid var(--color-border);
  padding-top: .9rem;
}
.pf-btn {
  cursor: pointer;
  padding: .5rem 1rem;
  font: 600 .9rem/1 inherit;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, transform 150ms;
}
.pf-btn:hover { border-color: var(--color-primary); transform: translateY(-1px); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.pf-btn.primary:hover { opacity: .92; }

@media (prefers-reduced-motion: reduce) {
  .rec-opt, .pf-btn, .rec-field input { transition: none; }
  .pf-btn:hover { transform: none; }
}

@media (max-width: 768px) {
  .util-page { padding: 1rem .85rem 1.5rem; }
  .util-head { flex-direction: column; align-items: flex-start; gap: .5rem; }
  .rec-grid { grid-template-columns: 1fr; }
  .rec-menu { grid-template-columns: 1fr; }
}
</style>
