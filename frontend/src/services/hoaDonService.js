import apiClient from '../api/apiClient';
import { createCrudService } from './createCrudService';

const crud = createCrudService('/hoa-don');

export const hoaDonService = {
  getAll: crud.getAll, // getAll({ trangThai })
  getById: crud.getById,
  create: crud.create,
  update: crud.update, // chỉ chạy được khi hóa đơn chưa chốt sổ / chưa hủy
  danhMucBanHang: () => apiClient.get('/hoa-don/danh-muc-ban-hang'),
  thanhToan: (id, data) => apiClient.post(`/hoa-don/${id}/thanh-toan`, data),
  // XÓA MỀM: chuyển sang "đã huỷ", bắt buộc lý do
  xoa: (id, lyDo) => apiClient.delete(`/hoa-don/${id}`, { params: { lyDo } }),
};