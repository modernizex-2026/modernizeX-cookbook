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

const OCCURS_ROW_COUNT: number = 11;

interface InvLine {
  line: string;
  prod: string;
  name: string;
  qty: string;
  amt: string;
  cost: string;
  margin: string;
}

const lines = computed<InvLine[]>(() => {
  const out: InvLine[] = [];
  for (let i = 1; i <= OCCURS_ROW_COUNT; i++) {
    out.push({
      line: valueOf(`WW-LINE(${i})`) || '',
      prod: valueOf(`WW-PROD(${i})`) || '',
      name: valueOf(`WW-NAME(${i})`) || '',
      qty: valueOf(`WW-QTY(${i})`) || '',
      amt: valueOf(`WW-AMT(${i})`) || '',
      cost: valueOf(`WW-COSTAMT(${i})`) || '',
      margin: valueOf(`WW-MARGIN(${i})`) || '',
    });
  }
  return out;
});

const visibleLines = computed<InvLine[]>(() =>
  lines.value.filter((l) =>
    [l.line, l.prod, l.name, l.qty, l.amt, l.cost, l.margin].some((v) => v.trim() !== ''),
  ),
);

const hasRows = computed(() => visibleLines.value.length > 0);
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
      <div class="scr-form-section" style="width: 100%">
        <div class="scr-form-section-body">
          <div class="scr-form-row-line">
            <span class="scr-label">Inv#:</span>
            <span class="scr-value" style="width: 10ch; display: inline-block">{{ valueOf('IH-NO') }}</span>
            <span class="scr-label">Date:</span>
            <span class="scr-value" style="width: 8ch; display: inline-block">{{ valueOf('IH-DATE') }}</span>
            <span class="scr-label">Kind:</span>
            <span class="scr-value" style="width: 8ch; display: inline-block">{{ valueOf('WK-KIND-TEXT') }}</span>
            <span class="scr-label">St:</span>
            <span class="scr-value" style="width: 10ch; display: inline-block">{{ valueOf('WK-STAT-TEXT') }}</span>
          </div>
          <div class="scr-form-row-line">
            <span class="scr-label">Cust:</span>
            <span class="scr-value" style="width: 6ch; display: inline-block">{{ valueOf('IH-CUST') }}</span>
            <span class="scr-value" style="width: 34ch; display: inline-block">{{ valueOf('WK-CUST-NAME') }}</span>
            <span class="scr-label">Ship#:</span>
            <span class="scr-value" style="width: 10ch; display: inline-block">{{ valueOf('IH-SHIP-NO') }}</span>
          </div>
          <div class="scr-form-row-line">
            <span class="scr-label">Net :</span>
            <span class="scr-value" style="width: 15ch; display: inline-block">{{ valueOf('IH-AMOUNT') }}</span>
            <span class="scr-label">Tax :</span>
            <span class="scr-value" style="width: 11ch; display: inline-block">{{ valueOf('IH-TAX-AMOUNT') }}</span>
            <span class="scr-label">Total:</span>
            <span class="scr-value" style="width: 15ch; display: inline-block">{{ valueOf('IH-TOTAL') }}</span>
          </div>
          <div class="scr-form-row-line">
            <span class="scr-label">Cost:</span>
            <span class="scr-value" style="width: 15ch; display: inline-block">{{ valueOf('IH-COST-TOTAL') }}</span>
            <span class="scr-label">Mgn :</span>
            <span class="scr-value" style="width: 11ch; display: inline-block">{{ valueOf('WK-MARGIN-TOT') }}</span>
            <span class="scr-label">Staff:</span>
            <span class="scr-value" style="width: 4ch; display: inline-block">{{ valueOf('IH-STAFF') }}</span>
          </div>
        </div>
      </div>

      <div class="scr-table-container" style="width: 100%">
        <table class="scr-table" style="width: 100%">
          <thead>
            <tr>
              <th colspan="2">Ln Product</th>
              <th>Name</th>
              <th>Qty</th>
              <th>Amount</th>
              <th>Cost</th>
              <th>Margin</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!hasRows">
              <td colspan="7" class="scr-empty-state">No data</td>
            </tr>
            <tr v-for="(r, i) in visibleLines" :key="i">
              <td class="scr-numeric">{{ r.line }}</td>
              <td class="scr-numeric">{{ r.prod }}</td>
              <td>{{ r.name }}</td>
              <td class="scr-numeric">{{ r.qty }}</td>
              <td class="scr-numeric">{{ r.amt }}</td>
              <td class="scr-numeric">{{ r.cost }}</td>
              <td class="scr-numeric">{{ r.margin }}</td>
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