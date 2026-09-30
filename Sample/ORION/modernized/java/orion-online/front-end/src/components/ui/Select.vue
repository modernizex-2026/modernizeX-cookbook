<script setup lang="ts">
import { computed, useId } from 'vue';

export interface SelectOption {
  label: string;
  value: string | number;
  disabled?: boolean;
}

export type SelectSize = 'sm' | 'md';

const props = withDefaults(
  defineProps<{
    modelValue?: string | number;
    label?: string;
    options: SelectOption[];
    placeholder?: string;
    disabled?: boolean;
    required?: boolean;
    size?: SelectSize;
    name?: string;
    id?: string;
  }>(),
  {
    modelValue: '',
    label: '',
    placeholder: '',
    disabled: false,
    required: false,
    size: 'sm',
    name: '',
    id: '',
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
  change: [event: Event];
  blur: [event: FocusEvent];
  focus: [event: FocusEvent];
}>();

const autoId = useId();
const selectId = computed(() => props.id || `ui-select-${autoId}`);
const valueAsString = computed(() => String(props.modelValue ?? ''));

function onChange(event: Event) {
  const value = (event.target as HTMLSelectElement).value;
  emit('update:modelValue', value);
  emit('change', event);
}
</script>

<template>
  <label v-if="label" class="ui-select-field" :for="selectId">{{ label }}</label>
  <div class="ui-select-wrap">
    <select
      :id="selectId"
      class="ui-select"
      :class="`ui-select--${size}`"
      :name="name || undefined"
      :value="valueAsString"
      :disabled="disabled"
      :required="required"
      @change="onChange"
      @blur="emit('blur', $event)"
      @focus="emit('focus', $event)"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option
        v-for="opt in options"
        :key="String(opt.value)"
        :value="String(opt.value)"
        :disabled="opt.disabled"
      >
        {{ opt.label }}
      </option>
    </select>
  </div>
</template>

<style scoped>
.ui-select-field {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--scr-text-label, #4b5563);
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
}

.ui-select-wrap {
  position: relative;
  width: 100%;
}

.ui-select-wrap::after {
  content: '';
  position: absolute;
  right: 12px;
  top: 50%;
  width: 8px;
  height: 8px;
  border-right: 2px solid var(--scr-text-muted, #6b7280);
  border-bottom: 2px solid var(--scr-text-muted, #6b7280);
  transform: translateY(-70%) rotate(45deg);
  pointer-events: none;
}

.ui-select {
  width: 100%;
  border: 1px solid var(--scr-border, #d1d5db);
  border-radius: var(--scr-radius-sm, 4px);
  background: #fff;
  color: var(--scr-text, #1f2937);
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
  box-sizing: border-box;
  appearance: none;
  padding-right: 34px;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    background-color 0.15s ease;
}

.ui-select--sm {
  height: 34px;
  padding: 6px 10px;
  font-size: 13px;
}

.ui-select--md {
  height: 38px;
  padding: 8px 12px;
  font-size: 14px;
}

.ui-select:hover:not(:disabled) {
  border-color: var(--color-border-gray, rgba(49, 46, 129, 0.3));
}

.ui-select:focus-visible {
  outline: none;
  border-color: var(--color-primary, #312E81);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-primary, #312E81) 18%, transparent);
}

.ui-select:disabled {
  background: var(--scr-bg-muted, #f3f4f6);
  color: var(--scr-text-muted, #6b7280);
  cursor: not-allowed;
}
</style>
