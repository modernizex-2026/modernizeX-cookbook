<!--
  StatusBadge — colored chip for enum/flag values.

  COBOL screens commonly have 1-character flag fields with referenced 88-level
  values (e.g. SC-KUBUN with picture 9(1), legend "0:登録 1:変更 2:削除").
  Rendering the raw digit as plain text loses the semantic — a badge with the
  decoded label + color is much more readable.

  Pipeline use: a StatusBadge is placed next to flag fields.

  Usage:
    <StatusBadge label="登録" variant="info" size="sm" />
-->
<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string;
    variant?: 'info' | 'success' | 'warning' | 'danger' | 'neutral' | 'pending';
    size?: 'xs' | 'sm' | 'md';
    outline?: boolean;
  }>(),
  { variant: 'neutral', size: 'sm', outline: false },
);
</script>

<template>
  <span :class="['scr-status-badge', `scr-status-${variant}`, `scr-status-${size}`, outline ? 'scr-status-outline' : '']">{{ label }}</span>
</template>

<style scoped>
.scr-status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: 600;
  letter-spacing: 0.25px;
  line-height: 1.4;
  border: 1px solid transparent;
  vertical-align: middle;
}
.scr-status-xs { font-size: 10px; padding: 1px 6px; }
.scr-status-sm { font-size: 11px; padding: 2px 8px; }
.scr-status-md { font-size: 12px; padding: 4px 12px; }

.scr-status-info     { background: #dbeafe; color: #1e40af; }
.scr-status-success  { background: #d1fae5; color: #065f46; }
.scr-status-warning  { background: #fef3c7; color: #78350f; }
.scr-status-danger   { background: #fee2e2; color: #7f1d1d; }
.scr-status-pending  { background: #ede9fe; color: #5b21b6; }
.scr-status-neutral  { background: #e5e7eb; color: #374151; }

.scr-status-outline.scr-status-info    { background: transparent; border-color: #1e40af; color: #1e40af; }
.scr-status-outline.scr-status-success { background: transparent; border-color: #065f46; color: #065f46; }
.scr-status-outline.scr-status-warning { background: transparent; border-color: #78350f; color: #78350f; }
.scr-status-outline.scr-status-danger  { background: transparent; border-color: #7f1d1d; color: #7f1d1d; }
.scr-status-outline.scr-status-pending { background: transparent; border-color: #5b21b6; color: #5b21b6; }
.scr-status-outline.scr-status-neutral { background: transparent; border-color: #374151; color: #374151; }
</style>
