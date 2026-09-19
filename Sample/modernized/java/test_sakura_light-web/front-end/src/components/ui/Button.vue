<script setup lang="ts">
export type ButtonVariant = 'primary' | 'outline' | 'danger' | 'success' | 'warning';
export type ButtonSize = 'sm' | 'md';

const props = withDefaults(
  defineProps<{
    label: string;
    hotkey?: string;
    variant?: ButtonVariant;
    size?: ButtonSize;
    disabled?: boolean;
    type?: 'button' | 'submit' | 'reset';
    width?: string;
  }>(),
  {
    variant: 'primary',
    size: 'sm',
    disabled: false,
    type: 'button',
  },
);

const emit = defineEmits<{
  click: [event: MouseEvent];
}>();

function onClick(event: MouseEvent) {
  if (props.disabled) return;
  emit('click', event);
}
</script>

<template>
  <button
    :type="type"
    class="ui-button"
    :class="[`ui-button--${variant}`, `ui-button--${size}`]"
    :disabled="disabled"
    @click="onClick"
    :style="{ width: width }"
  >
    <span class="ui-button__label">{{ label }}</span>
    <!-- <kbd v-if="hotkey" class="ui-button__hotkey">{{ hotkey }}</kbd> -->
  </button>
</template>

<style scoped>
.ui-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;
}

.ui-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  pointer-events: none;
}

.ui-button--sm {
  height: 44px;
  padding: 10px 20px;
  font-size: 14px;
}

.ui-button--md {
  height: 38px;
  padding: 10px 20px;
}

.ui-button--primary {
  background: #4338CA;
  color: var(--color-on-primary);
  border-color: #4338CA;
}

.ui-button--primary:hover:not(:disabled) {
  background: #3730A3;
  border-color: #3730A3;
}

.ui-button--primary:active:not(:disabled) {
  background: #3730A3;
  border-color: #3730A3;
}

.ui-button--outline {
  background: white;
  color: #4338CA;
  border-color: #4338CA;
}

.ui-button--outline:hover:not(:disabled) {
  background: var(--color-outline-hover-bg);
}

.ui-button__label {
  font-weight: 500;
}

.ui-button--danger {
  background: white;
  color: #C81E1E;
  border-color: #C81E1E;
}
</style>
