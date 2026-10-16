import apiClient from "./apiClient";
import { unwrap } from "../utils/unwrap";

const BASE = "/phieu-nhap";

export const phieuNhapApi = {
  thongKe: () => apiClient.get(`${BASE}/thong-ke`).then(unwrap),

  danhSach: (params) => apiClient.get(BASE, { params }).then(unwrap),

  chiTiet: (id) => apiClient.get(`${BASE}/${id}`).then(unwrap),

  tao: (payload) => apiClient.post(BASE, payload).then(unwrap),

  capNhat: (id, payload) =>
    apiClient.put(`${BASE}/${id}`, payload).then(unwrap),

  xuatExcel: (params) =>
    apiClient.get(`${BASE}/export`, { params, responseType: "blob" }),

  // Cập nhật thêm API xếp vị trí
  xepViTri: (id, payload) =>
    apiClient.post(`${BASE}/${id}/xep-vi-tri`, payload).then(unwrap),
};
