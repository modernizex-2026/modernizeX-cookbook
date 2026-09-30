<!-- AP0010HeadScreen — ap0010 / DS-HEAD (dialog archetype, entry-point → fullscreen form; no isTransparentOverlay per §4 entry-point exception) -->
<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import { useToast } from '../../composables/useToast';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';
import {
  FIELD_DATA_MAP,
  FIELD_DESC_MAP,
  NUMERIC_FIELDS,
  WIDTHS,
  LABELS,
  PF_OVERRIDES,
} from './constant';

defineOptions({ name: 'AP0010HeadScreen' });

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

// All 6 declared inputFields, in COBOL acceptance order.
const FIELDS: { name: string; hint?: string }[] = [
  { name: 'PY-DATE' },
  { name: 'PY-SUPP' },
  { name: 'PY-METHOD', hint: '1:cash 2:transfer 3:bill 4:offset' },
  { name: 'PY-AMOUNT' },
  { name: 'PY-BANK-CODE' },
  { name: 'PY-REMARK' },
];

// Display-only from-fields on this screen, routed through the typed lookup.
const DATA_MAP: Record<string, string> = {
  ...FIELD_DATA_MAP,
  'WK-PY-NO-D': 'WK-PY-NO-D',
  'WK-SUPP-NAME': 'WK-SUPP-NAME',
  'WK-CUR-BAL': 'WK-CUR-BAL',
  'WK-BANK-NAME': 'WK-BANK-NAME',
  'WK-SESS-CNT': 'WK-SESS-CNT',
  'WK-SESS-AMT': 'WK-SESS-AMT',
};
const DESC_MAP: Record<string, string> = {
  ...FIELD_DESC_MAP,
  'PY-SUPP': 'WK-SUPP-NAME',
  'PY-BANK-CODE': 'WK-BANK-NAME',
};

const isNumericField = (name: string) => NUMERIC_FIELDS.has(name);

const {
  activeFieldName,
  isFieldActive,
  activeWidth,
  valueOf,
  descOf,
  submit,
  onKey,
  onInput,
  bindActiveInput,
} = useScreenForm(props, {
  fieldDataMap: DATA_MAP,
  fieldDescMap: DESC_MAP,
  pfOverrides: PF_OVERRIDES,
  isNumericField,
  typed,
});

// Seed the editable buffer from the backend default on field activation
// (COBOL DISPLAY+ACCEPT semantics: displayed value is the editable initial content).
const editValue = ref('');
let prevFieldName = '';
watch(
  () => activeFieldName.value,
  (now) => {
    if (now !== prevFieldName) {
      editValue.value = now ? (valueOf(now) || '') : '';
      prevFieldName = now;
    }
  },
  { immediate: true },
);

function onFieldInput(e: Event) {
  onInput(e);
  editValue.value = (e.target as HTMLInputElement).value;
}

function onConfirm() {
  // '00' — server's mapAidKey resolves Enter to the field's advanceEsts.
  submit(editValue.value, '00');
}

function inputWidth(name: string): number {
  const w = WIDTHS[name] ?? 8;
  return Math.min(w + 2, 32);
}

// Inline contextual help sub-region (transient *-MSG frames).
const subRegionHint = computed(
  () => (props.state as { subRegionMessage?: string }).subRegionMessage ?? '',
);

