<!-- AR0010HeadScreen — DS-HEAD (dialog archetype, entry-point screen → fullscreen form, no transparent overlay) -->
<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import { useToast } from '../../composables/useToast';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';
import { FIELD_DATA_MAP, FIELD_DESC_MAP, NUMERIC_FIELDS, WIDTHS, LABELS, PF_OVERRIDES } from './constant';

defineOptions({ name: 'AR0010HeadScreen' });

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

// Display-only from-fields on this screen (header/summary values) — extend the
// shared map locally so valueOf() can resolve them; constant entries win.
const DATA_MAP: Record<string, string> = {
  'WK-RE-NO-D': 'WK-RE-NO-D',
  'WK-CUR-BAL': 'WK-CUR-BAL',
  'WK-SESS-CNT': 'WK-SESS-CNT',
  'WK-SESS-AMT': 'WK-SESS-AMT',
  ...FIELD_DATA_MAP,
};
const DESC_MAP: Record<string, string> = {
  'RE-CUST': 'WK-CUST-NAME',
  'RE-BANK-CODE': 'WK-BANK-NAME',
  ...FIELD_DESC_MAP,
};

const isNumericField = (name: string) => NUMERIC_FIELDS.has(name);

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

// ─── Local edit buffers (one per input — persists across sequential ACCEPTs) ──
const INPUT_FIELDS = ['RE-DATE', 'RE-CUST', 'RE-METHOD', 'RE-AMOUNT', 'RE-BANK-CODE', 'RE-REMARK'] as const;
const values = ref<Record<string, string>>({});
for (const f of INPUT_FIELDS) values.value[f] = '';

// Seed the editable buffer from the backend default when a field becomes
// active (COBOL DISPLAY+ACCEPT semantics — Enter accepts the displayed value).
let prevField = '';
watch(
  activeFieldName,
  (now) => {
    if (now && now !== prevField) {
      if (!values.value[now]) values.value[now] = valueOf(now) || '';
      prevField = now;
    }
  },
  { immediate: true },
);

function getValue(name: string): string {
  return values.value[name] || valueOf(name);
}

function inputWidth(name: string): string {
  return ((WIDTHS[name] ?? 8) + 2) + 'ch';
}

function onFieldInput(e: Event, name: string) {
  values.value[name] = (e.target as HTMLInputElement).value;
  onInput(e, name);
}

function currentValue(): string {
  const n = activeFieldName.value;
  return n ? values.value[n] || '' : '';
}

function onConfirm() {
  // '00' (Enter) — server mapAidKey resolves per-field advanceEsts.
  submit(currentValue(), '00');
}

// Contextual sub-region hint (inline help painted by COBOL next to a field).
const subRegionHint = computed(() => (props.state as { subRegionMessage?: string }).subRegionMessage ?? '');

