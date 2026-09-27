package com.pallet.backend.exception;

/**
 * Ném ra khi thao tác vi phạm quy tắc nghiệp vụ đã thống nhất trong thiết kế database, ví dụ:
 * - Xóa cứng nhân viên/NCC/khách hàng/mặt hàng đã có phát sinh giao dịch (DB đã có trigger chặn,
 *   exception này dùng khi muốn chặn SỚM ở tầng service, trả message rõ ràng hơn thay vì
 *   để lỗi SQL thô của trigger bắn ra tận controller).
 * - Sửa/hủy hóa đơn đã chốt sổ (da_chot_so = true) mà không qua quy trình mở khóa thủ công.
 * - Số lượng bán vượt quá tồn kho khả dụng (so_luong_ton_kho - so_luong_da_giu_cho).
 * Map sang HTTP 409 Conflict.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
