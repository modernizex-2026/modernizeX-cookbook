<script setup lang="ts">
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, MENU_OPTION_RE, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const fields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const optionField = computed<string>(() => fields.value.find((f: Field) => f.input)?.name ?? '')
const options = computed(() =>
  fields.value
    .filter((f: Field) => !f.input && typeof f.initial === 'string' && MENU_OPTION_RE.test(f.initial as string))
    .map((f: Field) => {
      const m = MENU_OPTION_RE.exec(f.initial as string)!
      return { code: m[1], label: (f.initial ?? '').trim() }
    }))
const title = computed<string>(() => titleOf(fields.value, props.screenName))

const trnName = computed<string>(() => props.values['TRNNAME'] ?? '')
const pgmName = computed<string>(() => props.values['PGMNAME'] ?? '')
const curDate = computed<string>(() => props.values['CURDATE'] ?? '')
const curTime = computed<string>(() => props.values['CURTIME'] ?? '')

const choice = computed<string>({
  get: () => props.values[optionField.value] ?? '',
  set: (v: string) => {
    props.values[optionField.value] = v
    if (typed) typed.value[optionField.value] = v
  },
})

function choose(code: string): void {
  choice.value = code
  props.send('ENTER')
}

function onKeydown(e: KeyboardEvent, code: string): void {
  if (e.key === 'Enter' || e.key === ' ') {
    e.preventDefault()
    choose(code)
  }
}
</script>

<template>
  <section class="am-page">
    <header class="am-meta">
      <div class="meta-left">
        <span class="meta-item"><span class="meta-label">Tran</span><span class="meta-value">{{ trnName }}</span></span>
        <span class="meta-item"><span class="meta-label">Pgm</span><span class="meta-value">{{ pgmName }}</span></span>
      </div>
      <div class="meta-right">
        <span class="meta-item"><span class="meta-label">Date</span><span class="meta-value">{{ curDate }}</span></span>
        <span class="meta-item"><span class="meta-label">Time</span><span class="meta-value">{{ curTime }}</span></span>
      </div>
    </header>

    <div class="am-body">
      <h1 class="am-title">{{ title }}</h1>

      <fieldset class="menu-fieldset">
        <legend class="sr-only">Menu options</legend>
        <div class="menu-grid" role="listbox" aria-label="Menu options">
          <button
            v-for="o in options"
            :key="o.code"
            type="button"
            class="menu-opt"
            role="option"
            :aria-selected="choice === o.code"
            @click="choose(o.code)"
            @keydown="(e: KeyboardEvent) => onKeydown(e, o.code)"
          >
            <svg class="menu-icon" viewBox="0 0 20 20" aria-hidden="true" focusable="false">
              <path d="M7 5l6 5-6 5" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span>{{ o.label }}</span>
          </button>
        </div>
      </fieldset>

      <fieldset class="select-fieldset">
        <legend class="sr-only">Selection</legend>
        <div class="am-select">
          <label :for="optionField">Option</label>
          <input
            :id="optionField"
            v-model="choice"
            class="option-input"
            inputmode="numeric"
            maxlength="2"
            autocomplete="off"
          />
          <button type="button" class="btn-primary" @click="props.send('ENTER')">
            <svg class="btn-icon" viewBox="0 0 20 20" aria-hidden="true" focusable="false">
              <path d="M4 10l4 4 8-8" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span>Process</span>
          </button>
        </div>
      </fieldset>

      <p v-if="message" class="msg" role="alert">{{ message }}</p>
      <p v-else class="msg msg-placeholder" aria-hidden="true">&nbsp;</p>
    </div>

    <nav class="pf" aria-label="Function keys">
      <button
        v-for="b in buttons"
        :key="b.aidKey"
        type="button"
        class="pf-btn"
        @click="props.send(b.aidKey)"
        :title="b.aidKey"
      >
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.am-page {
  max-width: var(--screen-max-width, 100%);
  margin: 0 auto;
  padding: 1.5rem 1.5rem 1rem;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.am-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: .75rem 1.5rem;
  padding-bottom: .75rem;
  border-bottom: 1px solid var(--color-border);
  font-family: monospace;
  font-size: .85rem;
}

.meta-left,
.meta-right {
  display: flex;
  gap: 1.25rem;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  gap: .35rem;
  align-items: baseline;
}

.meta-label {
  color: var(--color-text);
  opacity: .6;
  font-weight: 600;
}

.meta-value {
  color: var(--color-primary);
  font-weight: 600;
}

.am-body {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.am-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-text);
}

.menu-fieldset,
.select-fieldset {
  border: none;
  margin: 0;
  padding: 0;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: .6rem;
}

.menu-opt {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: .6rem;
  text-align: left;
  padding: .85rem 1rem;
  font-size: .95rem;
  font-weight: 500;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: background 150ms ease, border-color 150ms ease, transform 150ms ease;
}

.menu-opt:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.menu-opt:active {
  transform: translateY(1px);
}

.menu-opt:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.menu-opt[aria-selected='true'] {
  border-color: var(--color-primary);
  background: var(--c-blue-50);
}

.menu-icon {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
  color: var(--color-primary);
}

.am-select {
  display: flex;
  align-items: center;
  gap: .75rem;
  flex-wrap: wrap;
}

.am-select label {
  font-weight: 600;
  font-size: .9rem;
}

.option-input {
  width: 4rem;
  padding: .5rem .6rem;
  font: inherit;
  font-family: monospace;
  text-align: center;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: border-color 150ms ease, box-shadow 150ms ease;
}

.option-input:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
  border-color: var(--color-primary);
}

.btn-primary {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  padding: .5rem 1rem;
  font: inherit;
  font-weight: 600;
  color: var(--c-white);
  background: var(--color-primary);
  border: none;
  border-radius: 6px;
  transition: filter 150ms ease, transform 150ms ease;
}

.btn-primary:hover {
  filter: brightness(1.08);
}

.btn-primary:active {
  transform: translateY(1px);
}

.btn-primary:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.btn-icon {
  width: 1rem;
  height: 1rem;
}

.msg {
  margin: 0;
  min-height: 1.4em;
  color: var(--color-danger);
  font-weight: 600;
  font-size: .9rem;
}

.msg-placeholder {
  color: transparent;
}

.pf {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  padding-top: .75rem;
  border-top: 1px solid var(--color-border);
}

.pf-btn {
  cursor: pointer;
  padding: .5rem 1rem;
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms ease, border-color 150ms ease;
}

.pf-btn:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.pf-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (max-width: 768px) {
  .am-page {
    padding: 1rem;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }

  .am-select {
    width: 100%;
  }

  .btn-primary {
    flex: 1;
    justify-content: center;
  }
}

@media (prefers-reduced-motion: reduce) {
  .menu-opt,
  .option-input,
  .btn-primary,
  .pf-btn {
    transition: none;
  }
}
</style>
