<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, isNumericField, namedFields, titleOf, listCellNames } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const cellNames = computed<Set<string>>(() => listCellNames(props.screen ?? {}))
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

const fields = computed(() => namedFields(allFields.value, cellNames.value)
  .filter((f: Field) => f.name !== 'ERRMSG')
  .map((f: Field) => ({ field: f, label: labelFor(allFields.value, f), numeric: isNumericField(f) })))

function val(name: string): string { return props.values[name] ?? '' }
function onInput(name: string, e: Event) { const v = (e.target as HTMLInputElement).value; props.values[name] = v; if (typed) typed.value[name] = v }
</script>

<template>
  <section class="dlg-page">
    <header class="dlg-meta">
      <div class="dlg-meta-left">
        <span class="meta-item"><span class="meta-k">Tran</span><span class="meta-v">{{ val('TRNNAME') }}</span></span>
        <span class="meta-item"><span class="meta-k">Pgm</span><span class="meta-v">{{ val('PGMNAME') }}</span></span>
      </div>
      <h1 class="dlg-title">{{ title }}</h1>
      <div class="dlg-meta-right">
        <span class="meta-item"><span class="meta-k">Date</span><span class="meta-v">{{ val('CURDATE') }}</span></span>
        <span class="meta-item"><span class="meta-k">Time</span><span class="meta-v">{{ val('CURTIME') }}</span></span>
      </div>
    </header>

    <fieldset class="dlg-card">
      <legend>Account Group Details</legend>
      <div class="dlg-grid">
        <div v-for="it in fields" :key="it.field.name" class="dlg-field">
          <label :for="it.field.name">{{ it.label }}</label>
          <input :id="it.field.name"
                 type="text"
                 :inputmode="it.numeric ? 'numeric' : undefined"
                 :maxlength="it.field.length"
                 :value="val(it.field.name)"
                 @input="onInput(it.field.name, $event)" />
        </div>
      </div>
    </fieldset>

    <p class="dlg-msg" role="alert">{{ message || '\u00A0' }}</p>

    <nav class="dlg-pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.dlg-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.5rem 2rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.dlg-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
}
.dlg-meta-left, .dlg-meta-right { display: flex; gap: 1rem; }
.meta-item { display: flex; gap: .35rem; align-items: baseline; font-size: .8rem; }
.meta-k { color: var(--color-text-muted); }
.meta-v { font-family: monospace; color: var(--color-text); font-weight: 600; }
.dlg-title { font-size: 1.35rem; margin: 0; text-align: center; flex: 1 1 auto; color: var(--color-text); }

.dlg-card {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.25rem 1.5rem 1.5rem;
  background: var(--color-surface);
}
.dlg-card legend {
  padding: 0 .5rem;
  font-weight: 600;
  color: var(--color-primary);
  font-size: .95rem;
}

.dlg-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem 1.5rem;
}

.dlg-field { display: flex; flex-direction: column; gap: .35rem; }
.dlg-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text-muted);
}
.dlg-field input {
  padding: .5rem .65rem;
  font: inherit;
  font-family: monospace;
  color: var(--color-success);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  transition: border-color 200ms, box-shadow 200ms;
}
.dlg-field input:hover { border-color: var(--color-primary); }
.dlg-field input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}

.dlg-msg {
  min-height: 1.25rem;
  margin: 0;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
}

.dlg-pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}
.dlg-pf button {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  font-weight: 500;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, transform 150ms;
}
.dlg-pf button:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.dlg-pf button:active { transform: translateY(1px); }
.dlg-pf button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .dlg-field input, .dlg-pf button { transition: none; }
}

@media (max-width: 768px) {
  .dlg-meta { flex-direction: column; align-items: stretch; text-align: center; }
  .dlg-grid { grid-template-columns: 1fr; }
}
</style>
