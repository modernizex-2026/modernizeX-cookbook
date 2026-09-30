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
    .map((f: Field) => ({ code: MENU_OPTION_RE.exec(f.initial as string)![1], label: (f.initial ?? '').trim() })))
const title = computed<string>(() => titleOf(fields.value, props.screenName))

const trnName = computed<string>(() => props.values['TRNNAME'] ?? '')
const pgmName = computed<string>(() => props.values['PGMNAME'] ?? '')
const curDate = computed<string>(() => props.values['CURDATE'] ?? '')
const curTime = computed<string>(() => props.values['CURTIME'] ?? '')

const choice = computed<string>({
  get: () => props.values[optionField.value] ?? '',
  set: (v: string) => { props.values[optionField.value] = v; if (typed) typed.value[optionField.value] = v },
})
function choose(code: string): void { choice.value = code; props.send('ENTER') }
</script>

<template>
  <section class="menu-page">
    <header class="menu-head">
      <h1>{{ title }}</h1>
      <dl class="meta-strip">
        <template v-if="trnName">
          <dt>Tran</dt><dd>{{ trnName }}</dd>
        </template>
        <template v-if="pgmName">
          <dt>Pgm</dt><dd>{{ pgmName }}</dd>
        </template>
        <template v-if="curDate">
          <dt>Date</dt><dd>{{ curDate }}</dd>
        </template>
        <template v-if="curTime">
          <dt>Time</dt><dd>{{ curTime }}</dd>
        </template>
      </dl>
    </header>

    <fieldset class="menu-fieldset">
      <legend>Select an option</legend>
      <ul class="menu-list">
        <li v-for="o in options" :key="o.code">
          <button type="button" class="menu-opt" :class="{ 'menu-opt--active': choice === o.code }" @click="choose(o.code)">
            <span class="menu-opt-label">{{ o.label }}</span>
            <svg class="menu-opt-icon" aria-hidden="true" viewBox="0 0 20 20" width="18" height="18">
              <path d="M7 4l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
        </li>
      </ul>

      <div class="menu-select">
        <label :for="optionField">Option</label>
        <input :id="optionField" v-model="choice" inputmode="numeric" maxlength="2" autocomplete="off" />
        <button type="button" class="btn-primary" @click="send('ENTER')">Process</button>
      </div>
    </fieldset>

    <p class="msg" role="alert">{{ message }}</p>

    <nav class="pf-bar" aria-label="Function keys">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.menu-page {
  max-width: var(--screen-max-width, 1280px);
  margin: 0 auto;
  padding: 2rem 1.5rem;
  color: var(--color-text);
}

.menu-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: .75rem 1.5rem;
  border-bottom: 2px solid var(--color-primary);
  padding-bottom: .75rem;
  margin-bottom: 1.5rem;
}

.menu-head h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.meta-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 0 1.25rem;
  margin: 0;
  font-size: .85rem;
}

.meta-strip dt {
  display: inline;
  font-weight: 600;
  color: var(--color-text);
  opacity: .65;
}

.meta-strip dt::after { content: ':'; }

.meta-strip dd {
  display: inline;
  margin: 0 0 0 .3rem;
  font-family: monospace;
  color: var(--color-primary);
}

.menu-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem;
  margin: 0 0 1rem;
}

.menu-fieldset legend {
  padding: 0 .5rem;
  font-weight: 600;
  font-size: .9rem;
  color: var(--color-text);
}

.menu-list {
  list-style: none;
  margin: 0 0 1.25rem;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: .6rem;
}

.menu-opt {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: .5rem;
  width: 100%;
  text-align: left;
  padding: .75rem 1rem;
  font: inherit;
  font-size: .95rem;
  cursor: pointer;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: background 150ms, border-color 150ms, color 150ms;
}

.menu-opt-icon {
  flex: none;
  opacity: 0;
  transition: opacity 150ms, transform 150ms;
}

.menu-opt:hover,
.menu-opt:focus-visible {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.menu-opt:hover .menu-opt-icon,
.menu-opt:focus-visible .menu-opt-icon,
.menu-opt--active .menu-opt-icon {
  opacity: 1;
  transform: translateX(2px);
}

.menu-opt--active {
  border-color: var(--color-primary);
  background: var(--c-blue-50);
  color: var(--color-primary);
  font-weight: 600;
}

.menu-opt:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.menu-select {
  display: flex;
  align-items: center;
  gap: .75rem;
  flex-wrap: wrap;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}

.menu-select label {
  font-weight: 600;
  font-size: .9rem;
}

.menu-select input {
  width: 4rem;
  padding: .45rem .5rem;
  font: inherit;
  font-family: monospace;
  text-align: center;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: border-color 150ms, box-shadow 150ms;
}

.menu-select input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}

.btn-primary {
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

.msg {
  min-height: 1.4em;
  margin: 0 0 1rem;
  color: var(--color-danger);
  font-weight: 600;
}

.pf-bar {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  border-top: 1px solid var(--color-border);
  padding-top: .9rem;
}

.pf-bar button {
  cursor: pointer;
  padding: .45rem .9rem;
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms;
}

.pf-bar button:hover {
  background: var(--c-blue-50);
  border-color: var(--color-primary);
}

.pf-bar button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (max-width: 768px) {
  .menu-page { padding: 1.25rem 1rem; }
  .menu-list { grid-template-columns: 1fr; }
  .menu-select { align-items: stretch; }
}

@media (prefers-reduced-motion: reduce) {
  .menu-opt,
  .menu-opt-icon,
  .btn-primary,
  .pf-bar button,
  .menu-select input {
    transition: none;
  }
}
</style>
