<script setup lang="ts">
import { computed, useId } from 'vue';
import { filterJapaneseOnly, filterNoJapanese } from '@/utils/japaneseInput';

defineOptions({ inheritAttrs: false });

export type InputSize = 'sm' | 'md';

const props = withDefaults(
  defineProps<{
    modelValue?: string | number;
    label?: string;
    placeholder?: string;
    type?: 'text' | 'number' | 'password' | 'email' | 'search' | 'tel' | 'url';
    disabled?: boolean;
    readonly?: boolean;
    required?: boolean;
    size?: InputSize;
    name?: string;
    id?: string;
    autocomplete?: string;
    maxlength?: number;
    inputmode?: 'text' | 'numeric';
    dataFieldName?: string;
    dataActive?: boolean;
    className?: string;
    /** Restrict input to Japanese text: hiragana/katakana/kanji + digits
     * (half- and full-width). Renders a `data-japanese-only` attribute for
     * identification and strips any other character as the user types. */
    japaneseOnly?: boolean;
    noJapanese?: boolean;
  }>(),
  {
    modelValue: '',
    label: '',
    placeholder: '',
    type: 'text',
    disabled: false,
    readonly: false,
    required: false,
    size: 'sm',
    name: '',
    id: '',
    autocomplete: 'off',
    maxlength: undefined,
    inputmode: undefined,
    dataFieldName: undefined,
    dataActive: undefined,
    className: '',
    japaneseOnly: false,
    noJapanese: false,
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
  input: [event: Event];
  blur: [event: FocusEvent];
  focus: [event: FocusEvent];
}>();

const autoId = useId();
const inputId = computed(() => props.id || `ui-input-${autoId}`);
const valueAsString = computed(() => String(props.modelValue ?? ''));

function onInput(event: Event) {
  const target = event.target as HTMLInputElement;
  let value = target.value;
  if (props.japaneseOnly) {
    const filtered = filterJapaneseOnly(value);
    if (filtered !== value) {
      value = filtered;
      target.value = value;
    }
  }
  if (props.noJapanese) {
    const filtered = filterNoJapanese(value);
    if (filtered !== value) {
      value = filtered;
      target.value = value;
    }
  }
  emit('update:modelValue', value);
  emit('input', event);
}
</script>

<template>
  <label v-if="label" class="ui-input-field" :for="inputId">{{ label }}</label>
  <input
    :id="inputId"
    class="ui-input"
    :class="`ui-input--${size} ${className}`"
    :type="type"
    :name="name || undefined"
    :placeholder="placeholder"
    :value="valueAsString"
    :disabled="disabled"
    :readonly="readonly"
    :required="required"
    :autocomplete="autocomplete"
    :maxlength="maxlength"
    :inputmode="inputmode"
    :data-field-name="dataFieldName"
    :data-active="dataActive"
    :data-japanese-only="japaneseOnly"
    :data-no-japanese="noJapanese"
    v-bind="$attrs"
    @input="onInput"
    @blur="emit('blur', $event)"
    @focus="emit('focus', $event)"
  />
</template>

<style scoped>
.ui-input-field {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--scr-text-label, #4b5563);
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
}

.ui-input {
  border: 1px solid var(--scr-border, #d1d5db);
  border-radius: var(--scr-radius-sm, 4px);
  background: #fff;
  color: var(--scr-text, #1f2937);
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
  box-sizing: border-box;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    background-color 0.15s ease;
}

.ui-input--sm {
  height: 34px;
  padding: 6px 10px;
  font-size: 13px;
}

.ui-input--md {
  height: 38px;
  padding: 8px 12px;
  font-size: 14px;
}

.ui-input::placeholder {
  color: var(--scr-text-muted, #6b7280);
}

.ui-input:hover:not(:disabled):not(:readonly) {
  border-color: var(--color-border-gray, rgba(49, 46, 129, 0.3));
}

.ui-input:focus-visible {
  outline: none;
  border-color: var(--color-primary, #312E81);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-primary, #312E81) 18%, transparent);
}

.ui-input:disabled {
  cursor: not-allowed;
  background: var(--scr-bg-muted);
  color: var(--scr-text-muted);
  border-color: var(--scr-border);
}

.ui-input[readonly] {
  cursor: default;
}

.ui-input[readonly]:focus-visible {
  box-shadow: none;
  border: 1px solid var(--scr-border, #d1d5db);
}
</style>
