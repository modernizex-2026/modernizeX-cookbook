<!--
  SearchableSelect — code+name lookup with searchable dropdown.

  COBOL screens frequently pair a numeric code input (SC-FBUKACD, picture 9(4))
  with a display name field (DS-FBUKAMEI, from "HBUSYO"). The user types the
  code; the backend echoes the name. Modernized UX: a searchable dropdown
  where typing filters by either code or name.

  Pipeline use: a SearchableSelect is used when a numeric input is followed by
  a display-from of a name field (naming pattern *-NAME, *-MEI, *-MEISYO).

  Caller supplies options as { code, name }[] (populated from master data).
  Component handles in-memory filtering. No external deps.

  Usage:
    <SearchableSelect
      v-model="bukaCode"
      :options="bukaOptions"
      placeholder="部署を検索"
      width="320px"
    />
-->
<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue';

interface Option { code: string; name: string }

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    options: Option[];
    placeholder?: string;
    width?: string;
    readonly?: boolean;
  }>(),
  { placeholder: '検索...', width: '240px' },
);
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>();

const query = ref('');
const open = ref(false);
const wrapper = ref<HTMLElement | null>(null);

// Display: when value matches an option, show "{code} {name}"; else show query.
const selectedOption = computed(() => props.options.find(o => o.code === props.modelValue));
const displayValue = computed(() =>
  open.value ? query.value : (selectedOption.value ? `${selectedOption.value.code} ${selectedOption.value.name}` : (props.modelValue ?? ''))
);

const filteredOptions = computed(() => {
  const q = query.value.trim();
  if (!q) return props.options.slice(0, 50);
  const lower = q.toLowerCase();
  return props.options
    .filter(o => o.code.startsWith(q) || o.name.toLowerCase().includes(lower))
    .slice(0, 50);
});

function pick(opt: Option) {
  emit('update:modelValue', opt.code);
  query.value = '';
  open.value = false;
}

function onInput(e: Event) {
  query.value = (e.target as HTMLInputElement).value;
  open.value = true;
}

function onFocus() {
  if (!props.readonly) { open.value = true; query.value = ''; }
}

function onClickOutside(e: MouseEvent) {
  if (wrapper.value && !wrapper.value.contains(e.target as Node)) open.value = false;
}
onMounted(() => document.addEventListener('mousedown', onClickOutside));
onUnmounted(() => document.removeEventListener('mousedown', onClickOutside));

watch(() => props.modelValue, () => { query.value = ''; });
</script>

<template>
  <div ref="wrapper" class="scr-searchable-select" :style="{ width }">
    <input
      type="text"
      class="scr-input"
      :placeholder="placeholder"
      :value="displayValue"
      :readonly="readonly"
      @input="onInput"
      @focus="onFocus"
    />
    <ul v-if="open && filteredOptions.length > 0" class="scr-searchable-list">
      <li
        v-for="opt in filteredOptions"
        :key="opt.code"
        :class="['scr-searchable-item', opt.code === modelValue ? 'is-selected' : '']"
        @mousedown.prevent="pick(opt)"
      >
        <span class="scr-searchable-code">{{ opt.code }}</span>
        <span class="scr-searchable-name">{{ opt.name }}</span>
      </li>
    </ul>
    <div v-else-if="open && query" class="scr-searchable-empty">該当なし</div>
  </div>
</template>

<style scoped>
.scr-searchable-select { position: relative; display: inline-block; }
.scr-searchable-list {
  position: absolute; top: calc(100% + 2px); left: 0; right: 0;
  max-height: 280px; overflow-y: auto;
  margin: 0; padding: 4px 0; list-style: none;
  background: #fff;
  border: 1px solid var(--scr-border, #d1d5db);
  border-radius: 4px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  z-index: 100;
  font-size: 13px;
}
.scr-searchable-item {
  display: flex; gap: 8px; padding: 6px 10px;
  cursor: pointer;
}
.scr-searchable-item:hover { background: var(--scr-bg-hover, #dbeafe); }
.scr-searchable-item.is-selected { background: var(--scr-bg-muted, #f3f4f6); font-weight: 600; }
.scr-searchable-code { font-variant-numeric: tabular-nums; color: var(--scr-text-muted, #4b5563); min-width: 4em; }
.scr-searchable-name { color: var(--scr-text, #1f2937); }
.scr-searchable-empty {
  position: absolute; top: calc(100% + 2px); left: 0; right: 0;
  padding: 8px 10px; font-size: 12px; color: var(--scr-text-muted, #6b7280);
  background: #fff; border: 1px solid var(--scr-border, #d1d5db); border-radius: 4px;
}
</style>
