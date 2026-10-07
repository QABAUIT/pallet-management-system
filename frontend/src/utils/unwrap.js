/**
 * Các controller kho trả body thô (Page / DTO), còn các module khác (Supplier...)
 * trả ApiResponse { status, message, data }. Hàm này nhận cả hai dạng.
 */
export function unwrap(res) {
  if (res && typeof res === 'object' && !Array.isArray(res) && 'status' in res && 'data' in res) {
    return res.data
  }
  return res
}
