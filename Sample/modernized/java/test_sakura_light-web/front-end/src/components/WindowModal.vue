<script setup lang="ts">
import { computed } from 'vue';
import type { ActiveWindow } from '../types/screen';

const props = defineProps<{ window: ActiveWindow }>();

const charW = 0.6;
const charH = 1.2;

const positionStyle = computed(() => ({
  position: 'absolute' as const,
  top: `${(props.window.line - 1) * charH}em`,
  left: `${(props.window.col - 1) * charW}em`,
  width: `${props.window.width * charW}em`,
  height: `${props.window.height * charH}em`,
  border: props.window.border ? '1px solid #00ff00' : 'none',
  backgroundColor: 'transparent',
  zIndex: 10,
  pointerEvents: 'none' as const,
}));
</script>

<template>
  <div class="window-modal" :style="positionStyle"></div>
</template>
