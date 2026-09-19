<!-- OE0040InfoScreen — display archetype for oe0040 DS-INFO (order header info + detail table header) -->
<script setup lang="ts">
import { inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import { FIELD_DATA_MAP, FIELD_DESC_MAP } from './constant';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const { valueOf, submit } = useScreenForm(props, {
  fieldDataMap: FIELD_DATA_MAP,
  fieldDescMap: FIELD_DESC_MAP,
  typed,
});
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
      <!-- COBOL lines 3-5: order header info (literal labels + from-fields, one row per COBOL line) -->
      <div class="scr-form-section">
        <div class="scr-form-section-body">
          <!-- line 3 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Order#:</span>
            <span class="scr-value scr-numeric" :style="{ width: '12ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('OH-NO') }}</span>
            <span class="scr-label">Date:</span>
            <span class="scr-value scr-numeric" :style="{ width: '12ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('OH-DATE') }}</span>
            <span class="scr-label">Status:</span>
            <span class="scr-value" :style="{ width: '12ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('WK-STAT-TXT') }}</span>
          </div>
          <!-- line 4 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Cust :</span>
            <span class="scr-value scr-numeric" :style="{ width: '8ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('OH-CUST') }}</span>
            <span class="scr-value" :style="{ width: '34ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('WK-CUST-NAME') }}</span>
          </div>
          <!-- line 5 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Staff:</span>
            <span class="scr-value scr-numeric" :style="{ width: '8ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('OH-STAFF') }}</span>
            <span class="scr-label">Whse:</span>
            <span class="scr-value scr-numeric" :style="{ width: '8ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('OH-WHSE') }}</span>
            <span class="scr-label">Due:</span>
            <span class="scr-value scr-numeric" :style="{ width: '12ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('OH-DUE-DATE') }}</span>
          </div>
        </div>
      </div>

      <!-- COBOL line 6: detail table header literals -->
      <div class="scr-table-container" :style="{ width: '100%' }">
        <table class="scr-table" :style="{ width: '100%' }">
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
          <tbody></tbody>
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