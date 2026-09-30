<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const items = computed(() => namedFields(allFields.value, new Set())
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input || (props.values[it.field.name] ?? '').trim() !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const acctId = computed(() => items.value.find(it => it.field.name === 'ACCTID'))
const balance = computed(() => items.value.find(it => it.field.name === 'BLBAL'))
const payAmt = computed(() => items.value.find(it => it.field.name === 'BLAMT'))
const confirm = computed(() => items.value.find(it => it.field.name === 'BLCONF'))
</script>

<template>
  <section class="bill-page">
    <header class="bill-head">
      <div class="bill-titlewrap">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item"><dt>Tran</dt><dd>{{ values.TRNNAME }}</dd></div>
        <div class="meta-item"><dt>Prog</dt><dd>{{ values.PGMNAME }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ values.CURDATE }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ values.CURTIME }}</dd></div>
      </dl>
    </header>

    <fieldset class="bill-card">
      <legend>Account</legend>

      <div class="bill-field" v-if="acctId">
        <label for="ACCTID">{{ acctId.label }}</label>
        <input id="ACCTID" type="text" autofocus
               :maxlength="acctId.field.length"
               :value="val('ACCTID')"
               @input="onInput('ACCTID', $event)" />
      </div>

      <div class="bill-field readonly" v-if="balance && val('BLBAL')">
        <label for="BLBAL">{{ balance.label }}</label>
        <output id="BLBAL" class="mono">{{ val('BLBAL') }}</output>
      </div>

      <div class="bill-field" v-if="payAmt">
        <label for="BLAMT">{{ payAmt.label }}</label>
        <input id="BLAMT" type="text" inputmode="decimal" class="mono"
               :maxlength="payAmt.field.length"
               :value="val('BLAMT')"
               @input="onInput('BLAMT', $event)" />
      </div>

      <div class="bill-field confirm" v-if="confirm">
        <label for="BLCONF">{{ confirm.label }}</label>
        <input id="BLCONF" type="text" maxlength="1"
               :value="val('BLCONF')"
               @input="onInput('BLCONF', $event)" />
      </div>
    </fieldset>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>
    <p v-else class="msg msg-placeholder" aria-hidden="true">&nbsp;</p>

    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button"
              :class="['pf-btn', b.aidKey === 'ENTER' ? 'primary' : 'secondary']"
              @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.bill-page {
  max-width: var(--screen-max-width, 780px);
  margin: 2rem auto;
  padding: 0 1rem;
  color: var(--color-text);
}

.bill-head {
  display: flex;
  flex-direction: column;
  gap: .6rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.bill-titlewrap { display: flex; align-items: baseline; justify-content: space-between; gap: 1rem; }
.bill-titlewrap h1 { font-size: 1.4rem; margin: 0; }
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
  font-size: .8rem;
  color: var(--color-text-muted);
}
.meta-item { display: flex; gap: .35rem; align-items: baseline; }
.meta-item dt { font-weight: 600; margin: 0; }
.meta-item dd { margin: 0; font-family: monospace; }

.bill-card {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin: 0 0 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.bill-card legend {
  font-size: .75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .04em;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.bill-field {
  display: grid;
  grid-template-columns: minmax(120px, 200px) 1fr;
  align-items: center;
  gap: 1rem;
}
.bill-field label {
  font-size: .9rem;
  font-weight: 600;
  color: var(--color-text);
}
.bill-field input,
.bill-field output {
  padding: .55rem .7rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  width: 100%;
  box-sizing: border-box;
  transition: border-color .15s, box-shadow .15s;
}
.bill-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.bill-field.readonly output {
  color: var(--color-success);
  font-weight: 600;
  border-color: transparent;
  background: transparent;
  padding-left: 0;
}
.mono { font-family: monospace; }

.bill-field.confirm input { max-width: 4rem; text-align: center; text-transform: uppercase; }

@media (prefers-reduced-motion: reduce) {
  .bill-field input { transition: none; }
}

@media (max-width: 768px) {
  .bill-field { grid-template-columns: 1fr; align-items: start; gap: .35rem; }
}

.msg {
  color: var(--color-danger);
  font-weight: 600;
  min-height: 1.4em;
  margin: 0 0 1rem;
}
.msg-placeholder { visibility: hidden; }

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  margin-top: .5rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}
.pf-btn {
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  border-radius: 6px;
  transition: background .15s, border-color .15s, transform .15s;
}
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border: 1px solid var(--color-primary);
}
.pf-btn.secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
.pf-btn:hover { transform: translateY(-1px); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }

@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
  .pf-btn:hover { transform: none; }
}
</style>
