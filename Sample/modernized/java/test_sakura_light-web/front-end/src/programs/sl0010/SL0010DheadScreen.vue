<script lang="ts">
// Mid-flow dialog — TerminalScreen renders it as a transparent overlay above the
// last fullscreen (the main form stays visible as backdrop). See TerminalScreen
// isTransparentOverlay / lastFullscreen.
export default { isTransparentOverlay: true };
</script>

<script setup lang="ts">
import { inject, watch, ref } from 'vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Button from '../../components/ui/Button.vue';
import Modal from '../../components/ui/Modal.vue';
import Radio from '../../components/ui/Radio.vue';

// AUTO-GENERATED (deterministic dialog archetype — uniform + guaranteed input
// sizing across the cohort; do NOT hand-edit, regenerate). Text from the manifest.
const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;
const INPUTS = ['WK-STAFF-IN', 'WK-TAXTYPE-IN'];
const FIELD_DATA_MAP: Record<string, string> = {
  'WK-STAFF-IN': 'WK-STAFF-IN',
  'WK-TAXTYPE-IN': 'WK-TAXTYPE-IN',
};
const NUMERIC = new Set<string>(['WK-STAFF-IN', 'WK-TAXTYPE-IN']);
const valueOfCache = (n: string): string => (typed as any)?.values?.[n] ?? '';

const { isFieldActive, activeWidth, valueOf, submit, onKey, onInput, bindActiveInput } =
  useScreenForm(props, {
    fieldDataMap: FIELD_DATA_MAP,
    buildScreenValues: () => ({ 'WK-STAFF-IN': valueOfCache('WK-STAFF-IN'), 'WK-TAXTYPE-IN': valueOfCache('WK-TAXTYPE-IN') }),
    pfOverrides: { F3: '03' },
    isNumericField: (n: string) => NUMERIC.has(n),
    typed,
  });
function onConfirm() { submit(INPUTS.length === 1 ? valueOf(INPUTS[0]) : '', '00'); }

const toastMsg = ref('');
const showToast = ref(false);
watch(() => [props.state.statusMessage, props.state.messageNonce] as const,
  ([msg]) => { if (msg) { toastMsg.value = String(msg); showToast.value = true; } }, { immediate: true });
watch(() => [props.state.activeField?.fieldName, props.state.waitingFor],
  () => setTimeout(() => { const el = document.querySelector('input[data-active="true"]') as HTMLInputElement | null; if (el && document.activeElement !== el) { el.focus(); el.select?.(); } }, 0),
  { immediate: true, flush: 'post' });
</script>

<template>
  <Modal :visible="true" title="" min-width="510px" max-width="90vw" :close-able="false">
    <template #body>
      <div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Customer</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;"><span class="scr-value scr-numeric" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-SEL-CUST') }}</span> <span class="scr-desc-box" style="width:40ch; max-width:100%; margin-left:8px;">{{ valueOf('WK-CUST-NAME') }}</span></div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Sales Rep</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('WK-STAFF-IN')" class="scr-input scr-numeric" style="width:8ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('WK-STAFF-IN')" :maxlength="activeWidth ?? 4" data-active="true" :data-field-name="'WK-STAFF-IN'" @input="e => onInput(e, 'WK-STAFF-IN')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-STAFF-IN') }}</span>
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Tax Type</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <Radio name="WK-TAXTYPE-IN" :options="[{ value: '1', label: 'excl' }, { value: '2', label: 'incl' }, { value: '3', label: 'exempt' }]" :model-value="valueOf('WK-TAXTYPE-IN')" :disabled="!isFieldActive('WK-TAXTYPE-IN')" @update:model-value="v => { if (isFieldActive('WK-TAXTYPE-IN')) submit(v, '00'); }" />
            </div>
        </div>
      </div>
    </template>
    <template #footer>
      <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
      <Button variant="outline" size="md" label="(F3)" @click="submit('', '03')" />
    </template>
  </Modal>
  <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
</template>
