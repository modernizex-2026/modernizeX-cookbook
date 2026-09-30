<script setup lang="ts">
import { ref, onMounted, computed, useTemplateRef } from 'vue';
import type { ActiveField } from '../types/screen';
import { KEY_AID_MAP } from '../composables/aidCodes';

// Keys NOT intercepted while focus is inside this text input — preserves
// native browser semantics. Backspace = delete char; PageUp/PageDown = no-op
// on single-line inputs (the global useScreenForm.windowKey listener still
// translates them to BTB/UP/DW when focus is NOT on an input).
const INPUT_SKIP_KEYS = new Set(['Backspace', 'PageUp', 'PageDown']);

const props = withDefaults(defineProps<{
  field: ActiveField;
  autoFocus?: boolean;
  /** B2: đầy ô theo độ rộng PIC thì tự nhảy sang ô nhập kế tiếp (chỉ dùng ở chế độ acceptScreen). */
  autoskip?: boolean;
}>(), { autoFocus: true,
    autoskip: false,
  });

const emit = defineEmits<{
  submit: [value: string, aidKey: string];
  valueChange: [value: string];
}>();

const inputRef = useTemplateRef<HTMLInputElement>('inputRef');
const value = ref('');

// Numeric (3270/Hitachi PIC 9 / S9 numeric field) — terminal hardware blocks non-digit keys.
const isNumeric = computed(() => {
  const p = props.field.picture?.toUpperCase().trim() ?? '';
  return p.startsWith('9') || p.startsWith('S9');
});
const isSignedNumeric = computed(() => {
  const p = props.field.picture?.toUpperCase().trim() ?? '';
  return p.startsWith('S9');
});
const isSecure = computed(() => props.field.fieldName?.toUpperCase().includes('PASSWORD') ?? false);

const positionStyle = computed(() => ({
  position: 'absolute' as const,
  top: `${(props.field.line - 1) * 1.2}em`,
  left: `${(props.field.col - 1) * 0.6}em`,
  width: `${props.field.width * 0.6}em`,
  backgroundColor: '#000000',
}));

onMounted(() => {
  if (props.autoFocus) inputRef.value?.focus();
});

// Right-justify zero-pad a numeric value to the field's full width on submit —
// mirrors 3270 numeric ACCEPT semantics so a user typing "123" in PIC 9(6) sends "000123".
function padNumericForSubmit(raw: string): string {
  if (!isNumeric.value) return raw;
  // Strip everything except digits (and leading sign for signed numerics)
  let s = raw;
  if (isSignedNumeric.value) {
    const neg = s.trim().startsWith('-');
    s = s.replace(/[^0-9]/g, '');
    if (s.length > props.field.width - 1) s = s.slice(-(props.field.width - 1));
    s = s.padStart(props.field.width - 1, '0');
    return (neg ? '-' : '+') + s;
  }
  s = s.replace(/[^0-9]/g, '');
  if (s.length > props.field.width) s = s.slice(-props.field.width);
  return s.padStart(props.field.width, '0');
}

function onKeyDown(e: KeyboardEvent) {
  if (INPUT_SKIP_KEYS.has(e.key)) return;
  const aid = KEY_AID_MAP[e.key];
  if (aid) {
    e.preventDefault();
    e.stopPropagation();
    const padded = padNumericForSubmit((e.target as HTMLInputElement).value);
    emit('submit', padded, aid);
    return;
  }
  // 3270 numeric-field keyboard lock — block non-digit keystrokes physically.
  if (!isNumeric.value) return;
  if (e.ctrlKey || e.metaKey || e.altKey) return;
  if (e.isComposing) return; // let IME handle JP input; onInput cleans it
  const allowed = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'];
  if (allowed.includes(e.key)) return;
  if (e.key.length !== 1) return;
  // Allow leading minus on signed-numeric fields only
  if (isSignedNumeric.value && e.key === '-') {
    const input = e.target as HTMLInputElement;
    if (input.selectionStart === 0 && !input.value.includes('-')) return;
  }
  // Allow full-width JP digits (０..９) — onInput converts them
  if (e.key.length === 1) {
    const cp = e.key.codePointAt(0)!;
    if (cp >= 0xFF10 && cp <= 0xFF19) return;
  }
  if (!/^\d$/.test(e.key)) {
    e.preventDefault();
  }
}

function onInput(e: Event) {
  const target = e.target as HTMLInputElement;
  let val = target.value;
  if (isNumeric.value) {
    // Convert full-width JP digits → half-width
    val = val.replace(/[０-９]/g, (s) => String.fromCharCode(s.charCodeAt(0) - 0xFEE0));
    val = isSignedNumeric.value ? val.replace(/[^0-9-]/g, '') : val.replace(/[^0-9]/g, '');
  }
  if (val !== target.value) target.value = val;
  value.value = val;
  emit('valueChange', val);
  // B2 (R9) — autoskip: đầy ô theo độ rộng PIC thì nhảy sang ô nhập kế tiếp. Nhánh online
  // đã có hành vi này từ đầu (ScreenView), nhánh web thì chưa. CHỈ bật ở chế độ acceptScreen
  // (nhiều ô cùng lúc): ở acceptField server điều khiển thứ tự nên client không có 'ô kế'.
  if (props.autoskip && val.length >= props.field.width) {
    const inputs = Array.from(
      document.querySelectorAll<HTMLInputElement>('.screen-grid input.screen-input'));
    const i = inputs.indexOf(target);
    if (i >= 0 && i < inputs.length - 1) inputs[i + 1].focus();
  }
}
</script>

<template>
  <input
    ref="inputRef"
    class="screen-input"
    :type="isSecure ? 'password' : 'text'"
    :inputmode="isNumeric ? 'numeric' : undefined"
    :maxlength="field.width"
    :style="positionStyle"
    :value="value"
    @keydown="onKeyDown"
    @input="onInput"
  />
</template>
