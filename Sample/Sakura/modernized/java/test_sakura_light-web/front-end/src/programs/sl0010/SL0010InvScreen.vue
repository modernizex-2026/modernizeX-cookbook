<script setup lang="ts">
import { computed, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';

defineOptions({ name: 'SL0010InvScreen' });

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const { valueOf, submit } = useScreenForm(props, { typed });

// Header from-fields (manifest lines 3-5)
const invNo = computed(() => valueOf('WK-INV-NO'));
const sysDate = computed(() => valueOf('WK-SYSDATE'));
const selShip = computed(() => valueOf('WK-SEL-SHIP'));
const selCust = computed(() => valueOf('WK-SEL-CUST'));
const custName = computed(() => valueOf('WK-CUST-NAME'));
const taxType = computed(() => valueOf('WK-TAXTYPE-IN'));
const staff = computed(() => valueOf('WK-STAFF-IN'));

// OCCURS table (lines 7-18, 12 rows)
const OCCURS_ROW_COUNT: number = 12;

interface InvLine {
  prod: string;
  name: string;
  qty: string;
  price: string;
  amt: string;
  costamt: string;
}

const lines = computed<InvLine[]>(() =>
  Array.from({ length: OCCURS_ROW_COUNT }, (_, idx) => {
    const i = idx + 1;
    return {
      prod: valueOf(`WW-PROD(${i})`),
      name: valueOf(`WW-NAME(${i})`),
      qty: valueOf(`WW-QTY(${i})`),
      price: valueOf(`WW-PRICE(${i})`),
      amt: valueOf(`WW-AMT(${i})`),
      costamt: valueOf(`WW-COSTAMT(${i})`),
    };
  }),
);

function hasContent(l: InvLine): boolean {
  return !!(
    (l.prod && l.prod.trim()) ||
    (l.name && l.name.trim()) ||
    (l.qty && l.qty.trim()) ||
    (l.price && l.price.trim()) ||
    (l.amt && l.amt.trim()) ||
    (l.costamt && l.costamt.trim())
  );
}

const visibleLines = computed(() => lines.value.filter(hasContent));
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
          <h1 class="scr-title">Invoice</h1>
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <div class="scr-form-section" style="width: 100%">
        <div class="scr-form-section-body">
          <div class="scr-form-row-line">
            <span class="scr-label">Invoice#:</span>
            <span class="scr-value" style="width: 12ch; display: inline-block">{{ invNo }}</span>
            <span class="scr-label">Date:</span>
            <span class="scr-value" style="width: 10ch; display: inline-block">{{ sysDate }}</span>
            <span class="scr-label">Ship#:</span>
            <span class="scr-value" style="width: 12ch; display: inline-block">{{ selShip }}</span>
          </div>
          <div class="scr-form-row-line">
            <span class="scr-label">Cust :</span>
            <span class="scr-value" style="width: 8ch; display: inline-block">{{ selCust }}</span>
            <span class="scr-value" style="width: 36ch; display: inline-block">{{ custName }}</span>
          </div>
          <div class="scr-form-row-line">
            <span class="scr-label">Tax  :</span>
            <span class="scr-value" style="width: 3ch; display: inline-block">{{ taxType }}</span>
            <span class="scr-label">Staff:</span>
            <span class="scr-value" style="width: 6ch; display: inline-block">{{ staff }}</span>
          </div>
        </div>
      </div>

      <div class="scr-table-container" style="width: 100%">
        <table class="scr-table" style="width: 100%">
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
            <tr v-if="visibleLines.length === 0">
              <td colspan="6" class="scr-empty-state">No data</td>
            </tr>
            <tr v-for="(l, i) in visibleLines" :key="i">
              <td class="scr-numeric">{{ l.prod }}</td>
              <td>{{ l.name }}</td>
              <td class="scr-numeric">{{ l.qty }}</td>
              <td class="scr-numeric">{{ l.price }}</td>
              <td class="scr-numeric">{{ l.amt }}</td>
              <td class="scr-numeric">{{ l.costamt }}</td>
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
      :type="state.messageCode?.startsWith('E') ? 'error' : 'info'"
      :duration="state.messageCode?.startsWith('E') ? 0 : 4000"
    />
  </div>
</template>