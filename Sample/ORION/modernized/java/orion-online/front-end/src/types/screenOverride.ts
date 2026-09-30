// Runtime contract cho SFC modern override (per-program). BMS-native: dữ liệu + submit lấy từ
// ScreenView (đã nói /api/terminal/execute), KHÔNG phụ thuộc runtime SCREEN-SECTION của tool gốc.
import type { InjectionKey, Ref } from 'vue'

// R9.2 — hợp đồng CHUNG cho cả hai nhánh (nguồn duy nhất: resources/frontend/core/).
// SFC mới nên dùng useScreenContext(); props bên dưới GIỮ NGUYÊN cho SFC đã bàn giao (carddemo).
export type { ScreenContext, ScreenLayout, ScreenField, AidButton } from './screenContract'
export { SCREEN_CONTEXT_KEY, useScreenContext } from './screenContract'

/** Props mà một override-SFC nhận từ ScreenView. */
export interface ScreenOverrideProps {
  program: string
  screenName: string
  screen: any                                   // manifest screen: {rows,cols,fields[],recordLists?}
  values: Record<string, string>                // giá trị field hiện tại (2 chiều với ScreenView)
  buttons: { aidKey: string; label: string }[]  // PF/AID bar
  message: string
  send: (aid?: string) => void                  // submit AID (ENTER/PF3…) qua client.ts
}

/** Inject key cho values (component con sâu dùng chung, khỏi prop-drill). */
export const TYPED_VALUES_KEY: InjectionKey<Ref<Record<string, string>>> = Symbol('screenTypedValues')

/** Registry screenName(UPPER) → Vue component. Rỗng = mọi màn dùng ScreenView grid faithful. */
export type ScreenOverrides = Record<string, unknown>
