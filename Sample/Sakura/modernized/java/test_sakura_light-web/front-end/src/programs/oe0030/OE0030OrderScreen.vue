<script setup lang="ts">
import { computed, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';

defineOptions({ name: 'OE0030OrderScreen' });

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const { valueOf, submit } = useScreenForm(props, { typed });

const ROW_COUNT: number = 13;

// A table row is shown only when at least one of its fields carries data.
const rows = computed(() => {
  const out: number[] = [];
  for (let i = 1; i <= ROW_COUNT; i++) {
    const hasData =
      (valueOf(`WW-LINE(${i})`) || '').trim() !== '' ||
      (valueOf(`WW-PROD(${i})`) || '').trim() !== '' ||
      (valueOf(`WW-NAME(${i})`) || '').trim() !== '' ||
      (valueOf(`WW-ORD(${i})`) || '').trim() !== '' ||
      (valueOf(`WW-AVAIL(${i})`) || '').trim() !== '' ||
      (valueOf(`WW-ALLOC(${i})`) || '').trim() !== '' ||
      (valueOf(`WW-SHORT(${i})`) || '').trim() !== '';
    if (hasData) out.push(i);
  }
  return out;
});

const isErrorMessage = computed(() =>
  ((props.state as { messageCode?: string }).messageCode ?? '').startsWith('E'),
);
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
      <!-- COBOL line 3 -->
      <div class="scr-form-section">
        <div class="scr-form-section-body">
          <div class="scr-form-row-line">
            <span class="scr-label">Order#:</span>
            <span class="scr-value">{{ valueOf('OH-NO') }}</span>
            <span class="scr-label">Date:</span>
            <span class="scr-value">{{ valueOf('OH-DATE') }}</span>
            <span class="scr-label">Status:</span>
            <span class="scr-value">{{ valueOf('WK-STAT-TEXT') }}</span>
          </div>
          <!-- COBOL line 4 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Cust  :</span>
            <span class="scr-value">{{ valueOf('OH-CUST') }}</span>
            <span class="scr-value" :style="{ width: '34ch', maxWidth: '100%', display: 'inline-block' }">{{ valueOf('WK-CUST-NAME') }}</span>
          </div>
          <!-- COBOL line 5 -->
          <div class="scr-form-row-line">
            <span class="scr-label">Whse :</span>
            <span class="scr-value">{{ valueOf('OH-WHSE') }}</span>
            <span class="scr-label">Lines:</span>
            <span class="scr-value">{{ valueOf('WK-DCNT') }}</span>
            <span class="scr-label">TotAlloc:</span>
            <span class="scr-value">{{ valueOf('WK-TOT-ALLOC') }}</span>
            <span class="scr-label">Short:</span>
            <span class="scr-value">{{ valueOf('WK-TOT-SHORT') }}</span>
          </div>
        </div>
      </div>

      <!-- OCCURS table, COBOL lines 7-19 -->
      <div class="scr-table-container">
        <table class="scr-table">
          <thead>
            <tr>
              <th colspan="2">Ln Product</th>
              <th>Name</th>
              <th>Order</th>
              <th>Avail</th>
              <th>Alloc</th>
              <th>Short</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="rows.length === 0">
              <td colspan="7" class="scr-empty-state">No data</td>
            </tr>
            <tr v-for="i in rows" :key="i">
              <td class="scr-numeric">{{ valueOf(`WW-LINE(${i})`) }}</td>
              <td class="scr-numeric">{{ valueOf(`WW-PROD(${i})`) }}</td>
              <td>{{ valueOf(`WW-NAME(${i})`) }}</td>
              <td class="scr-numeric">{{ valueOf(`WW-ORD(${i})`) }}</td>
              <td class="scr-numeric">{{ valueOf(`WW-AVAIL(${i})`) }}</td>
              <td class="scr-numeric">{{ valueOf(`WW-ALLOC(${i})`) }}</td>
              <td class="scr-numeric">{{ valueOf(`WW-SHORT(${i})`) }}</td>
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