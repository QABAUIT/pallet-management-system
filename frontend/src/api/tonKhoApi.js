import apiClient from "./apiClient";
import { unwrap } from "../utils/unwrap";

const BASE = "/ton-kho";

export const tonKhoApi = {
  thongKe: () => apiClient.get(`${BASE}/thong-ke`).then(unwrap),
  danhSach: (params) => apiClient.get(BASE, { params }).then(unwrap),
  xuatExcel: (params) =>
    apiClient.get(`${BASE}/export`, { params, responseType: "blob" }),

  // API dùng chung lấy danh sách Kho và Vị trí của Kho đó
  layDanhSachKho: () => apiClient.get(`${BASE}/kho`).then(unwrap),
  layDanhSachViTri: (khoId) =>
    apiClient.get(`${BASE}/vi-tri/kho/${khoId}`).then(unwrap),
};
