<script setup lang="ts">
import { computed, useSlots } from 'vue';
import type { DataTableAlign, DataTableColumn, DataTableEditableState } from '../types/dataTable';

const props = withDefaults(
  defineProps<{
    columns: DataTableColumn[];
    /** Number of body rows (OCCURS count or list length). */
    rowCount: number;
    /** Returns display text when not editing. Falls back to empty placeholder. */
    getCellValue?: (rowIndex: number, columnKey: string) => string;
    /** COBOL active-field wiring for input/select columns. */
    editable?: DataTableEditableState;
    rowActive?: (rowIndex: number) => boolean;
    /** List archetype: `data-source` on `<tr>` for row tinting. */
    rowDataSource?: (rowIndex: number) => string | undefined;
    emptyText?: string;
    /**
     * `form` — product-entry style with rounded in-cell inputs and read-only panels.
     * `list` — flat cells for record-list screens (ColumnCell-compatible).
     */
    variant?: 'form' | 'list';
    stickyHeader?: boolean;
    maxHeight?: string;
  }>(),
  {
    variant: 'form',
    stickyHeader: true,
    getCellValue: () => '　',
    emptyText: 'データがありません',
  },
);

const slots = useSlots();

const rowIndices = computed(() => Array.from({ length: Math.max(0, props.rowCount) }, (_, i) => i));

function alignClass(align?: DataTableAlign): string {
  if (!align || align === 'left') return '';
  return `scr-table-align-${align}`;
}

function columnKind(col: DataTableColumn): DataTableColumn['kind'] {
  return col.kind ?? 'display';
}

function hasCellSlot(key: string): boolean {
  return Boolean(slots[`cell-${key}`]);
}

/** Thousands-separator formatting for display-only cells (e.g. "1000" → "1,000"). */
function formatNumber(raw: string): string {
  const trimmed = raw.trim();
  const negative = trimmed.startsWith('-');
  const digits = trimmed.replace(/[^0-9]/g, '');
  if (!digits) return raw;
  return (negative ? '-' : '') + Number(digits).toLocaleString('en-US');
}

function displayValue(rowIndex: number, col: DataTableColumn): string {
  const v = props.getCellValue?.(rowIndex, col.key);
  if (v == null || v === '') return '　';
  return col.format === 'number' ? formatNumber(v) : v;
}

function isEditing(rowIndex: number, col: DataTableColumn): boolean {
  if (!props.editable || !col.fieldName) return false;
  return props.editable.isEditing(rowIndex, col.fieldName);
}

function thStyle(col: DataTableColumn): Record<string, string> | undefined {
  if (!col.width) return undefined;
  return { width: col.width, minWidth: col.width };
}

function stepCell(e: MouseEvent, direction: 1 | -1) {
  const btn = e.currentTarget as HTMLElement;
  const input = btn.closest('.scr-table-cell-spinner')?.querySelector<HTMLInputElement>('input');
  if (!input) return;
  const next = Math.max(0, parseInt(input.value || '0', 10) + direction);
  input.value = String(next);
  input.dispatchEvent(new Event('input', { bubbles: true }));
}

function tdClass(col: DataTableColumn): string[] {
  const kind = columnKind(col);
  const classes = [alignClass(col.align), col.cellClass ?? ''].filter(Boolean);
  if (kind === 'index' || col.align === 'right' || col.inputMode === 'numeric') {
    classes.push('scr-numeric');
  }
  return classes;
}
</script>

