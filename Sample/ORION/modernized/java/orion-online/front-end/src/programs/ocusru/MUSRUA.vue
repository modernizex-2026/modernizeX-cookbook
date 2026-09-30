<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const metaFieldNames = new Set(['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME'])
function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const fields = computed(() => namedFields(allFields.value, cellNames.value)
  .filter((f: Field) => !metaFieldNames.has(f.name))
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

const trnName = computed(() => val('TRNNAME'))
const curDate = computed(() => val('CURDATE'))
const pgmName = computed(() => val('PGMNAME'))
const curTime = computed(() => val('CURTIME'))
</script>

<template>
  <section class="am-page">
    <div class="meta-strip">
      <span class="meta-item"><span class="meta-key">Tran</span><span class="meta-val">{{ trnName }}</span></span>
      <span class="meta-item"><span class="meta-key">Pgm</span><span class="meta-val">{{ pgmName }}</span></span>
      <span class="meta-item meta-right"><span class="meta-key">Date</span><span class="meta-val">{{ curDate }}</span></span>
      <span class="meta-item meta-right"><span class="meta-key">Time</span><span class="meta-val">{{ curTime }}</span></span>
    </div>

    <header class="am-head">
      <svg class="head-icon" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <circle cx="12" cy="8" r="4" />
        <path d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8" />
      </svg>
      <h1>{{ title }}</h1>
    </header>

    <fieldset class="rec-fieldset">
      <legend>User details</legend>
      <div class="rec-grid">
        <div v-for="it in fields" :key="it.field.name" class="rec-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input :id="it.field.name"
                 :type="it.dark ? 'password' : 'text'"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :autocomplete="it.dark ? 'new-password' : 'off'"
                 :class="{ mono: it.field.name === 'USERID' }"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
        </div>
      </div>
    </fieldset>

    <p class="msg-slot" :class="{ visible: !!message }" role="alert">
      <svg v-if="message" class="msg-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <circle cx="12" cy="12" r="9" />
        <path d="M12 8v5M12 16h.01" />
      </svg>
      <span>{{ message }}</span>
    </p>

    <nav class="pf">
      <button v-for="(b, i) in buttons" :key="b.aidKey" type="button"
              :class="['pf-btn', i === 0 ? 'primary' : 'secondary']"
              @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.75rem 2rem;
  color: var(--color-text);
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: .25rem 1.5rem;
  font-size: .78rem;
  color: var(--color-text-muted);
  margin-bottom: 1rem;
}
.meta-item { display: inline-flex; align-items: baseline; gap: .35rem; }
.meta-right { margin-left: auto; }
.meta-key { font-weight: 600; color: var(--color-primary); }
.meta-val { font-family: monospace; }

.am-head {
  display: flex;
  align-items: center;
  gap: .6rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: 1.5rem;
}
.head-icon { color: var(--color-primary); flex-shrink: 0; }
.am-head h1 { font-size: 1.3rem; margin: 0; font-weight: 700; }

.rec-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin: 0 0 1.25rem;
}
.rec-fieldset legend {
  font-size: .8rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .5rem;
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem 1.5rem;
}
.rec-field { display: flex; flex-direction: column; gap: .3rem; }
.rec-field label { font-size: .8rem; font-weight: 600; color: var(--color-text); }
.rec-field input {
  padding: .55rem .65rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 180ms, box-shadow 180ms;
}
.rec-field input.mono { font-family: monospace; letter-spacing: .02em; }
.rec-field input:hover { border-color: var(--color-primary); }
.rec-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
@media (prefers-reduced-motion: reduce) {
  .rec-field input { transition: none; }
}

.msg-slot {
  display: flex;
  align-items: center;
  gap: .4rem;
  min-height: 1.5rem;
  margin: 0 0 1rem;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
  visibility: hidden;
}
.msg-slot.visible { visibility: visible; }
.msg-icon { flex-shrink: 0; }

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
  border-radius: 6px;
  transition: background 180ms, border-color 180ms, transform 150ms;
}
.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border: 1px solid var(--color-primary);
}
.pf-btn.primary:hover { filter: brightness(1.08); }
.pf-btn.secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
.pf-btn.secondary:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn:active { transform: scale(.98); }
@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .am-page { padding: 1rem; }
  .rec-grid { grid-template-columns: 1fr; }
  .meta-strip { flex-direction: column; gap: .25rem; }
  .meta-right { margin-left: 0; }
}
</style>
