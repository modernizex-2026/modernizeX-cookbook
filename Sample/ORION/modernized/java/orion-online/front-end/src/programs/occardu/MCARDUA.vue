<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isDarkField, isNumericField, namedFields, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const cardFields = computed(() => namedFields(allFields.value, new Set())
  .filter((f: Field) => f.input)
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f), dark: isDarkField(f) })))

function val(name: string): string { return props.values[name] ?? '' }
function onInput(name: string, e: Event) {
  const v = (e.target as HTMLInputElement).value
  props.values[name] = v
  if (typed) typed.value[name] = v
}
</script>

<template>
  <section class="am-page">
    <header class="am-head">
      <div class="am-head-left">
        <h1>{{ title }}</h1>
        <span class="prog-tag">{{ program }}</span>
      </div>
      <dl class="meta-strip">
        <div class="meta-item"><dt>Tran</dt><dd>{{ values.TRNNAME }}</dd></div>
        <div class="meta-item"><dt>Pgm</dt><dd>{{ values.PGMNAME }}</dd></div>
        <div class="meta-item"><dt>Date</dt><dd>{{ values.CURDATE }}</dd></div>
        <div class="meta-item"><dt>Time</dt><dd>{{ values.CURTIME }}</dd></div>
      </dl>
    </header>

    <fieldset class="card-panel">
      <legend>
        <svg class="legend-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
          <rect x="2" y="5" width="20" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.6"/>
          <line x1="2" y1="9" x2="22" y2="9" stroke="currentColor" stroke-width="1.6"/>
        </svg>
        Card Details
      </legend>

      <div class="rec-grid">
        <div v-for="it in cardFields" :key="it.field.name" class="rec-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input
            :id="it.field.name"
            :type="it.dark ? 'password' : 'text'"
            :inputmode="it.numeric ? 'numeric' : undefined"
            :maxlength="it.field.length"
            :value="val(it.field.name)"
            autocomplete="off"
            @input="onInput(it.field.name, $event)"
          />
        </div>
      </div>
    </fieldset>

    <p class="msg" role="alert">{{ message }}&nbsp;</p>

    <nav class="pf" aria-label="Actions">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        :class="{ primary: b.aidKey === 'ENTER' }"
        :title="b.aidKey"
        @click="send(b.aidKey)"
      >{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem;
  color: var(--color-text);
  box-sizing: border-box;
}

.am-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem 2rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.am-head-left {
  display: flex;
  align-items: baseline;
  gap: .75rem;
}

.am-head h1 {
  font-size: 1.4rem;
  margin: 0;
  font-weight: 700;
}

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
  gap: 1.25rem;
  margin: 0;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: .4rem;
  font-size: .8rem;
}

.meta-item dt {
  color: var(--color-text-muted);
  font-weight: 600;
}

.meta-item dd {
  margin: 0;
  font-family: monospace;
  color: var(--color-text);
}

.card-panel {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.5rem;
  margin: 0 0 1rem;
  background: var(--color-surface);
}

.card-panel legend {
  display: flex;
  align-items: center;
  gap: .5rem;
  padding: 0 .5rem;
  font-weight: 700;
  font-size: .95rem;
  color: var(--color-primary);
}

.legend-icon {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
}

.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.1rem 1.5rem;
}

.rec-field {
  display: flex;
  flex-direction: column;
  gap: .3rem;
}

.rec-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
}

.rec-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 200ms, box-shadow 200ms;
}

.rec-field input:hover {
  border-color: var(--color-primary);
}

.rec-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

@media (prefers-reduced-motion: reduce) {
  .rec-field input {
    transition: none;
  }
}

.msg {
  min-height: 1.4rem;
  color: var(--color-danger);
  font-weight: 600;
  margin: 1rem 0;
}

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

.pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}

.pf-btn.primary:hover {
  opacity: .9;
  color: var(--c-white);
}

@media (prefers-reduced-motion: reduce) {
  .pf-btn {
    transition: none;
  }
}

@media (max-width: 768px) {
  .am-page {
    padding: 1rem;
  }

  .am-head {
    flex-direction: column;
  }

  .rec-grid {
    grid-template-columns: 1fr;
  }
}
</style>