// ─── Toast wiring ─────────────────────────────────────────────────────────
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

      <div style="width: min(840px, 100%); margin: 0 auto;">
        <div class="scr-form-section">
          <div class="scr-form-section-body">

            <!-- line 3: Receipt No (display) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">Receipt No :</label>
              <span class="scr-value scr-numeric" style="width: 12ch; display: inline-block;">{{ valueOf('WK-RE-NO-D') }}</span>
            </div>

            <!-- line 4: Date (input) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['RE-DATE'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('RE-DATE')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  :value="values['RE-DATE']"
                  :maxlength="activeWidth ?? WIDTHS['RE-DATE']"
                  :style="{ width: inputWidth('RE-DATE'), maxWidth: '100%' }"
                  inputmode="numeric"
                  data-field-name="RE-DATE"
                  data-active="true"
                  @input="(e) => onFieldInput(e, 'RE-DATE')"
                  @keydown="onKey"
                />
                <span v-else class="scr-value scr-numeric" :style="{ width: inputWidth('RE-DATE'), display: 'inline-block' }">{{ getValue('RE-DATE') }}</span>
              </div>
            </div>

            <!-- line 5: Customer (input + resolved name) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['RE-CUST'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('RE-CUST')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  :value="values['RE-CUST']"
                  :maxlength="activeWidth ?? WIDTHS['RE-CUST']"
                  :style="{ width: inputWidth('RE-CUST'), maxWidth: '100%' }"
                  inputmode="numeric"
                  data-field-name="RE-CUST"
                  data-active="true"
                  @input="(e) => onFieldInput(e, 'RE-CUST')"
                  @keydown="onKey"
                />
                <span v-else class="scr-value scr-numeric" :style="{ width: inputWidth('RE-CUST'), display: 'inline-block' }">{{ getValue('RE-CUST') }}</span>
                <span class="scr-desc-box" style="width: 40ch; max-width: 100%;">{{ descOf('RE-CUST') }}</span>
              </div>
            </div>

            <!-- line 6: AR Balance (display) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">AR Balance :</label>
              <span class="scr-value scr-numeric" style="width: 17ch; display: inline-block;">{{ valueOf('WK-CUR-BAL') }}</span>
            </div>

            <!-- line 7: Method (input + enum guide — always visible) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['RE-METHOD'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('RE-METHOD')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  :value="values['RE-METHOD']"
                  :maxlength="activeWidth ?? WIDTHS['RE-METHOD']"
                  :style="{ width: inputWidth('RE-METHOD'), maxWidth: '100%' }"
                  inputmode="numeric"
                  data-field-name="RE-METHOD"
                  data-active="true"
                  @input="(e) => onFieldInput(e, 'RE-METHOD')"
                  @keydown="onKey"
                />
                <span v-else class="scr-value scr-numeric" :style="{ width: inputWidth('RE-METHOD'), display: 'inline-block' }">{{ getValue('RE-METHOD') }}</span>
                <p class="scr-hint" style="width: auto; margin: 0;">1:cash 2:transfer 3:bill 4:offset</p>
              </div>
            </div>

            <!-- line 8: Amount (input) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['RE-AMOUNT'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('RE-AMOUNT')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  :value="values['RE-AMOUNT']"
                  :maxlength="activeWidth ?? WIDTHS['RE-AMOUNT']"
                  :style="{ width: inputWidth('RE-AMOUNT'), maxWidth: '100%' }"
                  inputmode="numeric"
                  data-field-name="RE-AMOUNT"
                  data-active="true"
                  @input="(e) => onFieldInput(e, 'RE-AMOUNT')"
                  @keydown="onKey"
                />
                <span v-else class="scr-value scr-numeric" :style="{ width: inputWidth('RE-AMOUNT'), display: 'inline-block' }">{{ getValue('RE-AMOUNT') }}</span>
              </div>
            </div>

            <!-- line 9: Bank Code (input + resolved name) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['RE-BANK-CODE'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('RE-BANK-CODE')"
                  :ref="bindActiveInput"
                  class="scr-input scr-numeric"
                  :value="values['RE-BANK-CODE']"
                  :maxlength="activeWidth ?? WIDTHS['RE-BANK-CODE']"
                  :style="{ width: inputWidth('RE-BANK-CODE'), maxWidth: '100%' }"
                  inputmode="numeric"
                  data-field-name="RE-BANK-CODE"
                  data-active="true"
                  @input="(e) => onFieldInput(e, 'RE-BANK-CODE')"
                  @keydown="onKey"
                />
                <span v-else class="scr-value scr-numeric" :style="{ width: inputWidth('RE-BANK-CODE'), display: 'inline-block' }">{{ getValue('RE-BANK-CODE') }}</span>
                <span class="scr-desc-box" style="width: 30ch; max-width: 100%;">{{ descOf('RE-BANK-CODE') }}</span>
              </div>
            </div>

            <!-- line 10: Remark (input, text — left-aligned) -->
            <div class="scr-form-grid" style="margin-bottom: 8px;">
              <label class="scr-label" style="text-align: left;">{{ LABELS['RE-REMARK'] }}</label>
              <div class="scr-form-row-line">
                <input
                  v-if="isFieldActive('RE-REMARK')"
                  :ref="bindActiveInput"
                  class="scr-input"
                  :value="values['RE-REMARK']"
                  :maxlength="activeWidth ?? WIDTHS['RE-REMARK']"
                  :style="{ width: inputWidth('RE-REMARK'), maxWidth: '100%' }"
                  data-field-name="RE-REMARK"
                  data-active="true"
                  @input="(e) => onFieldInput(e, 'RE-REMARK')"
                  @keydown="onKey"
                />
                <span v-else class="scr-value" :style="{ width: inputWidth('RE-REMARK'), display: 'inline-block' }">{{ getValue('RE-REMARK') }}</span>
              </div>
            </div>

            <!-- line 11: Session cnt / amt (display, two pairs on one COBOL line) -->
            <div class="scr-form-grid">
              <label class="scr-label" style="text-align: left;">Session cnt:</label>
              <div class="scr-form-row-line">
                <span class="scr-value scr-numeric" style="width: 7ch; display: inline-block;">{{ valueOf('WK-SESS-CNT') }}</span>
                <span class="scr-label">amt:</span>
                <span class="scr-value scr-numeric" style="width: 17ch; display: inline-block;">{{ valueOf('WK-SESS-AMT') }}</span>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
      <Button variant="outline" size="md" label="Confirm (F4)" @click="submit(currentValue(), '04')" />
      <Button variant="outline" size="md" label="Cancel (F3)" @click="submit(currentValue(), '03')" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="hideToast" />
  </div>
</template>