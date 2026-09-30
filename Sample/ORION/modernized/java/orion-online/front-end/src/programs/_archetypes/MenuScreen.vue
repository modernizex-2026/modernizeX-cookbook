<script setup lang="ts">
// MenuScreen — deterministic 'menu' archetype (bms-screen-modernize).
// Options/title from the injected manifest; submit via foundation send().
import { computed, inject } from 'vue'
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride'
import { type Field, MENU_OPTION_RE, titleOf } from './screenModel'

const props = defineProps<ScreenOverrideProps>()
const typed = inject(TYPED_VALUES_KEY, null)

const fields = computed<Field[]>(() => (props.screen?.fields ?? []) as Field[])
const optionField = computed<string>(() => fields.value.find((f: Field) => f.input)?.name ?? '')
const options = computed(() =>
  fields.value
    .filter((f: Field) => !f.input && typeof f.initial === 'string' && MENU_OPTION_RE.test(f.initial as string))
    .map((f: Field) => ({ code: MENU_OPTION_RE.exec(f.initial as string)![1], label: (f.initial ?? '').trim() })))
const title = computed<string>(() => titleOf(fields.value, props.screenName))

const choice = computed<string>({
  get: () => props.values[optionField.value] ?? '',
  set: (v: string) => { props.values[optionField.value] = v; if (typed) typed.value[optionField.value] = v },
})
function choose(code: string) { choice.value = code; props.send('ENTER') }
</script>

<template>
  <section class="am-page">
    <header class="am-head"><h1>{{ title }}</h1><span class="prog-tag">{{ program }}</span></header>
    <div class="menu-grid">
      <button v-for="o in options" :key="o.code" type="button" class="menu-opt" @click="choose(o.code)">{{ o.label }}</button>
    </div>
    <div class="am-select">
      <label :for="optionField">Option</label>
      <input :id="optionField" v-model="choice" inputmode="numeric" maxlength="2" />
      <button type="button" class="btn-primary" @click="send('ENTER')">OK</button>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>
    <nav class="pf">
      <button v-for="b in buttons" :key="b.aidKey" type="button" @click="send(b.aidKey)" :title="b.aidKey">{{ b.label }}</button>
    </nav>
  </section>
</template>

<style scoped>
.am-page { max-width: var(--screen-max-width, 1280px); margin: 2rem auto; padding: 0 1rem; color: var(--color-text); }
.am-head { display: flex; align-items: baseline; justify-content: space-between; gap: 1rem;
  border-bottom: 2px solid var(--color-primary); padding-bottom: .5rem; margin-bottom: 1.25rem; }
.am-head h1 { font-size: 1.4rem; margin: 0; }
.prog-tag { font: 600 .75rem/1 monospace; color: var(--color-primary);
  background: var(--c-blue-50); border-radius: 999px; padding: .3rem .6rem; }
.menu-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: .6rem; margin-bottom: 1.25rem; }
.menu-opt { text-align: left; padding: .75rem 1rem; font-size: .95rem; cursor: pointer;
  color: var(--color-text); background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: 8px; transition: background .12s, border-color .12s; }
.menu-opt:hover { background: var(--c-blue-50); border-color: var(--color-primary); }
.am-select { display: flex; align-items: center; gap: .6rem; margin-bottom: 1rem; }
.am-select label { font-weight: 600; }
.am-select input { width: 4rem; padding: .4rem .5rem; font: inherit; text-align: center;
  border: 1px solid var(--color-border); border-radius: 6px; }
.btn-primary { cursor: pointer; padding: .45rem .9rem; font: inherit; color: var(--c-white);
  background: var(--color-primary); border: none; border-radius: 6px; }
.msg { color: var(--color-warning); font-weight: 600; }
.pf { display: flex; flex-wrap: wrap; gap: .5rem; margin-top: 1rem;
  border-top: 1px solid var(--color-border); padding-top: .75rem; }
.pf button { cursor: pointer; padding: .45rem .9rem; font: inherit; background: var(--color-surface);
  color: var(--color-text); border: 1px solid var(--color-border); border-radius: 6px; }
</style>
