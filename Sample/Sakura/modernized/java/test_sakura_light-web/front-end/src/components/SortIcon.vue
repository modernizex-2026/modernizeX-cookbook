<!--
  SortIcon — three-state arrow indicator for sortable table columns.

  States: 'asc' | 'desc' | null (idle). Click handler is on the parent <th>
  (this component is purely a visual indicator).

  Used by OCCURS tables in form/list archetypes when the column count is ≥3
  and the underlying data is non-trivial. A SortIcon is placed next to each
  sortable <th>'s text label.

  Usage:
    <th @click="toggleSort('hanbaicd')">
      品目CD <SortIcon :state="sortKey === 'hanbaicd' ? sortDir : null" />
    </th>
-->
<script setup lang="ts">
defineProps<{
  state: 'asc' | 'desc' | null;
  size?: number;
}>();
</script>

<template>
  <span :class="['scr-sort-icon', state ?? 'idle']" aria-hidden="true">
    <svg :width="size ?? 12" :height="size ?? 12" viewBox="0 0 12 12" fill="none">
      <path d="M6 2 L9 6 L3 6 Z" :class="state === 'asc' ? 'scr-sort-active' : 'scr-sort-dim'" fill="currentColor" />
      <path d="M6 10 L9 6 L3 6 Z" :class="state === 'desc' ? 'scr-sort-active' : 'scr-sort-dim'" fill="currentColor" transform="translate(0 0)" />
    </svg>
  </span>
</template>

<style scoped>
.scr-sort-icon { display: inline-flex; align-items: center; margin-left: 4px; vertical-align: middle; }
.scr-sort-active { fill: var(--scr-primary, #2563eb); opacity: 1; }
.scr-sort-dim { fill: currentColor; opacity: 0.25; }
.scr-sort-icon.asc .scr-sort-active,
.scr-sort-icon.desc .scr-sort-active { opacity: 1; }
.scr-sort-icon.idle { color: var(--scr-text-muted, #6b7280); }
</style>
