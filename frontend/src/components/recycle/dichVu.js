/**
 * Giai đoạn của phiếu dịch vụ tháo dỡ cho khách, quy từ trang_thai thật của BE:
 * da_tiep_nhan -> cho_duyet -> da_thao_do -> da_tra_hang (hoặc da_huy).
 */
export const DICH_VU_GIAI_DOAN = {
  da_tiep_nhan: { label: "Chờ tháo dỡ", color: "blue" },
  cho_duyet: { label: "Chờ duyệt kết quả", color: "gold" },
  da_duyet: { label: "Đã duyệt, chờ trả hàng", color: "cyan" },
  da_tra: { label: "Đã trả hàng", color: "green" },
  da_huy: { label: "Đã huỷ", color: "default" },
};

const GIAI_DOAN_THEO_TRANG_THAI = {
  da_tiep_nhan: "da_tiep_nhan",
  cho_duyet: "cho_duyet",
  da_thao_do: "da_duyet",
  da_tra_hang: "da_tra",
  da_huy: "da_huy",
};

export function giaiDoan(r) {
  return GIAI_DOAN_THEO_TRANG_THAI[r.trangThai] ?? "da_tiep_nhan";
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
