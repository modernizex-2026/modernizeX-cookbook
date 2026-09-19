import type { InjectionKey, Reactive } from 'vue';
import type { ScreenState } from '../composables/useTerminalSocket';

// R9.2 — hợp đồng CHUNG cho cả hai nhánh (nguồn duy nhất: resources/frontend/core/).
// SFC mới nên dùng useScreenContext(); mọi thứ bên dưới GIỮ NGUYÊN để SFC đã bàn giao
// cho khách tiếp tục biên dịch và chạy y như cũ.
export type { ScreenContext, ScreenLayout, ScreenField, AidButton } from './screenContract';
export { SCREEN_CONTEXT_KEY, useScreenContext } from './screenContract';

/** Props passed to every per-program screen override component. */
export interface ScreenOverrideProps {
  state: ScreenState;
  sendMessage: (msg: object) => void;
  updateBufferAt: (line: number, col: number, width: number, value: string) => void;
}

/** Provide/inject API for cross-mount-durable form state in override SFCs. */
export interface TypedValuesApi {
  values: Reactive<Record<string, string>>;
  snapshotted: Reactive<Record<string, string>>;
  setTyped: (fieldName: string, value: string) => void;
  snapshotRead: (key: string, live: string) => string;
}

export const TYPED_VALUES_KEY: InjectionKey<TypedValuesApi> = Symbol('screenTypedValues');
