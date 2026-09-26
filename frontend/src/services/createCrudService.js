import apiClient from '../api/apiClient';

/**
 * Factory tạo nhanh 1 service CRUD chuẩn cho 1 module, tránh mỗi người viết
 * lặp lại getAll/getById/create/update/delete.
 *
 * Dùng: const khachHangService = createCrudService('/khach-hang');
 *
 * Quy ước API (khớp với Controller ở BE, mỗi người tự tạo Controller theo
 * đúng path/param này cho module của mình):
 *   GET    {basePath}?page=0&size=10&sort=...&<cac_tham_so_loc_khac>
 *          -> ApiResponse<Page<T>>  (Page chuẩn Spring Data: content, totalElements, number, size)
 *   GET    {basePath}/{id}          -> ApiResponse<T>
 *   POST   {basePath}                -> ApiResponse<T>
 *   PUT    {basePath}/{id}          -> ApiResponse<T>
 *   DELETE {basePath}/{id}          -> ApiResponse<Void>
 */
export function createCrudService(basePath) {
  return {
    getAll: (params) => apiClient.get(basePath, { params }),
    getById: (id) => apiClient.get(`${basePath}/${id}`),
    create: (payload) => apiClient.post(basePath, payload),
    update: (id, payload) => apiClient.put(`${basePath}/${id}`, payload),
    remove: (id) => apiClient.delete(`${basePath}/${id}`),
  };
}
