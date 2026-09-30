<script setup lang="ts">
import { computed, useId } from 'vue';

export interface RadioOption {
  label?: string;
  value: string;
}

const props = withDefaults(
  defineProps<{
    /** Group label shown on the left (e.g. 目的, 区分). */
    label?: string;
    options: RadioOption[];
    modelValue: string;
    /** Native `name` for the radio group; auto-generated when omitted. */
    name?: string;
    disabled?: boolean;
  }>(),
  {
    disabled: false,
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const autoName = useId();
const groupName = computed(() => props.name ?? `ui-radio-${autoName}`);

function onSelect(value: string) {
  if (props.disabled || props.modelValue === value) return;
  emit('update:modelValue', value);
}
</script>

<template>
  <div class="ui-radio-group" role="radiogroup" :aria-label="label">
    <span v-if="label" class="ui-radio-group__label">{{ label }}</span>
    <div class="ui-radio-group__options">
      <label
        v-for="opt in options"
        :key="opt.value"
        class="ui-radio"
        :class="{ 'ui-radio--checked': modelValue === opt.value, 'ui-radio--disabled': disabled }"
      >
        <input
          type="radio"
          class="ui-radio__input"
          :name="groupName"
          :value="opt.value"
          :checked="modelValue === opt.value"
          :disabled="disabled"
          @change="onSelect(opt.value)"
        />
        <span class="ui-radio__control" aria-hidden="true" />
        <span class="ui-radio__text">{{ opt.label }}</span>
      </label>
    </div>
  </div>
</template>

<style scoped>
.ui-radio-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px 24px;
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
}

.ui-radio-group__label {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 400;
  color: #272C36;
  white-space: nowrap;
}

.ui-radio-group__options {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 20px;
}

.ui-radio {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.ui-radio--disabled {
  cursor: not-allowed;
}

.ui-radio__input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.ui-radio__control {
  position: relative;
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  border: 1px solid #BEC3CF;
  border-radius: 50%;
  background: #F3F4F6;
  box-sizing: border-box;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease;
}

.ui-radio__control::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4338CA;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.15s ease;
}

.ui-radio--checked .ui-radio__control {
  border-color: #4338CA;
  background: white;
}

.ui-radio--checked .ui-radio__control::after {
  transform: translate(-50%, -50%) scale(1);
}

.ui-radio:not(.ui-radio--disabled):hover .ui-radio__control {
  border-color: #4338CA;
}

.ui-radio__text {
  font-size: 14px;
  font-weight: 400;
  color: #272C36;
  line-height: 20px;
  white-space: nowrap;
}

.ui-radio__input:focus-visible + .ui-radio__control {
  outline: 1px solid #4338CA;
  outline-offset: 1px;
}
</style>
