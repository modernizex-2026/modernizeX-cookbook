/**
 * debugLog.ts — cờ gỡ lỗi phía FE, dùng CHUNG hai nhánh (R9 / B7).
 *
 * Trước đây mỗi nhánh tự đọc `localStorage.cobol_debug`: nhánh web trong `useTerminalSocket`,
 * nhánh online trong `api/client.ts`. Cùng một quy ước, hai chỗ sửa. Gom về đây.
 *
 * Bật: `localStorage.cobol_debug = '1'` trong console trình duyệt. Tắt (mặc định): 0 chi phí.
 *
 * GIỮ NGUYÊN ngữ nghĩa cũ của TỪNG nhánh — cố ý:
 *   - {@link DEBUG_ENABLED} tính MỘT LẦN lúc nạp module (đúng như nhánh web đang làm),
 *     dùng cho log tần suất cao trong vòng nhận frame.
 *   - {@link isDebugEnabled} đọc lại MỖI LẦN gọi (đúng như nhánh online đang làm),
 *     nên bật/tắt giữa chừng có tác dụng ngay.
 * Không hợp nhất hai ngữ nghĩa này thành một: sẽ làm đổi hành vi log của ít nhất một bên.
 */

function readFlag(): boolean {
  try {
    return typeof window !== 'undefined' && window.localStorage?.getItem('cobol_debug') === '1';
  } catch {
    return false; // SSR / môi trường test không có localStorage
  }
}

/** Trạng thái cờ tại thời điểm nạp module (nhánh web dựa vào đây). */
export const DEBUG_ENABLED: boolean = readFlag();

/** Đọc cờ tại thời điểm gọi (nhánh online dựa vào đây). */
export function isDebugEnabled(): boolean {
  return readFlag();
}

/**
 * Log chẩn đoán một dòng, định dạng khớp log SLF4J phía backend:
 *   `[COBOL-CATEGORY] key1=v1 key2=v2 …`
 */
export function dbg(category: string, fields: Record<string, unknown>): void {
  if (!DEBUG_ENABLED) return;
  const parts: string[] = [];
  for (const k in fields) {
    const v = fields[k];
    parts.push(`${k}=${typeof v === 'object' ? JSON.stringify(v) : v}`);
  }
  // eslint-disable-next-line no-console
  console.log(`[COBOL-${category}]`, ...parts);
}
