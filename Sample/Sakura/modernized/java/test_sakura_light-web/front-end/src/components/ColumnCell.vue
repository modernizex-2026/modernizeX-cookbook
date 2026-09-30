<!--
  ColumnCell — generic dispatcher for declarative column rendering.

  Used by list-archetype SFCs together with the COLUMNS metadata pattern
  (see src/composables/columnExtraction.ts).

  Renders one <td> per render mode:
    - plain     : single string, default left-align
    - numeric   : single string, .scr-numeric (right-align + mono font)
    - stacked   : N-line vertical stack with NBSP placeholders for empty lines
    - stamps    : stamp seal boxes (filled name OR — placeholder)
    - computed  : same as plain but value comes from compute() not buffer

  Empty detection uses isBlankCell() which strips ZWSP + full-width-space
  (needed because COBOL `MOVE ALL NC"　" TO field` fills with U+3000).
-->
<script setup lang="ts">
import { computed } from 'vue';
import { isBlankCell } from '../composables/columnExtraction';
import type { ColumnDef, ExtractedRecord } from '../composables/columnExtraction';

const props = defineProps<{
  col: ColumnDef;
  row: ExtractedRecord;
}>();

/** Strip artifacts that COBOL field padding leaves behind:
 * - U+FFFD (REPLACEMENT CHAR) — appears when byte-buffer decode hits invalid
 * Shift-JIS / non-charset bytes (NEC WEBCOBOL stores PIC N(n) as 2n bytes;
 * ASCII content like "Adm" leaves 3 trailing padding bytes that may not
 * round-trip cleanly through JSON UTF-8 → render as `Adm�`).
 * - U+3000 (full-width space) — COBOL `MOVE ALL NC"　"` padding.
 * - U+200B (zero-width space) — sometimes injected by paint-buffer.
 * - Standard whitespace — trim. */
function sanitizeCellValue(s: string): string {
  return s.replace(/�/g, '').replace(/[　​\s]+$/g, '').trim();
}

const rawValue = computed(() => props.row[props.col.key]);
const value = computed(() => {
  const v = rawValue.value;
  return typeof v === 'string' ? sanitizeCellValue(v) : v;
});

const cellClass = computed(() => {
  const base = props.col.cellClass ?? '';
  if (props.col.render === 'numeric') return `scr-numeric ${base}`.trim();
  return base;
});

const stackedLines = computed<string[]>(() => {
  const v = rawValue.value;
  const arr = Array.isArray(v) ? v : (typeof v === 'string' ? [v] : []);
  return arr.map(sanitizeCellValue);
});

const stampList = computed<string[]>(() => {
  const v = rawValue.value;
  const arr = Array.isArray(v) ? v : (typeof v === 'string' ? [v] : []);
  return arr.map(sanitizeCellValue);
});
</script>

<template>
  <!-- Plain / numeric / computed: single value -->
  <td v-if="col.render === 'plain' || col.render === 'numeric' || col.render === 'computed' || !col.render"
      :class="cellClass">
    {{ value || ' ' }}
  </td>

  <!-- Stacked: one <div> per source, NBSP placeholder keeps consistent line height
       so the data row matches the multi-line header row count. -->
  <td v-else-if="col.render === 'stacked'" :class="cellClass">
    <div v-for="(line, i) in stackedLines" :key="i" :class="i === 0 && col.cellClass?.includes('scr-numeric') ? 'scr-numeric' : null">
      {{ line || ' ' }}
    </div>
  </td>

  <!-- Stamps: one stamp box per element. Empty → scr-stamp-seal-empty placeholder. -->
  <template v-else-if="col.render === 'stamps'">
    <td v-for="(s, j) in stampList" :key="j">
      <span v-if="isBlankCell(s)" class="scr-stamp-seal-empty">—</span>
      <span v-else class="scr-stamp-seal">{{ s }}</span>
    </td>
  </template>
</template>
