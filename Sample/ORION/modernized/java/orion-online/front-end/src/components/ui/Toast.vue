<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue';

export type ToastType = 'info' | 'success' | 'warning' | 'error';

const props = withDefaults(
  defineProps<{
    message: string;
    type?: ToastType;
    visible: boolean;
    duration?: number;
  }>(),
  {
    type: 'info',
    duration: 3500,
  },
);

const emit = defineEmits<{
  hide: [];
}>();

const timer = ref<number | null>(null);

function clearTimer() {
  if (!timer.value) return;
  clearTimeout(timer.value);
  timer.value = null;
}

function armTimer() {
  clearTimer();
  if (props.visible && props.duration > 0) {
    timer.value = window.setTimeout(() => emit('hide'), props.duration);
  }
}

watch(() => [props.visible, props.duration], armTimer, { immediate: true });
onUnmounted(clearTimer);
</script>

<template>
  <Transition name="ui-toast-slide">
    <div v-if="visible" class="ui-toast" :class="`ui-toast--${type}`" role="status" aria-live="polite">
      <span class="ui-toast__message">{{ message }}</span>
      <button class="ui-toast__close" type="button" aria-label="Close" @click="emit('hide')">x</button>
    </div>
  </Transition>
</template>

<style scoped>
.ui-toast {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 220;
  min-width: 280px;
  max-width: min(560px, calc(100vw - 24px));
  padding: 10px 14px 10px 16px;
  border-radius: var(--scr-radius, 6px);
  border-left: 4px solid;
  box-shadow: var(--scr-shadow, 0 4px 12px rgba(0, 0, 0, 0.08));
  font-family: var(--scr-font-ui, 'Meiryo UI', 'Yu Gothic UI', sans-serif);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.ui-toast__message {
  flex: 1;
  line-height: 1.4;
}

.ui-toast__close {
  border: 0;
  background: transparent;
  color: currentColor;
  font-size: 17px;
  line-height: 1;
  cursor: pointer;
  opacity: 0.7;
  padding: 0;
}

.ui-toast__close:hover {
  opacity: 1;
}

.ui-toast--info {
  background: #eff6ff;
  color: #1e40af;
  border-color: #2563eb;
}

.ui-toast--success {
  background: #f0fdf4;
  color: #14532d;
  border-color: #16a34a;
}

.ui-toast--warning {
  background: #fffbeb;
  color: #78350f;
  border-color: #d97706;
}

.ui-toast--error {
  background: #fef2f2;
  color: #7f1d1d;
  border-color: #dc2626;
}

.ui-toast-slide-enter-from,
.ui-toast-slide-leave-to {
  transform: translate(-50%, -16px);
  opacity: 0;
}

.ui-toast-slide-enter-active,
.ui-toast-slide-leave-active {
  transition:
    transform 0.2s ease,
    opacity 0.2s ease;
}
</style>
