<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }

const cardnumField = computed<Field | undefined>(() => fieldByName('CARDNUM'))
const cardnumLabel = computed<string>(() => cardnumField.value ? labelFor(allFields.value, cardnumField.value) : 'Card Num')
const cardnumNumeric = computed<boolean>(() => cardnumField.value ? isNumericField(cardnumField.value) : false)

interface OutRow { name: string; label: string }
const outRows = computed<OutRow[]>(() => {
  const names = ['CDACCT', 'CDNAME', 'CDEXP', 'CDSTAT']
  return names
    .map((n: string): OutRow | null => {
      const f = fieldByName(n)
      if (!f) return null
      return { name: n, label: labelFor(allFields.value, f) }
    })
    .filter((r: OutRow | null): r is OutRow => r !== null)
})

function hasValue(name: string): boolean {
  const f = fieldByName(name)
  const v = val(name).trim()
  if (v !== '') return true
  return !!(f?.initial ?? '').trim()
}
const hasAnyDetail = computed<boolean>(() => outRows.value.some((r: OutRow) => hasValue(r.name)))

const trnname = computed<string>(() => val('TRNNAME'))
const pgmname = computed<string>(() => val('PGMNAME'))
const curdate = computed<string>(() => val('CURDATE'))
const curtime = computed<string>(() => val('CURTIME'))
</script>

<template>
  <section class="cv-page">
    <header class="cv-meta">
      <div class="cv-meta-left">
        <span v-if="trnname" class="cv-tag">{{ trnname }}</span>
        <span v-if="pgmname" class="cv-tag cv-tag-sub">{{ pgmname }}</span>
      </div>
      <div class="cv-meta-right">
        <span v-if="curdate">{{ curdate }}</span>
        <span v-if="curtime">{{ curtime }}</span>
      </div>
    </header>

    <h1 class="cv-title">{{ title }}</h1>

    <fieldset class="cv-search">
      <legend>Lookup</legend>
      <div class="cv-field">
        <label for="CARDNUM">{{ cardnumLabel }}</label>
        <input
          id="CARDNUM"
          type="text"
          :inputmode="cardnumNumeric ? 'numeric' : undefined"
          :maxlength="cardnumField?.length"
          :value="val('CARDNUM')"
          @input="onInput('CARDNUM', $event)"
          autocomplete="off"
        />
      </div>
    </fieldset>

    <fieldset v-if="hasAnyDetail" class="cv-details">
      <legend>Card Details</legend>
      <dl class="cv-dl">
        <template v-for="r in outRows" :key="r.name">
          <template v-if="hasValue(r.name)">
            <dt>{{ r.label }}</dt>
            <dd :class="{ mono: r.name === 'CARDNUM' || r.name === 'CDACCT' }">{{ val(r.name) }}</dd>
          </template>
        </template>
      </dl>
    </fieldset>

    <p v-if="message" class="cv-msg" role="alert">{{ message }}</p>
    <p v-else class="cv-msg cv-msg-placeholder" aria-hidden="true">&nbsp;</p>

    <nav class="cv-pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="cv-btn"
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
.cv-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
  font-family: inherit;
}

.cv-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  font: 500 .8rem/1 monospace;
  color: var(--color-text-muted);
  margin-bottom: .9rem;
  flex-wrap: wrap;
}
.cv-meta-left, .cv-meta-right { display: flex; gap: .6rem; align-items: center; }
.cv-tag {
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .2rem .55rem;
}
.cv-tag-sub { color: var(--color-text-muted); background: transparent; border: 1px solid var(--color-border); }

.cv-title {
  font-size: 1.4rem;
  margin: 0 0 1.25rem;
  padding-bottom: .6rem;
  border-bottom: 2px solid var(--color-primary);
}

.cv-search, .cv-details {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.1rem 1.15rem;
  margin: 0 0 1.25rem;
}
.cv-search legend, .cv-details legend {
  font-size: .78rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
  text-transform: uppercase;
  letter-spacing: .03em;
}

.cv-field { display: flex; flex-direction: column; gap: .35rem; max-width: 320px; }
.cv-field label { font-size: .82rem; font-weight: 600; color: var(--color-text); }
.cv-field input {
  padding: .55rem .65rem;
  font: 500 .95rem/1.2 monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 150ms, box-shadow 150ms;
}
.cv-field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}
@media (prefers-reduced-motion: reduce) {
  .cv-field input { transition: none; }
}

.cv-dl {
  display: grid;
  grid-template-columns: max-content 1fr;
  gap: .55rem 1rem;
  margin: 0;
}
.cv-dl dt {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
  white-space: nowrap;
}
.cv-dl dd {
  margin: 0;
  font-size: .95rem;
  color: var(--color-text);
  word-break: break-word;
}
.cv-dl dd.mono { font-family: monospace; }

.cv-msg {
  min-height: 1.4rem;
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem;
}
.cv-msg-placeholder { visibility: hidden; }

.cv-pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}
.cv-btn {
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms, color 150ms;
}
.cv-btn:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.cv-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.cv-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.cv-btn.primary:hover { opacity: .9; }
@media (prefers-reduced-motion: reduce) {
  .cv-btn { transition: none; }
}

@media (max-width: 768px) {
  .cv-meta { flex-direction: column; align-items: flex-start; }
  .cv-field { max-width: 100%; }
  .cv-dl { grid-template-columns: 1fr; }
  .cv-dl dt { margin-top: .3rem; }
}
</style>