<template>
  <div
    class="scr-table-container"
    :class="{ 'scr-table-container--sticky': stickyHeader }"
    :style="maxHeight ? { maxHeight } : undefined"
  >
    <table class="scr-table" :class="[`scr-table--${variant}`]">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :class="[alignClass(col.align), col.headerClass]"
            :style="thStyle(col)"
          >
            <slot :name="`header-${col.key}`" :column="col">
              {{ col.label }}
            </slot>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="rowCount === 0">
          <td :colspan="columns.length" class="is-empty">
            {{ emptyText }}
          </td>
        </tr>
        <tr
          v-for="rowIndex in rowIndices"
          :key="rowIndex"
          :class="{ 'scr-row-active': rowActive?.(rowIndex) }"
          :data-source="rowDataSource?.(rowIndex)"
        >
          <td v-for="col in columns" :key="col.key" :class="tdClass(col)" :style="thStyle(col)">
            <slot
              v-if="hasCellSlot(col.key)"
              :name="`cell-${col.key}`"
              :row-index="rowIndex"
              :column="col"
              :value="displayValue(rowIndex, col)"
              :editing="isEditing(rowIndex, col)"
            />

            <template v-else-if="columnKind(col) === 'index'">
              <span class="scr-table-index">{{ rowIndex + 1 }}</span>
            </template>

            <template v-else-if="columnKind(col) === 'input' && editable">
              <template v-if="isEditing(rowIndex, col)">
                <!-- Numeric: custom spinner wrapper (only when col.spinner is set) -->
                <div v-if="col.spinner" class="scr-table-cell-spinner">
                  <input
                    :ref="editable.bindActiveInput"
                    class="scr-table-cell-input scr-numeric"
                    inputmode="numeric"
                    type="text"
                    autocomplete="off"
                    :value="editable.editValue"
                    :maxlength="editable.activeWidth ?? col.maxlength"
                    :data-field-name="col.fieldName"
                    data-active="true"
                    @input="editable.onInput"
                    @keydown="editable.onKeydown"
                    @blur="editable.onBlur?.($event)"
                  />
                  <div class="scr-table-cell-spinner-btns">
                    <button
                      type="button"
                      class="scr-table-cell-spinner-btn"
                      tabindex="-1"
                      @mousedown.prevent="stepCell($event, 1)"
                    >
                      <svg width="8" height="5" viewBox="0 0 8 5" fill="none">
                        <path
                          d="M1 4.5L4 1.5L7 4.5"
                          stroke="currentColor"
                          stroke-width="1.5"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        />
                      </svg>
                    </button>
                    <button
                      type="button"
                      class="scr-table-cell-spinner-btn"
                      tabindex="-1"
                      @mousedown.prevent="stepCell($event, -1)"
                    >
                      <svg width="8" height="5" viewBox="0 0 8 5" fill="none">
                        <path
                          d="M1 1L4 4L7 1"
                          stroke="currentColor"
                          stroke-width="1.5"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        />
                      </svg>
                    </button>
                  </div>
                </div>

                <!-- Plain input (numeric or text per col.inputMode) -->
                <input
                  v-else
                  :ref="editable.bindActiveInput"
                  class="scr-table-cell-input"
                  :class="{
                    'scr-table-align-center': col.align === 'center',
                    'scr-numeric': col.inputMode === 'numeric',
                  }"
                  :inputmode="col.inputMode === 'numeric' ? 'numeric' : undefined"
                  type="text"
                  autocomplete="off"
                  :value="editable.editValue"
                  :maxlength="editable.activeWidth ?? col.maxlength"
                  :data-field-name="col.fieldName"
                  data-active="true"
                  @input="editable.onInput"
                  @keydown="editable.onKeydown"
                  @blur="editable.onBlur?.($event)"
                />
              </template>

              <span
                v-else
                class="scr-table-cell-display"
                :class="[
                  alignClass(col.align),
                  { 'scr-table-cell-clickable': !!editable?.onRowClick },
                ]"
                @click="editable?.onRowClick?.(rowIndex, col.fieldName)"
              >
                {{ displayValue(rowIndex, col) }}
              </span>
            </template>

            <template v-else-if="columnKind(col) === 'select' && editable">
              <select
                v-if="isEditing(rowIndex, col)"
                :ref="editable.bindActiveInput"
                class="scr-table-cell-input scr-table-cell-select"
                :class="alignClass(col.align)"
                :data-field-name="col.fieldName"
                data-active="true"
                :value="editable.editValue"
                @change="editable.onInput"
                @keydown="editable.onKeydown"
              >
                <option v-for="opt in col.options ?? []" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
              <span v-else class="scr-table-cell-display" :class="alignClass(col.align)">
                {{
                  col.options?.find((o) => o.value === displayValue(rowIndex, col))?.label ??
                  displayValue(rowIndex, col)
                }}
              </span>
            </template>

            <span
              v-else
              class="scr-table-cell-display"
              :class="[
                alignClass(col.align),
                { 'scr-table-cell-display--plain': variant === 'list' },
                { 'scr-table-cell-clickable': !!editable?.onRowClick },
              ]"
              @click="editable?.onRowClick?.(rowIndex, col.fieldName)"
            >
              {{ displayValue(rowIndex, col) }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
