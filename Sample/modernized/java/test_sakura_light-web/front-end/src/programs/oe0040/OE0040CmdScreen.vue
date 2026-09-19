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
const INPUTS = ['WK-CMD', 'WK-CMD-ARG'];
const FIELD_DATA_MAP: Record<string, string> = {
  'WK-CMD': 'WK-CMD',
  'WK-CMD-ARG': 'WK-CMD-ARG',
};
const NUMERIC = new Set<string>(['WK-CMD-ARG']);
const valueOfCache = (n: string): string => (typed as any)?.values?.[n] ?? '';

const { isFieldActive, activeWidth, valueOf, submit, onKey, onInput, bindActiveInput } =
  useScreenForm(props, {
    fieldDataMap: FIELD_DATA_MAP,
    buildScreenValues: () => ({ 'WK-CMD': valueOfCache('WK-CMD'), 'WK-CMD-ARG': valueOfCache('WK-CMD-ARG') }),
    pfOverrides: { F3: '03', F6: '06' },
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
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Cmd</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('WK-CMD')" class="scr-input" style="width:8ch; max-width:100%;" :ref="bindActiveInput" :value="valueOf('WK-CMD')" :maxlength="activeWidth ?? 1" data-active="true" :data-field-name="'WK-CMD'" @input="e => onInput(e, 'WK-CMD')" @keydown="onKey" /><span v-else class="scr-value" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-CMD') }}</span>
              <span class="scr-label" style="font-size:13px; white-space:nowrap; margin-left:8px;">Line</span>
              <input v-if="isFieldActive('WK-CMD-ARG')" class="scr-input scr-numeric" style="width:8ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('WK-CMD-ARG')" :maxlength="activeWidth ?? 3" data-active="true" :data-field-name="'WK-CMD-ARG'" @input="e => onInput(e, 'WK-CMD-ARG')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('WK-CMD-ARG') }}</span>
              <p class="scr-hint" style="flex-basis:100%; margin:0;">A=Add C=Chg D=Del H=Hdr X=Cxl S=Save</p>
              <p class="scr-hint" style="flex-basis:100%; margin:0;">Line: A=Add C=Chg D=Del H=Hdr X=Cxl S=Save</p>
            </div>
        </div>
      </div>
    </template>
    <template #footer>
      <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
      <Button variant="outline" size="md" label="(F3)" @click="submit('', '03')" />
      <Button variant="outline" size="md" label="(F6)" @click="submit('', '06')" />
    </template>
  </Modal>
  <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
</template>
