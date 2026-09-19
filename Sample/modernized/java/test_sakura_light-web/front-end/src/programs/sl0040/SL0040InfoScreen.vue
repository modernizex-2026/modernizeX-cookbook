<script setup lang="ts">
import { inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';

defineOptions({ name: 'SL0040InfoScreen' });

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const { valueOf, submit } = useScreenForm(props, { typed });
void submit;
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
          <h1 class="scr-title">Credit vs Invoice#: {{ valueOf('WK-ORIG-INV') }}</h1>
        </div>
        <div class="scr-header-right">
          <span class="scr-status-badge scr-status-danger">CREDIT</span>
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <div class="scr-form-section" style="width: min(840px, 100%); margin: 0 auto;">
        <div class="scr-form-section-body">
          <!-- COBOL line 4: Cust : code + name -->
          <div class="scr-form-row-line">
            <span class="scr-label">Cust :</span>
            <span class="scr-value scr-numeric" style="width: 8ch; display: inline-block;">{{ valueOf('WK-SEL-CUST') }}</span>
            <span class="scr-value" style="width: 34ch; max-width: 100%; display: inline-block;">{{ valueOf('WK-CUST-NAME') }}</span>
          </div>
          <!-- COBOL line 5: Tax + Staff on one line -->
          <div class="scr-form-row-line">
            <span class="scr-label">Tax  :</span>
            <span class="scr-value scr-numeric" style="width: 3ch; display: inline-block;">{{ valueOf('WK-TAXTYPE-IN') }}</span>
            <span class="scr-label">Staff:</span>
            <span class="scr-value scr-numeric" style="width: 6ch; display: inline-block;">{{ valueOf('WK-STAFF-IN') }}</span>
          </div>
        </div>
      </div>

      <!-- COBOL line 6: detail column headers -->
      <div class="scr-table-container" style="width: min(840px, 100%); margin: 12px auto 0;">
        <table class="scr-table">
          <thead>
            <tr>
              <th>Ln</th>
              <th>Product</th>
              <th>Name</th>
              <th>OrigQ</th>
              <th>CrdQ</th>
              <th>Price</th>
              <th>Amount</th>
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