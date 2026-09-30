<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount } from 'vue';
import Button from '@/components/ui/Button.vue';
const props = defineProps<{ text: string | null }>();
const emit = defineEmits<{ ok: [] }>();

const overlayRef = ref<HTMLDivElement | null>(null);
let captureAttached = false;

function dismiss() {
  emit('ok');
}

/** Enter/Esc must work even when focus stays on the underlying form input (login, OCCURS table). */
function onCaptureKey(e: KeyboardEvent) {
  if (props.text === null) return;
  if (e.key !== 'Enter' && e.key !== 'Escape') return;
  if (e.shiftKey || e.ctrlKey || e.altKey || e.metaKey) return;
  e.preventDefault();
  e.stopImmediatePropagation();
  dismiss();
}

function syncCaptureListener(visible: boolean) {
  if (visible && !captureAttached) {
    window.addEventListener('keydown', onCaptureKey, { capture: true });
    captureAttached = true;
  } else if (!visible && captureAttached) {
    window.removeEventListener('keydown', onCaptureKey, { capture: true });
    captureAttached = false;
  }
}

watch(
  () => props.text,
  (t) => {
    const visible = t !== null;
    syncCaptureListener(visible);
    if (visible) nextTick(() => overlayRef.value?.focus());
  },
  { immediate: true },
);

onBeforeUnmount(() => syncCaptureListener(false));
</script>

<template>
  <Teleport to="body">
    <Transition name="guide">
      <div
        v-if="text !== null"
        ref="overlayRef"
        class="guide-overlay"
        tabindex="0"
        @keydown.enter.prevent.stop="dismiss"
        @keydown.esc.prevent.stop="dismiss"
      >
        <div class="guide-dialog" role="alertdialog" aria-modal="true">
          <div class="guide-body">
            <span class="guide-icon" aria-hidden="true">
              <img src="/icons/warning.svg" alt="Warning"/>
            </span>
            <p class="guide-text">
              {{ text }}
            </p>
          </div>
          <div class="guide-actions">
            <Button
              variant="primary"
              label="OK"
              width="100%"
              @click="dismiss"
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.guide-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
}
.guide-dialog {
  width: 100%;
  min-width: 380px;
  max-width: 3%;
  overflow: hidden;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35), 0 2px 6px rgba(0, 0, 0, 0.12);
}
.guide-body {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 14px;
  padding: 26px 24px 22px;
}
.guide-icon {
  flex: 0 0 auto;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFF8ED;
}
.guide-text {
  text-align: center;
  margin: 3px 0 0;
  font-size: 15px;
  line-height: 1.65;
  color: #1f2937;
  word-break: break-word;
}
.guide-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px;
  border-top: 1px solid #eef0f2;
}
.guide-ok {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 22px;
  border-radius: 8px;
  cursor: pointer;
  font: inherit;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  transition: background 0.12s ease, transform 0.06s ease;
}
.guide-ok:hover {
  background: #d97706;
}
.guide-ok:active {
  transform: translateY(1px);
}
.guide-ok:focus-visible {
  outline: 2px solid #1f2937;
  outline-offset: 2px;
}
.guide-ok kbd {
  font-family: inherit;
  font-size: 11px;
  font-weight: 500;
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.4);
}
/* entrance / exit */
.guide-enter-active {
  transition: opacity 0.18s ease;
}
.guide-enter-active .guide-dialog {
  transition: transform 0.2s cubic-bezier(0.34, 1.3, 0.64, 1), opacity 0.18s ease;
}
.guide-leave-active {
  transition: opacity 0.14s ease;
}
.guide-enter-from {
  opacity: 0;
}
.guide-enter-from .guide-dialog {
  opacity: 0;
  transform: scale(0.94) translateY(6px);
}
.guide-leave-to {
  opacity: 0;
}
@media (prefers-reduced-motion: reduce) {
  .guide-overlay {
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
  }
  .guide-enter-active,
  .guide-leave-active,
  .guide-enter-active .guide-dialog {
    transition: none;
  }
}
</style>
