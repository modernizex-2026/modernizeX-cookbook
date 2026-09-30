<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function fieldByName(name: string): Field | undefined {
  return allFields.value.find((f: Field) => f.name === name)
}

const ttcdField = computed<Field | undefined>(() => fieldByName('TTCD'))
const ttdescField = computed<Field | undefined>(() => fieldByName('TTDESC'))
const ttcdLabel = computed<string>(() => labelFor(allFields.value, ttcdField.value as Field))
const ttdescLabel = computed<string>(() => labelFor(allFields.value, ttdescField.value as Field))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

const trnname = computed<string>(() => val('TRNNAME'))
const pgmname = computed<string>(() => val('PGMNAME'))
const curdate = computed<string>(() => val('CURDATE'))
const curtime = computed<string>(() => val('CURTIME'))
</script>

<template>
  <section class="scr-page">
    <header class="scr-meta">
      <div class="meta-left">
        <span class="meta-item"><span class="meta-key">Tran</span><span class="meta-val">{{ trnname }}</span></span>
        <span class="meta-item"><span class="meta-key">Pgm</span><span class="meta-val">{{ pgmname }}</span></span>
      </div>
      <h1 class="scr-title">{{ title }}</h1>
      <div class="meta-right">
        <span class="meta-item"><span class="meta-key">Date</span><span class="meta-val">{{ curdate }}</span></span>
        <span class="meta-item"><span class="meta-key">Time</span><span class="meta-val">{{ curtime }}</span></span>
      </div>
    </header>

    <fieldset class="scr-card">
      <legend>Type Details</legend>

      <div class="scr-field" v-if="ttcdField">
        <label for="TTCD">{{ ttcdLabel || 'Type Code' }}</label>
        <input
          id="TTCD"
          type="text"
          class="input-code"
          :inputmode="isNumericField(ttcdField) ? 'numeric' : undefined"
          :maxlength="ttcdField.length"
          :value="val('TTCD')"
          @input="onInput('TTCD', $event)"
          autocomplete="off"
        />
      </div>

      <div class="scr-field" v-if="ttdescField">
        <label for="TTDESC">{{ ttdescLabel || 'Description' }}</label>
        <input
          id="TTDESC"
          type="text"
          class="input-desc"
          :inputmode="isNumericField(ttdescField) ? 'numeric' : undefined"
          :maxlength="ttdescField.length"
          :value="val('TTDESC')"
          @input="onInput('TTDESC', $event)"
          autocomplete="off"
        />
      </div>
    </fieldset>

    <p class="msg" :class="{ 'msg-active': message }" role="alert">{{ message }}</p>

    <nav class="pf-bar" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ 'pf-primary': b.aidKey === 'ENTER' }"
        @click="send(b.aidKey)"
        :title="b.aidKey"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.scr-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.5rem 1.25rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.scr-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border);
}

.meta-left,
.meta-right {
  display: flex;
  gap: 1.25rem;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: 0.35rem;
  font-size: 0.8rem;
}

.meta-key {
  color: var(--color-text-muted, var(--color-text));
  font-weight: 600;
}

.meta-val {
  font-family: monospace;
  color: var(--color-primary);
}

.scr-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
  text-align: center;
  flex: 1 1 auto;
}

.scr-card {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  background: var(--color-surface);
}

.scr-card legend {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 0.5rem;
}

.scr-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  max-width: 480px;
}

.scr-field label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text);
}

.scr-field input {
  padding: 0.55rem 0.7rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 200ms, box-shadow 200ms;
}

.scr-field input.input-code {
  max-width: 6rem;
  text-transform: uppercase;
}

.scr-field input.input-desc {
  max-width: none;
}

.scr-field input:hover {
  border-color: var(--color-primary);
}

.scr-field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}

@media (prefers-reduced-motion: reduce) {
  .scr-field input {
    transition: none;
  }
}

.msg {
  min-height: 1.4rem;
  margin: 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: transparent;
}

.msg.msg-active {
  color: var(--color-danger);
}

.pf-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}

.pf-btn {
  cursor: pointer;
  padding: 0.55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, color 200ms;
}

.pf-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.pf-btn.pf-primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}

.pf-btn.pf-primary:hover {
  opacity: 0.9;
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .scr-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .scr-title {
    text-align: left;
  }

  .scr-card {
    padding: 1.1rem;
  }

  .scr-field {
    max-width: none;
  }
}
</style>
