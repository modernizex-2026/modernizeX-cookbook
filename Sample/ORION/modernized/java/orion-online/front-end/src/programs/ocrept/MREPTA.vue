<script setup lang="ts">
import { computed } from 'vue'
import type { ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, labelFor, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => val('TITLE') || titleOf(allFields.value, props.screenName))

function val(name: string): string { return props.values[name] ?? '' }
function onInput(name: string, e: Event) { props.values[name] = (e.target as HTMLInputElement).value }
function label(name: string): string { const f = allFields.value.find((x: Field) => x.name === name); return f ? labelFor(allFields.value, f) : '' }

const RPTYPE_OPTIONS: { code: string; text: string }[] = [
  { code: '1', text: 'Trans' },
  { code: '2', text: 'Accts' },
  { code: '3', text: 'Cards' },
]
function chooseType(code: string) { props.values.RPTYPE = code }
</script>

<template>
  <section class="mr-page">
    <div class="mr-meta">
      <span class="mr-meta-item"><span class="mr-meta-label">{{ label('TRNNAME') || 'Tran' }}</span><span class="mr-meta-val">{{ val('TRNNAME') }}</span></span>
      <span class="mr-meta-item"><span class="mr-meta-label">{{ label('PGMNAME') || 'Pgm' }}</span><span class="mr-meta-val">{{ val('PGMNAME') }}</span></span>
      <span class="mr-meta-spacer"></span>
      <span class="mr-meta-item"><span class="mr-meta-label">{{ label('CURDATE') || 'Date' }}</span><span class="mr-meta-val">{{ val('CURDATE') }}</span></span>
      <span class="mr-meta-item"><span class="mr-meta-label">{{ label('CURTIME') || 'Time' }}</span><span class="mr-meta-val">{{ val('CURTIME') }}</span></span>
    </div>

    <header class="mr-head">
      <svg class="mr-head-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M6 3h9l4 4v14a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
        <path d="M14 3v5h5" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
        <path d="M8 12h8M8 15h8M8 18h5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
      </svg>
      <h1>{{ title }}</h1>
    </header>

    <fieldset class="mr-card">
      <legend>Report Parameters</legend>

      <div class="mr-field">
        <label id="rptype-legend" class="mr-label">{{ label('RPTYPE') || 'Report Type' }}</label>
        <div class="mr-segmented" role="radiogroup" aria-labelledby="rptype-legend">
          <button
            v-for="o in RPTYPE_OPTIONS"
            :key="o.code"
            type="button"
            role="radio"
            class="mr-seg-btn"
            :class="{ active: val('RPTYPE').trim() === o.code }"
            :aria-checked="val('RPTYPE').trim() === o.code"
            @click="chooseType(o.code)"
          >
            <span class="mr-seg-code">{{ o.code }}</span>{{ o.text }}
          </button>
        </div>
        <input
          id="RPTYPE"
          class="mr-type-input"
          type="text"
          inputmode="numeric"
          :maxlength="2"
          aria-label="Report type code"
          :value="val('RPTYPE')"
          @input="onInput('RPTYPE', $event)"
        />
      </div>

      <div class="mr-grid">
        <div class="mr-field">
          <label for="RPFROM">{{ label('RPFROM') || 'From Date' }}</label>
          <input
            id="RPFROM"
            type="text"
            placeholder="mm/dd/yyyy"
            :maxlength="10"
            :value="val('RPFROM')"
            @input="onInput('RPFROM', $event)"
          />
        </div>
        <div class="mr-field">
          <label for="RPTO">{{ label('RPTO') || 'To Date' }}</label>
          <input
            id="RPTO"
            type="text"
            placeholder="mm/dd/yyyy"
            :maxlength="10"
            :value="val('RPTO')"
            @input="onInput('RPTO', $event)"
          />
        </div>
      </div>
    </fieldset>

    <p class="mr-msg" :class="{ 'has-msg': !!message }" role="alert">{{ message }}</p>

    <nav class="mr-pf">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="mr-pf-btn"
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
.mr-page {
  max-width: var(--screen-max-width, 720px);
  margin: 2rem auto;
  padding: 0 1rem 1.5rem;
  color: var(--color-text);
}

.mr-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0 1.5rem;
  font: 500 .8rem/1.4 monospace;
  color: var(--color-text-muted);
  padding: .5rem .75rem;
  margin-bottom: 1.25rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
}
.mr-meta-item { display: inline-flex; gap: .4rem; }
.mr-meta-label { color: var(--color-primary); font-weight: 700; }
.mr-meta-spacer { flex: 1 1 auto; }

.mr-head {
  display: flex;
  align-items: center;
  gap: .6rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .6rem;
  margin-bottom: 1.5rem;
}
.mr-head-icon { width: 1.5rem; height: 1.5rem; color: var(--color-primary); flex-shrink: 0; }
.mr-head h1 { font-size: 1.3rem; margin: 0; font-weight: 700; }

.mr-card {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.25rem 1.5rem;
  margin: 0 0 1.25rem;
  background: var(--color-surface);
}
.mr-card legend {
  font-size: .85rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0 .4rem;
}

.mr-field { display: flex; flex-direction: column; gap: .4rem; margin-bottom: 1.1rem; }
.mr-field:last-child { margin-bottom: 0; }
.mr-field label,
.mr-label { font-size: .8rem; font-weight: 600; color: var(--color-primary); }

.mr-field input {
  padding: .55rem .65rem;
  font: inherit;
  font-family: monospace;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 200ms, box-shadow 200ms;
}
.mr-field input:hover { border-color: var(--color-primary); }
.mr-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.mr-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.1rem; }

.mr-segmented {
  display: inline-flex;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
  width: fit-content;
}
.mr-seg-btn {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  cursor: pointer;
  padding: .5rem .9rem;
  font: inherit;
  font-size: .85rem;
  font-weight: 600;
  color: var(--color-text);
  background: var(--color-surface);
  border: none;
  border-right: 1px solid var(--color-border);
  transition: background 200ms, color 200ms;
}
.mr-seg-btn:last-child { border-right: none; }
.mr-seg-btn .mr-seg-code {
  font-family: monospace;
  font-size: .75rem;
  color: var(--color-text-muted);
}
.mr-seg-btn.active { background: var(--color-primary); color: var(--c-white); }
.mr-seg-btn.active .mr-seg-code { color: var(--c-white); }
.mr-seg-btn:hover:not(.active) { background: var(--c-blue-50); }
.mr-seg-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: -2px; }

.mr-type-input {
  width: 4.5rem;
  margin-top: .25rem;
}

.mr-msg {
  min-height: 1.4rem;
  margin: 0 0 .75rem;
  font-weight: 600;
  color: var(--color-danger);
  visibility: hidden;
}
.mr-msg.has-msg { visibility: visible; }

.mr-pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}
.mr-pf-btn {
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 200ms, border-color 200ms, color 200ms;
}
.mr-pf-btn:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.mr-pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.mr-pf-btn.primary {
  background: var(--color-primary);
  color: var(--c-white);
  border-color: var(--color-primary);
}
.mr-pf-btn.primary:hover { opacity: .92; }

@media (prefers-reduced-motion: reduce) {
  .mr-field input,
  .mr-seg-btn,
  .mr-pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .mr-page { margin: 1rem auto; }
  .mr-grid { grid-template-columns: 1fr; }
  .mr-meta { font-size: .75rem; gap: .5rem 1rem; }
}
</style>
