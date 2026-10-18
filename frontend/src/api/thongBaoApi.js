import apiClient from './apiClient'

export const thongBaoApi = {
  // Lấy danh sách thông báo (có thể lọc theo daDoc, loaiThongBao)
  danhSach: (params) => apiClient.get('/thong-bao', { params }),

  // Thống kê số lượng tổng, chưa đọc, đã đọc
  thongKe: () => apiClient.get('/thong-bao/thong-ke'),

  // Lấy chi tiết 1 thông báo
  chiTiet: (id) => apiClient.get(`/thong-bao/${id}`),

  // Đánh dấu đã đọc / chưa đọc
  danhDauDaDoc: (id, daDoc = true) =>
    apiClient.patch(`/thong-bao/${id}/doc`, null, { params: { daDoc } }),

  // Đánh dấu tất cả đã đọc
  danhDauTatCaDaDoc: () => apiClient.post('/thong-bao/doc-tat-ca'),

  // Xóa thông báo
  xoa: (id) => apiClient.delete(`/thong-bao/${id}`),
}
