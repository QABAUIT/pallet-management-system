import apiClient from "./apiClient";

const BASE_URL = "/nha-cung-cap";

export const nhaCungCapApi = {
  // Lấy danh sách có phân trang, tìm kiếm và lọc
  getAll: (params) => {
    return apiClient.get(BASE_URL, { params });
  },

  // Lấy chi tiết 1 nhà cung cấp
  getById: (id) => {
    return apiClient.get(`${BASE_URL}/${id}`);
  },

  // Thêm mới
  create: (data) => {
    return apiClient.post(BASE_URL, data);
  },

  // Cập nhật
  update: (id, data) => {
    return apiClient.put(`${BASE_URL}/${id}`, data);
  },

  // Xóa (xóa mềm ở backend: đổi trạng thái)
  delete: (id) => {
    return apiClient.delete(`${BASE_URL}/${id}`);
  },
};
