<script setup lang="ts">
import { computed, ref, useId, watch } from 'vue';
import {
  displayToYymmdd,
  isValidYymmdd,
  isoToYymmdd,
  yymmddToDisplay,
  yymmddToIso,
} from '@/utils/ymd';

const props = withDefaults(
  defineProps<{
    /** Wire value — COBOL PIC 9(6) YYMMDD sent to BE. */
    modelValue?: string;
    readonly?: boolean;
    disabled?: boolean;
    dataFieldName?: string;
    dataActive?: boolean;
    className?: string;
  }>(),
  {
    modelValue: '',
    readonly: false,
    disabled: false,
    dataFieldName: undefined,
    dataActive: undefined,
    className: '',
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
  /** Fired when a date is committed via the calendar picker (wire = YYMMDD). */
  pick: [wire: string];
  input: [event: Event];
  keydown: [event: KeyboardEvent];
  focus: [event: FocusEvent];
  blur: [event: FocusEvent];
}>();

const autoId = useId();
const inputId = computed(() => `ui-date-${autoId}`);
const pickerRef = ref<HTMLInputElement | null>(null);
const displayText = ref('');
const isFocused = ref(false);
// True when the current display text parses to a well-formed YYMMDD but is NOT a real
// calendar date (e.g. 2026/02/30). Such a value is never committed to modelValue, so it
// can't be submitted/registered; the field is flagged invalid and keeps the typed text.
const isInvalid = ref(false);

watch(
  () => props.modelValue,
  (wire) => {
    // Don't clobber what the user is typing, nor an invalid value they still need to fix.
    if (!isFocused.value && !isInvalid.value) {
      displayText.value = yymmddToDisplay(wire ?? '');
    }
  },
  { immediate: true },
);

const pickerIso = computed(() => yymmddToIso(props.modelValue ?? ''));

function commitWire(wire: string) {
  emit('update:modelValue', wire);
}

/**
 * Commit the entered text as a YYMMDD wire, rejecting impossible calendar dates.
 * displayToYymmdd() stays lenient (the date-trio relies on it forwarding raw
 * input); the strict calendar check lives here so EVERY DateInput usage gets validation
 * without importing anything. Returns the wire that was committed (''=nothing valid).
 */
function commitFromDisplay(): string {
  const wire = displayToYymmdd(displayText.value);
  isInvalid.value = !!wire && !isValidYymmdd(wire);
  const committed = isInvalid.value ? '' : wire;
  commitWire(committed);
  return committed;
}

function onDisplayInput(event: Event) {
  const el = event.target as HTMLInputElement;
  displayText.value = el.value;
  commitFromDisplay();
  emit('input', event);
}

function onDisplayFocus(event: FocusEvent) {
  isFocused.value = true;
  isInvalid.value = false;
  emit('focus', event);
}

function onDisplayBlur(event: FocusEvent) {
  isFocused.value = false;
  const committed = commitFromDisplay();
  // Reformat to canonical YYYY/MM/DD only for a valid date; an invalid one keeps the
  // user's raw text on screen (flagged) so they can see and fix it.
  if (committed) displayText.value = yymmddToDisplay(committed);
  emit('blur', event);
}

function onPickerChange(event: Event) {
  const iso = (event.target as HTMLInputElement).value;
  const wire = isoToYymmdd(iso);
  if (!wire) return;
  isInvalid.value = false;
  displayText.value = yymmddToDisplay(wire);
  commitWire(wire);
  emit('pick', wire);
  emit('input', event);
}

function openPicker() {
  if (props.readonly || props.disabled) return;
  const el = pickerRef.value;
  if (!el) return;
  el.focus();
  // showPicker() is unsupported in some browsers/webviews (silently missing,
  // not just throwing) and can also throw InvalidStateError in others — cover
  // both by checking for the method AND catching, falling back to a native
  // .click(), which most browsers still use to open the calendar on a
  // focused date input.
  if (typeof el.showPicker === 'function') {
    try {
      el.showPicker();
      return;
    } catch {
      // fall through to .click() below
    }
  }
  el.click();
}
</script>

<template>
  <div class="ui-date-input" :class="{ 'ui-date-input--invalid': isInvalid }" lang="ja">
    <input
      :id="inputId"
      type="text"
      class="ui-date-input__text"
      :class="className"
      inputmode="numeric"
      placeholder="YYYY/MM/DD"
      autocomplete="off"
      :readonly="readonly"
      :disabled="disabled"
      :value="displayText"
      :aria-invalid="isInvalid || undefined"
      :data-wire-value="modelValue"
      :data-field-name="dataFieldName"
      :data-active="dataActive"
      @input="onDisplayInput"
      @keydown="emit('keydown', $event)"
      @focus="onDisplayFocus"
      @blur="onDisplayBlur"
    />
    <input
      ref="pickerRef"
      type="date"
      class="ui-date-input__picker"
      tabindex="-1"
      aria-hidden="true"
      :disabled="disabled"
      :readonly="readonly"
      :value="pickerIso"
      @change="onPickerChange"
    />
    <button
      type="button"
      class="ui-date-input__btn"
      :disabled="disabled"
      :readonly="readonly"
      aria-label="日付を選択"
      @click="openPicker"
    >
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path
          fill="currentColor"
          d="M19 4h-1V2h-2v2H8V2H6v2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2Zm0 16H5V10h14v10ZM7 12h2v2H7v-2Zm4 0h2v2h-2v-2Zm4 0h2v2h-2v-2Z"
        />
      </svg>
    </button>
  </div>
</template>

<style scoped>
.ui-date-input {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  border: 1px solid var(--scr-border, #d1d5db);
  background: #fff;
  background-color: white;
  border-radius: 4px;
  overflow: hidden;
}

.ui-date-input__text {
  width: 12.5em;
  height: 34px;
  padding: 6px 10px;
  border: none;
  border-radius: var(--scr-radius-sm, 4px);
  background: #fff;
  color: var(--scr-text, #1f2937);
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  box-sizing: border-box;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.ui-date-input__text:focus-visible {
  outline: none;
  border: none;
}
.ui-date-input__text:hover:not(:disabled):not([readonly]) {
  border: none;
}

.ui-date-input__text:focus-visible {
  outline: none;
  border: none;
}

.ui-date-input__text:disabled {
  background: var(--scr-bg-muted);
  color: var(--scr-text-muted);
  border-color: var(--scr-border);
}

.ui-date-input__picker {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

.ui-date-input__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  outline: none;
  border: none;
  background-color: white;
  color: var(--scr-text-muted, #6b7280);
  cursor: pointer;
  flex-shrink: 0;
}

.ui-date-input__btn:hover:not(:disabled) {
  /* border-color: var(--color-primary, #312E81); */
  color: var(--color-primary, #312E81);
}

.ui-date-input__btn:disabled {
  cursor: not-allowed;
  background: var(--scr-bg-muted);
  color: var(--scr-text-muted);
  border-color: var(--scr-border);
}

.ui-date-input__btn:read-only {
  color: var(--scr-text-muted);
}

.ui-date-input__btn:read-only:hover {
  color: var(--scr-text-muted);
  cursor: default;
}
</style>
