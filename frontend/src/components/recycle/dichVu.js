/**
 * Giai đoạn của phiếu dịch vụ tháo dỡ cho khách. Tính từ dữ liệu phiếu (thay vì phụ thuộc
 * tên cột trang_thai trong DB) nên luôn khớp với cách BE ghi nhận từng bước.
 */
export const DICH_VU_GIAI_DOAN = {
  da_tiep_nhan: { label: "Chờ tháo dỡ", color: "blue" },
  cho_duyet: { label: "Chờ duyệt kết quả", color: "gold" },
  da_duyet: { label: "Đã duyệt, chờ trả hàng", color: "cyan" },
  da_tra: { label: "Đã trả hàng", color: "green" },
  da_huy: { label: "Đã huỷ", color: "default" },
};

export function giaiDoan(r) {
  if (r.lyDoHuy || String(r.trangThai ?? "").includes("huy")) return "da_huy";
  if (r.thoiGianTraHang) return "da_tra";
  if (r.thoiGianDuyet) return "da_duyet";
  if (r.soPalletThaoDo > 0) return "cho_duyet";
  return "da_tiep_nhan";
}

/** Dùng cho useLocalList: đưa giai đoạn vào trường trangThai để bộ lọc dùng chung chạy được. */
export const gomGiaiDoan = (list) =>
  (list ?? []).map((r) => ({ ...r, trangThaiGoc: r.trangThai, trangThai: giaiDoan(r) }));

export const PHUONG_THUC_TT = [
  { value: "tien_mat", label: "Tiền mặt" },
  { value: "chuyen_khoan", label: "Chuyển khoản" },
  { value: "qr_code", label: "QR code" },
];

export const vndInput = {
  formatter: (v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, "."),
  parser: (v) => v?.replace(/\./g, ""),
};
