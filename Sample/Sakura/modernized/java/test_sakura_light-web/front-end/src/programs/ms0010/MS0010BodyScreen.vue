<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';
import {
  FIELD_DATA_MAP,
  FIELD_DESC_MAP,
  NUMERIC_FIELDS,
  WIDTHS,
  LABELS,
  HINTS,
  PF_OVERRIDES,
} from './constant';

defineOptions({ name: 'MS0010BodyScreen' });

const props = defineProps<ScreenOverrideProps>();

// COBOL DS-BODY literal labels — verbatim from the manifest rows (lines 6-20).
const FIELD_LABELS: Record<string, string> = {
  ...LABELS,
  'CU-NAME': 'Name          :',
  'CU-KANA': 'Search Name   :',
  'CU-ZIP': 'Post Code     :',
  'CU-ADDR1': 'Address 1     :',
  'CU-ADDR2': 'Address 2     :',
  'CU-TEL': 'Telephone     :',
  'CU-FAX': 'Facsimile     :',
  'CU-REGION': 'Region Code   :',
  'CU-STAFF': 'Sales Rep     :',
  'CU-CLOSE-DAY': 'Closing Day   :',
  'CU-PAY-METHOD': 'Pay Method    :',
  'CU-TAX-TYPE': 'Tax Type      :',
  'CU-CREDIT-LIMIT': 'Credit Limit  :',
  'CU-PRICE-RANK': 'Price Rank    :',
  'CU-BANK-CODE': 'Bank Code     :',
};

// Enum-guide literals painted by the COBOL screen — always visible (§6.7.1 A).
const FIELD_HINTS: Record<string, string> = {
  ...HINTS,
  'CU-PAY-METHOD': '1:cash 2:transfer 3:bill',
  'CU-TAX-TYPE': '1:excl 2:incl 3:exempt',
};

// Adjacent WK-*-NAME from-fields resolve code → name (line 13/14/20 col 26).
const DESC_MAP: Record<string, string> = {
  ...FIELD_DESC_MAP,
  'CU-REGION': 'WK-REGION-NAME',
  'CU-STAFF': 'WK-STAFF-NAME',
  'CU-BANK-CODE': 'WK-BANK-NAME',
};

// Fields whose code input carries a resolved-name desc box (always rendered).
const DESC_FIELDS = new Set(['CU-REGION', 'CU-STAFF', 'CU-BANK-CODE']);

// PF BINDING CONTRACT union: 00, 03, 04, 09 → manifest.pfKeys PF3='03', PF4='04'.
const PF_MAP_OVERRIDES: Record<string, string> = {
  ...PF_OVERRIDES,
  F3: '03',
  F4: '04',
};

// COBOL acceptance order (manifest inputFields) — single flat section, no
// invented section titles (the screen paints none).
const SECTIONS: { fields: string[] }[] = [
  {
    fields: [
      'CU-NAME',
      'CU-KANA',
      'CU-ZIP',
      'CU-ADDR1',
      'CU-ADDR2',
      'CU-TEL',
      'CU-FAX',
      'CU-REGION',
      'CU-STAFF',
      'CU-CLOSE-DAY',
      'CU-PAY-METHOD',
      'CU-TAX-TYPE',
      'CU-CREDIT-LIMIT',
      'CU-PRICE-RANK',
      'CU-BANK-CODE',
    ],
  },
];

const typed = inject(TYPED_VALUES_KEY)!;

const {
  activeFieldName,
  isFieldActive,
  activeWidth,
  valueOf,
  descOf,
  submit,
  onKey,
  inputRef,
} = useScreenForm(props, {
  fieldDataMap: FIELD_DATA_MAP,
  fieldDescMap: DESC_MAP,
  pfOverrides: PF_MAP_OVERRIDES,
  picKeyLock: true,
  typed,
});

