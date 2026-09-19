<script setup lang="ts">
import { ref, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import { useToast } from '../../composables/useToast';
import AppToast from '../../components/AppToast.vue';
import {
  FIELD_DATA_MAP,
  FIELD_DESC_MAP,
  NUMERIC_FIELDS,
  PF_OVERRIDES,
} from './constant';

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

// ─── Fields owned by THIS screen (manifest order = COBOL acceptance order) ──
const FIELDS: { name: string; label: string; width: number; numeric?: boolean; hint?: string }[] = [
  { name: 'WH-NAME',    label: 'Name',      width: 30 },
  { name: 'WH-ZIP',     label: 'Post Code', width: 8 },
  { name: 'WH-ADDR',    label: 'Address',   width: 40 },
  { name: 'WH-TEL',     label: 'Telephone', width: 15 },
  { name: 'WH-TYPE',    label: 'Type',      width: 1, numeric: true, hint: '1:normal 2:transit 3:defect' },
  { name: 'WH-MANAGER', label: 'Manager',   width: 4, numeric: true },
];

// WK-MGR-NAME (line 11, col 26) is the resolved manager-name display adjacent
// to WH-MANAGER — bind it as the description field so it renders inline.
const DESC_MAP: Record<string, string> = {
  ...FIELD_DESC_MAP,
  'WH-MANAGER': 'WK-MGR-NAME',
};

// manifest.pfKeys diverges from defaults for the accepted codes on this
// screen (union 00/03/04/09): PF3→'03', PF4→'04', PF9→'09'.
const PF: Record<string, string> = {
  ...PF_OVERRIDES,
  F3: '03',
  F4: '04',
  F9: '09',
};

const isNumericField = (name: string) => NUMERIC_FIELDS.has(name);

const {
  activeFieldName, isFieldActive, activeWidth,
  valueOf, descOf, submit, onKey, onInput, bindActiveInput,
} = useScreenForm(props, {
  fieldDataMap: FIELD_DATA_MAP,
  fieldDescMap: DESC_MAP,
  pfOverrides: PF,
  isNumericField,
  typed,
});

// ─── Local edit values — seeded from the backend default on activation ─────
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

// ─── Toast wiring (DS-MESSAG → AppToast) ────────────────────────────────────
const { showToast, toastMsg, toastType, hideToast } = useToast(props.state);
</script>

<template>
  <div class="scr-dialog-overlay">
    <div class="scr-dialog">
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
              :style="{ width: f.width + 2 + 'ch', maxWidth: '100%' }"
              :data-field-name="f.name"
              data-active="true"
              @input="(e) => onFieldInput(e, f.name)"
              @keydown="onKey"
            />
            <span
              v-else
              class="scr-value"
              :class="{ 'scr-numeric': f.numeric }"
              :style="{ width: f.width + 2 + 'ch', maxWidth: '100%', display: 'inline-block' }"
            >{{ getValue(f.name) }}</span>
            <span
              v-if="f.name === 'WH-MANAGER'"
              class="scr-desc-box"
              :style="{ width: '32ch', maxWidth: '100%' }"
            >{{ descOf('WH-MANAGER') }}</span>
            <p v-if="f.hint" class="scr-hint">{{ f.hint }}</p>
          </div>
        </div>
      </div>
      <div class="scr-dialog-actions" style="padding: 12px 18px">
        <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
        <button class="btn btn-outline btn-sm" @click="submit(activeFieldName ? values[activeFieldName] || '' : '', '09')">
          Back
        </button>
      </div>
    </div>
    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="hideToast" />
  </div>
</template>