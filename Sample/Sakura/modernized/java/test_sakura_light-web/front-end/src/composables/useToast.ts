import { ref, watch } from 'vue';
import type { ScreenState } from './useTerminalSocket';

export type ToastType = 'info' | 'success' | 'warning' | 'error';

/**
 * Watches statusMessage + messageNonce on ScreenState and exposes reactive
 * toast state. messageCode starting with 'E' → error; Japanese success
 * keywords → success; otherwise info.
 */
export function useToast(state: ScreenState) {
  const showToast = ref(false);
  const toastMsg = ref('');
  const toastType = ref<ToastType>('info');

  watch(
    () => [state.statusMessage, state.messageNonce] as const,
    ([msg]) => {
      // Guide messages (code prefix "GF", emitted before each input
      // field) are redundant with the function-key hints shown at the bottom of
      // the screen, so suppress them across all programs. Error messages (E*)
      // still show.
      if (msg && !state.messageCode.startsWith('GF')) {
        toastMsg.value = String(msg);
        toastType.value = state.messageCode.startsWith('E')
          ? 'error'
          : /完了|成功|登録しました/.test(msg)
            ? 'success'
            : 'info';
        showToast.value = true;
      }
    },
    { immediate: true },
  );

  function hideToast() {
    showToast.value = false;
  }

  return { showToast, toastMsg, toastType, hideToast };
}
