import apiClient from './apiClient'
import { unwrap } from '../utils/unwrap'

const BASE = '/kiem-ke'

export const kiemKeApi = {
  taoPhien: (payload) => apiClient.post(`${BASE}/phien`, payload).then(unwrap),
  chiTiet: (phienId) => apiClient.get(`${BASE}/phien/${phienId}/chi-tiet`).then(unwrap),
  thongKe: (phienId) => apiClient.get(`${BASE}/phien/${phienId}/thong-ke`).then(unwrap),
  capNhatChiTiet: (chiTietId, payload) =>
    apiClient.put(`${BASE}/chi-tiet/${chiTietId}`, payload).then(unwrap),

  // BE trả lỗi dạng { error: "..." } (không phải ApiResponse) nên apiClient chỉ đọc được
  // chữ "Không kết nối được máy chủ". Ở đây thay bằng thông báo đúng nghĩa.
  hoanTat: (phienId, nguoiChotId) =>
    apiClient
      .post(`${BASE}/phien/${phienId}/hoan-tat`, null, { params: { nguoiChotId } })
      .then(unwrap)
      .catch((err) => {
        if (err?.status === 400) {
          return Promise.reject({
            ...err,
            message: 'Không thể chốt phiên kiểm kê. Hãy kiểm tra còn mã nào đang ở trạng thái Chờ kiểm không.',
          })
        }
        return Promise.reject(err)
      }),
}
