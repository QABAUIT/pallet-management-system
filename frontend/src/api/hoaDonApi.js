import apiClient from './apiClient'

export const hoaDonApi = {
  danhSach: (trangThai) => apiClient.get('/hoa-don', { params: { trangThai } }),
  danhMucBanHang: () => apiClient.get('/hoa-don/danh-muc-ban-hang'),
  chiTiet: (id) => apiClient.get(`/hoa-don/${id}`),
  taoMoi: (data) => apiClient.post('/hoa-don', data),
  thanhToan: (id, data) => apiClient.post(`/hoa-don/${id}/thanh-toan`, data),
  capNhat: (id, data) => apiClient.put(`/hoa-don/${id}`, data),
  huy: (id, lyDo) => apiClient.post(`/hoa-don/${id}/huy`, { lyDo }),
}