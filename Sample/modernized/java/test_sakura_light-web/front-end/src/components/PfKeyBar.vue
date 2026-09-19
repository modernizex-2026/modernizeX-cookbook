<script setup lang="ts">
const emit = defineEmits<{ pfKey: [key: string] }>();

const KEYS = [
  { key: 'Enter', label: 'Enter' },
  { key: 'F1', label: 'F1' },
  { key: 'F3', label: 'F3' },
  { key: 'F6', label: 'F6' },
  { key: 'F7', label: 'F7' },
  { key: 'F8', label: 'F8' },
  { key: 'F9', label: 'F9' },
  { key: 'F10', label: 'F10' },
  { key: 'F12', label: 'F12' },
];
</script>

<template>
  <div class="pf-key-bar">
    <button
      v-for="{ key, label } in KEYS"
      :key="key"
      class="pf-key-btn"
      @click="emit('pfKey', key)"
    >{{ label }}</button>
  </div>
</template>
