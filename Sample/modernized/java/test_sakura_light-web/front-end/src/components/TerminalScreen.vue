<script lang="ts">
// Re-export shared injection key — override SFCs import from @/types/screenOverride.
// Must match the Symbol used in provide() below.
export type { ScreenOverrideProps, TypedValuesApi } from '@/types/screenOverride';
export { TYPED_VALUES_KEY } from '@/types/screenOverride';
</script>

<script setup lang="ts">
import { ref, reactive, computed, watch, provide, useTemplateRef, nextTick, type Component } from 'vue';
import { TYPED_VALUES_KEY, SCREEN_CONTEXT_KEY, type ScreenContext } from '@/types/screenOverride';
import { useTerminalSocket } from '../composables/useTerminalSocket';
import type { ScreenState } from '../composables/useTerminalSocket';
import { KEY_AID_MAP } from '../composables/aidCodes';
import InputField from './InputField.vue';
// PfKeyBar removed every accepted PFkey is now rendered
// as a per-screen footer button with an inline <kbd> badge. Window-level
// keyboard listener below still dispatches F1..F12 globally; the global UI
// bar is gone. Screens that lack a per-screen footer (login, dialog, mini)
// rely on keyboard input only — see TC-23 / TC-48.
import WindowModal from './WindowModal.vue';
import AlertDialog from './AlertDialog.vue';
import type { ScreenInputMsg, EndStatusInputMsg, FieldInputMsg } from '../types/screen';

import Button from './ui/Button.vue';

// A Vue ComponentType that may carry routing markers.
// isTransparentOverlay — render the component AS an overlay above the last
// non-transparent fullscreen (so the previous "main view" stays visible
// behind a dialog / mini-info popup).
// isTransientSetup    — declares the component is a setup/wizard step (e.g.
// login). It is rendered fullscreen, but is NEVER tracked as the backdrop
// for subsequent transparent overlays. Without this marker, a dialog that
// follows a login would render over the still-mounted login as backdrop
// instead of over the eventual main view (or the terminal).
type OverrideComponent = Component & {
  isTransparentOverlay?: boolean;
  isTransientSetup?: boolean;
};

const props = defineProps<{
  programId: string;
  screenOverrides?: Record<string, OverrideComponent>;
}>();

const emit = defineEmits<{ disconnect: []; 'auth-error': []; connected: [] }>();

// F9 is deliberately NOT special-cased here. It goes through KEY_AID_MAP like every
// other PF key (F9 → 'P9'), so the COBOL program runs its own exit path and the
// backend reports the finish — see the "Program finished" bar below. An earlier
// capture-phase handler swallowed F9 for a hardcoded program list and called
// window.close() instead; that skipped the program's termination logic, and
// window.close() is a no-op anyway since nothing here is opened via window.open().

const { state, sendMessage, updateBufferAt } = useTerminalSocket(() => props.programId);
// Blocking dialog: the BE program thread is parked on waitForInput(). Sending
// guideDisplayAck on OK/Esc unblocks it so it resumes at the next statement.
function ackGuideDisplay(): void {
  if (state.guideDisplay === null) return;
  state.guideDisplay = null;
  sendMessage({ type: 'guideDisplayAck' });
  // After dialog closes, restore focus to the currently active input.
  // AlertDialog captured focus when it opened; when it closes the browser
  // drops focus to body. Without this, F10 (登録) keydown goes to terminalRef
  // instead of the SFC input. Focus the live [data-active] element directly.
  nextTick(() => {
    const el = terminalRef.value?.querySelector<HTMLElement>('[data-active="true"]');
    el?.focus();
  });
}

// Propagate auth/connection failure to parent (App.vue shows the close-window dialog)
watch(
  () => state.error,
  (err) => {
    if (err) emit('auth-error');
  },
);

watch(
  () => state.connected,
  (val) => {
    if (val) emit('connected');
  },
);

const terminalRef = useTemplateRef<HTMLDivElement>('terminalRef');
const fieldValues = reactive<Record<string, string>>({});
let inputSubmitting = false;

