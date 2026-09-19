<!-- MS0030BodyScreen — large-form archetype for ms0030 DS-BODY (22 input fields) -->
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

defineOptions({ name: 'MS0030BodyScreen' });

const props = defineProps<ScreenOverrideProps>();

// Manifest per-line literals are the label source of truth; the shared LABELS
// map collapses same-line fields onto the first literal, so fix those here.
const LABELS_MAP: Record<string, string> = {
  ...LABELS,
  'PR-NAME': 'Name',
  'PR-KANA': 'Search Name',
  'PR-SPEC': 'Spec / Model',
  'PR-CATEGORY': 'Category',
  'PR-BARCODE': 'Barcode',
  'PR-UNIT': 'Unit',
  'PR-STD-COST': 'Std Cost',
  'PR-LIST-PRICE': 'List Price',
  'PR-LAST-COST': 'Last Cost',
  'PR-TAX-CATEGORY': 'Tax',
  'PR-RANK-PRICE(1)': 'Rank1',
  'PR-RANK-PRICE(2)': 'Rank2',
  'PR-RANK-PRICE(3)': 'Rank3',
  'PR-RANK-PRICE(4)': 'Rank4',
  'PR-RANK-PRICE(5)': 'Rank5',
  'PR-SAFETY-STOCK': 'Safety Stock',
  'PR-REORDER-POINT': 'Reorder Pt',
  'PR-REORDER-QTY': 'Reorder Qty',
  'PR-LEAD-DAYS': 'Lead Days',
  'PR-DFLT-SUPP': 'Dflt Supplier',
  'PR-DFLT-WHSE': 'Dflt Warehouse',
  'PR-STOCK-MNG': 'Stock Mng',
};

// Enum-guide hints from the CYAN manifest literals — rendered unconditionally.
const HINTS_MAP: Record<string, string> = {
  ...HINTS,
  'PR-TAX-CATEGORY': '1:std 2:reduced 3:exempt',
  'PR-STOCK-MNG': '0:no 1:managed',
};

// Description name boxes (WK-* from-fields adjacent to code inputs).
const DESC_MAP: Record<string, string> = {
  ...FIELD_DESC_MAP,
  'PR-CATEGORY': 'WK-CATG-NAME',
  'PR-DFLT-SUPP': 'WK-SUPP-NAME',
  'PR-DFLT-WHSE': 'WK-WHSE-NAME',
};

// PARITY CONTRACT — flattened SECTIONS order matches manifest inputFields
// order 1:1 (COBOL acceptance order). 22 fields total.
const SECTIONS: { title: string; fields: string[] }[] = [
  {
    title: 'Name / Category / Barcode',
    fields: ['PR-NAME', 'PR-KANA', 'PR-SPEC', 'PR-CATEGORY', 'PR-BARCODE', 'PR-UNIT'],
  },
  {
    title: 'Cost / Price / Tax',
    fields: ['PR-STD-COST', 'PR-LIST-PRICE', 'PR-LAST-COST', 'PR-TAX-CATEGORY'],
  },
  {
    title: 'Rank1 - Rank5',
    fields: [
      'PR-RANK-PRICE(1)',
      'PR-RANK-PRICE(2)',
      'PR-RANK-PRICE(3)',
      'PR-RANK-PRICE(4)',
      'PR-RANK-PRICE(5)',
    ],
  },
  {
    title: 'Safety Stock / Reorder',
    fields: ['PR-SAFETY-STOCK', 'PR-REORDER-POINT', 'PR-REORDER-QTY', 'PR-LEAD-DAYS'],
  },
  {
    title: 'Dflt Supplier / Warehouse / Stock Mng',
    fields: ['PR-DFLT-SUPP', 'PR-DFLT-WHSE', 'PR-STOCK-MNG'],
  },
];

// Typed-value sink — persists user edits across overlay remounts.
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
  pfOverrides: PF_OVERRIDES,
  picKeyLock: true,
  typed,
});

function widthOf(fieldName: string): number {
  return WIDTHS[fieldName] ?? 12;
}

// Editable buffer — seeded from the backend-displayed default on field
// activation so Enter accepts the COBOL DISPLAY-then-ACCEPT default.
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

// Contextual help sub-region (inline hint channel, separate from statusMessage).
const subRegionHint = computed(
  () => (props.state as { subRegionMessage?: string }).subRegionMessage ?? '',
);

// Toast wiring — observe [statusMessage, messageNonce] tuple so identical
// re-emitted messages still refire; immediate so pre-mount messages show.
const messageCode = computed(
  () => (props.state as { messageCode?: string }).messageCode ?? '',
);
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
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
        </div>
      </div>
    </div>

    <div class="scr-page-body">
      <div style="width: min(840px, 100%); margin: 0 auto;">
        <p
          v-if="subRegionHint"
          class="scr-hint"
          :style="{ background: '#fffbe6', border: '1px solid #ffe58f', padding: '6px 12px', margin: '0 0 10px', borderRadius: '4px', color: '#614700' }"
        >
          {{ subRegionHint }}
        </p>

        <div v-for="sec in SECTIONS" :key="sec.title" class="scr-form-section">
          <h3 class="scr-form-section-title">{{ sec.title }}</h3>
          <div class="scr-form-section-body">
            <div
              v-for="fieldName in sec.fields"
              :key="fieldName"
              class="scr-form-grid"
              style="align-items: start; margin-bottom: 8px;"
            >
              <label class="scr-label" style="text-align: left; padding-top: 9px; font-size: 13px;">
                {{ LABELS_MAP[fieldName] ?? fieldName }}
              </label>
              <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                <input
                  v-if="isFieldActive(fieldName)"
                  ref="inputRef"
                  :class="['scr-input', { 'scr-numeric': NUMERIC_FIELDS.has(fieldName) }]"
                  :value="editValue"
                  :maxlength="activeWidth"
                  :inputmode="NUMERIC_FIELDS.has(fieldName) ? 'numeric' : undefined"
                  :style="{ width: widthOf(fieldName) + 2 + 'ch', maxWidth: '100%' }"
                  :data-field-name="fieldName"
                  data-active="true"
                  @input="onInput"
                  @keydown="onKey"
                />
                <span
                  v-else
                  class="scr-value"
                  :style="{ width: widthOf(fieldName) + 2 + 'ch', maxWidth: '100%', display: 'inline-block' }"
                >{{ valueOf(fieldName) }}</span>
                <span
                  v-if="DESC_MAP[fieldName]"
                  class="scr-desc-box"
                  :style="{ width: '30ch', maxWidth: '100%' }"
                >{{ descOf(fieldName) }}</span>
                <p v-if="HINTS_MAP[fieldName]" class="scr-hint" style="width: auto; margin: 0;">
                  {{ HINTS_MAP[fieldName] }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="OK (Enter)" @click="submit(editValue, '00')" />
      <Button variant="outline" size="md" label="Confirm (F4)" @click="submit(editValue, '04')" />
      <Button variant="outline" size="md" label="Cancel (F3)" @click="submit(editValue, '03')" />
      <Button variant="outline" size="md" label="Back (F9)" @click="submit(editValue, '09')" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="showToast = false" />
  </div>
</template>