<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import { useToast } from '../../composables/useToast';
import { FIELD_DATA_MAP, FIELD_DESC_MAP, NUMERIC_FIELDS, WIDTHS, PF_OVERRIDES } from './constant';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';

defineOptions({ name: 'MS0020BodyScreen' });

const props = defineProps<ScreenOverrideProps>();

// Display-only from-fields on this screen (AP Balance line 18, bank name line 19)
const DATA_MAP: Record<string, string> = {
  ...FIELD_DATA_MAP,
  'SP-BALANCE': 'SP-BALANCE',
  'WK-BANK-NAME': 'WK-BANK-NAME',
};

// One row per COBOL screen line, in ACCEPT order (= manifest inputFields order).
// label = the COBOL literal VERBATIM (incl. colon); hint = same-line CYAN literal.
interface RowDef {
  name: string;
  label: string;
  hint?: string;
  readOnly?: boolean;
}
const ROWS: RowDef[] = [
  { name: 'SP-NAME',       label: 'Name          :' },
  { name: 'SP-KANA',       label: 'Search Name   :' },
  { name: 'SP-ZIP',        label: 'Post Code     :' },
  { name: 'SP-ADDR1',      label: 'Address 1     :' },
  { name: 'SP-ADDR2',      label: 'Address 2     :' },
  { name: 'SP-TEL',        label: 'Telephone     :' },
  { name: 'SP-FAX',        label: 'Facsimile     :' },
  { name: 'SP-CLOSE-DAY',  label: 'Closing Day   :', hint: '(99=month end)' },
  { name: 'SP-PAY-MONTH',  label: 'Pay Month     :', hint: '0:same 1:next 2:+2' },
  { name: 'SP-PAY-DAY',    label: 'Pay Day       :', hint: '(99=month end)' },
  { name: 'SP-PAY-METHOD', label: 'Pay Method    :', hint: '1:cash 2:transfer 3:bill' },
  { name: 'SP-TAX-TYPE',   label: 'Tax Type      :', hint: '1:excl 2:incl 3:exempt' },
  { name: 'SP-BALANCE',    label: 'AP Balance    :', readOnly: true },
  { name: 'SP-BANK-CODE',  label: 'Bank Code     :' },
  { name: 'SP-BANK-ACCT',  label: 'Bank Account  :' },
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
  fieldDataMap: DATA_MAP,
  fieldDescMap: FIELD_DESC_MAP,
  pfOverrides: PF_OVERRIDES,
  picKeyLock: true,
  isNumericField: (n: string) => NUMERIC_FIELDS.has(n),
  typed,
});

// Seed the editable buffer from the backend-displayed default on activation
// (COBOL DISPLAY-then-ACCEPT semantics — Enter accepts the shown value).
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

function widthOf(name: string): number {
  if (name === 'SP-BALANCE') return 12;
  return WIDTHS[name] ?? 12;
}

const bankName = computed(() => valueOf('WK-BANK-NAME'));

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
      <div style="width: min(840px, 100%); margin: 0 auto;">
        <div class="scr-form-section">
          <div class="scr-form-section-body">
            <template v-for="row in ROWS" :key="row.name">
              <div class="scr-form-grid" style="grid-template-columns: 180px 1fr; margin-bottom: 8px; align-items: start;">
                <label class="scr-label" style="text-align: left; padding-top: 9px;">{{ row.label }}</label>
                <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                  <!-- Display-only line (AP Balance) -->
                  <span
                    v-if="row.readOnly"
                    class="scr-value scr-numeric"
                    :style="{ width: widthOf(row.name) + 'ch', maxWidth: '100%', display: 'inline-block' }"
                  >{{ valueOf(row.name) }}</span>

                  <!-- Input field -->
                  <template v-else>
                    <input
                      v-if="isFieldActive(row.name)"
                      ref="inputRef"
                      :class="['scr-input', { 'scr-numeric': NUMERIC_FIELDS.has(row.name) }]"
                      :value="editValue"
                      :maxlength="activeWidth"
                      :inputmode="NUMERIC_FIELDS.has(row.name) ? 'numeric' : undefined"
                      :style="{ width: widthOf(row.name) + 'ch', maxWidth: '100%' }"
                      :data-field-name="row.name"
                      data-active="true"
                      @input="onInput"
                      @keydown="onKey"
                    />
                    <span
                      v-else
                      :class="['scr-value', { 'scr-numeric': NUMERIC_FIELDS.has(row.name) }]"
                      :style="{ width: widthOf(row.name) + 'ch', maxWidth: '100%', display: 'inline-block' }"
                    >{{ valueOf(row.name) }}</span>
                  </template>

                  <!-- Resolved bank name beside Bank Code (WK-BANK-NAME, line 19) -->
                  <span
                    v-if="row.name === 'SP-BANK-CODE'"
                    class="scr-desc-box"
                    :style="{ width: '30ch', maxWidth: '100%' }"
                  >{{ bankName }}</span>

                  <!-- Enum-guide / suffix literal from the COBOL line — always visible -->
                  <p v-if="row.hint" class="scr-hint" style="margin: 0; width: auto;">{{ row.hint }}</p>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="OK (Enter)" @click="submit(editValue, '00')" />
      <Button variant="primary" size="md" label="Confirm (F4)" @click="submit('', '04')" />
      <Button variant="outline" size="md" label="Cancel (F3)" @click="submit('', '03')" />
      <Button variant="outline" size="md" label="Back (F9)" @click="submit('', '09')" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="hideToast" />
  </div>
</template>