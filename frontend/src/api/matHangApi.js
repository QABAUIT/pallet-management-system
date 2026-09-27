import apiClient from './apiClient'

export const matHangApi = {
  danhSach: (trangThai) => apiClient.get('/mat-hang', { params: { trangThai } }),
  chiTiet: (id) => apiClient.get(`/mat-hang/${id}`),
  taoMoi: (data) => apiClient.post('/mat-hang', data),
  capNhat: (id, data) => apiClient.put(`/mat-hang/${id}`, data),
  ngungKinhDoanh: (id) => apiClient.put(`/mat-hang/${id}/ngung-kinh-doanh`),
}
