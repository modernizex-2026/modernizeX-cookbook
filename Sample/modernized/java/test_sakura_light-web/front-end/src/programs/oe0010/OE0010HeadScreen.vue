<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import { FIELD_DATA_MAP, FIELD_DESC_MAP, NUMERIC_FIELDS, PF_OVERRIDES } from './constant';

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

// ─── Configuration owned by THIS screen (labels verbatim from DS-HEAD literals) ──
const FIELDS: { name: string; label: string; width: number; numeric?: boolean; hint?: string }[] = [
  { name: 'OH-DATE',     label: 'Order Date :', width: 8,  numeric: true },
  { name: 'OH-CUST',     label: 'Customer   :', width: 6,  numeric: true },
  { name: 'OH-STAFF',    label: 'Sales Rep  :', width: 4,  numeric: true },
  { name: 'OH-WHSE',     label: 'Warehouse  :', width: 3,  numeric: true },
  { name: 'OH-DUE-DATE', label: 'Due Date   :', width: 8,  numeric: true },
  { name: 'OH-CUST-PO',  label: 'Customer PO:', width: 20 },
  { name: 'OH-TAX-TYPE', label: 'Tax Type   :', width: 1,  numeric: true, hint: '1:excl 2:incl 3:exempt' },
];

// Extend shared maps only where this screen needs extra from-field bindings.
const DATA_MAP: Record<string, string> = { ...FIELD_DATA_MAP, 'WK-ORD-NO-D': 'WK-ORD-NO-D' };
const DESC_MAP: Record<string, string> = { ...FIELD_DESC_MAP, 'OH-CUST': 'WK-CUST-NAME' };

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

// ─── Local input values — seeded from the backend default on activation ──
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

// ─── Toast wiring ───────────────────────────────────────────────────────
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
      <div class="scr-dialog-header">
        <span class="scr-label">Order No&nbsp;&nbsp;&nbsp;:</span>
        <span class="scr-value scr-numeric" style="margin-left: 8px; display: inline-block; width: 10ch;">
          {{ valueOf('WK-ORD-NO-D') }}
        </span>
      </div>
      <div class="scr-dialog-body">
        <div v-for="f in FIELDS" :key="f.name" class="scr-form-grid" style="margin-bottom: 8px;">
          <label class="scr-label" style="text-align: left;">{{ f.label }}</label>
          <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
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
              v-if="f.name === 'OH-CUST'"
              class="scr-desc-box"
              :style="{ width: '40ch', maxWidth: '100%' }"
            >{{ descOf('OH-CUST') }}</span>
            <p v-if="f.hint" class="scr-hint">{{ f.hint }}</p>
          </div>
        </div>
      </div>
      <div class="scr-dialog-actions" style="padding: 12px 18px;">
        <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
        <Button variant="outline" size="md" label="Cancel (F3)" @click="submit('', '03')" />
      </div>
    </div>
    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="showToast = false" />
  </div>
</template>