// Cross-mount-durable form state. Lives at this parent so it survives the
// remount of <component :is="primaryOverride"> when overlays swap in/out.
// Override SFCs inject TYPED_VALUES_KEY and read/write via the API instead
// of owning local reactive maps.
const typedValuesMap = reactive<Record<string, string>>({});
const snapshotted = reactive<Record<string, string>>({});
provide(TYPED_VALUES_KEY, {
  values: typedValuesMap,
  snapshotted,
  setTyped(fieldName: string, value: string) {
    typedValuesMap[fieldName] = value;
  },
  snapshotRead(key: string, live: string) {
    if (snapshotted[key] !== undefined) return snapshotted[key];
    if (live && live.trim()) snapshotted[key] = live;
    return snapshotted[key] ?? live;
  },
});

// R9.2 — shared screen contract (both branches). Provided, NOT passed as props: delivered SFCs
// declare only the legacy props, and extra props would fall through onto their root DOM element.
// Getters keep it reactive without copying state.
provide(SCREEN_CONTEXT_KEY, {
  branch: 'web',
  get program() {
    return props.programId;
  },
  get screenName() {
    return state.currentScreenName;
  },
  // Layout lives on the BACKEND in this branch (the server paints frames; the SPA never reads
  // manifest.json) — see core/types/screenContract.ts.
  screen: null,
  get values() {
    return state.fieldsByName;
  },
  get message() {
    return state.statusMessage;
  },
  // No per-screen AID list at runtime here: the global PfKeyBar was removed and each SFC owns
  // its footer buttons (see the comment on the PfKeyBar import).
  buttons: [],
  send: (aid?: string) => submitAid(aid ?? '00'),
  // Cùng đường với phím thật: KEY_AID_MAP + guard SC-OK — không nhân bản luật nào.
  sendKey: (key: string) => handlePfKey(key),
  setValue: (name: string, value: string) => {
    setFieldValue(name, value);
    typedValuesMap[name] = value;
  },
  isActive: (name: string) =>
    state.waitingFor === 'field'
      ? state.activeField?.fieldName === name
      : state.waitingFor === 'screen' &&
        (state.screenFields ?? []).some((f) => f.fieldName === name),
  get native() {
    return state;
  },
} as ScreenContext);

// Last full-screen override — kept across transient transparent-overlay events.
const lastFullscreen = ref<OverrideComponent | null>(null);

// PF key dispatch uses KEY_AID_MAP from aidCodes.ts — single source of truth
// per NEC COBOL85 Pro Programming Guide Table 7-1. See aidCodes.ts for full
// reference + modifier combinations (handled by useScreenForm.windowKey).

function isAscii(ch: string): boolean {
  const cp = ch?.codePointAt(0) ?? 0;
  return cp >= 0x20 && cp <= 0x7e;
}
function isFullWidth(ch: string): boolean {
  const cp = ch?.codePointAt(0) ?? 0;
  return (
    (cp >= 0x1100 && cp <= 0x115f) ||
    (cp >= 0x2e80 && cp <= 0xa4cf) ||
    (cp >= 0xac00 && cp <= 0xd7af) ||
    (cp >= 0xf900 && cp <= 0xfaff) ||
    (cp >= 0xfe10 && cp <= 0xfe19) ||
    (cp >= 0xfe30 && cp <= 0xfe6f) ||
    (cp >= 0xff01 && cp <= 0xff60) ||
    (cp >= 0xffe0 && cp <= 0xffe6) ||
    (cp >= 0x20000 && cp <= 0x2fffd) ||
    (cp >= 0x30000 && cp <= 0x3fffd)
  );
}

// Submit a single field with optional aidKey.
function handleFieldSubmit(value: string, aidKey: string = '00') {
  if (inputSubmitting) return;
  inputSubmitting = true;
  sendMessage({ type: 'fieldInput', value, aidKey } as FieldInputMsg);
  if (state.activeField) {
    updateBufferAt(state.activeField.line, state.activeField.col, state.activeField.width, value);
  }
  setTimeout(() => {
    inputSubmitting = false;
  }, 200);
}

function handlePfKey(key: string) {
  if (inputSubmitting) return;
  if (key === 'Enter' && state.activeField?.fieldName === 'SC-OK') {
    return; // SC-OK is a submit button, not a PF key. Let the field handle it.
  }

  submitAid(KEY_AID_MAP[key] ?? '00');
}

/**
 * Submit an AID CODE that is already translated. Split out of handlePfKey (R9.2) so the shared
 * screen contract (core/types/screenContract.ts → ctx.send) lands on the EXACT same dispatch:
 * one code path for the keyboard, the per-screen footer buttons and modern SFCs.
 */
