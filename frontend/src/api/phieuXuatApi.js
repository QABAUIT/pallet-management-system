import apiClient from './apiClient'
import { unwrap } from '../utils/unwrap'

const BASE = '/phieu-xuat'

export const phieuXuatApi = {
  thongKe: () => apiClient.get(`${BASE}/thong-ke`).then(unwrap),
  danhSach: (params) => apiClient.get(BASE, { params }).then(unwrap),
  chiTiet: (id) => apiClient.get(`${BASE}/${id}`).then(unwrap),
  tao: (payload) => apiClient.post(BASE, payload).then(unwrap),
  xuatExcel: (params) => apiClient.get(`${BASE}/export`, { params, responseType: 'blob' }),
}
