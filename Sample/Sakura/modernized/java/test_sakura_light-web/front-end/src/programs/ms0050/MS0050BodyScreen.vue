<!-- MS0050BodyScreen — DS-BODY (dialog archetype, entry-point fullscreen form) -->
<script setup lang="ts">
import { ref, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import { useToast } from '../../composables/useToast';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';
import { FIELD_DATA_MAP, FIELD_DESC_MAP, NUMERIC_FIELDS, LABELS, PF_OVERRIDES } from './constant';

defineOptions({ name: 'MS0050BodyScreen' });

const props = defineProps<ScreenOverrideProps>();
const typed = inject(TYPED_VALUES_KEY)!;

// COBOL acceptance order (manifest.inputFields) — widths from PIC clauses.
const FIELDS: { name: string; width: number; numeric?: boolean; hasDesc?: boolean }[] = [
  { name: 'SF-NAME', width: 30 },
  { name: 'SF-KANA', width: 30 },
  { name: 'SF-DEPT', width: 4, numeric: true, hasDesc: true },
  { name: 'SF-TEL', width: 15 },
  { name: 'SF-EMAIL', width: 40 },
  { name: 'SF-TITLE', width: 20 },
];

// WK-DEPT-NAME (line 8, col 26) is the resolved name box adjacent to SF-DEPT.
const DESC_MAP: Record<string, string> = { ...FIELD_DESC_MAP, 'SF-DEPT': 'WK-DEPT-NAME' };

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
  fieldDataMap: FIELD_DATA_MAP,
  fieldDescMap: DESC_MAP,
  pfOverrides: PF_OVERRIDES,
  isNumericField,
  typed,
});

// Local edit buffer per field — seeded from the backend default on activation
// (COBOL DISPLAY + ACCEPT semantics: displayed value is the editable initial content).
const values = ref<Record<string, string>>({});
for (const f of FIELDS) values.value[f.name] = '';
let prevFieldName = '';
watch(
  activeFieldName,
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

const { showToast, toastMsg, toastType, hideToast } = useToast(props.state);
</script>

<template>
  <div class="scr-page">
    <Header />
    <div class="scr-page-sticky-top">
      <div class="scr-header">
        <div class="scr-header-left">
        </div>
        <div class="scr-header-right"></div>
      </div>
    </div>

    <div class="scr-page-body">
      <div style="width: min(840px, 100%); margin: 0 auto">
        <div class="scr-form-section">
          <div class="scr-form-section-body">
            <div
              v-for="f in FIELDS"
              :key="f.name"
              class="scr-form-grid"
              style="margin-bottom: 8px"
            >
              <label class="scr-label" style="text-align: left">{{ LABELS[f.name] }}</label>
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
                  >{{ getValue(f.name) }}</span
                >
                <span
                  v-if="f.hasDesc"
                  class="scr-desc-box"
                  :style="{ width: '32ch', maxWidth: '100%' }"
                  >{{ descOf(f.name) }}</span
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="OK (Enter)" @click="onConfirm" />
      <Button variant="outline" size="md" label="Confirm (F4)" @click="submit('', '04')" />
      <Button variant="outline" size="md" label="Back (F3)" @click="submit('', '03')" />
    </div>

    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="hideToast" />
  </div>
</template>