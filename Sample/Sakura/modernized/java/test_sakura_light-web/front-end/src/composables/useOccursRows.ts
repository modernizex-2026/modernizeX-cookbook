import { ref, computed, toValue, type MaybeRefOrGetter } from 'vue';

/**
 * Manages a growable OCCURS row count for 出庫明細 tables.
 * Starts at `baseRows` (may be a ref/getter so it can track the loaded record
 * count, e.g. `max(8, records.length)`), grows by `rowsPerAdd` per addRows()
 * call, capped at `maxRows`.
 */
export function useOccursRows(baseRows: MaybeRefOrGetter<number>, maxRows = 90, rowsPerAdd = 10) {
  const extraRows = ref(0);
  const displayRowCount = computed(() => Math.min(toValue(baseRows) + extraRows.value, maxRows));

  function addRows() {
    const remaining = maxRows - displayRowCount.value;
    if (remaining <= 0) return;
    extraRows.value += Math.min(rowsPerAdd, remaining);
  }

  return { displayRowCount, addRows, MAX_ROWS: maxRows };
}
