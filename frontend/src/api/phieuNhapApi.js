import apiClient from './apiClient'
import { unwrap } from '../utils/unwrap'

const BASE = '/phieu-nhap'

export const phieuNhapApi = {
  thongKe: () => apiClient.get(`${BASE}/thong-ke`).then(unwrap),
  // params: keyword, trangThai, tuNgay, denNgay, page, size
  danhSach: (params) => apiClient.get(BASE, { params }).then(unwrap),
  chiTiet: (id) => apiClient.get(`${BASE}/${id}`).then(unwrap),
  tao: (payload) => apiClient.post(BASE, payload).then(unwrap),
  // Trả về Blob (apiClient đã giải nén response.data)
  xuatExcel: (params) => apiClient.get(`${BASE}/export`, { params, responseType: 'blob' }),
}
