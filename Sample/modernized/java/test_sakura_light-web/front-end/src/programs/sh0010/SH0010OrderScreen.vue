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

function rowHasData(i: number): boolean {
  return !!(
    (valueOf(`WW-LINE(${i})`) || '').trim() ||
    (valueOf(`WW-PROD(${i})`) || '').trim() ||
    (valueOf(`WW-NAME(${i})`) || '').trim()
  );
}

const hasAnyRow = computed(() => {
  for (let i = 1; i <= OCCURS_ROW_COUNT; i++) {
    if (rowHasData(i)) return true;
  }
  return false;
});
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
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <!-- Header context — one grid row per COBOL line -->
      <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
        <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Order#:</label>
        <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
          <span class="scr-value scr-numeric" style="width:12ch; display:inline-block;">{{ valueOf('OH-NO') }}</span>
          <span class="scr-label" style="font-size:13px; white-space:nowrap; margin-left:8px;">Date:</span>
          <span class="scr-value scr-numeric" style="width:10ch; display:inline-block;">{{ valueOf('OH-DATE') }}</span>
          <span class="scr-label" style="font-size:13px; white-space:nowrap; margin-left:8px;">Status:</span>
          <span class="scr-value" style="width:14ch; display:inline-block;">{{ valueOf('WK-STAT-TEXT') }}</span>
        </div>
      </div>
      <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
        <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Cust  :</label>
        <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
          <span class="scr-value scr-numeric" style="width:8ch; display:inline-block;">{{ valueOf('OH-CUST') }}</span>
          <span class="scr-value" style="width:36ch; display:inline-block;">{{ valueOf('WK-CUST-NAME') }}</span>
        </div>
      </div>
      <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
        <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Whse :</label>
        <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
          <span class="scr-value scr-numeric" style="width:5ch; display:inline-block;">{{ valueOf('OH-WHSE') }}</span>
          <span class="scr-label" style="font-size:13px; white-space:nowrap; margin-left:8px;">Staff:</span>
          <span class="scr-value scr-numeric" style="width:6ch; display:inline-block;">{{ valueOf('OH-STAFF') }}</span>
          <span class="scr-label" style="font-size:13px; white-space:nowrap; margin-left:8px;">Due:</span>
          <span class="scr-value scr-numeric" style="width:10ch; display:inline-block;">{{ valueOf('OH-DUE-DATE') }}</span>
        </div>
      </div>

      <!-- OCCURS table (COBOL lines 7-19, 13 rows) -->
      <div class="scr-table-container" style="width:100%;">
        <table class="scr-table" style="width:100%;">
          <thead>
            <tr>
              <th>Ln</th>
              <th>Product</th>
              <th>Name</th>
              <th>Ord</th>
              <th>Shpd</th>
              <th>Aloc</th>
              <th>Ship</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!hasAnyRow">
              <td colspan="7" class="scr-empty-state">No data</td>
            </tr>
            <template v-else>
              <tr v-for="i in OCCURS_ROW_COUNT" :key="i">
                <td class="scr-numeric">{{ valueOf(`WW-LINE(${i})`) }}</td>
                <td class="scr-numeric">{{ valueOf(`WW-PROD(${i})`) }}</td>
                <td>{{ valueOf(`WW-NAME(${i})`) }}</td>
                <td class="scr-numeric">{{ valueOf(`WW-ORD(${i})`) }}</td>
                <td class="scr-numeric">{{ valueOf(`WW-SHIPPED(${i})`) }}</td>
                <td class="scr-numeric">{{ valueOf(`WW-ALLOC(${i})`) }}</td>
                <td class="scr-numeric">{{ valueOf(`WW-SHIP(${i})`) }}</td>
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