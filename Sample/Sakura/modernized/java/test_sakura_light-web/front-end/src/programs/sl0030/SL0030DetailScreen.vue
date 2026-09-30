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

// AUTO-GENERATED (deterministic dialog archetype — uniform + guaranteed input
// sizing across the cohort; do NOT hand-edit, regenerate). Text from the manifest.
const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;
const INPUTS = ['WK-D-PROD', 'WK-D-WHSE', 'WK-D-QTY', 'WK-D-PRICE'];
const FIELD_DATA_MAP: Record<string, string> = {
  'WK-D-PROD': 'WK-D-PROD',
  'WK-D-WHSE': 'WK-D-WHSE',
  'WK-D-QTY': 'WK-D-QTY',
  'WK-D-PRICE': 'WK-D-PRICE',
};
const NUMERIC = new Set<string>(['WK-D-PROD', 'WK-D-WHSE', 'WK-D-QTY', 'WK-D-PRICE']);
const valueOfCache = (n: string): string => (typed as any)?.values?.[n] ?? '';

const { isFieldActive, activeWidth, valueOf, submit, onKey, onInput, bindActiveInput } =
  useScreenForm(props, {
    fieldDataMap: FIELD_DATA_MAP,
    buildScreenValues: () => ({ 'WK-D-PROD': valueOfCache('WK-D-PROD'), 'WK-D-WHSE': valueOfCache('WK-D-WHSE'), 'WK-D-QTY': valueOfCache('WK-D-QTY'), 'WK-D-PRICE': valueOfCache('WK-D-PRICE') }),
    pfOverrides: { F3: '03', F4: '04' },
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
  <Modal :visible="true" title="Return line entry" min-width="510px" max-width="90vw" :close-able="false">
    <template #body>
      <div>
        <div class="scr-form-grid" style="align-items:start; grid-template-columns:100px 1fr;">
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Product</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('WK-D-PROD')" class="scr-input scr-numeric" style="width:10ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('WK-D-PROD')" :maxlength="activeWidth ?? 8" data-active="true" :data-field-name="'WK-D-PROD'" @input="e => onInput(e, 'WK-D-PROD')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:10ch; max-width:100%; display:inline-block;">{{ valueOf('WK-D-PROD') }}</span>
              <span class="scr-desc-box" style="width:40ch; max-width:100%; margin-left:8px;">{{ valueOf('WK-D-NAME') }}</span>
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Warehouse</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('WK-D-WHSE')" class="scr-input scr-numeric" style="width:8ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('WK-D-WHSE')" :maxlength="activeWidth ?? 3" data-active="true" :data-field-name="'WK-D-WHSE'" @input="e => onInput(e, 'WK-D-WHSE')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-D-WHSE') }}</span>
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Return Qty</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('WK-D-QTY')" class="scr-input scr-numeric" style="width:11ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('WK-D-QTY')" :maxlength="activeWidth ?? 9" data-active="true" :data-field-name="'WK-D-QTY'" @input="e => onInput(e, 'WK-D-QTY')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:11ch; max-width:100%; display:inline-block;">{{ valueOf('WK-D-QTY') }}</span>
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Unit Price</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('WK-D-PRICE')" class="scr-input scr-numeric" style="width:11ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('WK-D-PRICE')" :maxlength="activeWidth ?? 9" data-active="true" :data-field-name="'WK-D-PRICE'" @input="e => onInput(e, 'WK-D-PRICE')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:11ch; max-width:100%; display:inline-block;">{{ valueOf('WK-D-PRICE') }}</span>
            </div>
        </div>
      </div>
    </template>
    <template #footer>
      <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
      <Button variant="outline" size="md" label="(F3)" @click="submit('', '03')" />
      <Button variant="outline" size="md" label="(F4)" @click="submit('', '04')" />
    </template>
  </Modal>
  <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
</template>
