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

const { valueOf, submit } = useScreenForm(props, { pfOverrides: { F3: '03', F6: '06' }, typed });

// Packed report rows: COBOL formats each row into ONE fixed-width string; the
// header literal's token offsets give the column layout. Slice at runtime.
const PACKED_COLS: { label: string; start: number }[] = [{ label: 'Ln', start: 0 }, { label: 'Product', start: 4 }, { label: 'Name', start: 13 }, { label: 'Ordered', start: 31 }, { label: 'Received', start: 41 }];
const PACKED_ROWS: string[] = ['WR-BUF(1)', 'WR-BUF(2)', 'WR-BUF(3)', 'WR-BUF(4)', 'WR-BUF(5)', 'WR-BUF(6)', 'WR-BUF(7)', 'WR-BUF(8)', 'WR-BUF(9)'];
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
      <Button variant="outline" size="md" label="(F3)" @click="submit('', '03')" />
      <Button variant="outline" size="md" label="(F6)" @click="submit('', '06')" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
  </div>
</template>
