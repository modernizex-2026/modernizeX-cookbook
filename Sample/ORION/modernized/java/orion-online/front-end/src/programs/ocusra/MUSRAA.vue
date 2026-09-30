<script setup lang="ts">
import { computed } from 'vue'
import type { ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, isDarkField, titleOf } from '../_archetypes/screenModel'

const props = defineProps<ScreenOverrideProps>()

const allFields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const title = computed<string>(() => titleOf(allFields.value, props.screenName))

function val(name: string): string { return props.values[name] ?? '' }
function onInput(name: string, e: Event) { props.values[name] = (e.target as HTMLInputElement).value }
function fieldByName(name: string): Field | undefined { return allFields.value.find((f: Field) => f.name === name) }

const userIdField = computed<Field | undefined>(() => fieldByName('USERID'))
const firstField = computed<Field | undefined>(() => fieldByName('USFNAM'))
const lastField = computed<Field | undefined>(() => fieldByName('USLNAM'))
const pwdField = computed<Field | undefined>(() => fieldByName('USPWD'))
const typeField = computed<Field | undefined>(() => fieldByName('USTYPE'))
</script>

<template>
  <section class="usr-page">
    <header class="usr-meta">
      <div class="meta-item">
        <span class="meta-label">Tran</span>
        <span class="meta-value">{{ props.values['TRNNAME'] || '' }}</span>
      </div>
      <div class="meta-title">{{ title }}</div>
      <div class="meta-item">
        <span class="meta-label">Date</span>
        <span class="meta-value">{{ props.values['CURDATE'] || '' }}</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">Pgm</span>
        <span class="meta-value">{{ props.values['PGMNAME'] || '' }}</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">Time</span>
        <span class="meta-value">{{ props.values['CURTIME'] || '' }}</span>
      </div>
    </header>

    <div class="usr-form">
      <fieldset class="usr-fieldset">
        <legend>User Account</legend>

        <div class="usr-grid">
          <div class="usr-field" v-if="userIdField">
            <label for="USERID">User ID</label>
            <input id="USERID" type="text" :maxlength="userIdField.length"
                   :value="val('USERID')" @input="onInput('USERID', $event)"
                   autocomplete="off" spellcheck="false" class="mono" />
          </div>

          <div class="usr-field" v-if="firstField">
            <label for="USFNAM">First</label>
            <input id="USFNAM" type="text" :maxlength="firstField.length"
                   :value="val('USFNAM')" @input="onInput('USFNAM', $event)"
                   autocomplete="off" />
          </div>

          <div class="usr-field" v-if="lastField">
            <label for="USLNAM">Last</label>
            <input id="USLNAM" type="text" :maxlength="lastField.length"
                   :value="val('USLNAM')" @input="onInput('USLNAM', $event)"
                   autocomplete="off" />
          </div>

          <div class="usr-field" v-if="pwdField">
            <label for="USPWD">Password</label>
            <input id="USPWD" :type="isDarkField(pwdField) ? 'password' : 'text'"
                   :maxlength="pwdField.length"
                   :value="val('USPWD')" @input="onInput('USPWD', $event)"
                   autocomplete="off" class="mono" />
          </div>

          <div class="usr-field" v-if="typeField">
            <label for="USTYPE">Type A/U</label>
            <input id="USTYPE" type="text" :maxlength="typeField.length"
                   :value="val('USTYPE')" @input="onInput('USTYPE', $event)"
                   autocomplete="off" class="mono usr-type" />
          </div>
        </div>
      </fieldset>
    </div>

    <p class="usr-msg" role="alert">{{ props.message }}</p>

    <nav class="usr-pf" aria-label="Actions">
      <button v-for="b in props.buttons" :key="b.aidKey" type="button"
              class="pf-btn" :class="{ primary: b.aidKey === 'ENTER' }"
              @click="props.send(b.aidKey)" :title="b.aidKey">
        {{ b.label }}
      </button>
    </nav>
  </section>
</template>

<style scoped>
.usr-page {
  max-width: var(--screen-max-width, 960px);
  margin: 0 auto;
  padding: 1.5rem 1rem 2rem;
  color: var(--color-text);
}

.usr-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.25rem;
  padding-bottom: .85rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid var(--color-primary);
}
.meta-title {
  flex: 1 1 auto;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text);
  order: 2;
}
.meta-item {
  display: flex;
  align-items: baseline;
  gap: .4rem;
  font-size: .8rem;
  order: 1;
}
.meta-item:last-child { order: 3; margin-left: auto; }
.meta-item:nth-child(4) { order: 3; margin-left: auto; }
.meta-label {
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: .04em;
  font-weight: 600;
}
.meta-value {
  font-family: monospace;
  color: var(--color-primary);
  font-weight: 600;
}

.usr-form { margin-bottom: 1.5rem; }
.usr-fieldset {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem 1.5rem 1.5rem;
  margin: 0;
}
.usr-fieldset legend {
  padding: 0 .5rem;
  font-weight: 700;
  font-size: .95rem;
  color: var(--color-primary);
}

.usr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem 1.5rem;
}
.usr-field {
  display: flex;
  flex-direction: column;
  gap: .35rem;
}
.usr-field label {
  font-size: .8rem;
  font-weight: 600;
  color: var(--color-text);
}
.usr-field input {
  padding: .55rem .65rem;
  font: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: border-color 150ms, box-shadow 150ms;
}
.usr-field input:focus-visible {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--c-blue-50);
}
.usr-field input.mono { font-family: monospace; letter-spacing: .03em; }
.usr-type { max-width: 4rem; text-align: center; text-transform: uppercase; }

@media (prefers-reduced-motion: reduce) {
  .usr-field input { transition: none; }
}

.usr-msg {
  min-height: 1.25rem;
  color: var(--color-danger);
  font-weight: 600;
  margin: 0 0 1rem;
}

.usr-pf {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
}
.pf-btn {
  cursor: pointer;
  padding: .55rem 1.1rem;
  font: inherit;
  font-weight: 600;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background 150ms, border-color 150ms, transform 150ms;
}
.pf-btn:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.pf-btn:focus-visible { outline: 2px solid var(--color-primary); outline-offset: 2px; }
.pf-btn.primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--c-white);
}
.pf-btn.primary:hover { opacity: .92; }

@media (prefers-reduced-motion: reduce) {
  .pf-btn { transition: none; }
}

@media (max-width: 768px) {
  .usr-meta { flex-direction: column; align-items: flex-start; gap: .5rem; }
  .meta-item, .meta-item:last-child, .meta-item:nth-child(4) { order: initial; margin-left: 0; }
  .usr-grid { grid-template-columns: 1fr; }
}
</style>
