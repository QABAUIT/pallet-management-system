/**
 * Ánh xạ giá trị trạng thái (đúng chuỗi lưu trong DB, xem CHECK constraint
 * trong schema) sang { label, color } để hiển thị bằng <Tag color={...}>{label}</Tag>.
 *
 * QUAN TRỌNG: key ở đây phải khớp 100% với giá trị cột trong DB
 * (ví dụ hoa_don.trang_thai). Nếu BE đổi giá trị enum, sửa ở đây theo.
 * Mỗi module tự thêm map riêng vào file này khi cần, đừng tạo rải rác
 * nhiều nơi để tránh lệch nhau.
 */

export const HOA_DON_TRANG_THAI = {
  nhap: { label: 'Nháp', color: 'default' },
  cho_duyet: { label: 'Chờ duyệt', color: 'gold' },
  da_thanh_toan: { label: 'Đã thanh toán', color: 'green' },
  qua_han: { label: 'Quá hạn', color: 'red' },
  da_huy: { label: 'Đã huỷ', color: 'default' },
};

export const PHIEU_KHO_TRANG_THAI = {
  cho_xu_ly: { label: 'Chờ xử lý', color: 'gold' },
  da_hoan_thanh: { label: 'Đã hoàn thành', color: 'green' },
  da_huy: { label: 'Đã huỷ', color: 'default' },
};

export const TRANG_THAI_HOAT_DONG = {
  hoat_dong: { label: 'Hoạt động', color: 'green' },
  ngung_hoat_dong: { label: 'Ngừng hoạt động', color: 'default' },
  dang_hop_tac: { label: 'Đang hợp tác', color: 'green' },
  ngung_hop_tac: { label: 'Ngừng hợp tác', color: 'default' },
  dang_kinh_doanh: { label: 'Đang kinh doanh', color: 'green' },
  ngung_kinh_doanh: { label: 'Ngừng kinh doanh', color: 'default' },
};

/** Fallback an toàn: nếu chưa khai báo map cho 1 giá trị, vẫn hiện được. */
export function resolveStatus(map, value) {
  return map[value] || { label: value ?? '-', color: 'default' };
}
