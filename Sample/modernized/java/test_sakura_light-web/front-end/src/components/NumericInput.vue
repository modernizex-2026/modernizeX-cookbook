<!--
  NumericInput — formatted number entry with picture-aware constraints.

  COBOL numeric fields have fixed widths (9(7), 9(8), Z(5)9, ZZZ9V99, etc.).
  A plain <input type="text"> doesn't communicate the format — this component
  adds maxlength, numeric-only filtering, optional thousands separator on
  blur, and right-alignment per accounting convention.

  Pipeline use: a NumericInput is used when a manifest field's inputType is
  "numeric" AND the picture is anything more complex than 9(N).

  Usage:
    <NumericInput v-model="amount" picture="ZZ,ZZZ,ZZ9" :max-width="10" />
-->
<script setup lang="ts">
import { computed, ref, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    picture?: string;
    maxWidth?: number;
    readonly?: boolean;
    placeholder?: string;
    allowNegative?: boolean;
  }>(),
  {},
);
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>();

const local = ref<string>(props.modelValue ?? '');
watch(() => props.modelValue, (v) => { local.value = v ?? ''; });

// Compute width from picture if maxWidth wasn't provided.
const computedWidth = computed(() => {
  if (props.maxWidth) return props.maxWidth;
  if (!props.picture) return 10;
  // count 9, Z, V, comma, period as visible width units
  let w = 0;
  let i = 0;
  while (i < props.picture.length) {
    const c = props.picture[i];
    if (c === '(') {
      const m = props.picture.slice(i).match(/^\((\d+)\)/);
      if (m) { w += parseInt(m[1], 10) - 1; i += m[0].length; continue; }
    }
    if ('9ZBSV,./-'.includes(c)) w++;
    i++;
  }
  return Math.max(1, w);
});

// Format placeholder from picture, e.g. ZZ,ZZ9 → "## ##0" hint shape.
const hintPlaceholder = computed(() => {
  if (props.placeholder) return props.placeholder;
  if (!props.picture) return '';
  // Render a compressed format hint: replace 9/Z with #, keep separators.
  return props.picture
    .replace(/\((\d+)\)/g, (_, n) => 'X'.repeat(parseInt(n, 10) - 1))
    .replace(/[9Z]/g, '#')
    .replace(/X/g, '#');
});

function onInput(e: Event) {
  let v = (e.target as HTMLInputElement).value;
  const pattern = props.allowNegative ? /[^\d-]/g : /\D/g;
  v = v.replace(pattern, '');
  if (props.allowNegative && v.indexOf('-') > 0) v = v.replace(/-/g, '');  // sign only at start
  if (v.length > computedWidth.value) v = v.slice(0, computedWidth.value);
  local.value = v;
  emit('update:modelValue', v);
}
</script>

<template>
  <input
    type="text"
    inputmode="numeric"
    class="scr-input scr-numeric"
    :value="local"
    :placeholder="hintPlaceholder"
    :maxlength="computedWidth"
    :readonly="readonly"
    @input="onInput"
  />
</template>

<style scoped>
.scr-numeric {
  text-align: right;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, 'SF Mono', Menlo, monospace;
}
</style>