const { showToast, toastMsg, toastType, hideToast } = useToast(props.state);
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

      <div :style="{ width: 'min(840px, 100%)', margin: '0 auto' }">
        <div class="scr-form-section">
          <div class="scr-form-section-body">

            <!-- line 3: Payment No (display) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">Payment No :</label>
              <span
                class="scr-value"
                :style="{ width: '12ch', maxWidth: '100%', display: 'inline-block' }"
              >{{ valueOf('WK-PY-NO-D') }}</span>
            </div>

            <!-- line 4: Date (input) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['PY-DATE'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('PY-DATE')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  inputmode="numeric"
                  :value="editValue"
                  :maxlength="activeWidth ?? WIDTHS['PY-DATE']"
                  :style="{ width: inputWidth('PY-DATE') + 'ch', maxWidth: '100%' }"
                  data-field-name="PY-DATE"
                  data-active="true"
                  @input="onFieldInput"
                  @keydown="onKey"
                />
                <span
                  v-else
                  class="scr-value"
                  :style="{ width: inputWidth('PY-DATE') + 'ch', maxWidth: '100%', display: 'inline-block' }"
                >{{ valueOf('PY-DATE') }}</span>
              </div>
            </div>

            <!-- line 5: Supplier (input) + resolved name -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['PY-SUPP'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('PY-SUPP')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  inputmode="numeric"
                  :value="editValue"
                  :maxlength="activeWidth ?? WIDTHS['PY-SUPP']"
                  :style="{ width: inputWidth('PY-SUPP') + 'ch', maxWidth: '100%' }"
                  data-field-name="PY-SUPP"
                  data-active="true"
                  @input="onFieldInput"
                  @keydown="onKey"
                />
                <span
                  v-else
                  class="scr-value"
                  :style="{ width: inputWidth('PY-SUPP') + 'ch', maxWidth: '100%', display: 'inline-block' }"
                >{{ valueOf('PY-SUPP') }}</span>
                <span
                  class="scr-desc-box"
                  :style="{ width: '40ch', maxWidth: '100%', marginLeft: '8px' }"
                >{{ descOf('PY-SUPP') }}</span>
              </div>
            </div>

            <!-- line 6: AP Balance (display) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">AP Balance :</label>
              <span
                class="scr-value scr-numeric"
                :style="{ width: '17ch', maxWidth: '100%', display: 'inline-block' }"
              >{{ valueOf('WK-CUR-BAL') }}</span>
            </div>

            <!-- line 7: Method (input) + enum guide (ALWAYS visible) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['PY-METHOD'] }}</label>
              <div>
                <div class="scr-form-row-line">
                  <input
                    v-if="isFieldActive('PY-METHOD')"
                    :ref="bindActiveInput"
                    class="scr-input scr-numeric"
                    inputmode="numeric"
                    :value="editValue"
                    :maxlength="activeWidth ?? WIDTHS['PY-METHOD']"
                    :style="{ width: inputWidth('PY-METHOD') + 'ch', maxWidth: '100%' }"
                    data-field-name="PY-METHOD"
                    data-active="true"
                    @input="onFieldInput"
                    @keydown="onKey"
                  />
                  <span
                    v-else
                    class="scr-value"
                    :style="{ width: inputWidth('PY-METHOD') + 'ch', maxWidth: '100%', display: 'inline-block' }"
                  >{{ valueOf('PY-METHOD') }}</span>
                </div>
                <p class="scr-hint">1:cash 2:transfer 3:bill 4:offset</p>
              </div>
            </div>

            <!-- line 8: Amount (input) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['PY-AMOUNT'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('PY-AMOUNT')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  inputmode="numeric"
                  :value="editValue"
                  :maxlength="activeWidth ?? WIDTHS['PY-AMOUNT']"
                  :style="{ width: inputWidth('PY-AMOUNT') + 'ch', maxWidth: '100%' }"
                  data-field-name="PY-AMOUNT"
                  data-active="true"
                  @input="onFieldInput"
                  @keydown="onKey"
                />
                <span
                  v-else
                  class="scr-value scr-numeric"
                  :style="{ width: inputWidth('PY-AMOUNT') + 'ch', maxWidth: '100%', display: 'inline-block' }"
                >{{ valueOf('PY-AMOUNT') }}</span>
              </div>
            </div>

            <!-- line 9: Bank Code (input) + resolved name -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['PY-BANK-CODE'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('PY-BANK-CODE')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  inputmode="numeric"
                  :value="editValue"
                  :maxlength="activeWidth ?? WIDTHS['PY-BANK-CODE']"
                  :style="{ width: inputWidth('PY-BANK-CODE') + 'ch', maxWidth: '100%' }"
                  data-field-name="PY-BANK-CODE"
                  data-active="true"
                  @input="onFieldInput"
                  @keydown="onKey"
                />
                <span
                  v-else
                  class="scr-value"
                  :style="{ width: inputWidth('PY-BANK-CODE') + 'ch', maxWidth: '100%', display: 'inline-block' }"
                >{{ valueOf('PY-BANK-CODE') }}</span>
                <span
                  class="scr-desc-box"
                  :style="{ width: '30ch', maxWidth: '100%', marginLeft: '8px' }"
                >{{ descOf('PY-BANK-CODE') }}</span>
              </div>
            </div>

            <!-- line 10: Remark (input, alphanumeric) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['PY-REMARK'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('PY-REMARK')"
                  :ref="bindActiveInput"
                  class="scr-input"
                  :value="editValue"
                  :maxlength="activeWidth ?? WIDTHS['PY-REMARK']"
                  :style="{ width: inputWidth('PY-REMARK') + 'ch', maxWidth: '100%' }"
                  data-field-name="PY-REMARK"
                  data-active="true"
                  @input="onFieldInput"
                  @keydown="onKey"
                />
                <span
                  v-else
                  class="scr-value"
                  :style="{ width: inputWidth('PY-REMARK') + 'ch', maxWidth: '100%', display: 'inline-block' }"
                >{{ valueOf('PY-REMARK') }}</span>
              </div>
            </div>

            <!-- line 11: Session cnt / amt (display-only trailing row) -->
            <div class="scr-form-grid">
              <label class="scr-label" style="text-align: left;">Session cnt:</label>
              <div class="scr-form-row-line">
                <span
                  class="scr-value scr-numeric"
                  :style="{ width: '7ch', display: 'inline-block' }"
                >{{ valueOf('WK-SESS-CNT') }}</span>
                <span class="scr-label">amt:</span>
                <span
                  class="scr-value scr-numeric"
                  :style="{ width: '17ch', display: 'inline-block' }"
                >{{ valueOf('WK-SESS-AMT') }}</span>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>

    <div class="scr-page-footer">
      <!-- '00' → server maps Enter to the active field's advanceEsts -->
      <Button variant="primary" size="md" label="Confirm (Enter)" @click="onConfirm" />
      <!-- field-level accepted PF '04' (ESTS-ADV) — explicit submit affordance -->
      <Button variant="primary" size="md" label="Confirm (F4)" @click="submit(editValue, '04')" />
      <!-- field-level accepted PF '03' -->
      <Button variant="outline" size="md" label="Back (F3)" @click="submit('', '03')" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="hideToast" />
  </div>
</template>