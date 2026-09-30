<script setup lang="ts">
// RecordScreen — deterministic 'form/dialog/display/list' archetype (bms-screen-modernize).
// Renders labeled field pairs (label = nearest left literal on the same row) plus any
// record-list regions as tables. Depends ONLY on the foundation + screenModel helpers.
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from './screenModel'
import NumericInput from '../../components/NumericInput.vue'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))
const items = computed(() => namedFields(allFields.value, cellNames.value)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  // keep every input; drop output fields that have no value AND no real initial
  // (avoids empty labelled rows like OPTN007..012 on menu-style screens).
  .filter(it => it.field.input
    || (props.values[it.field.name] ?? '').trim() !== ''
    || (it.field.initial ?? '').trim() !== ''))
const lists = computed<Array<{ columns: string[]; headers?: string[]; rows: Array<Record<string, string>> }>>(
  () => (props.screen?.recordLists ?? []))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }

// A field's label may be empty (unlabelled output). Split so the layout reads right:
//  - gridItems : inputs + labelled fields → the label/value grid.
//  - options   : unlabelled OUTPUT whose runtime value is a numbered option ("1. …") →
//                clickable menu items (handles DYNAMIC menus the manifest can't pre-detect).
//  - textLines : the remaining unlabelled outputs (titles/notes) → plain lines.
const OPT_RE = /^\s*(\d+)\s*[.．)）]\s+(.+)/
const optionField = computed<string>(() => allFields.value.find((f: Field) => f.input)?.name ?? '')
const gridItems = computed(() => items.value.filter(it => it.field.input || it.label))
const looseOut = computed(() => items.value.filter(it => !it.field.input && !it.label))
const options = computed(() => looseOut.value
  .map(it => { const v = val(it.field.name); const m = OPT_RE.exec(v); return m ? { code: m[1], text: v, name: it.field.name } : null })
  .filter((o): o is { code: string; text: string; name: string } => o !== null))
const textLines = computed(() => looseOut.value.filter(it => { const v = val(it.field.name); return v !== '' && !OPT_RE.test(v) }))
function choose(code: string) { if (optionField.value) { setVal(optionField.value, code); props.send('ENTER') } }
</script>

<template>
  <section class="am-page">
    <header class="am-head"><h1>{{ title }}</h1><span class="prog-tag">{{ program }}</span></header>

    <p v-for="it in textLines" :key="'t_' + it.field.name" class="rec-note">{{ val(it.field.name) }}</p>

    <div v-if="gridItems.length" class="rec-grid">
      <div v-for="it in gridItems" :key="it.field.name" class="rec-field" :class="{ 'no-label': !it.label }">
        <label v-if="it.label" :for="it.field.name">{{ it.label }}</label>
        <NumericInput v-if="it.field.input && it.numeric" :id="it.field.name"
                       :model-value="val(it.field.name)" :picture="it.field.picIn || undefined"
                       :max-width="it.field.length"
                       @update:model-value="(v: string) => setVal(it.field.name, v)" />
        <input v-else-if="it.field.input" :id="it.field.name"
               :type="it.dark ? 'password' : 'text'"
               :inputmode="it.numeric ? 'numeric' : undefined"
               :maxlength="it.field.length"
               :value="val(it.field.name)"
               @input="onInput(it.field.name, $event)" />
        <output v-else :id="it.field.name">{{ val(it.field.name) || (it.field.initial || '').trim() }}</output>
      </div>
    </div>

    <div v-if="options.length" class="rec-menu">
      <button v-for="o in options" :key="o.name" type="button" class="rec-opt" @click="choose(o.code)">{{ o.text }}</button>
    </div>

    <table v-for="(rl, ri) in lists" :key="'rl' + ri" class="rec-table">
      <thead><tr><th v-for="(c, ci) in rl.columns" :key="c">{{ (rl.headers && rl.headers[ci]) || c }}</th></tr></thead>
      <tbody>
        <tr v-for="(row, idx) in rl.rows" :key="idx">
          <td v-for="c in rl.columns" :key="c">
            <input v-if="fieldByName(row[c])?.input"
                   :value="val(row[c])" :maxlength="fieldByName(row[c])?.length"
                   @input="onInput(row[c], $event)" />
            <span v-else>{{ val(row[c]) }}</span>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-if="message" class="msg">{{ message }}</p>
    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page { max-width: var(--screen-max-width, 1280px); margin: 2rem auto; padding: 0 1rem; color: var(--color-text); }
.am-head { display: flex; align-items: baseline; justify-content: space-between; gap: 1rem;
  border-bottom: 2px solid var(--color-primary); padding-bottom: .5rem; margin-bottom: 1.25rem; }
.am-head h1 { font-size: 1.4rem; margin: 0; }
.prog-tag { font: 600 .75rem/1 monospace; color: var(--color-primary);
  background: var(--c-blue-50); border-radius: 999px; padding: .3rem .6rem; }
.rec-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: .75rem 1.25rem; margin-bottom: 1.25rem; }
.rec-field { display: flex; flex-direction: column; gap: .25rem; }
.rec-field label { font-size: .8rem; font-weight: 600; color: var(--color-primary); }
.rec-field input, .rec-field output { padding: .45rem .55rem; font: inherit;
  border: 1px solid var(--color-border); border-radius: 6px; background: var(--color-surface); }
.rec-field output { background: transparent; border-color: transparent; padding-left: 0; }
.rec-note { color: var(--color-text-muted); margin: .15rem 0; }
.rec-menu { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: .5rem; margin: 1rem 0 1.25rem; }
.rec-opt { text-align: left; padding: .7rem 1rem; font: inherit; cursor: pointer; color: var(--color-text);
  background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 8px; transition: background .15s, border-color .15s; }
.rec-opt:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.rec-opt:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .rec-opt { transition: none; } }
.rec-table { width: 100%; border-collapse: collapse; margin-bottom: 1.25rem; }
.rec-table th, .rec-table td { border: 1px solid var(--color-border); padding: .4rem .55rem; text-align: left; font-size: .9rem; }
.rec-table thead th { background: var(--c-blue-50); color: var(--color-primary); }
.rec-table input { width: 100%; box-sizing: border-box; padding: .3rem .4rem; font: inherit;
  border: 1px solid var(--color-border); border-radius: 4px; }
.msg { color: var(--color-warning); font-weight: 600; }
.pf { display: flex; flex-wrap: wrap; gap: .5rem; margin-top: 1rem;
  border-top: 1px solid var(--color-border); padding-top: .75rem; }
.pf button { cursor: pointer; padding: .45rem .9rem; font: inherit; background: var(--color-surface);
  color: var(--color-text); border: 1px solid var(--color-border); border-radius: 6px; }
</style>
