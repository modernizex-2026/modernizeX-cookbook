<script setup lang="ts">
import { inject, watch, ref } from 'vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Button from '../../components/ui/Button.vue';
import Modal from '../../components/ui/Modal.vue';

// AUTO-GENERATED (deterministic display archetype — uniform across the cohort; do
// NOT hand-edit, regenerate). Read-only screen; text from the manifest verbatim.
const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const { valueOf, submit } = useScreenForm(props, { pfOverrides: { F3: '03', F6: '06' }, typed });

const toastMsg = ref('');
const showToast = ref(false);
watch(() => [props.state.statusMessage, props.state.messageNonce] as const,
  ([msg]) => { if (msg) { toastMsg.value = String(msg); showToast.value = true; } }, { immediate: true });
</script>

<template>
  <Modal :visible="true" title="" min-width="560px" max-width="90vw" :close-able="false">
    <template #body>
      <div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">PO No</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:25ch; max-width:100%; display:inline-block;">{{ valueOf('PH-NO') }}</span> <span class="scr-label" style="white-space:nowrap; font-size:13px; padding-top:9px;">Status</span> <span class="scr-value" style="width:14ch; max-width:100%; display:inline-block;">{{ valueOf('WK-STAT-TEXT') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Date</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:10ch; max-width:100%; display:inline-block;">{{ valueOf('PH-DATE') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Supplier</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('PH-SUPP') }}</span> <span class="scr-value" style="width:40ch; max-width:100%; display:inline-block;">{{ valueOf('WK-SUPP-NAME') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Warehouse</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('PH-WHSE') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Buyer</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('PH-STAFF') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Due Date</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:10ch; max-width:100%; display:inline-block;">{{ valueOf('PH-DUE-DATE') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Net</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:19ch; max-width:100%; display:inline-block;">{{ valueOf('PH-AMOUNT') }}</span> <span class="scr-label" style="white-space:nowrap; font-size:13px; padding-top:9px;">Tax</span> <span class="scr-value" style="width:19ch; max-width:100%; display:inline-block;">{{ valueOf('PH-TAX-AMOUNT') }}</span> <span class="scr-label" style="white-space:nowrap; font-size:13px; padding-top:9px;">Tot</span> <span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('PH-TOTAL') }}</span></div>
        </div>
      </div>
    </template>
    <template #footer>
      <Button variant="primary" size="md" label="OK (Enter)" @click="submit('', '00')" />
      <Button variant="outline" size="md" label="(F3)" @click="submit('', '03')" />
      <Button variant="outline" size="md" label="(F6)" @click="submit('', '06')" />
    </template>
  </Modal>
  <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
</template>
