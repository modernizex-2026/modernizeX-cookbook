<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const items = computed(() => namedFields(allFields.value, new Set<string>())
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) }))
  .filter(it => it.field.input
    || (props.values[it.field.name] ?? '').trim() !== ''
    || (it.field.initial ?? '').trim() !== ''))

function val(name: string): string { return props.values[name] ?? '' }
function setVal(name: string, v: string) { props.values[name] = v; if (typed) typed.value[name] = v }
function onInput(name: string, e: Event) { setVal(name, (e.target as HTMLInputElement).value) }

function metaVal(name: string): string { return val(name) }
</script>

<template>
  <section class="cd-page">
    <header class="cd-meta">
      <div class="cd-meta-group">
        <span class="cd-meta-item"><span class="cd-meta-label">Tran</span><span class="cd-meta-value">{{ metaVal('TRNNAME') }}</span></span>
        <span class="cd-meta-item"><span class="cd-meta-label">Pgm</span><span class="cd-meta-value">{{ metaVal('PGMNAME') }}</span></span>
      </div>
      <div class="cd-meta-group">
        <span class="cd-meta-item"><span class="cd-meta-label">Date</span><span class="cd-meta-value">{{ metaVal('CURDATE') }}</span></span>
        <span class="cd-meta-item"><span class="cd-meta-label">Time</span><span class="cd-meta-value">{{ metaVal('CURTIME') }}</span></span>
      </div>
    </header>

    <div class="cd-card">
      <h1 class="cd-title">{{ title }}</h1>

      <fieldset class="cd-fieldset">
        <legend>Card Lookup</legend>
        <div class="cd-field">
          <label for="CARDNUM">Card Num</label>
          <input
            id="CARDNUM"
            type="text"
            :inputmode="isNumericField(allFields.find((f: Field) => f.name === 'CARDNUM')!) ? 'numeric' : undefined"
            :maxlength="16"
            :value="val('CARDNUM')"
            autofocus
            @input="onInput('CARDNUM', $event)"
          />
        </div>
      </fieldset>

      <fieldset class="cd-fieldset" v-if="val('CDACCT') || val('CDNAME') || val('CDEXP') || val('CDSTAT')">
        <legend>Card Details</legend>
        <div class="cd-grid">
          <div class="cd-field" v-if="val('CDACCT')">
            <span class="cd-label">Acct ID</span>
            <output class="cd-mono">{{ val('CDACCT') }}</output>
          </div>
          <div class="cd-field" v-if="val('CDNAME')">
            <span class="cd-label">Name</span>
            <output>{{ val('CDNAME') }}</output>
          </div>
          <div class="cd-field" v-if="val('CDEXP')">
            <span class="cd-label">Expiry</span>
            <output class="cd-mono">{{ val('CDEXP') }}</output>
          </div>
          <div class="cd-field" v-if="val('CDSTAT')">
            <span class="cd-label">Status</span>
            <output class="cd-mono">{{ val('CDSTAT') }}</output>
          </div>
        </div>
      </fieldset>
    </div>

    <p class="cd-msg" role="alert" :class="{ visible: !!message }">{{ message }}</p>

    <nav class="cd-pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="cd-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        :title="b.aidKey"
        @click="send(b.aidKey)"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.cd-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.cd-meta {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: .5rem 1.5rem;
  font-size: .8rem;
  color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: .6rem;
}

.cd-meta-group {
  display: flex;
  gap: 1.25rem;
}

.cd-meta-item {
  display: inline-flex;
  gap: .35rem;
}

.cd-meta-label {
  font-weight: 600;
  color: var(--color-primary);
}

.cd-meta-value {
  font-family: monospace;
}

.cd-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.cd-title {
  font-size: 1.3rem;
  margin: 0;
  color: var(--color-text);
}

.cd-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem 1.1rem 1.1rem;
  margin: 0;
}

.cd-fieldset legend {
  padding: 0 .4rem;
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
}

.cd-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
  max-width: 320px;
}

.cd-field label,
.cd-label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-primary);
}

.cd-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 150ms, box-shadow 150ms;
}

.cd-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

@media (prefers-reduced-motion: reduce) {
  .cd-field input {
    transition: none;
  }
}

.cd-field output {
  padding: .3rem 0;
  color: var(--color-text);
}

.cd-mono {
  font-family: monospace;
}

.cd-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: .9rem 1.5rem;
}

.cd-grid .cd-field {
  max-width: none;
}

.cd-msg {
  min-height: 1.2rem;
  margin: 0;
  color: var(--color-danger);
  font-weight: 600;
  opacity: 0;
  visibility: hidden;
}

.cd-msg.visible {
  opacity: 1;
  visibility: visible;
}

.cd-pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}

.cd-btn {
  cursor: pointer;
  padding: .5rem 1.1rem;
  font: inherit;
  font-weight: 500;
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms, color 150ms;
}

.cd-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.cd-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.cd-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}

.cd-btn.primary:hover {
  opacity: .9;
}

@media (prefers-reduced-motion: reduce) {
  .cd-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .cd-page {
    padding: 1rem;
  }

  .cd-meta {
    flex-direction: column;
    gap: .35rem;
  }

  .cd-field {
    max-width: none;
  }
}
</style>
