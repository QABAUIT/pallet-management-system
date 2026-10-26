// Trạng thái báo giá (khớp quy tắc ở BE: nhap -> da_gui -> da_chap_nhan | tu_choi; quá hạn -> het_han)
export const BAO_GIA_TRANG_THAI = {
  nhap: { label: "Nháp", color: "default" },
  da_gui: { label: "Đã gửi khách", color: "blue" },
  da_chap_nhan: { label: "Khách chấp nhận", color: "green" },
  tu_choi: { label: "Khách từ chối", color: "red" },
  het_han: { label: "Hết hạn", color: "orange" },
};

export const DEFAULT_VAT = 8;
export const DEFAULT_HIEU_LUC = 15;
