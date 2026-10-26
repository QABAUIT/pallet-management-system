import apiClient from "./apiClient";
import { unwrap } from "../utils/unwrap";

/** CRUD + duyệt/huỷ dùng chung cho 3 loại phiếu tái chế (cùng khuôn endpoint ở BE). */
const phieuApi = (base) => ({
  danhSach: (params) => apiClient.get(base, { params }).then(unwrap),
  chiTiet: (id) => apiClient.get(`${base}/${id}`).then(unwrap),
  tao: (payload) => apiClient.post(base, payload).then(unwrap),
  capNhat: (id, payload) =>
    apiClient.put(`${base}/${id}`, payload).then(unwrap),
  duyet: (id) => apiClient.post(`${base}/${id}/duyet`).then(unwrap),
  huy: (id, lyDo) =>
    apiClient
      .post(`${base}/${id}/huy`, null, { params: { lyDo: lyDo || undefined } })
      .then(unwrap),
});

// Phân loại lô (phiếu tái chế)
export const taiCheApi = {
  ...phieuApi("/tai-che"),
  danhMuc: () => apiClient.get("/tai-che/danh-muc").then(unwrap),
};

// Tháo dỡ theo đợt
export const thaoDoApi = {
  ...phieuApi("/thao-do"),
  danhMuc: () => apiClient.get("/thao-do/danh-muc").then(unwrap),
};

// Xử lý hàng cần xử lý thêm (thanh / cục)
export const xuLyThemApi = {
  ...phieuApi("/xu-ly-them"),
  ton: () => apiClient.get("/xu-ly-them/ton").then(unwrap),
};

// Đơn mua
const DON_MUA = "/don-mua";
export const donMuaApi = {
  danhSach: (params) => apiClient.get(DON_MUA, { params }).then(unwrap),
  danhMuc: () => apiClient.get(`${DON_MUA}/danh-muc`).then(unwrap),
  chiTiet: (id) => apiClient.get(`${DON_MUA}/${id}`).then(unwrap),
  tao: (payload) => apiClient.post(DON_MUA, payload).then(unwrap),
  capNhat: (id, payload) =>
    apiClient.put(`${DON_MUA}/${id}`, payload).then(unwrap),
  xacNhan: (id) => apiClient.post(`${DON_MUA}/${id}/xac-nhan`).then(unwrap),
  nhanHang: (id, payload) =>
    apiClient.post(`${DON_MUA}/${id}/nhan-hang`, payload).then(unwrap),
  huy: (id, lyDo) =>
    apiClient
      .post(`${DON_MUA}/${id}/huy`, null, { params: { lyDo: lyDo || undefined } })
      .then(unwrap),
};

// Đóng pallet thành phẩm theo định mức
export const dongPalletApi = {
  ...phieuApi("/dong-pallet"),
  danhMuc: () => apiClient.get("/dong-pallet/danh-muc").then(unwrap),
  // Xem trước nhu cầu / khả dụng / thiếu của từng vật tư
  duKien: (params) => apiClient.get("/dong-pallet/du-kien", { params }).then(unwrap),
};

// Định mức vật tư (BOM) cho từng thành phẩm
const DINH_MUC = "/dinh-muc";
export const dinhMucApi = {
  danhSach: (chiHoatDong = false) =>
    apiClient.get(DINH_MUC, { params: { chiHoatDong } }).then(unwrap),
  chiTiet: (id) => apiClient.get(`${DINH_MUC}/${id}`).then(unwrap),
  tao: (payload) => apiClient.post(DINH_MUC, payload).then(unwrap),
  capNhat: (id, payload) => apiClient.put(`${DINH_MUC}/${id}`, payload).then(unwrap),
  ngung: (id) => apiClient.post(`${DINH_MUC}/${id}/ngung`).then(unwrap),
  kichHoat: (id) => apiClient.post(`${DINH_MUC}/${id}/kich-hoat`).then(unwrap),
};

// Dịch vụ tháo dỡ pallet cho khách (hàng của khách tách riêng khỏi kho công ty)
const DV = "/dich-vu-thao-do";
export const dichVuApi = {
  danhSach: (params) => apiClient.get(DV, { params }).then(unwrap),
  chiTiet: (id) => apiClient.get(`${DV}/${id}`).then(unwrap),
  tonKhach: (khachHangId) =>
    apiClient.get(`${DV}/ton-khach`, { params: { khachHangId: khachHangId || undefined } }).then(unwrap),
  tiepNhan: (payload) => apiClient.post(DV, payload).then(unwrap),
  ketQua: (id, payload) => apiClient.put(`${DV}/${id}/ket-qua`, payload).then(unwrap),
  duyet: (id) => apiClient.post(`${DV}/${id}/duyet`).then(unwrap),
  traHang: (id) => apiClient.post(`${DV}/${id}/tra-hang`).then(unwrap),
  huy: (id, lyDo) =>
    apiClient.post(`${DV}/${id}/huy`, null, { params: { lyDo: lyDo || undefined } }).then(unwrap),
  lapHoaDon: (id, payload) => apiClient.post(`${DV}/${id}/lap-hoa-don`, payload ?? {}).then(unwrap),
  muaLaiPheLieu: (id, payload) => apiClient.post(`${DV}/${id}/mua-lai-phe-lieu`, payload).then(unwrap),
};
