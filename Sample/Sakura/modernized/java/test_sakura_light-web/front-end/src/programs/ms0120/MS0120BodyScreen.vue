<script setup lang="ts">
import { ref, computed, watch, inject } from 'vue';
import Button from '../../components/ui/Button.vue';
import { TYPED_VALUES_KEY, type ScreenOverrideProps } from '../../types/screenOverride';
import { useScreenForm } from '../../composables/useScreenForm';
import AppToast from '../../components/AppToast.vue';
import Header from '../../components/layout/Header.vue';
import AppDialog from '../../components/AppDialog.vue';
import { FIELD_DATA_MAP, FIELD_DESC_MAP, NUMERIC_FIELDS, WIDTHS, PF_OVERRIDES } from './constant';

defineOptions({ name: 'MS0120BodyScreen' });

const props = defineProps<ScreenOverrideProps>();

// Display-only from-fields on this screen (WK-STAFF-NAME line 9, US-LAST-LOGIN
// line 18) — extend the shared input map so valueOf() can resolve them.
const DATA_MAP: Record<string, string> = {
  ...FIELD_DATA_MAP,
  'WK-STAFF-NAME': 'WK-STAFF-NAME',
  'US-LAST-LOGIN': 'US-LAST-LOGIN',
};

// One row per COBOL manifest line, in COBOL acceptance/display order.
// `literal` = verbatim screen literal; `display` marks read-only from-fields.
interface RowDef {
  name: string;
  literal: string;
  hint?: string;
  display?: boolean;
}
const ROWS: RowDef[] = [
  { name: 'US-LOGIN',       literal: 'Login ID      :' },
  { name: 'US-PASSWORD',    literal: 'Password      :' },
  { name: 'US-NAME',        literal: 'User Name     :' },
  { name: 'WK-STAFF-NAME',  literal: 'Staff Name    :', display: true },
  { name: 'US-ROLE',        literal: 'Role          :', hint: '1:admin 2:manager 3:clerk' },
  { name: 'US-AUTH-MASTER', literal: 'Auth Master   :', hint: '0:no 1:yes' },
  { name: 'US-AUTH-ORDER',  literal: 'Auth Order    :' },
  { name: 'US-AUTH-SALES',  literal: 'Auth Sales    :' },
  { name: 'US-AUTH-PURCH',  literal: 'Auth Purchase :' },
  { name: 'US-AUTH-CLOSE',  literal: 'Auth Close    :' },
  { name: 'US-LAST-LOGIN',  literal: 'Last Login    :', display: true },
];

const DISPLAY_WIDTHS: Record<string, number> = {
  'WK-STAFF-NAME': 30,
  'US-LAST-LOGIN': 10,
};

function widthOf(name: string): number {
  const w = (WIDTHS as Record<string, number>)[name] ?? DISPLAY_WIDTHS[name] ?? 12;
  // +3ch allowance for the input's own horizontal padding/borders so the
  // declared character count stays visible.
  return w + 3;
}

const typed = inject(TYPED_VALUES_KEY)!;

const {
  activeFieldName,
  isFieldActive,
  activeWidth,
  valueOf,
  submit,
  onKey,
  inputRef,
} = useScreenForm(props, {
  fieldDataMap: DATA_MAP,
  fieldDescMap: FIELD_DESC_MAP,
  pfOverrides: PF_OVERRIDES,
  picKeyLock: true,
  typed,
});

// Seed the editable buffer from the backend-displayed default when a field
// becomes active (COBOL DISPLAY-then-ACCEPT: Enter accepts the default).
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

// F3 sends '03' (per PF contract) — destructive, so route the keystroke
// through the confirmation dialog instead of submitting directly.
function onKeyGuard(e: KeyboardEvent) {
  if (e.key === 'F3') {
    e.preventDefault();
    e.stopPropagation();
    showConfirmDelete.value = true;
    return;
  }
  onKey(e);
}

// ─── Delete confirmation (aidKey '03' per PF BINDING CONTRACT) ───────────
const showConfirmDelete = ref(false);
function onDeleteClick() {
  showConfirmDelete.value = true;
}
function doDelete() {
  showConfirmDelete.value = false;
  submit('', '03');
}

// ─── Inline sub-region help message ──────────────────────────────────────
const subRegionHint = computed(
  () => (props.state as { subRegionMessage?: string }).subRegionMessage ?? '',
);

// ─── Toast wiring — DS-MESSAG status line ────────────────────────────────
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
      <div style="width: min(840px, 100%); margin: 0 auto;">
        <p
          v-if="subRegionHint"
          class="scr-hint"
          :style="{ background: '#fffbe6', border: '1px solid #ffe58f', padding: '6px 12px', margin: '0 0 10px', borderRadius: '4px', color: '#614700' }"
        >
          {{ subRegionHint }}
        </p>

        <div class="scr-form-section">
          <div class="scr-form-section-body">
            <div
              v-for="row in ROWS"
              :key="row.name"
              class="scr-form-grid"
              style="margin-bottom: 8px;"
            >
              <label class="scr-label" style="text-align: left;">{{ row.literal }}</label>
              <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                <template v-if="!row.display && isFieldActive(row.name)">
                  <input
                    ref="inputRef"
                    :class="['scr-input', { 'scr-numeric': NUMERIC_FIELDS.has(row.name) }]"
                    :value="editValue"
                    :maxlength="activeWidth"
                    :inputmode="NUMERIC_FIELDS.has(row.name) ? 'numeric' : undefined"
                    :style="{ width: widthOf(row.name) + 'ch', maxWidth: '100%' }"
                    :data-field-name="row.name"
                    data-active="true"
                    @input="onInput"
                    @keydown="onKeyGuard"
                  />
                </template>
                <template v-else>
                  <span
                    :class="['scr-value', { 'scr-numeric': NUMERIC_FIELDS.has(row.name) }]"
                    :style="{ width: widthOf(row.name) + 'ch', maxWidth: '100%', display: 'inline-block' }"
                  >{{ valueOf(row.name) }}</span>
                </template>
                <p v-if="row.hint" class="scr-hint" style="width: auto; margin: 0;">{{ row.hint }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="scr-page-footer">
      <Button variant="primary" size="md" label="Confirm (Enter)" @click="submit(editValue, '00')" />
      <Button variant="outline" size="md" label="Register (F4)" @click="submit('', '04')" />
      <Button variant="outline" size="md" label="Delete (F3)" @click="onDeleteClick" />
      <Button variant="outline" size="md" label="Back (F9)" @click="submit('', '09')" />
    </div>

    <AppDialog
      title="Delete"
      message="Delete this user? This operation cannot be undone."
      confirmLabel="Delete"
      confirmVariant="danger"
      :visible="showConfirmDelete"
      @confirm="doDelete"
      @cancel="showConfirmDelete = false"
    />

    <AppToast :visible="showToast" :message="toastMsg" :type="toastType" @hide="showToast = false" />
  </div>
</template>