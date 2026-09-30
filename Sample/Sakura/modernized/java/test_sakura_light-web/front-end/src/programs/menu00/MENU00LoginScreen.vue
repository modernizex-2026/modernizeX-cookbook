<script lang="ts">
// Login is a transient setup step — TerminalScreen must NOT keep it as the
// fullscreen backdrop (a dialog opened after login would otherwise render over
// the still-mounted login). See TerminalScreen lastFullscreen / isTransientSetup.
export default { isTransientSetup: true };
</script>

<script setup lang="ts">
import { inject, watch, ref } from 'vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Button from '../../components/ui/Button.vue';

// AUTO-GENERATED (deterministic login-card archetype — the maintained app's
// scr-login-card template; do NOT hand-edit, regenerate). Text from the manifest.
const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;
const INPUTS = ['KL-LOGIN', 'KL-PASSWORD'];
const FIELD_DATA_MAP: Record<string, string> = {
  'KL-LOGIN': 'KL-LOGIN',
  'KL-PASSWORD': 'KL-PASSWORD',
};
const FIELD_DESC_MAP: Record<string, string> = {

};
const NUMERIC = new Set<string>([]);
const valueOfCache = (n: string): string => (typed as any)?.values?.[n] ?? '';

const { isFieldActive, activeWidth, valueOf, descOf, submit, onKey, onInput, bindActiveInput } =
  useScreenForm(props, {
    fieldDataMap: FIELD_DATA_MAP,
    fieldDescMap: FIELD_DESC_MAP,
    buildScreenValues: () => ({ 'KL-LOGIN': valueOfCache('KL-LOGIN'), 'KL-PASSWORD': valueOfCache('KL-PASSWORD') }),
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
  <div class="scr-login-page">
    <div class="scr-login-card">
      <div class="scr-login-header">
        
        
      </div>
      <div class="scr-login-body">
        <div class="scr-input-group">
          <label class="scr-label" style="text-align:left; font-size:13px;">Login ID</label>
          <div class="scr-input-with-desc">
            <input
              :ref="bindActiveInput"
              :disabled="!isFieldActive('KL-LOGIN')"
              class="scr-input"
              style="width:100%;"
              :value="valueOf('KL-LOGIN')"
              :maxlength="activeWidth ?? 12"
              :data-active="isFieldActive('KL-LOGIN')"
              :data-field-name="'KL-LOGIN'"
              @input="e => onInput(e, 'KL-LOGIN')"
              @keydown="onKey"
            />
            
          </div>
        </div>
        <div class="scr-input-group">
          <label class="scr-label" style="text-align:left; font-size:13px;">Password</label>
          <div class="scr-input-with-desc">
            <input
              :ref="bindActiveInput"
              :disabled="!isFieldActive('KL-PASSWORD')"
              class="scr-input"
              type="password"
              style="width:100%;"
              :value="valueOf('KL-PASSWORD')"
              :maxlength="activeWidth ?? 16"
              :data-active="isFieldActive('KL-PASSWORD')"
              :data-field-name="'KL-PASSWORD'"
              @input="e => onInput(e, 'KL-PASSWORD')"
              @keydown="onKey"
            />
            
          </div>
        </div>

        <div class="scr-dialog-actions" style="flex-wrap:wrap; justify-content:flex-end;">
          <Button label="F3" variant="outline" size="md" @click="submit('', '03')" />
          <Button label="OK (Enter)" size="md" @click="onConfirm" />
        </div>
      </div>
    </div>
    <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
  </div>
</template>
