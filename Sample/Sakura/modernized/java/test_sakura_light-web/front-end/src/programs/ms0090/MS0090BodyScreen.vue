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
const INPUTS = ['TX-RATE', 'TX-NAME'];
const FIELD_DATA_MAP: Record<string, string> = {
  'TX-RATE': 'TX-RATE',
  'TX-NAME': 'TX-NAME',
};
const NUMERIC = new Set<string>(['TX-RATE']);
const valueOfCache = (n: string): string => (typed as any)?.values?.[n] ?? '';

const { isFieldActive, activeWidth, valueOf, submit, onKey, onInput, bindActiveInput } =
  useScreenForm(props, {
    fieldDataMap: FIELD_DATA_MAP,
    buildScreenValues: () => ({ 'TX-RATE': valueOfCache('TX-RATE'), 'TX-NAME': valueOfCache('TX-NAME') }),
    pfOverrides: { F3: '03', F4: '04', F9: '09' },
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
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Tax Rate</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('TX-RATE')" class="scr-input scr-numeric" style="width:8ch; max-width:100%;" inputmode="numeric" :ref="bindActiveInput" :value="valueOf('TX-RATE')" :maxlength="activeWidth ?? 2" data-active="true" :data-field-name="'TX-RATE'" @input="e => onInput(e, 'TX-RATE')" @keydown="onKey" /><span v-else class="scr-value scr-numeric" style="width:8ch; max-width:100%; display:inline-block;">{{ valueOf('TX-RATE') }}</span>
              <p class="scr-hint" style="flex-basis:100%; margin:0;">e.g. 0.100 = 10 percent</p>
            </div>
          <label class="scr-label" style="text-align:left; padding-top:9px; font-size:13px;">Rate Name</label>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              <input v-if="isFieldActive('TX-NAME')" class="scr-input" style="width:22ch; max-width:100%;" :ref="bindActiveInput" :value="valueOf('TX-NAME')" :maxlength="activeWidth ?? 20" data-active="true" :data-field-name="'TX-NAME'" @input="e => onInput(e, 'TX-NAME')" @keydown="onKey" /><span v-else class="scr-value" style="width:22ch; max-width:100%; display:inline-block;">{{ valueOf('TX-NAME') }}</span>
            </div>
        </div>
      </div>
    </template>
    <template #footer>
      <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
      <Button variant="outline" size="md" label="(F3)" @click="submit('', '03')" />
      <Button variant="outline" size="md" label="(F4)" @click="submit('', '04')" />
      <Button variant="outline" size="md" label="(F9)" @click="submit('', '09')" />
    </template>
  </Modal>
  <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
</template>
