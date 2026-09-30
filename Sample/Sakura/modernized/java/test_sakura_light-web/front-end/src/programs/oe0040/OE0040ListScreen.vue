<!-- OE0040ListScreen — oe0040 / DS-LIST (display archetype, OCCURS table 10 rows) -->
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

const OCCURS_ROW_COUNT: number = 10;
const COLUMN_COUNT: number = 7;

interface DisplayRow {
  line: string;
  prod: string;
  name: string;
  qty: string;
  price: string;
  amt: string;
  alloc: string;
}

const tableRows = computed<DisplayRow[]>(() => {
  const out: DisplayRow[] = [];
  for (let i = 1; i <= OCCURS_ROW_COUNT; i++) {
    out.push({
      line: valueOf(`WW-LINE(${i})`) || '',
      prod: valueOf(`WW-PROD(${i})`) || '',
      name: valueOf(`WW-NAME(${i})`) || '',
      qty: valueOf(`WW-QTY(${i})`) || '',
      price: valueOf(`WW-PRICE(${i})`) || '',
      amt: valueOf(`WW-AMT(${i})`) || '',
      alloc: valueOf(`WW-ALLOC(${i})`) || '',
    });
  }
  return out;
});

const hasData = computed(() =>
  tableRows.value.some((r) =>
    [r.line, r.prod, r.name, r.qty, r.price, r.amt, r.alloc].some(
      (v) => v && v.replace(/[\s\u200b\u3000]/g, '') !== '',
    ),
  ),
);
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-body">
      <div class="scr-form-section" style="width: 100%">
        <div class="scr-table-container">
          <table class="scr-table" style="width: 100%">
            <thead>
              <tr>
                <th>Ln</th>
                <th>Product</th>
                <th>Name</th>
                <th>Qty</th>
                <th>Price</th>
                <th>Amount</th>
                <th>Alloc</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!hasData">
                <td :colspan="COLUMN_COUNT" class="scr-empty-state">No data</td>
              </tr>
              <tr v-for="(r, i) in hasData ? tableRows : []" :key="i">
                <td class="scr-numeric">{{ r.line || ' ' }}</td>
                <td class="scr-numeric">{{ r.prod || ' ' }}</td>
                <td>{{ r.name || ' ' }}</td>
                <td class="scr-numeric">{{ r.qty || ' ' }}</td>
                <td class="scr-numeric">{{ r.price || ' ' }}</td>
                <td class="scr-numeric">{{ r.amt || ' ' }}</td>
                <td class="scr-numeric">{{ r.alloc || ' ' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
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