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

const { valueOf, submit } = useScreenForm(props, { pfOverrides: {}, typed });

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
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Record #</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-SEEN') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Product</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:9ch; max-width:100%; display:inline-block;">{{ valueOf('SK-PROD') }}</span> <span class="scr-value" style="width:40ch; max-width:100%; display:inline-block;">{{ valueOf('WK-PR-NAME') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Warehouse</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:9ch; max-width:100%; display:inline-block;">{{ valueOf('SK-WHSE') }}</span> <span class="scr-value" style="width:32ch; max-width:100%; display:inline-block;">{{ valueOf('WK-WH-NAME') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Reorder/Safe</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:13ch; max-width:100%; display:inline-block;">{{ valueOf('WK-REORDER') }}</span> <span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-SAFETY') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">On Hand</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('SK-ONHAND') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Allocated</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('SK-ALLOCATED') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Available</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:13ch; max-width:100%; display:inline-block;">{{ valueOf('WK-AVAIL') }}</span> <span class="scr-value" style="width:14ch; max-width:100%; display:inline-block;">{{ valueOf('WK-STAT') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">On Order</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('SK-ON-ORDER') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Avg Cost</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('SK-AVG-COST') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Location</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:12ch; max-width:100%; display:inline-block;">{{ valueOf('SK-LOCATION') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Last In</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:10ch; max-width:100%; display:inline-block;">{{ valueOf('SK-LAST-IN-DATE') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Last Out</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:10ch; max-width:100%; display:inline-block;">{{ valueOf('SK-LAST-OUT-DATE') }}</span></div>
        </div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr; margin-bottom:8px;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">YTD In / Out</label>
          <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-width:0;"><span class="scr-value" style="width:19ch; max-width:100%; display:inline-block;">{{ valueOf('SK-YTD-IN') }}</span> <span class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('SK-YTD-OUT') }}</span></div>
        </div>
      </div>
    </template>
    <template #footer>
      <Button variant="primary" size="md" label="OK (Enter)" @click="submit('', '00')" />
    </template>
  </Modal>
  <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
</template>
