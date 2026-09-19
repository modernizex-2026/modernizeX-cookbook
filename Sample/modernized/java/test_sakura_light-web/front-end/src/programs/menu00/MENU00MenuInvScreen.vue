<script setup lang="ts">
import { inject, watch, ref } from 'vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';
import Button from '../../components/ui/Button.vue';

// AUTO-GENERATED (deterministic menu archetype — uniform across the cohort; do
// NOT hand-edit, regenerate). Options + labels taken verbatim from the manifest.
const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

const CHOICE = 'WK-CHOICE';
const FIELD_DATA_MAP: Record<string, string> = { [CHOICE]: 'WK-CHOICE' };

const { isFieldActive, activeWidth, valueOf, submit, onKey, onInput, bindActiveInput } =
  useScreenForm(props, {
    fieldDataMap: FIELD_DATA_MAP,
    pfOverrides: { F3: '03' },
    isNumericField: (n: string) => n === CHOICE,
    typed,
  });

// Click an option = type its number + Enter (same underlying choice field).
function selectOption(code: string) { submit(code, '00'); }
function onConfirm() { submit(valueOf(CHOICE), '00'); }

const toastMsg = ref('');
const showToast = ref(false);
watch(
  () => [props.state.statusMessage, props.state.messageNonce] as const,
  ([msg]) => { if (msg) { toastMsg.value = String(msg); showToast.value = true; } },
  { immediate: true },
);
watch(
  () => [props.state.activeField?.fieldName, props.state.waitingFor],
  () => setTimeout(() => {
    const el = document.querySelector('input[data-active="true"]') as HTMLInputElement | null;
    if (el && document.activeElement !== el) { el.focus(); el.select?.(); }
  }, 0),
  { immediate: true, flush: 'post' },
);
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">          <h1 class="scr-title">INVENTORY / AR / AP</h1>
          
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <div class="scr-form-section" style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('1')">1. Stock Inquiry</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('2')">2. Stock Adjustment</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('3')">3. Stocktaking</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('4')">4. Movement Inquiry</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('5')">5. Cash Receipt Entry</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('6')">6. AR Inquiry</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('7')">7. Payment Entry</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('8')">8. AP Inquiry</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('9')">9. Warehouse Transfer</button>
          <button class="btn btn-outline" style="text-align:left; justify-content:flex-start; width:100%;" @click="selectOption('0')">0. Back</button>
      </div>
      <div style="display:flex; align-items:center; gap:10px; margin-top:12px;">
        <label class="scr-label" style="margin:0;">Select :</label>
        <input v-if="isFieldActive(CHOICE)" class="scr-input scr-numeric"
          style="width: 2ch; max-width:100%;" inputmode="numeric"
          :ref="bindActiveInput" :value="valueOf(CHOICE)" :maxlength="activeWidth ?? 2"
          data-active="true" :data-field-name="CHOICE"
          @input="e => onInput(e, CHOICE)" @keydown="onKey" />
        <span v-else class="scr-value scr-numeric">{{ valueOf(CHOICE) }}</span>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="Select (Enter)" @click="onConfirm" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" @hide="showToast = false" />
  </div>
</template>
