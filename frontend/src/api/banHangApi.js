import apiClient from "./apiClient";
import { unwrap } from "../utils/unwrap";

// Báo giá bán hàng (GD/PGD/NVBANHANG)
const BG = "/bao-gia-ban-hang";
export const baoGiaApi = {
  danhSach: (params) => apiClient.get(BG, { params }).then(unwrap),
  danhMuc: () => apiClient.get(`${BG}/danh-muc`).then(unwrap),
  chiTiet: (id) => apiClient.get(`${BG}/${id}`).then(unwrap),
  tao: (payload) => apiClient.post(BG, payload).then(unwrap),
  capNhat: (id, payload) => apiClient.put(`${BG}/${id}`, payload).then(unwrap),
  gui: (id) => apiClient.post(`${BG}/${id}/gui`).then(unwrap),
  chapNhan: (id) => apiClient.post(`${BG}/${id}/chap-nhan`).then(unwrap),
  tuChoi: (id, lyDo) =>
    apiClient.post(`${BG}/${id}/tu-choi`, null, { params: { lyDo: lyDo || undefined } }).then(unwrap),
  // Trả về hóa đơn vừa lập (có id)
  chuyenHoaDon: (id, payload) => apiClient.post(`${BG}/${id}/chuyen-hoa-don`, payload ?? {}).then(unwrap),
};

// Bán phế liệu theo kg
const PL = "/ban-phe-lieu";
export const banPheLieuApi = {
  danhMuc: () => apiClient.get(`${PL}/danh-muc`).then(unwrap),
  ban: (payload) => apiClient.post(PL, payload).then(unwrap),
};