function submitAid(code: string) {
  if (state.waitingFor === 'screen') {
    sendMessage({
      type: 'screenInput',
      values: { ...fieldValues },
      aidKey: code,
    } as ScreenInputMsg);
  } else if (state.waitingFor === 'endStatus') {
    sendMessage({ type: 'endStatusInput', aidKey: code } as EndStatusInputMsg);
  } else if (state.waitingFor === 'field') {
    sendMessage({
      type: 'fieldInput',
      value: fieldValues[state.activeField?.fieldName ?? ''] ?? '',
      aidKey: code,
    } as FieldInputMsg);
  }
  for (const k of Object.keys(fieldValues)) delete fieldValues[k];
  setTimeout(() => {
    inputSubmitting = false;
  }, 200);
}

function handleKeyDown(e: KeyboardEvent) {
  // When a screen override is mounted it dispatches PF keys itself (useScreenForm
  // windowPfDispatch); the raw-terminal handler firing too would DOUBLE-send the
  // AID (develop fix — one keypress produced two fieldInput frames).
  if (screenOverrideActive.value) return;
  if (KEY_AID_MAP[e.key]) {
    e.preventDefault();
    handlePfKey(e.key);
  }
}

function setFieldValue(name: string, val: string) {
  fieldValues[name] = val;
}

// Terminal focus watcher moved below screenOverrideActive declaration (see line ~175).

// ── Routing ────────────────────────────────────────────────────────────────
// Two-tier override lookup: FIELD:<fieldName> first, then <screenName>.
const fieldKey = computed(() =>
  state.activeField?.fieldName ? `FIELD:${state.activeField.fieldName}` : null,
);
// Group-ACCEPT routing (acceptScreen): the server names the accepted fields in
// state.screenFields but sets NO activeField, and a region-paint screen
// (clearScreen=false, no window) never updates currentScreenName — so neither
// lookup tier fires and the override silently falls back to the raw terminal
// (DS-KEY entry dialogs). Mirror the acceptField FIELD:* routing over
// the screenFields set: the first member with a FIELD:* entry wins.
const screenFieldsKey = computed(() => {
  if (state.waitingFor !== 'screen') return null;
  for (const f of state.screenFields ?? []) {
    const name = typeof f === 'string' ? f : (f as { fieldName?: string })?.fieldName;
    if (name && props.screenOverrides?.[`FIELD:${name}`]) return `FIELD:${name}`;
  }
  return null;
});
const rawOverride = computed<OverrideComponent | undefined>(() => {
  // Field-key override always takes priority.
  if (fieldKey.value) {
    const fieldOverride = props.screenOverrides?.[fieldKey.value];
    if (fieldOverride) return fieldOverride;
    // Active field not found in field-key map. Only fall back to currentScreenName
    // when it maps to a PRIMARY (non-transparent) screen. Transparent overlays must
    // be triggered explicitly via FIELD:* entries — never via a stale currentScreenName
    // left over from a previous overlay interaction (e.g. DS-AREA-HENPIN lingering
    // after the Henpin modal was dismissed, causing unrelated fields to re-open it).
    const screenFallback = props.screenOverrides?.[state.currentScreenName];
    return screenFallback?.isTransparentOverlay ? undefined : screenFallback;
  }
  if (screenFieldsKey.value) return props.screenOverrides?.[screenFieldsKey.value];
  return props.screenOverrides?.[state.currentScreenName];
});
const isTransparentOverlay = computed(() => !!rawOverride.value?.isTransparentOverlay);

// Update lastFullscreen when a non-transparent, non-transient override
// becomes active. Transient setup screens (e.g. login) are deliberately
// excluded so dialogs that follow them don't render with the setup screen
// as a stale backdrop.
watch(
  [rawOverride, isTransparentOverlay],
  ([raw, transparent]) => {
    if (raw && !transparent && !(raw as OverrideComponent).isTransientSetup) {
      lastFullscreen.value = raw;
    }
  },
  { immediate: true },
);

// Edge case: transparent overlay opens before any fullscreen has been mounted.
// Render it once as primary; skip overlay slot so component doesn't render twice.
const hasFullscreenBg = computed(() => !!lastFullscreen.value);
const primaryOverride = computed<OverrideComponent | null>(() =>
  isTransparentOverlay.value
    ? lastFullscreen.value || rawOverride.value || null
    : rawOverride.value ?? lastFullscreen.value ?? null,
);
const overlayOverride = computed<OverrideComponent | null>(() =>
  isTransparentOverlay.value && hasFullscreenBg.value ? rawOverride.value || null : null,
);

