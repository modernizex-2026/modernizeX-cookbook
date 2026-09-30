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
const INPUTS = ['VH-DATE', 'VH-SUPP', 'VH-TAX-TYPE', 'VH-REMARK'];
const FIELD_DATA_MAP: Record<string, string> = {
  'VH-DATE': 'VH-DATE',
  'VH-SUPP': 'VH-SUPP',
  'VH-TAX-TYPE': 'VH-TAX-TYPE',
  'VH-REMARK': 'VH-REMARK',
};
const NUMERIC = new Set<string>(['VH-DATE', 'VH-SUPP', 'VH-TAX-TYPE']);
const valueOfCache = (n: string): string => (typed as any)?.values?.[n] ?? '';

const { isFieldActive, activeWidth, valueOf, submit, onKey, onInput, bindActiveInput } =
  useScreenForm(props, {
    fieldDataMap: FIELD_DATA_MAP,
    buildScreenValues: () => ({ 'VH-DATE': valueOfCache('VH-DATE'), 'VH-SUPP': valueOfCache('VH-SUPP'), 'VH-TAX-TYPE': valueOfCache('VH-TAX-TYPE'), 'VH-REMARK': valueOfCache('VH-REMARK') }),
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
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Return No</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;"><span class="scr-value scr-numeric" style="width:12ch; max-width:100%; display:inline-block;">{{ valueOf('WK-VH-NO-D') }}</span></div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Return Date</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('VH-DATE')" class="scr-input scr-numeric" style="width:10ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('VH-DATE')" :maxlength="activeWidth ?? 8" data-active="true" :data-field-name="'VH-DATE'" @input="e => onInput(e, 'VH-DATE')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:10ch; max-width:100%; display:inline-block;">{{ valueOf('VH-DATE') }}</span>
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Supplier</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('VH-SUPP')" class="scr-input scr-numeric" style="width:8ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('VH-SUPP')" :maxlength="activeWidth ?? 6" data-active="true" :data-field-name="'VH-SUPP'" @input="e => onInput(e, 'VH-SUPP')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('VH-SUPP') }}</span>
              <span class="scr-desc-box" style="width:40ch; max-width:100%; margin-left:8px;">{{ valueOf('WK-SUPP-NAME') }}</span>
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Tax Type</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <Radio name="VH-TAX-TYPE" :options="[{ value: '1', label: 'excl' }, { value: '2', label: 'incl' }, { value: '3', label: 'exempt' }]" :model-value="valueOf('VH-TAX-TYPE')" :disabled="!isFieldActive('VH-TAX-TYPE')" @update:model-value="v => { if (isFieldActive('VH-TAX-TYPE')) submit(v, '00'); }" />
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Remark</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('VH-REMARK')" class="scr-input" style="width:32ch; max-width:100%;" :ref="bindActiveInput" :value="valueOf('VH-REMARK')" :maxlength="activeWidth ?? 40" data-active="true" :data-field-name="'VH-REMARK'" @input="e => onInput(e, 'VH-REMARK')" @keydown="onKey" /><span v-else class="scr-value" style="width:32ch; max-width:100%; display:inline-block;">{{ valueOf('VH-REMARK') }}</span>
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
