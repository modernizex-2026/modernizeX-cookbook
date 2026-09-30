<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, MENU_OPTION_RE, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const fields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const optionField = computed<string>(() => fields.value.find((f: Field) => f.input)?.name ?? '')
const title = computed<string>(() => titleOf(fields.value, props.screenName))

const options = computed(() =>
  fields.value
    .filter((f: Field) => !f.input && typeof f.initial === 'string' && MENU_OPTION_RE.test(f.initial as string))
    .map((f: Field) => {
      const m = MENU_OPTION_RE.exec(f.initial as string)!
      return { code: m[1], label: (f.initial ?? '').trim() }
    }))

const subtitle = computed<string>(() => {
  const f = fields.value.find((f: Field) => f.color === 'TURQUOISE' && !f.input && f.row !== 24)
  return f?.initial?.trim() ?? ''
})

function fieldByName(name: string) {
  return fields.value.find((f: Field) => f.name === name)
}
const trnName = computed(() => fieldByName('TRNNAME')?.initial ?? props.values['TRNNAME'] ?? '')
const pgmName = computed(() => fieldByName('PGMNAME')?.initial ?? props.values['PGMNAME'] ?? '')
const curDate = computed(() => props.values['CURDATE'] ?? '')
const curTime = computed(() => props.values['CURTIME'] ?? '')

const choice = computed<string>({
  get: () => props.values[optionField.value] ?? '',
  set: (v: string) => {
    props.values[optionField.value] = v
    if (typed) typed.value[optionField.value] = v
  },
})

function choose(code: string) {
  choice.value = code
  props.send('ENTER')
}
</script>

<template>
  <section class="am-page">
    <div class="meta-strip">
      <span class="meta-item"><span class="meta-label">Tran</span><span class="meta-value">{{ trnName }}</span></span>
      <span class="meta-item"><span class="meta-label">Pgm</span><span class="meta-value">{{ pgmName }}</span></span>
      <span class="meta-item meta-right"><span class="meta-label">Date</span><span class="meta-value">{{ curDate }}</span></span>
      <span class="meta-item meta-right"><span class="meta-label">Time</span><span class="meta-value">{{ curTime }}</span></span>
    </div>

    <header class="am-head">
      <h1>{{ title }}</h1>
      <span class="prog-tag">{{ program }}</span>
    </header>

    <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>

    <fieldset class="menu-fieldset">
      <legend>Select an option</legend>
      <div class="menu-grid" role="list">
        <button
          v-for="o in options"
          :key="o.code"
          type="button"
          class="menu-opt"
          role="listitem"
          :aria-pressed="choice === o.code"
          :class="{ active: choice === o.code }"
          @click="choose(o.code)"
        >
          <span class="menu-num" aria-hidden="true">{{ o.code }}</span>
          <span class="menu-label">{{ o.label.replace(/^\d+\.\s*/, '') }}</span>
          <svg class="menu-arrow" aria-hidden="true" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>
      </div>
    </fieldset>

    <div class="am-select">
      <label :for="optionField">Option</label>
      <input
        :id="optionField"
        v-model="choice"
        inputmode="numeric"
        maxlength="2"
        class="option-input"
        autocomplete="off"
      />
      <button type="button" class="btn-primary" @click="send('ENTER')">
        <svg aria-hidden="true" width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Process
      </button>
    </div>

    <p class="msg-slot" :class="{ visible: !!message }" role="alert">{{ message }}</p>

    <nav class="pf" aria-label="Actions">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2rem;
  color: var(--color-text);
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  font-family: monospace;
  font-size: .8rem;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: .5rem .9rem;
  margin-bottom: 1.25rem;
}
.meta-item { display: inline-flex; gap: .35rem; align-items: baseline; }
.meta-right { margin-left: auto; }
.meta-label { color: var(--color-text); opacity: .6; }
.meta-value { font-weight: 600; }

.am-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .5rem;
  margin-bottom: .5rem;
}
.am-head h1 { font-size: 1.4rem; margin: 0; letter-spacing: -0.01em; }
.prog-tag {
  font: 600 .75rem/1 monospace;
  color: var(--color-primary);
  background: var(--c-blue-50);
  border-radius: 999px;
  padding: .3rem .6rem;
}

.subtitle {
  font-size: .85rem;
  color: var(--color-text);
  opacity: .75;
  margin: 0 0 1.25rem;
}

.menu-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1rem 1.1rem 1.1rem;
  margin: 0 0 1.25rem;
}
.menu-fieldset legend {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text);
  opacity: .7;
  padding: 0 .4rem;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: .6rem;
}

.menu-opt {
  display: flex;
  align-items: center;
  gap: .65rem;
  text-align: left;
  padding: .75rem 1rem;
  font-size: .95rem;
  cursor: pointer;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: background 150ms, border-color 150ms, transform 150ms;
}
.menu-opt:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.menu-opt:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.menu-opt.active { border-color: var(--color-primary); background: var(--c-blue-50); }
.menu-num {
  font-family: monospace;
  font-weight: 700;
  color: var(--color-primary);
  min-width: 1.2rem;
}
.menu-label { flex: 1; }
.menu-arrow { color: var(--color-primary); flex-shrink: 0; opacity: .7; }

.am-select {
  display: flex;
  align-items: center;
  gap: .6rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}
.am-select label { font-weight: 600; font-size: .9rem; }
.option-input {
  width: 4rem;
  padding: .45rem .5rem;
  font: inherit;
  font-family: monospace;
  text-align: center;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: border-color 150ms;
}
.option-input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  cursor: pointer;
  padding: .5rem 1.1rem;
  font: inherit;
  font-weight: 600;
  color: var(--c-white);
  background: var(--color-primary);
  border: none;
  border-radius: 6px;
  transition: filter 150ms, transform 150ms;
}
.btn-primary:hover { filter: brightness(1.08); }
.btn-primary:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }

.msg-slot {
  min-height: 1.4em;
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem;
  opacity: 0;
  transition: opacity 200ms;
}
.msg-slot.visible { opacity: 1; }

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  margin-top: 1rem;
  border-top: 1px solid var(--color-border);
  padding-top: .75rem;
}
.pf button {
  cursor: pointer;
  padding: .45rem .9rem;
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms;
}
.pf button:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf button:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }

@media (max-width: 768px) {
  .am-select { flex-direction: column; align-items: stretch; }
  .btn-primary { justify-content: center; }
}

@media (prefers-reduced-motion: reduce) {
  .menu-opt, .btn-primary, .option-input, .msg-slot, .pf button {
    transition: none;
  }
}
</style>
