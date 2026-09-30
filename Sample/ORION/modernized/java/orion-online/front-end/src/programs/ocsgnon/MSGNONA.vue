<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const metaNames = ['TRNNAME', 'CURDATE', 'PGMNAME', 'CURTIME']
const items = computed(() => namedFields(allFields.value, new Set())
  .filter((f: Field) => !metaNames.includes(f.name))
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

const credentialItems = computed(() => items.value.filter(it => it.field.input))
const noteItems = computed(() => items.value.filter(it => !it.field.input && (val(it.field.name) || (it.field.initial ?? '').trim()) !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const trnName = computed(() => val('TRNNAME'))
const curDate = computed(() => val('CURDATE'))
const pgmName = computed(() => val('PGMNAME'))
const curTime = computed(() => val('CURTIME'))
</script>

<template>
  <section class="am-page">
    <header class="am-meta" v-if="trnName || curDate || pgmName || curTime">
      <span class="meta-item"><span class="meta-label">Tran</span><span class="meta-val">{{ trnName }}</span></span>
      <span class="meta-item"><span class="meta-label">Pgm</span><span class="meta-val">{{ pgmName }}</span></span>
      <span class="meta-item"><span class="meta-label">Date</span><span class="meta-val">{{ curDate }}</span></span>
      <span class="meta-item"><span class="meta-label">Time</span><span class="meta-val">{{ curTime }}</span></span>
    </header>

    <div class="am-center">
      <div class="am-card">
        <div class="am-card-head">
          <svg class="am-icon" viewBox="0 0 24 24" width="28" height="28" fill="none" aria-hidden="true">
            <path d="M12 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10Z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M4 21c1.5-4.5 5-6 8-6s6.5 1.5 8 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <h1>{{ title }}</h1>
        </div>

        <p v-for="it in noteItems" :key="'n_' + it.field.name" class="am-note">{{ val(it.field.name) }}</p>

        <fieldset class="am-fieldset">
          <legend class="sr-only">Credentials</legend>
          <div v-for="it in credentialItems" :key="it.field.name" class="am-field">
            <label :for="it.field.name">{{ it.label }}</label>
            <input :id="it.field.name"
                   :type="it.dark ? 'password' : 'text'"
                   :inputmode="it.numeric ? 'numeric' : undefined"
                   :maxlength="it.field.length"
                   autocomplete="off"
                   :value="val(it.field.name)"
                   @input="onInput(it.field.name, $event)" />
          </div>
        </fieldset>

        <p v-if="message" class="am-msg" role="alert">{{ message }}</p>

        <nav class="am-pf">
          <button v-for="b in buttons" :key="b.aidKey" type="button"
                  class="pf-btn" :class="{ primary: b.aidKey === 'ENTER' }"
                  @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
        </nav>
      </div>
    </div>
  </section>
</template>

<style scoped>
.am-page { max-width: var(--screen-max-width, 100%); margin: 0 auto; min-height: 100vh;
  display: flex; flex-direction: column; color: var(--color-text); }

.am-meta { display: flex; flex-wrap: wrap; gap: 1.25rem; padding: .6rem 1.25rem;
  border-bottom: 1px solid var(--color-border); font-size: .8rem; font-family: monospace; }
.meta-item { display: inline-flex; gap: .4rem; align-items: baseline; }
.meta-label { color: var(--color-text-muted); }
.meta-val { color: var(--color-primary); font-weight: 600; }

.am-center { flex: 1; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; }

.am-card { width: 100%; max-width: 420px; background: var(--color-surface);
  border: 1px solid var(--color-border); border-radius: 10px; padding: 2rem; }

.am-card-head { display: flex; flex-direction: column; align-items: center; gap: .5rem; margin-bottom: 1.25rem; }
.am-icon { color: var(--color-primary); }
.am-card-head h1 { font-size: 1.25rem; margin: 0; text-align: center; }

.am-note { color: var(--color-text-muted); text-align: center; font-size: .9rem; margin: 0 0 1.25rem; }

.am-fieldset { border: none; padding: 0; margin: 0 0 1rem; display: flex; flex-direction: column; gap: 1rem; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); white-space: nowrap; }

.am-field { display: flex; flex-direction: column; gap: .35rem; }
.am-field label { font-size: .8rem; font-weight: 600; color: var(--color-text-muted); }
.am-field input { padding: .6rem .7rem; font: inherit; font-family: monospace; letter-spacing: .02em;
  border: 1px solid var(--color-border); border-radius: 6px; background: var(--color-surface);
  color: var(--color-text); transition: border-color 200ms, box-shadow 200ms; }
.am-field input:focus-visible { outline: none; border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50); }

.am-msg { color: var(--color-danger); font-size: .85rem; font-weight: 600; text-align: center;
  min-height: 1.2em; margin: 0 0 1rem; }

.am-pf { display: flex; gap: .6rem; }
.pf-btn { flex: 1; cursor: pointer; padding: .6rem .9rem; font: inherit; font-weight: 600;
  background: var(--color-surface); color: var(--color-text); border: 1px solid var(--color-border);
  border-radius: 6px; transition: background 200ms, border-color 200ms, transform 150ms; }
.pf-btn:hover { border-color: var(--color-primary); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary { background: var(--color-primary); color: var(--c-white); border-color: var(--color-primary); }
.pf-btn.primary:hover { transform: translateY(-1px); }

@media (prefers-reduced-motion: reduce) {
  .am-field input, .pf-btn { transition: none; }
  .pf-btn.primary:hover { transform: none; }
}

@media (max-width: 768px) {
  .am-card { padding: 1.5rem; }
  .am-meta { justify-content: center; }
}
</style>
