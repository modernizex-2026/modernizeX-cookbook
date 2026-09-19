<!--
  ModeSelectModal — full-card modal shown at session start when the COBOL
  screen requires a mode-of-operation choice (e.g. KB-SYONIN-SW: 1=承認者
  2=申請者).

  Differs from AppDialog: this is a launch-time mandatory selection (no
  cancel), typically with 2-3 large card buttons rather than text+yes/no.

  Persistence: callers should remember the chosen mode in sessionStorage and
  skip the modal on subsequent visits.

  Usage:
    <ModeSelectModal
      :visible="showModeModal"
      title="表示モードを選択"
      :options="[
        { value: '1', label: '承認者モード', desc: '承認待ちの申請書を確認', variant: 'info' },
        { value: '2', label: '申請者モード', desc: '新規申請・編集',         variant: 'success' },
      ]"
      @select="onModeSelected"
    />
-->
<script setup lang="ts">
interface ModeOption {
  value: string;
  label: string;
  desc?: string;
  variant?: 'info' | 'success' | 'warning' | 'danger' | 'neutral';
}

withDefaults(
  defineProps<{
    visible: boolean;
    title: string;
    subtitle?: string;
    options: ModeOption[];
  }>(),
  {},
);
const emit = defineEmits<{ (e: 'select', value: string): void }>();
</script>

<template>
  <Teleport to="body">
    <Transition name="mode-fade">
      <div v-if="visible" class="scr-mode-overlay" role="dialog" aria-modal="true">
        <div class="scr-mode-card">
          <div class="scr-mode-header">
            <h2>{{ title }}</h2>
            <p v-if="subtitle">{{ subtitle }}</p>
          </div>
          <div class="scr-mode-grid">
            <button
              v-for="opt in options"
              :key="opt.value"
              :class="['scr-mode-option', `scr-mode-${opt.variant ?? 'info'}`]"
              @click="emit('select', opt.value)"
            >
              <span class="scr-mode-label">{{ opt.label }}</span>
              <span v-if="opt.desc" class="scr-mode-desc">{{ opt.desc }}</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.scr-mode-overlay {
  position: fixed;
  inset: 0;
  z-index: 180;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}
.scr-mode-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
  min-width: 480px;
  max-width: 640px;
  width: 100%;
  overflow: hidden;
}
.scr-mode-header {
  padding: 24px 28px 18px;
  background: linear-gradient(
    135deg,
    var(--scr-primary, #1e3a8a),
    var(--scr-primary-hover, #1e40af)
  );
  color: #fff;
}
.scr-mode-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.scr-mode-header p {
  margin: 6px 0 0;
  opacity: 0.85;
  font-size: 13px;
}
.scr-mode-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding: 24px;
}
.scr-mode-option {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 18px 20px;
  background: var(--scr-bg-card, #fff);
  border: 2px solid var(--scr-border, #d1d5db);
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition:
    border-color 0.15s,
    transform 0.1s,
    box-shadow 0.15s;
}
.scr-mode-option:hover {
  border-color: var(--scr-primary, #2563eb);
  box-shadow: 0 4px 14px rgba(30, 64, 175, 0.12);
  transform: translateY(-1px);
}
.scr-mode-option:active {
  transform: translateY(0);
}
.scr-mode-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--scr-text, #1f2937);
}
.scr-mode-desc {
  font-size: 12px;
  color: var(--scr-text-muted, #4b5563);
  line-height: 1.45;
}

.scr-mode-info:hover {
  border-color: #1e40af;
}
.scr-mode-success:hover {
  border-color: #16a34a;
}
.scr-mode-warning:hover {
  border-color: #d97706;
}
.scr-mode-danger:hover {
  border-color: #dc2626;
}

.mode-fade-enter-from,
.mode-fade-leave-to {
  opacity: 0;
}
.mode-fade-enter-active,
.mode-fade-leave-active {
  transition: opacity 0.2s ease;
}
</style>
