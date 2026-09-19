<!-- OE0020 / DS-ORDER — display archetype (order detail readout, OCCURS 13-line table) -->
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

const OCCURS_ROW_COUNT: number = 13;

function cell(name: string, i: number): string {
  return (valueOf(`${name}(${i})`) || '').trim();
}

const hasRows = computed(() => {
  for (let i = 1; i <= OCCURS_ROW_COUNT; i++) {
    if (cell('WW-LINE', i) || cell('WW-PROD', i) || cell('WW-NAME', i)) return true;
  }
  return false;
});

const messageCode = computed(() => (props.state as { messageCode?: string }).messageCode ?? '');
const isErrorMessage = computed(() => messageCode.value.startsWith('E'));
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
          <h1 class="scr-title">Order#: {{ valueOf('OH-NO') }}</h1>
        </div>
        <div class="scr-header-right">
          <span class="scr-value">{{ valueOf('WK-STAT-TEXT') }}</span>
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <div class="scr-form-section" style="width: 100%">
        <div class="scr-form-section-body">
          <!-- COBOL line 3 -->
          <div class="scr-form-grid" style="align-items: start; grid-template-columns: 100px 1fr; margin-bottom: 8px">
            <label class="scr-label" style="text-align: left; padding-top: 9px; font-size: 13px">Order#:</label>
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <span class="scr-value scr-numeric" style="width: 12ch; display: inline-block">{{ valueOf('OH-NO') }}</span>
              <span class="scr-label" style="font-size: 13px; white-space: nowrap; margin-left: 8px">Date:</span>
              <span class="scr-value scr-numeric" style="width: 10ch; display: inline-block">{{ valueOf('OH-DATE') }}</span>
              <span class="scr-label" style="font-size: 13px; white-space: nowrap; margin-left: 8px">Status:</span>
              <span class="scr-value" style="width: 14ch; display: inline-block">{{ valueOf('WK-STAT-TEXT') }}</span>
            </div>
          </div>
          <!-- COBOL line 4 -->
          <div class="scr-form-grid" style="align-items: start; grid-template-columns: 100px 1fr; margin-bottom: 8px">
            <label class="scr-label" style="text-align: left; padding-top: 9px; font-size: 13px">Cust  :</label>
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <span class="scr-value scr-numeric" style="width: 8ch; display: inline-block">{{ valueOf('OH-CUST') }}</span>
              <span class="scr-desc-box" style="width: 36ch; max-width: 100%">{{ valueOf('WK-CUST-NAME') }}</span>
            </div>
          </div>
          <!-- COBOL line 5 -->
          <div class="scr-form-grid" style="align-items: start; grid-template-columns: 100px 1fr; margin-bottom: 8px">
            <label class="scr-label" style="text-align: left; padding-top: 9px; font-size: 13px">Staff :</label>
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <span class="scr-value scr-numeric" style="width: 6ch; display: inline-block">{{ valueOf('OH-STAFF') }}</span>
              <span class="scr-label" style="font-size: 13px; white-space: nowrap; margin-left: 8px">Whse:</span>
              <span class="scr-value scr-numeric" style="width: 5ch; display: inline-block">{{ valueOf('OH-WHSE') }}</span>
              <span class="scr-label" style="font-size: 13px; white-space: nowrap; margin-left: 8px">Due:</span>
              <span class="scr-value scr-numeric" style="width: 10ch; display: inline-block">{{ valueOf('OH-DUE-DATE') }}</span>
              <span class="scr-label" style="font-size: 13px; white-space: nowrap; margin-left: 8px">PO:</span>
              <span class="scr-value" style="width: 22ch; display: inline-block">{{ valueOf('OH-CUST-PO') }}</span>
            </div>
          </div>
          <!-- COBOL line 6 -->
          <div class="scr-form-grid" style="align-items: start; grid-template-columns: 100px 1fr">
            <label class="scr-label" style="text-align: left; padding-top: 9px; font-size: 13px">Net :</label>
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <span class="scr-value scr-numeric" style="width: 17ch; display: inline-block">{{ valueOf('OH-AMOUNT') }}</span>
              <span class="scr-label" style="font-size: 13px; white-space: nowrap; margin-left: 8px">Tax :</span>
              <span class="scr-value scr-numeric" style="width: 13ch; display: inline-block">{{ valueOf('OH-TAX-AMOUNT') }}</span>
              <span class="scr-label" style="font-size: 13px; white-space: nowrap; margin-left: 8px">Total :</span>
              <span class="scr-value scr-numeric" style="width: 17ch; display: inline-block">{{ valueOf('OH-TOTAL') }}</span>
            </div>
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
              <th>Price</th>
              <th>Amount</th>
              <th>Shp</th>
              <th>Alc</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!hasRows">
              <td colspan="8" class="scr-empty-state">No data</td>
            </tr>
            <template v-else>
              <tr v-for="i in OCCURS_ROW_COUNT" :key="i">
                <td class="scr-numeric">{{ cell('WW-LINE', i) }}</td>
                <td class="scr-numeric">{{ cell('WW-PROD', i) }}</td>
                <td>{{ cell('WW-NAME', i) }}</td>
                <td class="scr-numeric">{{ cell('WW-QTY', i) }}</td>
                <td class="scr-numeric">{{ cell('WW-PRICE', i) }}</td>
                <td class="scr-numeric">{{ cell('WW-AMT', i) }}</td>
                <td class="scr-numeric">{{ cell('WW-SHIP', i) }}</td>
                <td class="scr-numeric">{{ cell('WW-ALLOC', i) }}</td>
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
      :type="isErrorMessage ? 'error' : 'info'"
      :duration="isErrorMessage ? 0 : 4000"
    />
  </div>
</template>