<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import {
  FIELD_DATA_MAP,
  FIELD_DESC_MAP,
  NUMERIC_FIELDS,
  WIDTHS,
  LABELS,
  PF_OVERRIDES,
} from './constant';

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

// Field order follows the manifest inputFields (= COBOL acceptance order).
const FIELDS: { name: string; label: string; width: number; numeric: boolean; hint?: string }[] = [
  { name: 'OH-DATE',     label: LABELS['OH-DATE']     ?? 'Order Date',  width: WIDTHS['OH-DATE']     ?? 8,  numeric: NUMERIC_FIELDS.has('OH-DATE') },
  { name: 'OH-CUST',     label: LABELS['OH-CUST']     ?? 'Customer',    width: WIDTHS['OH-CUST']     ?? 6,  numeric: NUMERIC_FIELDS.has('OH-CUST') },
  { name: 'OH-STAFF',    label: LABELS['OH-STAFF']    ?? 'Sales Rep',   width: WIDTHS['OH-STAFF']    ?? 4,  numeric: NUMERIC_FIELDS.has('OH-STAFF') },
  { name: 'OH-WHSE',     label: LABELS['OH-WHSE']     ?? 'Warehouse',   width: WIDTHS['OH-WHSE']     ?? 3,  numeric: NUMERIC_FIELDS.has('OH-WHSE') },
  { name: 'OH-DUE-DATE', label: LABELS['OH-DUE-DATE'] ?? 'Due Date',    width: WIDTHS['OH-DUE-DATE'] ?? 8,  numeric: NUMERIC_FIELDS.has('OH-DUE-DATE') },
  { name: 'OH-CUST-PO',  label: LABELS['OH-CUST-PO']  ?? 'Customer PO', width: WIDTHS['OH-CUST-PO']  ?? 20, numeric: NUMERIC_FIELDS.has('OH-CUST-PO') },
  { name: 'OH-TAX-TYPE', label: LABELS['OH-TAX-TYPE'] ?? 'Tax Type',    width: WIDTHS['OH-TAX-TYPE'] ?? 1,  numeric: NUMERIC_FIELDS.has('OH-TAX-TYPE'),
    hint: '1:excl 2:incl 3:exempt' },
  { name: 'OH-REMARK',   label: LABELS['OH-REMARK']   ?? 'Remark',      width: WIDTHS['OH-REMARK']   ?? 40, numeric: NUMERIC_FIELDS.has('OH-REMARK') },
];

// OH-CUST resolves its name box from WK-CUST-NAME (manifest line 5, col 24).
const DESC_MAP: Record<string, string> = { ...FIELD_DESC_MAP, 'OH-CUST': 'WK-CUST-NAME' };

const isNumericField = (name: string) => NUMERIC_FIELDS.has(name);

const {
  activeFieldName, isFieldActive, activeWidth,
  valueOf, descOf, submit, onKey, onInput, bindActiveInput,
} = useScreenForm(props, {
  fieldDataMap: FIELD_DATA_MAP,
  fieldDescMap: DESC_MAP,
  pfOverrides: PF_OVERRIDES,
  isNumericField,
  typed,
});

// Local edit buffers — seeded from the backend default when a field activates
// (COBOL DISPLAY + ACCEPT semantics: displayed value is the editable initial content).
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
  // '00' (Enter) — server's mapAidKey resolves field.advanceEsts per manifest.
  const v = activeFieldName.value ? values.value[activeFieldName.value] || '' : '';
  submit(v, '00');
}

// ─── Toast wiring ─────────────────────────────────────────────────────────
const messageCode = computed(() => (props.state as { messageCode?: string }).messageCode ?? '');
const toastMsg = ref('');
const toastType = ref<'info' | 'success' | 'warning' | 'error'>('info');
const showToast = ref(false);
watch(
  () => [props.state.statusMessage, props.state.messageNonce] as const,
  ([msg]) => {
    if (msg) {
      toastMsg.value = msg;
      toastType.value = messageCode.value.startsWith('E') ? 'error' : 'info';
      showToast.value = true;
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="scr-dialog-overlay">
    <div class="scr-dialog">
      <div class="scr-dialog-body">
        <div class="scr-form-grid">
          <template v-for="f in FIELDS" :key="f.name">
            <label class="scr-label">{{ f.label }}</label>
            <div class="scr-form-row-line">
              <input
                v-if="isFieldActive(f.name)"
                :ref="bindActiveInput"
                class="scr-input"
                :class="{ 'scr-numeric': f.numeric }"
                :style="{ width: `calc(${f.width}ch + 26px)`, maxWidth: '100%' }"
                :inputmode="f.numeric ? 'numeric' : undefined"
                :value="values[f.name]"
                :maxlength="activeWidth ?? f.width"
                :data-field-name="f.name"
                data-active="true"
                @input="(e) => onFieldInput(e, f.name)"
                @keydown="onKey"
              />
              <span
                v-else
                class="scr-value"
                :class="{ 'scr-numeric': f.numeric }"
                :style="{ width: f.width + 'ch', maxWidth: '100%', display: 'inline-block' }"
              >{{ getValue(f.name) }}</span>
              <span
                v-if="f.name === 'OH-CUST'"
                class="scr-desc-box"
                :style="{ width: '40ch', maxWidth: '100%', marginLeft: '8px' }"
              >{{ descOf('OH-CUST') }}</span>
              <p v-if="f.hint" class="scr-hint">{{ f.hint }}</p>
            </div>
          </template>
        </div>
      </div>
      <div class="scr-dialog-actions" style="padding: 12px 18px">
        <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
        <Button variant="outline" size="md" label="Back (F3)" @click="submit('', '03')" />
      </div>
    </div>
    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="showToast = false" />
  </div>
</template>