// Seed the editable buffer from the backend default on field activation
// (COBOL DISPLAY-then-ACCEPT: Enter accepts the shown default).
const editValue = ref('');
let prevFieldName = '';
watch(
  () => activeFieldName.value,
  (now) => {
    if (now !== prevFieldName) {
      editValue.value = !now ? '' : (valueOf(now) || '');
      prevFieldName = now;
    }
  },
  { immediate: true },
);

function onInput(e: Event) {
  editValue.value = (e.target as HTMLInputElement).value;
}

function wOf(fieldName: string): number {
  const w = WIDTHS[fieldName] ?? 12;
  return Math.min(Math.max(w + 2, 4), 42);
}

function fieldHint(fieldName: string): string {
  return FIELD_HINTS[fieldName] ?? '';
}

function isNumeric(fieldName: string): boolean {
  return NUMERIC_FIELDS.has(fieldName);
}

const subRegionHint = computed(
  () => (props.state as { subRegionMessage?: string }).subRegionMessage ?? '',
);

// Toast — observe [statusMessage, messageNonce] tuple so identical repeated
// messages still refire (§ Status-message watch).
const toastMsg = ref('');
const toastType = ref<'info' | 'success' | 'warning' | 'error'>('info');
const showToast = ref(false);
const messageCode = computed(
  () => (props.state as { messageCode?: string }).messageCode ?? '',
);
watch(
  () => [props.state.statusMessage, (props.state as { messageNonce?: number }).messageNonce] as const,
  ([msg]) => {
    if (msg) {
      toastMsg.value = msg as string;
      toastType.value = messageCode.value.startsWith('E') ? 'error' : 'info';
      showToast.value = true;
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <p
        v-if="subRegionHint"
        class="scr-hint"
        :style="{ background: '#fffbe6', border: '1px solid #ffe58f', padding: '6px 12px', margin: '0 0 10px', borderRadius: '4px', color: '#614700' }"
      >
        {{ subRegionHint }}
      </p>

      <div style="width: min(840px, 100%); margin: 0 auto;">
        <div v-for="(sec, si) in SECTIONS" :key="si" class="scr-form-section">
          <div class="scr-form-section-body">
            <div
              v-for="fieldName in sec.fields"
              :key="fieldName"
              class="scr-form-grid"
              style="align-items: start; margin-bottom: 8px;"
            >
              <label class="scr-label" style="text-align: left; padding-top: 9px; font-size: 13px;">
                {{ FIELD_LABELS[fieldName] ?? fieldName }}
              </label>
              <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                <input
                  v-if="isFieldActive(fieldName)"
                  ref="inputRef"
                  :class="['scr-input', { 'scr-numeric': isNumeric(fieldName) }]"
                  :value="editValue"
                  :maxlength="activeWidth"
                  :inputmode="isNumeric(fieldName) ? 'numeric' : undefined"
                  :style="{ width: wOf(fieldName) + 'ch', maxWidth: '100%' }"
                  :data-field-name="fieldName"
                  data-active="true"
                  @input="onInput"
                  @keydown="onKey"
                />
                <span
                  v-else
                  :class="['scr-value', { 'scr-numeric': isNumeric(fieldName) }]"
                  :style="{ width: wOf(fieldName) + 'ch', maxWidth: '100%', display: 'inline-block' }"
                >{{ valueOf(fieldName) }}</span>
                <span
                  v-if="DESC_FIELDS.has(fieldName)"
                  class="scr-desc-box"
                  :style="{ width: '32ch', maxWidth: '100%' }"
                >{{ descOf(fieldName) }}</span>
                <p v-if="fieldHint(fieldName)" class="scr-hint">{{ fieldHint(fieldName) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="OK (Enter)" @click="submit(editValue, '00')" />
      <Button variant="primary" size="md" label="Register (F4)" @click="submit(editValue, '04')" />
      <Button variant="outline" size="md" label="Delete (F3)" @click="submit(editValue, '03')" />
      <button class="btn btn-outline btn-sm" @click="submit(editValue, '09')">
        Back
      </button>
    </div>

    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="showToast = false" />
  </div>
</template>