<!--
  SL0030InvScreen — DS-INV (display archetype)
  Read-only return-invoice view: header context (Return# / Date / Cust / Tax /
  Staff / OrigInv) + 12-row OCCURS product table. Enter dismisses.
-->
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

const OCCURS_ROW_COUNT: number = 12;
const COLUMN_COUNT: number = 6;

interface InvRow {
  prod: string;
  name: string;
  qty: string;
  price: string;
  amt: string;
  cost: string;
}

const rows = computed<InvRow[]>(() =>
  Array.from({ length: OCCURS_ROW_COUNT }, (_, k) => {
    const i = k + 1;
    return {
      prod: valueOf(`WW-PROD(${i})`),
      name: valueOf(`WW-NAME(${i})`),
      qty: valueOf(`WW-QTY(${i})`),
      price: valueOf(`WW-PRICE(${i})`),
      amt: valueOf(`WW-AMT(${i})`),
      cost: valueOf(`WW-COSTAMT(${i})`),
    };
  }).filter((r) =>
    [r.prod, r.name, r.qty, r.price, r.amt, r.cost].some((v) => v && v.trim() !== ''),
  ),
);

const messageCode = computed(
  () => (props.state as { messageCode?: string }).messageCode ?? '',
);
const isErrorMessage = computed(() => messageCode.value.startsWith('E'));
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
        </div>
        <div class="scr-header-right">
          <span class="scr-status-badge scr-status-danger">RETURN</span>
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <div class="scr-form-section" style="width: 100%;">
        <div class="scr-form-section-body">
          <!-- COBOL line 3 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Return#:</span>
            <span class="scr-value scr-numeric" style="width: 12ch; display: inline-block;">{{ valueOf('WK-INV-NO') }}</span>
            <span class="scr-label">Date:</span>
            <span class="scr-value scr-numeric" style="width: 10ch; display: inline-block;">{{ valueOf('WK-SYSDATE') }}</span>
          </div>
          <!-- COBOL line 4 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Cust :</span>
            <span class="scr-value scr-numeric" style="width: 8ch; display: inline-block;">{{ valueOf('WK-SEL-CUST') }}</span>
            <span class="scr-value" style="width: 36ch; display: inline-block;">{{ valueOf('WK-CUST-NAME') }}</span>
          </div>
          <!-- COBOL line 5 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Tax  :</span>
            <span class="scr-value scr-numeric" style="width: 3ch; display: inline-block;">{{ valueOf('WK-TAXTYPE-IN') }}</span>
            <span class="scr-label">Staff:</span>
            <span class="scr-value scr-numeric" style="width: 6ch; display: inline-block;">{{ valueOf('WK-STAFF-IN') }}</span>
            <span class="scr-label">OrigInv:</span>
            <span class="scr-value scr-numeric" style="width: 12ch; display: inline-block;">{{ valueOf('WK-ORIG-INV') }}</span>
          </div>
        </div>
      </div>

      <div class="scr-table-container" style="width: 100%;">
        <table class="scr-table" style="width: 100%;">
          <thead>
            <tr>
              <th>Product</th>
              <th>Name</th>
              <th>Qty</th>
              <th>Price</th>
              <th>Amount</th>
              <th>Cost</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="rows.length === 0">
              <td :colspan="COLUMN_COUNT" class="scr-empty-state">No data</td>
            </tr>
            <tr v-for="(r, i) in rows" :key="i">
              <td class="scr-numeric">{{ r.prod }}</td>
              <td>{{ r.name }}</td>
              <td class="scr-numeric">{{ r.qty }}</td>
              <td class="scr-numeric">{{ r.price }}</td>
              <td class="scr-numeric">{{ r.amt }}</td>
              <td class="scr-numeric">{{ r.cost }}</td>
            </tr>
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
      :type="isErrorMessage ? 'error' : 'info'"
      :duration="isErrorMessage ? 0 : 4000"
    />
  </div>
</template>