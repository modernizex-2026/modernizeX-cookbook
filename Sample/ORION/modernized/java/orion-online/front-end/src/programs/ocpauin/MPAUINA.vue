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

const authItem = computed(() => items.value.find(it => it.field.name === 'AUTHID'))
const detailItems = computed(() => items.value.filter(it => it.field.name !== 'AUTHID' && it.field.name !== 'ERRMSG'))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
</script>

<template>
  <section class="auin-page">
    <header class="auin-meta">
      <div class="meta-item"><span class="meta-label">Tran</span><span class="meta-value">{{ values.TRNNAME }}</span></div>
      <div class="meta-item meta-title">{{ title }}</div>
      <div class="meta-item"><span class="meta-label">Date</span><span class="meta-value">{{ values.CURDATE }}</span></div>
      <div class="meta-item"><span class="meta-label">Pgm</span><span class="meta-value">{{ values.PGMNAME }}</span></div>
      <div class="meta-item"><span class="meta-label">Time</span><span class="meta-value">{{ values.CURTIME }}</span></div>
    </header>

    <fieldset class="auin-search">
      <legend>Authorization Lookup</legend>
      <div class="search-row" v-if="authItem">
        <label for="AUTHID">{{ authItem.label }}</label>
        <input id="AUTHID"
               type="text"
               :inputmode="authItem.numeric ? 'numeric' : undefined"
               :maxlength="authItem.field.length"
               :value="val('AUTHID')"
               placeholder="Enter authorization ID"
               @input="onInput('AUTHID', $event)" />
        <button type="button" class="btn-primary" @click="send('ENTER')">
          <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="9" r="6" />
            <line x1="14" y1="14" x2="18" y2="18" />
          </svg>
          Inquire
        </button>
      </div>
    </fieldset>

    <fieldset v-if="detailItems.length" class="auin-detail">
      <legend>Authorization Details</legend>
      <dl class="detail-grid">
        <template v-for="it in detailItems" :key="it.field.name">
          <dt>{{ it.label }}</dt>
          <dd :class="{ mono: true, 'is-amount': it.field.name === 'PAAMT' }">{{ val(it.field.name) }}</dd>
        </template>
      </dl>
    </fieldset>

    <p v-if="message" class="msg" role="alert">{{ message }}</p>

    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.auin-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
  color: var(--color-text);
}

.auin-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1.5rem;
  padding: .6rem 1rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  background: var(--c-blue-50);
  border: 1px solid var(--color-border);
  font-size: .8rem;
}
.meta-item { display: flex; align-items: baseline; gap: .35rem; }
.meta-label { font-weight: 600; color: var(--color-primary); text-transform: uppercase; letter-spacing: .03em; font-size: .7rem; }
.meta-value { font-family: monospace; color: var(--color-text); }
.meta-title { margin-left: auto; font-weight: 700; font-size: 1rem; color: var(--color-text); }

fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1rem 1.25rem 1.25rem;
  margin: 0 0 1.25rem;
}
legend {
  padding: 0 .5rem;
  font-weight: 600;
  font-size: .85rem;
  color: var(--color-primary);
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 1rem;
}
.search-row label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
  display: block;
  margin-bottom: .3rem;
}
.search-row input {
  padding: .55rem .7rem;
  font: 1rem/1.2 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  min-width: 220px;
  transition: border-color .2s, box-shadow .2s;
}
.search-row input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  color: var(--color-surface);
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  transition: opacity .2s, transform .15s;
}
.btn-primary:hover { opacity: .9; }
.btn-primary:active { transform: translateY(1px); }
.btn-primary:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .btn-primary { transition: none; } }

.detail-grid {
  display: grid;
  grid-template-columns: max-content 1fr;
  row-gap: .6rem;
  column-gap: 1.25rem;
  margin: 0;
}
.detail-grid dt {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
  align-self: center;
}
.detail-grid dd {
  margin: 0;
  font-size: .95rem;
  color: var(--color-text);
  align-self: center;
  word-break: break-word;
}
.detail-grid dd.mono { font-family: monospace; }
.detail-grid dd.is-amount { font-weight: 700; color: var(--color-success); }

.msg {
  min-height: 1.4rem;
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  padding-top: .75rem;
  border-top: 1px solid var(--color-border);
}
.pf button {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background .2s, border-color .2s;
}
.pf button:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf button:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .pf button { transition: none; } }

@media (max-width: 768px) {
  .auin-meta { flex-direction: column; align-items: flex-start; gap: .4rem; }
  .meta-title { margin-left: 0; }
  .detail-grid { grid-template-columns: 1fr; row-gap: .2rem; }
  .detail-grid dt { margin-top: .5rem; }
  .search-row { flex-direction: column; align-items: stretch; }
  .search-row input { min-width: 0; }
}
</style>