const screenOverrideActive = computed(() => !!rawOverride.value);
const showTerminalBlanket = computed(() => !!props.screenOverrides && !primaryOverride.value && !overlayOverride.value);
const terminalClass = computed(
  () =>
    'terminal-shell' +
    (screenOverrideActive.value && !isTransparentOverlay.value ? ' override-active' : ''),
);

// Focus management.
// terminalRef catches PF keys when no input is focused. The previous version
// stole focus on every endStatus transition — including the brief endStatus
// that fires BETWEEN readEndStatus and the next acceptField (sub-second race).
// During that race, an override's <input> was about to receive focus from the
// override's own post-watcher, but the terminal-steal happened first → final
// focus landed on body when both watchers ran.
//
// Fix: defer to a macrotask (after Vue commit) AND only steal focus if no
// input has taken it naturally. If an override mounted an input and bound
// focus to it during the same tick, document.activeElement is already that
// input → leave it alone.
watch(
  () => [state.waitingFor, state.finished],
  () => {
    if (state.waitingFor === 'endStatus' || state.finished) {
      setTimeout(() => {
        // If backend followed endStatus immediately with acceptField/acceptScreen
        // (e.g. SC-OK → F4 → DS-AREA1 → acceptField SC-DENPYO), skip the
        // terminal-focus steal so the field's own post-flush watcher keeps control.
        if (state.waitingFor === 'field' || state.waitingFor === 'screen') return;
        const ae = document.activeElement;
        const focusedInputAlready = ae && (ae.tagName === 'INPUT' || ae.tagName === 'TEXTAREA');
        if (!focusedInputAlready) {
          terminalRef.value?.focus();
        }
      }, 30);
    }
  },
);

// State passed to primary slot — when transparent, blank out activeField/waitingFor
// so the background fullscreen doesn't react to popup-only events.
// use a Proxy instead of spread `{...state}` so the
// background SFC keeps DEEP reactivity on the real state. A spread would freeze
// statusMessage/messageNonce at overlay-open time and break
// AppToast/EI165 visibility for the primary screen during popup phases.
const overlayMask: Partial<ScreenState> = { activeField: null, waitingFor: 'none' };
const stateForPrimaryProxy = new Proxy(state, {
  get(target, prop, receiver) {
    // Mask ONLY when the transparent overlay actually renders ABOVE a
    // fullscreen background. In the entry-point edge case (transparent dialog
    // opens before any fullscreen mounted — see primaryOverride fallback), the
    // dialog itself IS the primary: masking would blank activeField/waitingFor
    // for the very component that needs them → isFieldActive() always false →
    // its input stuck readonly (every program whose first ACCEPT is a small
    // key-entry dialog, e.g. DS-KEY screens).
    if (isTransparentOverlay.value && hasFullscreenBg.value && prop in overlayMask) {
      return (overlayMask as Record<string | symbol, unknown>)[prop as string];
    }
    return Reflect.get(target, prop, receiver);
  },
}) as ScreenState;
const stateForPrimary = computed<ScreenState>(() => {
  // Touch isTransparentOverlay so callers re-render when the overlay flips,
  // but always hand back the same Proxy so deep reactivity is preserved.
  void isTransparentOverlay.value;
  return stateForPrimaryProxy;
});

// Render rows lazily — Vue's reactivity will re-evaluate when buffers change.
interface RenderSpan {
  key: string;
  text: string;
  classes: string;
  fullWidth: boolean;
  cell: boolean;
}
interface RenderRow {
  key: number;
  spans: RenderSpan[];
}

