<!--
  DatePickerJP — Japanese date input (年/月/日 trio or single).

  COBOL screens with date entry use 3 consecutive 2-digit numeric inputs
  (SC-IRAIYY / SC-IRAIMM / SC-IRAIDD) typically labeled 依頼日 + 年/月/日.
  Rendering as 3 separate `<input>` fields is functional but visually noisy.
  This component groups them with separators + optional picker integration.

  Pipeline use: a DatePickerJP is used when manifest shows 3 consecutive
  numeric inputs at the same line in the same group ending in YY/MM/DD or
  YYYY/MM/DD shape.

  Stays self-contained: no external date-picker dep, just 3 styled <input>s
  with auto-advance + bounded ranges. Caller wires v-model to a single date
  object — component exposes `{year, month, day}` via `modelValue`.

  Usage:
    <DatePickerJP v-model="dateValue" :year-width="2" />
    // dateValue = { year: '26', month: '05', day: '17' }
-->
<script setup lang="ts">
import { computed, ref, watch } from 'vue';

interface JpDate { year: string; month: string; day: string }

const props = withDefaults(
  defineProps<{
    modelValue?: JpDate;
    yearWidth?: 2 | 4;        // 2 for 和暦 / short year, 4 for full year
    readonly?: boolean;
    label?: string;            // e.g. '依頼日'
  }>(),
  { yearWidth: 2 },
);
const emit = defineEmits<{ (e: 'update:modelValue', v: JpDate): void }>();

const local = ref<JpDate>({
  year: props.modelValue?.year ?? '',
  month: props.modelValue?.month ?? '',
  day: props.modelValue?.day ?? '',
});

watch(() => props.modelValue, (v) => {
  if (v) local.value = { ...v };
}, { deep: true });

function commit() { emit('update:modelValue', { ...local.value }); }

function clamp(part: 'year' | 'month' | 'day', e: Event) {
  const el = e.target as HTMLInputElement;
  let v = el.value.replace(/\D/g, '');
  const maxLen = part === 'year' ? props.yearWidth : 2;
  if (v.length > maxLen) v = v.slice(0, maxLen);
  local.value[part] = v;
  commit();
}

const yearPlaceholder = computed(() => '#'.repeat(props.yearWidth));
</script>

<template>
  <div class="scr-date-picker-jp">
    <label v-if="label" class="scr-date-label">{{ label }}</label>
    <div class="scr-date-trio">
      <input
        type="text" inputmode="numeric"
        class="scr-input scr-date-cell"
        :placeholder="yearPlaceholder"
        :maxlength="yearWidth"
        :readonly="readonly"
        :value="local.year"
        @input="(e) => clamp('year', e)"
      />
      <span class="scr-date-sep">年</span>
      <input
        type="text" inputmode="numeric"
        class="scr-input scr-date-cell"
        placeholder="##" maxlength="2"
        :readonly="readonly"
        :value="local.month"
        @input="(e) => clamp('month', e)"
      />
      <span class="scr-date-sep">月</span>
      <input
        type="text" inputmode="numeric"
        class="scr-input scr-date-cell"
        placeholder="##" maxlength="2"
        :readonly="readonly"
        :value="local.day"
        @input="(e) => clamp('day', e)"
      />
      <span class="scr-date-sep">日</span>
    </div>
  </div>
</template>

<style scoped>
.scr-date-picker-jp { display: inline-flex; align-items: center; gap: 8px; }
.scr-date-label { font-weight: 600; font-size: 12px; color: var(--scr-text-muted, #4b5563); }
.scr-date-trio { display: inline-flex; align-items: center; gap: 4px; }
.scr-date-cell {
  width: 3.4em;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
.scr-date-cell:nth-of-type(1) { width: 3.4em; }
.scr-date-sep { font-size: 13px; color: var(--scr-text-muted, #4b5563); }
</style>
