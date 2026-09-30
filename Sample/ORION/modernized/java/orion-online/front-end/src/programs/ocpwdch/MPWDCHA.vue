<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const items = computed(() => namedFields(allFields.value, cellNames.value)
  .filter((f: Field) => f.name !== 'ERRMSG')
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), dark: isDarkField(f) }))
  .filter(it => it.field.input || (props.values[it.field.name] ?? '').trim() !== '' || (it.field.initial ?? '').trim() !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const primary = computed(() => props.buttons.find(b => b.aidKey === 'ENTER'))
const secondary = computed(() => props.buttons.filter(b => b.aidKey !== 'ENTER'))
</script>

<template>
  <section class="pwd-page">
    <header class="pwd-head">
      <div class="meta-strip">
        <span class="meta-item"><span class="meta-key">Tran</span><span class="meta-val">{{ values.TRNNAME }}</span></span>
        <span class="meta-item"><span class="meta-key">Pgm</span><span class="meta-val">{{ values.PGMNAME }}</span></span>
        <span class="meta-item meta-right"><span class="meta-key">Date</span><span class="meta-val">{{ values.CURDATE }}</span></span>
        <span class="meta-item meta-right"><span class="meta-key">Time</span><span class="meta-val">{{ values.CURTIME }}</span></span>
      </div>
      <h1>{{ title }}</h1>
    </header>

    <div class="pwd-card">
      <fieldset class="pwd-fieldset">
        <legend>Change Password</legend>
        <div class="pwd-grid">
          <div v-for="it in items" :key="it.field.name" class="pwd-field">
            <label :for="it.field.name">{{ it.label }}</label>
            <div class="input-wrap">
              <svg v-if="it.dark" class="field-icon" aria-hidden="true" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="4" y="10" width="16" height="10" rx="2" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3" />
              </svg>
              <svg v-else class="field-icon" aria-hidden="true" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="8" r="4" />
                <path d="M4 20c0-4 3.5-6 8-6s8 2 8 6" />
              </svg>
              <input :id="it.field.name"
                     :type="it.dark ? 'password' : 'text'"
                     :maxlength="it.field.length"
                     :value="val(it.field.name)"
                     autocomplete="off"
                     @input="onInput(it.field.name, $event)" />
            </div>
          </div>
        </div>
      </fieldset>
    </div>

    <p class="msg" :class="{ 'msg--visible': !!message }" role="alert">{{ message }}</p>

    <nav class="pf">
      <button v-if="primary" type="button" class="pf-btn pf-primary" @click="send(primary.aidKey)" :title="primary.aidKey">
        {{ primary.label }}
      </button>
      <button v-for="b in secondary" :key="b.aidKey" type="button" class="pf-btn pf-secondary" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.pwd-page {
  max-width: var(--screen-max-width, 640px);
  margin: 2.5rem auto;
  padding: 0 1rem;
  color: var(--color-text);
}

.pwd-head { margin-bottom: 1.5rem; }

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font: 500 .75rem/1 monospace;
  color: var(--color-text-muted);
  margin-bottom: .75rem;
}
.meta-item { display: inline-flex; gap: .35rem; align-items: baseline; }
.meta-key { color: var(--color-primary); font-weight: 700; }
.meta-right { margin-left: auto; }
@media (max-width: 480px) { .meta-right { margin-left: 0; } }

.pwd-head h1 {
  font-size: 1.4rem;
  margin: 0;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .5rem;
}

.pwd-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.5rem;
  margin-bottom: 1.25rem;
}

.pwd-fieldset { border: none; margin: 0; padding: 0; }
.pwd-fieldset legend {
  font-weight: 700;
  font-size: 1rem;
  color: var(--color-text);
  padding: 0 0 1rem 0;
}

.pwd-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pwd-field { display: flex; flex-direction: column; gap: .3rem; }
.pwd-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.field-icon {
  position: absolute;
  left: .65rem;
  color: var(--color-text-muted);
  pointer-events: none;
}
.pwd-field input {
  width: 100%;
  box-sizing: border-box;
  padding: .55rem .6rem .55rem 2.2rem;
  font: 1rem/1.2 monospace;
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color .2s, box-shadow .2s;
}
.pwd-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
@media (prefers-reduced-motion: reduce) { .pwd-field input { transition: none; } }

.msg {
  min-height: 1.4rem;
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem 0;
  visibility: hidden;
}
.msg--visible { visibility: visible; }

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
  transition: background .18s, border-color .18s, color .18s;
}
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .pf-btn { transition: none; } }

.pf-primary {
  background: var(--color-primary);
  color: var(--c-white);
  border: 1px solid var(--color-primary);
}
.pf-primary:hover { opacity: .92; }

.pf-secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
.pf-secondary:hover { background: var(--c-blue-50); border-color: var(--color-primary); }

@media (max-width: 768px) {
  .pwd-page { padding: 0 .75rem; }
  .pwd-card { padding: 1.1rem; }
  .pf { flex-direction: column; }
  .pf-btn { width: 100%; }
}
</style>
