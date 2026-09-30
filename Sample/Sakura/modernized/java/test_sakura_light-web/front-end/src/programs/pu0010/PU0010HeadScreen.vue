<!--
  PU0010HeadScreen — pu0010 / DS-HEAD (dialog archetype)
  PO header entry: PO Date / Supplier / Warehouse / Buyer/Staff / Due Date /
  Tax Type / Remark. Entry-point screen of the program — NOT a transparent
  overlay (no underlying fullscreen precedes it), so no isTransparentOverlay.
-->
<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import { FIELD_DATA_MAP, FIELD_DESC_MAP, NUMERIC_FIELDS, LABELS } from './constant';

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

// ─── Configuration owned by THIS screen ─────────────────────────────────
const FIELDS: { name: string; label: string; width: number; numeric?: boolean; hint?: string }[] = [
  { name: 'PH-DATE',     label: LABELS['PH-DATE']     ?? 'PO Date',     width: 8,  numeric: true },
  { name: 'PH-SUPP',     label: LABELS['PH-SUPP']     ?? 'Supplier',    width: 6,  numeric: true },
  { name: 'PH-WHSE',     label: LABELS['PH-WHSE']     ?? 'Warehouse',   width: 3,  numeric: true },
  { name: 'PH-STAFF',    label: LABELS['PH-STAFF']    ?? 'Buyer/Staff', width: 4,  numeric: true },
  { name: 'PH-DUE-DATE', label: LABELS['PH-DUE-DATE'] ?? 'Due Date',    width: 8,  numeric: true },
  { name: 'PH-TAX-TYPE', label: LABELS['PH-TAX-TYPE'] ?? 'Tax Type',    width: 1,  numeric: true,
    hint: '1:excl 2:incl 3:exempt' },
  { name: 'PH-REMARK',   label: LABELS['PH-REMARK']   ?? 'Remark',      width: 40 },
];

// Extend shared maps with the header/desc from-fields this screen renders
// (WK-PO-NO-D header value, WK-SUPP-NAME desc box beside PH-SUPP).
const DATA_MAP: Record<string, string> = {
  ...FIELD_DATA_MAP,
  'WK-PO-NO-D': 'WK-PO-NO-D',
};
const DESC_MAP: Record<string, string> = {
  ...FIELD_DESC_MAP,
  'PH-SUPP': 'WK-SUPP-NAME',
};

// PF BINDING CONTRACT: field-level ACCEPTs on this screen handle only '03'
// (manifest.pfKeys PF3 = '03', diverging from the default 'P3') — F3 keystroke
// must send '03' or COBOL falls through WHEN OTHER.
const PF_OVERRIDES: Record<string, string> = {
  F3: '03',
};

const isNumericField = (name: string) => NUMERIC_FIELDS.has(name);

// ─── Composable ─────────────────────────────────────────────────────────
const {
  activeFieldName, isFieldActive, activeWidth,
  valueOf, descOf, submit, onKey, onInput, bindActiveInput,
} = useScreenForm(props, {
  fieldDataMap: DATA_MAP,
  fieldDescMap: DESC_MAP,
  pfOverrides: PF_OVERRIDES,
  isNumericField,
  typed,
});

// ─── Local edit buffers — seeded from backend default on field activation ──
const values = ref<Record<string, string>>({});
for (const f of FIELDS) values.value[f.name] = '';

let prevFieldName = '';
watch(
  () => activeFieldName.value,
  (now) => {
    if (now && now !== prevFieldName) {
      values.value[now] = valueOf(now) || '';
      prevFieldName = now;
    }
  },
  { immediate: true },
);

function getValue(name: string): string {
  return values.value[name] || valueOf(name);
}

function onFieldInput(e: Event, name: string) {
  values.value[name] = (e.target as HTMLInputElement).value;
  onInput(e, name);
}

function onConfirm() {
  const v = activeFieldName.value ? values.value[activeFieldName.value] || '' : '';
  submit(v, '00');
}

const poNo = computed(() => (valueOf('WK-PO-NO-D') || '').trim());

// ─── Toast wiring ───────────────────────────────────────────────────────
const messageCode = computed(() => (props.state as { messageCode?: string }).messageCode ?? '');
const isErrorMessage = computed(() => messageCode.value.startsWith('E'));
const toastMsg = ref('');
const toastType = ref<'info' | 'success' | 'warning' | 'error'>('info');
const showToast = ref(false);
watch(
  () => [props.state.statusMessage, props.state.messageNonce] as const,
  ([msg]) => {
    if (msg) {
      toastMsg.value = msg;
      toastType.value = isErrorMessage.value ? 'error' : 'info';
      showToast.value = true;
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="scr-dialog-overlay">
    <div class="scr-dialog" style="width: min(720px, 92vw)">
      <div class="scr-dialog-header">
        <span class="scr-label">PO No      :</span>
        <span class="scr-value scr-numeric" style="margin-left: 8px">{{ poNo }}</span>
      </div>
      <div class="scr-dialog-body">
        <div v-for="f in FIELDS" :key="f.name" class="scr-form-grid" style="margin-bottom: 8px">
          <label class="scr-label">{{ f.label }}</label>
          <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
            <input
              v-if="isFieldActive(f.name)"
              :ref="bindActiveInput"
              class="scr-input"
              :class="{ 'scr-numeric': f.numeric }"
              :inputmode="f.numeric ? 'numeric' : undefined"
              :value="values[f.name]"
              :maxlength="activeWidth ?? f.width"
              :style="{ width: (f.width + 2) + 'ch', maxWidth: '100%' }"
              :data-field-name="f.name"
              data-active="true"
              @input="(e) => onFieldInput(e, f.name)"
              @keydown="onKey"
            />
            <span
              v-else
              class="scr-value"
              :class="{ 'scr-numeric': f.numeric }"
              :style="{ width: (f.width + 2) + 'ch', maxWidth: '100%', display: 'inline-block' }"
            >{{ getValue(f.name) }}</span>
            <span
              v-if="f.name === 'PH-SUPP'"
              class="scr-desc-box"
              :style="{ width: '40ch', maxWidth: '100%' }"
            >{{ descOf('PH-SUPP') }}</span>
            <p v-if="f.hint" class="scr-hint" style="margin: 0">{{ f.hint }}</p>
          </div>
        </div>
      </div>
      <div class="scr-dialog-actions" style="padding: 12px 18px">
        <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
        <Button variant="outline" size="md" label="Cancel (F3)" @click="submit('', '03')" />
      </div>
    </div>
    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="showToast = false" />
  </div>
</template>