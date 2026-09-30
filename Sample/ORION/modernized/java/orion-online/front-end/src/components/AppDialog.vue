<!--
  AppDialog — confirmation modal with title + message + 2 buttons.

  Use for destructive/irreversible actions: 削除, 一括承認, 終了, etc.
  Mirrors Vercel POC pattern (mode-modal, delete-confirm, batch-approve).

  Pipeline note: this is a SHARED component, NOT an AI-generated SFC override.
  Generated screen SFCs import + use it inline for ad-hoc confirmations; the
  parent TerminalScreen.vue is NOT involved.

  Usage:
    <AppDialog
      title="削除確認"
      message="この申請書を削除しますか？"
      confirmLabel="削除"
      :visible="showConfirm"
      @confirm="doDelete"
      @cancel="showConfirm = false"
    />
-->
<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import Button from '@/components/ui/Button.vue';

const props = withDefaults(
  defineProps<{
    title: string;
    message?: string;
    confirmLabel?: string;
    cancelLabel?: string;
    visible: boolean;
    confirmVariant?: 'primary' | 'danger' | 'success' | 'warning';
  }>(),
  {
    confirmLabel: 'OK',
    cancelLabel: 'キャンセル',
    confirmVariant: 'primary',
  },
);

const emit = defineEmits<{ (e: 'confirm'): void; (e: 'cancel'): void }>();

// Root is <Teleport><Transition><div v-if>, so Vue's automatic attrs
// fallthrough doesn't reliably reach the teleported node — a consumer's
// `class="foo"` silently never lands on .scr-dialog-overlay. Take it over
// explicitly instead (v-bind="$attrs" on that div still merges its own
// static `class` per Vue's special class/style handling).
defineOptions({ inheritAttrs: false });

const overlayRef = ref<HTMLElement | null>(null);

watch(() => props.visible, async (val) => {
  if (val) {
    await nextTick();
    overlayRef.value?.focus();
  }
});

function onBackdrop() { emit('cancel'); }
function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape' || e.key === 'F6') emit('cancel');
  if (e.key === 'Enter') emit('confirm');
}
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog-fade">
      <div v-if="visible" v-bind="$attrs" ref="overlayRef" class="scr-dialog-overlay" @click.self="onBackdrop" @keydown.stop="onKey" tabindex="0" style="outline:none">
        <div class="scr-app-dialog" role="dialog" :aria-label="title">
          <div class="scr-app-dialog-header">
            <h3 class="scr-app-dialog-title">{{ title }}</h3>
          </div>
          <div class="scr-app-dialog-body">
            <slot>{{ message }}</slot>
          </div>
          <div class="scr-app-dialog-actions">
            <Button
              class="btn-outline"
              size="sm"
              variant="outline"
              width="50%"
              :label="cancelLabel"
              @click="emit('cancel')"
            />
            <Button
              :class="['btn', `btn-${confirmVariant}`, 'btn-sm']"
              :label="confirmLabel"
              :variant="confirmVariant"
              width="50%"
              autofocus
              @click="emit('confirm')"
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.scr-dialog-overlay {
  position: fixed; inset: 0; z-index: 150;
  background: rgba(15, 23, 42, 0.45);
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.scr-app-dialog {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  min-width: 320px; max-width: 360px; width: 100%;
  overflow: hidden;
}
.scr-app-dialog-header {
  padding: 24px 16px;
  text-align: center;
  background: var(--scr-bg-card, #fff);
}
.scr-app-dialog-title { margin: 0; font-size: 15px; font-weight: 600; color: var(--scr-text, #1f2937); }
.scr-app-dialog-body { padding: 20px; font-size: 14px; text-align: center; color: var(--scr-text, #69707C); line-height: 1.6; }
.scr-app-dialog-actions {
  padding: 16px;
  display: flex; justify-content: center; gap: 8px;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to { opacity: 0; }
.dialog-fade-enter-active,
.dialog-fade-leave-active { transition: opacity 0.15s ease; }
</style>
