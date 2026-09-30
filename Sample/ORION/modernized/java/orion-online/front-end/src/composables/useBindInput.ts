/**
 * Factory composable for focus-binding helpers used with Vue's :ref on UI wrapper
 * components. Unwraps Vue component instances or raw Elements down to a native
 * <input> before forwarding to useScreenForm's bindActiveInput.
 *
 * @param bindActiveInput - The bindActiveInput function returned by useScreenForm.
 * @returns bindFormInput — for Input/generic wrapper refs; bindDateInput — for DateInput refs.
 */
export function useBindInput(bindActiveInput: (el: Element | object | null) => void) {
  /** Unwraps a Vue component ref or raw element to its native <input> and forwards to bindActiveInput. */
  function bindFormInput(el: Element | object | null): void {
    if (!el) {
      bindActiveInput(null);
      return;
    }

    if (el instanceof HTMLInputElement) {
      bindActiveInput(el);
      return;
    }

    const root = (el as { $el?: unknown }).$el;
    if (root instanceof HTMLInputElement) {
      bindActiveInput(root);
      return;
    }

    if (root instanceof HTMLElement) {
      const nested = root.querySelector('input');
      if (nested instanceof HTMLInputElement) {
        bindActiveInput(nested);
        return;
      }
    }
    bindActiveInput(null);
  }

  /** Like bindFormInput but targets the text sub-input inside DateInput (.ui-date-input__text). */
  function bindDateInput(el: Element | object | null): void {
    if (!el) {
      bindActiveInput(null);
      return;
    }

    const root = (el as { $el?: unknown }).$el;
    if (root instanceof HTMLElement) {
      const text = root.querySelector('.ui-date-input__text');
      if (text instanceof HTMLInputElement) {
        bindActiveInput(text);
        return;
      }
    }
    bindFormInput(el);
  }

  return { bindFormInput, bindDateInput };
}
