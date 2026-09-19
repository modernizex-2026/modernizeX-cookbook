<script setup lang="ts">
import { inject, watch, ref, computed } from 'vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';
import Button from '../../components/ui/Button.vue';

// AUTO-GENERATED (deterministic display archetype — uniform across the cohort; do
// NOT hand-edit, regenerate). Read-only screen; text from the manifest verbatim.
const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const { valueOf, submit } = useScreenForm(props, { pfOverrides: {}, typed });

// Packed report rows: COBOL formats each row into ONE fixed-width string; the
// header literal's token offsets give the column layout. Slice at runtime.
const PACKED_COLS: { label: string; start: number }[] = [{ label: 'Date', start: 0 }, { label: 'Kind', start: 12 }, { label: 'Debit', start: 22 }, { label: 'Credit', start: 36 }];
const PACKED_ROWS: string[] = ['WK-LIST(1)', 'WK-LIST(2)', 'WK-LIST(3)', 'WK-LIST(4)', 'WK-LIST(5)', 'WK-LIST(6)', 'WK-LIST(7)', 'WK-LIST(8)', 'WK-LIST(9)', 'WK-LIST(10)', 'WK-LIST(11)', 'WK-LIST(12)'];
const packedRows = computed(() => PACKED_ROWS
  .map((n) => valueOf(n) || '')
  .filter((v) => v.trim())
  .map((v) => PACKED_COLS.map((c, i) =>
    v.slice(c.start, i + 1 < PACKED_COLS.length ? PACKED_COLS[i + 1].start : undefined).trim())));

const toastMsg = ref('');
const showToast = ref(false);
watch(() => [props.state.statusMessage, props.state.messageNonce] as const,
  ([msg]) => { if (msg) { toastMsg.value = String(msg); showToast.value = true; } }, { immediate: true });
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
      <div class="scr-form-section" style="box-sizing:border-box; padding:16px 20px 14px;">
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Supplier</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-KEY-SUPP') }}</span> <span class="scr-value" style="width:40ch; max-width:100%; display:inline-block;">{{ valueOf('WK-SUPP-NAME') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">AP Balance</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-CUR-BAL') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Tot Paid</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-TOT-DR') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Tot Purch</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-TOT-CR') }}</span></div>
        </div>
        <div class="scr-form-row" style="display:flex; align-items:center; gap:10px; margin-bottom:8px;">
          <span style="white-space:nowrap;">Recent ledger lines:</span>
        </div>
        <table class="scr-table" style="width:100%; margin:8px 0;">
          <thead>
            <tr><th v-for="c in PACKED_COLS" :key="c.start">{{ c.label }}</th></tr>
          </thead>
          <tbody>
            <tr v-for="(r, ri) in packedRows" :key="ri">
              <td v-for="(cell, ci) in r" :key="ci" style="font-variant-numeric:tabular-nums; white-space:nowrap;">{{ cell }}</td>
            </tr>
            <tr v-if="!packedRows.length">
              <td :colspan="PACKED_COLS.length" style="text-align:center; color:#8a94a6;">No data</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="OK (Enter)" @click="submit('', '00')" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
  </div>
</template>
