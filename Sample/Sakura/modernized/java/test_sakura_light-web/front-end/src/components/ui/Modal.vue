<!--
  Modal — COBOL screen dialog shell (overlay + card + header / body / footer slots).
  Usage:
    <Modal title="購買情報" min-width="400px">
      <template #body>...</template>
      <template #footer>
        <button class="btn btn-primary btn-sm">確定</button>
      </template>
    </Modal>
-->
<script setup lang="ts">
import { computed, useSlots } from 'vue';

const props = withDefaults(
  defineProps<{
    title?: string;
    visible?: boolean;
    minWidth?: string;
    maxWidth?: string;
    class?: string;
    closeAble?: boolean;
  }>(),
  {
    title: '',
    visible: true,
    class: '',
    closeAble: true,
  },
);

const slots = useSlots();
const { title, visible, minWidth, class: className, maxWidth } = props;
const dialogStyle = computed(() => {
  const style: Record<string, string> = {};
  if (minWidth) style.minWidth = minWidth;
  if (maxWidth) style.maxWidth = maxWidth;
  return style;
});

const showHeader = computed(() => Boolean(title || slots.header));
const showFooter = computed(() => Boolean(slots.footer));
</script>

<template>
  <div v-if="visible" class="scr-dialog-overlay" :class="className">
    <div
      class="scr-dialog scr-modal"
      role="dialog"
      aria-modal="true"
      :aria-label="title || undefined"
      :style="dialogStyle"
    >
      <div v-if="showHeader" class="scr-dialog-header">
        <slot name="header">
          <h3 v-if="title" class="scr-modal__title">{{ title }}</h3>
        </slot>
        <button
          v-if="closeAble"
          type="button"
          class="scr-dialog-close"
          aria-label="Close"
          @click="$emit('update:visible', false)" >
          <img src="/icons/close.svg" alt="Close">
        </button>
      </div>

      <div class="scr-dialog-body">
        <slot name="body" />
      </div>

      <div v-if="showFooter" class="scr-dialog-actions">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.scr-modal {
  padding: 0;
}

.scr-dialog-header {
  flex: 0 0 auto;
  padding: 16px;
}

.scr-modal__title,
.scr-dialog-header :deep(h3) {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #272C36;
}

.scr-modal .scr-dialog-body {
  padding-top: 8px;
}

.scr-modal .scr-dialog-actions {
  margin-top: 0;
  padding: 16px;
  padding-top: 0;
}
.scr-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.scr-dialog-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  border: none;
}
</style>
