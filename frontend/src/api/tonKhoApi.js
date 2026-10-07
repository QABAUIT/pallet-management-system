import apiClient from './apiClient'
import { unwrap } from '../utils/unwrap'

const BASE = '/ton-kho'

export const tonKhoApi = {
  thongKe: () => apiClient.get(`${BASE}/thong-ke`).then(unwrap),
  // params: keyword, chatLieu (go | nhua | sat | khac), page, size
  danhSach: (params) => apiClient.get(BASE, { params }).then(unwrap),
  xuatExcel: (params) => apiClient.get(`${BASE}/export`, { params, responseType: 'blob' }),
}
