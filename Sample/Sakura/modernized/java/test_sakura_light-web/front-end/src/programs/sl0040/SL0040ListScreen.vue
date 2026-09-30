<!-- SL0040ListScreen — display archetype (DS-LIST, OCCURS table 10 rows) -->
<script setup lang="ts">
import { computed, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const { valueOf, submit } = useScreenForm(props, { typed });

const OCCURS_ROW_COUNT = 10;

// Column headers from sibling screen DS-INFO header line (line 6) — verbatim.
const COLUMNS: { key: string; label: string; numeric: boolean }[] = [
  { key: 'WW-LINE',  label: 'Ln',      numeric: true },
  { key: 'WW-PROD',  label: 'Product', numeric: true },
  { key: 'WW-NAME',  label: 'Name',    numeric: false },
  { key: 'WW-OQTY',  label: 'OrigQ',   numeric: true },
  { key: 'WW-CQTY',  label: 'CrdQ',    numeric: true },
  { key: 'WW-PRICE', label: 'Price',   numeric: true },
  { key: 'WW-AMT',   label: 'Amount',  numeric: true },
];

interface DisplayRow {
  cells: { key: string; value: string; numeric: boolean }[];
  hasData: boolean;
}

const rows = computed<DisplayRow[]>(() => {
  const out: DisplayRow[] = [];
  for (let i = 1; i <= OCCURS_ROW_COUNT; i++) {
    const cells = COLUMNS.map((c) => ({
      key: `${c.key}(${i})`,
      value: valueOf(`${c.key}(${i})`) || '',
      numeric: c.numeric,
    }));
    out.push({ cells, hasData: cells.some((c) => c.value.trim().length > 0) });
  }
  return out;
});

const hasAnyData = computed(() => rows.value.some((r) => r.hasData));
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <div class="scr-table-container" style="width: 100%;">
        <table class="scr-table" style="width: 100%;">
          <thead>
            <tr>
              <th v-for="col in COLUMNS" :key="col.key">{{ col.label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!hasAnyData">
              <td :colspan="COLUMNS.length" class="scr-empty-state">No data</td>
            </tr>
            <template v-else>
              <tr v-for="(r, i) in rows" :key="i">
                <td
                  v-for="cell in r.cells"
                  :key="cell.key"
                  :class="cell.numeric ? 'scr-numeric' : null"
                >{{ cell.value || '\u00A0' }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="OK (Enter)" @click="submit('', '00')" />
    </div>

    <AppToast
      :visible="!!state.statusMessage"
      :message="state.statusMessage"
      :type="state.messageCode?.startsWith('E') ? 'error' : 'info'"
      :duration="state.messageCode?.startsWith('E') ? 0 : 4000"
    />
  </div>
</template>