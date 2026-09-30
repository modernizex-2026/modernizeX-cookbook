<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const metaTran = computed<string>(() => props.values['TRNNAME'] ?? '')
const metaPgm = computed<string>(() => props.values['PGMNAME'] ?? '')
const metaDate = computed<string>(() => props.values['CURDATE'] ?? '')
const metaTime = computed<string>(() => props.values['CURTIME'] ?? '')

const items = computed(() => namedFields(allFields.value, cellNames.value)
  .filter((f: Field) => !['TRNNAME', 'PGMNAME', 'CURDATE', 'CURTIME', 'ERRMSG'].includes(f.name))
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <div class="am-title">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item"><dt>Tran</dt><dd>{{ metaTran }}</dd></div>
        <div class="meta-item"><dt>Pgm</dt><dd>{{ metaPgm }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ metaDate }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ metaTime }}</dd></div>
      </dl>
    </header>

    <fieldset class="rec-fieldset">
      <legend>User</legend>
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
      <svg class="msg-icon" viewBox="0 0 20 20" aria-hidden="true" fill="none">
        <circle cx="10" cy="10" r="9" stroke="currentColor" stroke-width="1.5" />
        <path d="M10 6v5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
        <circle cx="10" cy="14" r="1" fill="currentColor" />
      </svg>
      {{ message }}
    </p>
    <p v-else class="msg-placeholder" aria-hidden="true">&nbsp;</p>

    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" class="pf-btn"
              :class="{ primary: b.aidKey === 'ENTER' }"
              @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
  color: var(--color-text);
}
.am-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}
.am-title { display: flex; align-items: baseline; gap: .75rem; }
.am-head h1 { font-size: 1.4rem; margin: 0; }
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
.meta-item dd { margin: 0; font-family: monospace; color: var(--color-text); }

.rec-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem;
  margin: 0 0 1.25rem;
}
.rec-fieldset legend {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 .4rem;
}
.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem 1.5rem;
}
.rec-field { display: flex; flex-direction: column; gap: .35rem; }
.rec-field label { font-size: .8rem; font-weight: 600; color: var(--color-primary); }
.rec-field input {
  padding: .55rem .65rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color .2s, box-shadow .2s;
}
.rec-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.rec-field output {
  padding: .55rem 0;
  font: inherit;
  font-family: monospace;
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
  margin: 0 0 1rem;
  min-height: 1.5rem;
}
.msg-icon { width: 1.1rem; height: 1.1rem; flex-shrink: 0; }
.msg-placeholder { min-height: 1.5rem; margin: 0 0 1rem; }

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
  transition: background .18s, border-color .18s, transform .18s;
}
.pf-btn:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.pf-btn.primary:hover { transform: translateY(-1px); }
@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
  .pf-btn.primary:hover { transform: none; }
}

@media (max-width: 768px) {
  .am-head { flex-direction: column; align-items: flex-start; }
  .rec-grid { grid-template-columns: 1fr; }
}
</style>
