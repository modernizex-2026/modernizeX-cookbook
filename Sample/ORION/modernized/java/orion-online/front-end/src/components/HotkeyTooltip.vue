<!--
  HotkeyTooltip — `?` icon button that pops a list of keyboard shortcuts
  on hover/click.

  Use when the screen has 3+ action buttons each bound to a PF/AID key.
  Discoverability win: users don't have to memorize which F-key does what.

  Mirrors Vercel POC's HotkeyTooltip pattern. Click outside or Escape closes.

  Usage:
    <HotkeyTooltip :shortcuts="[
      { keys: 'F3',  label: '終了' },
      { keys: 'F5',  label: '参照検索' },
      { keys: 'F9',  label: 'モード切替' },
      { keys: 'F10', label: '一括印刷' },
      { keys: 'Enter', label: '決定' },
    ]" />
-->
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

interface Shortcut { keys: string; label: string }
defineProps<{ shortcuts: Shortcut[]; align?: 'left' | 'right' }>();

const open = ref(false);
const wrapper = ref<HTMLElement | null>(null);

function toggle() { open.value = !open.value; }
function close() { open.value = false; }

function onKey(e: KeyboardEvent) { if (e.key === 'Escape') close(); }
function onClickOutside(e: MouseEvent) {
  if (wrapper.value && !wrapper.value.contains(e.target as Node)) close();
}
onMounted(() => {
  document.addEventListener('mousedown', onClickOutside);
  document.addEventListener('keydown', onKey);
});
onUnmounted(() => {
  document.removeEventListener('mousedown', onClickOutside);
  document.removeEventListener('keydown', onKey);
});
</script>

<template>
  <span ref="wrapper" class="scr-hotkey-tooltip" :class="align === 'right' ? 'align-right' : ''">
    <button class="scr-hotkey-trigger" @click="toggle" type="button" title="キーボードショートカット一覧">?</button>
    <div v-if="open" class="scr-hotkey-popover">
      <h4 class="scr-hotkey-title">キーボードショートカット</h4>
      <ul class="scr-hotkey-list">
        <li v-for="sc in shortcuts" :key="sc.keys" class="scr-hotkey-row">
          <kbd>{{ sc.keys }}</kbd>
          <span>{{ sc.label }}</span>
        </li>
      </ul>
    </div>
  </span>
</template>

<style scoped>
.scr-hotkey-tooltip { position: relative; display: inline-block; }
.scr-hotkey-trigger {
  width: 22px; height: 22px; border-radius: 50%;
  border: 1px solid var(--scr-border, #d1d5db);
  background: var(--scr-bg-card, #fff);
  color: var(--scr-text-muted, #4b5563);
  font-size: 11px; font-weight: 700;
  cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
  line-height: 1;
}
.scr-hotkey-trigger:hover { background: var(--scr-bg-muted, #f3f4f6); color: var(--scr-text, #1f2937); }
.scr-hotkey-popover {
  position: absolute; top: calc(100% + 6px); left: 0;
  min-width: 240px;
  background: #fff;
  border: 1px solid var(--scr-border, #d1d5db);
  border-radius: 6px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  padding: 8px 0;
  z-index: 50;
}
.align-right .scr-hotkey-popover { left: auto; right: 0; }
.scr-hotkey-title {
  margin: 0 12px 6px;
  padding-bottom: 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--scr-text-muted, #4b5563);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--scr-border, #d1d5db);
}
.scr-hotkey-list { list-style: none; margin: 0; padding: 0; }
.scr-hotkey-row {
  display: flex; align-items: center; gap: 10px;
  padding: 4px 12px;
  font-size: 12px;
  color: var(--scr-text, #1f2937);
}
.scr-hotkey-row kbd {
  display: inline-block;
  min-width: 36px;
  padding: 1px 8px;
  background: var(--scr-bg-muted, #f3f4f6);
  border: 1px solid var(--scr-border, #d1d5db);
  border-radius: 3px;
  font-size: 11px;
  font-family: ui-monospace, monospace;
  text-align: center;
  color: var(--scr-text-muted, #4b5563);
}
</style>