const renderRows = computed<RenderRow[]>(() => {
  const rows: RenderRow[] = [];
  for (let r = 0; r < 24; r++) {
    const spans: RenderSpan[] = [];
    let i = 0;
    while (i < 80) {
      const ch = state.charBuffer[r][i];
      const color = state.colorBuffer[r][i];
      const attr = state.attrBuffer[r][i];
      const classes = [color?.toLowerCase(), attr].filter(Boolean).join(' ');
      if (isFullWidth(ch)) {
        spans.push({
          key: `${r}-${i}`,
          text: ch,
          classes: `cell-fw ${classes}`.trim(),
          fullWidth: true,
          cell: false,
        });
        i += 2;
      } else if (!isAscii(ch)) {
        const display = ch === '​' ? ' ' : ch;
        spans.push({
          key: `${r}-${i}`,
          text: display,
          classes: `cell ${classes}`.trim(),
          fullWidth: false,
          cell: true,
        });
        i++;
      } else {
        let j = i + 1;
        while (
          j < 80 &&
          state.colorBuffer[r][j] === color &&
          state.attrBuffer[r][j] === attr &&
          isAscii(state.charBuffer[r][j])
        ) {
          j++;
        }
        const text = state.charBuffer[r].slice(i, j).join('');
        spans.push({ key: `${r}-${i}`, text, classes, fullWidth: false, cell: false });
        i = j;
      }
    }
    rows.push({ key: r, spans });
  }
  return rows;
});
</script>

<template>
  <div v-if="state.error" class="error-screen">
    <p>Error: {{ state.error }}</p>
    <button @click="emit('disconnect')">Back</button>
  </div>
  <div v-else-if="!state.connected && !state.finished" class="connecting">
    Connecting to {{ programId }}...
  </div>
  <div v-else ref="terminalRef" :class="terminalClass" tabindex="0" @keydown="handleKeyDown">
    <div class="screen-grid">
      <!-- Skip terminal render when a full-screen modern component replaces it.
           Transparent overlays still need the terminal behind them. -->
      <template v-if="!screenOverrideActive || isTransparentOverlay">
        <div v-for="row in renderRows" :key="row.key" class="screen-row">
          <span v-for="span in row.spans" :key="span.key" :class="span.classes || undefined">{{
            span.text
          }}</span>
        </div>
      </template>

      <template v-if="!screenOverrideActive">
        <!-- Input fields overlay -->
        <InputField
          v-if="state.waitingFor === 'field' && state.activeField"
          :key="state.activeField.fieldName"
          :field="state.activeField"
          @submit="handleFieldSubmit"
          @value-change="(val: string) => setFieldValue(state.activeField!.fieldName, val)"
        />

        <InputField
          v-for="(sf, idx) in state.waitingFor === 'screen' ? state.screenFields : []"
          :key="sf.fieldName"
          :field="{
            fieldName: sf.fieldName,
            line: sf.line ?? 1,
            col: sf.col ?? 1,
            width: sf.width ?? 10,
            picture: sf.picture,
          }"
          :auto-focus="idx === 0"
          :autoskip="true"
          @value-change="(val: string) => setFieldValue(sf.fieldName, val)"
        />

        <WindowModal v-if="state.activeWindow" :window="state.activeWindow" />
      </template>
    </div>

    <!-- Blanket: raw terminal hidden when no modern component covers it -->
    <div v-if="showTerminalBlanket" class="terminal-blanket"></div>

    <!-- PRIMARY slot — stable across transient transparent-overlay transitions. -->
    <component
      v-if="primaryOverride"
      :is="primaryOverride"
      :state="stateForPrimary"
      :send-message="sendMessage"
      :update-buffer-at="updateBufferAt"
    />

    <!-- OVERLAY slot — transparent overlays render on top of the primary. -->
    <component
      v-if="overlayOverride"
      :is="overlayOverride"
      :state="state"
      :send-message="sendMessage"
      :update-buffer-at="updateBufferAt"
    />

    <!-- Status bar -->
    <!-- <div class="status-bar">
      <span v-if="state.statusMessage" class="status-message">{{ state.statusMessage }}</span>
    </div> -->

    <!-- PfKeyBar removed . Per-screen SFC footer now owns
         every accepted PFkey as a visible button with an inline <kbd> badge.
         Keys F1/F6/F12 (rarely surfaced per-screen) remain dispatchable via
         the window-level keyboard listener — see TC-23 / TC-48. -->

    <div v-if="state.finished" class="finished-bar">
      <span>Program finished</span>
      <button @click="emit('disconnect')">Back</button>
    </div>
  </div>

  <!-- Guide Display Window — COBOL STOP 'literal' / DISPLAY ... UPON GDD (blocking dialog).
       BE is parked on waitForInput(); only OK / Esc send guideDisplayAck to resume (no backdrop-close). -->
  <AlertDialog :text="state.guideDisplay" @ok="ackGuideDisplay" />
</template>

