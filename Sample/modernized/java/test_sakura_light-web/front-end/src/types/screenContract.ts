/**
 * screenContract.ts — hợp đồng CHUNG giữa "host màn hình" và SFC override, dùng cho CẢ HAI nhánh
 * (R9.2). Nguồn duy nhất: resources/frontend/core/ — mọi SPA sinh ra đều nhận file này y hệt.
 *
 * Vì sao cần: trước R9, mỗi nhánh có một `ScreenOverrideProps` riêng dù cùng tên file, cùng tên
 * symbol — SFC viết cho nhánh này không đọc nổi dữ liệu của nhánh kia, và mọi helper dùng chung
 * đều phải viết hai lần.
 *
 *   - web (SCREEN SECTION / WebSocket): server ĐẨY frame đã vẽ; host là TerminalScreen.vue
 *   - online (BMS / REST):              client KÉO giá trị theo tên field; host là ScreenView.vue
 *
 * Hai mô hình hội thoại đó KHÔNG hợp nhất được (xem plan §3.6), nhưng phần dữ liệu mà một màn hình
 * hiện đại thật sự cần thì giống nhau: tên chương trình/màn hình, map field-name → value, thông
 * điệp, danh sách phím AID, và một hàm submit. Đó chính là {@link ScreenContext}.
 *
 * TƯƠNG THÍCH NGƯỢC (bắt buộc): props cũ của từng nhánh GIỮ NGUYÊN — SFC đã bàn giao cho khách
 * tiếp tục chạy không sửa một dòng. Contract chung được cấp thêm qua provide/inject
 * nên KHÔNG có prop lạ rơi xuống DOM của component cũ (fallthrough attrs).
 *
 * Dùng trong SFC mới:
 *
 *   const ctx = useScreenContext()
 *   const soTien = computed(() => ctx.values['AMT'] ?? '')
 *   function ok() { ctx.send('ENTER') }     // online: 'ENTER'/'PF3' · web: mã AID NEC ('00'/'P3')
 */
import { inject, type InjectionKey } from 'vue'

/** Một ô trên lưới terminal — toạ độ 1-based như COBOL/BMS. */
export interface ScreenField {
  name: string
  row: number
  col: number
  length: number
  /** true = ô nhập liệu (unprotected). */
  input: boolean
  picture?: string | null
  initial?: string | null
}

/**
 * Bố cục một màn hình. Online lấy từ `public/manifest/<prog>.json`; web trả `null` vì layout do
 * BACKEND giữ (FE chỉ nhận frame đã vẽ) — SFC web đọc dữ liệu qua `values`, không qua toạ độ.
 * Khoá thừa của manifest (recordLists, clearScreen…) giữ nguyên qua index signature.
 */
export interface ScreenLayout {
  rows: number
  cols: number
  fields: ScreenField[]
  inputFields?: string[]
  [key: string]: unknown
}

/** Một phím AID hiển thị trên thanh chức năng. */
export interface AidButton {
  aidKey: string
  label: string
}

/** Phần GIAO của hai nhánh — mọi SFC hiện đại chỉ cần từng này để chạy được ở cả hai bên. */
export interface ScreenContext {
  /** Nhánh đang chạy — chỉ dùng khi SFC buộc phải phân biệt (hạn chế tối đa). */
  branch: 'web' | 'online'
  program: string
  screenName: string
  /** Bố cục nếu FE sở hữu layout (online); `null` ở web. */
  screen: ScreenLayout | null
  /** field-name → giá trị hiển thị hiện tại (2 chiều với host). */
  values: Record<string, string>
  message: string
  buttons: AidButton[]
  /**
   * Gửi một AID THÔ của nhánh (web: '00'/'P3'/'04'… theo NEC Table 7-1; online: 'ENTER'/'PF3'…
   * theo CICS). Hai từ vựng này KHÔNG quy đổi cho nhau được nếu không bịa ngữ nghĩa, nên SFC
   * muốn chạy được cả hai bên hãy dùng {@link ScreenContext.sendKey}.
   */
  send: (aid?: string) => void
  /**
   * Gửi theo PHÍM BÀN PHÍM ('Enter', 'F3', 'Escape'…) — từ vựng CHUNG thật sự: mỗi nhánh tự
   * tra bảng phím→AID sẵn có của mình. Đây là cách viết một SFC chạy được ở cả web lẫn online.
   * Phím không có trong bảng của nhánh thì không gửi gì (no-op).
   */
  sendKey: (key: string) => void
  /** Ghi giá trị người dùng nhập cho một field. */
  setValue: (name: string, value: string) => void
  /** Field này có đang nhận nhập liệu không (web: đang ACCEPT; online: ô unprotected). */
  isActive: (name: string) => boolean
  /** Trạng thái thô của nhánh (web: ScreenState; online: ScreenData) — lối thoát khi cần đặc thù. */
  native: unknown
}

export const SCREEN_CONTEXT_KEY: InjectionKey<ScreenContext> = Symbol('screenOverride')

/** Lấy contract chung trong `setup()` của SFC override. Ném lỗi rõ ràng nếu host chưa provide. */
export function useScreenContext(): ScreenContext {
  const ctx = inject(SCREEN_CONTEXT_KEY, null)
  if (!ctx) {
    throw new Error(
      'useScreenContext(): không tìm thấy SCREEN_CONTEXT_KEY — SFC phải được mount bên trong ' +
        'host màn hình (TerminalScreen.vue ở nhánh web, ScreenView.vue ở nhánh online).',
    )
  }
  return ctx
}
