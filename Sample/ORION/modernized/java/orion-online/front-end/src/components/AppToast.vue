<!--
  AppToast — transient notification banner.

  Use this from any generated SFC to surface ACCEPT/DISPLAY-driven status
  messages (success/warn/error) without taking screen space.

  Mirrors the Vercel POC pattern: 4 variants × auto-dismiss timer × slide-in
  from top. Inline in this template repo so every modernize-pipeline output
  can `import AppToast from '../../components/AppToast.vue'`.

  Usage:
    <AppToast :message="msg" :type="'success'" :visible="show" @hide="show=false" />
-->
<script setup lang="ts">
import { watch, onUnmounted, ref } from 'vue';

const props = withDefaults(
  defineProps<{
    message: string;
    type?: 'info' | 'success' | 'warning' | 'error';
    visible: boolean;
    duration?: number; // ms; 0 = sticky
  }>(),
  { type: 'info', duration: 3500 },
);

const emit = defineEmits<{ (e: 'hide'): void }>();

const timer = ref<number | null>(null);

function arm() {
  if (timer.value) {
    clearTimeout(timer.value);
    timer.value = null;
  }
  if (props.visible && props.duration > 0) {
    timer.value = window.setTimeout(() => emit('hide'), props.duration);
  }
}
watch(() => [props.visible, props.duration], arm, { immediate: true });
onUnmounted(() => {
  if (timer.value) clearTimeout(timer.value);
});
</script>

<template>
  <Transition name="toast-slide">
    <div v-if="visible" :class="['scr-toast', `scr-toast-${type}`]" role="status">
      <span class="scr-toast-msg">{{ message }}</span>
      <button class="scr-toast-close" @click="emit('hide')" aria-label="close">×</button>
    </div>
  </Transition>
</template>

<style scoped>
.scr-toast {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 200;
  padding: 10px 14px 10px 18px;
  border-radius: 6px;
  min-width: 280px;
  max-width: 540px;
  font-size: 13px;
  font-family: var(--scr-font-ui, inherit);
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border-left: 4px solid;
}
.scr-toast-msg {
  flex: 1;
}
.scr-toast-close {
  background: transparent;
  border: 0;
  color: inherit;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  opacity: 0.65;
}
.scr-toast-close:hover {
  opacity: 1;
}

.scr-toast-info {
  background: #eff6ff;
  color: #1e40af;
  border-color: #2563eb;
}
.scr-toast-success {
  background: #f0fdf4;
  color: #14532d;
  border-color: #16a34a;
}
.scr-toast-warning {
  background: #fffbeb;
  color: #78350f;
  border-color: #d97706;
}
.scr-toast-error {
  background: #fef2f2;
  color: #7f1d1d;
  border-color: #dc2626;
}

.toast-slide-enter-from {
  transform: translate(-50%, -20px);
  opacity: 0;
}
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition:
    transform 0.2s ease,
    opacity 0.2s ease;
}
.toast-slide-leave-to {
  transform: translate(-50%, -20px);
  opacity: 0;
}
</style>
