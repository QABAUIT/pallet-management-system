import apiClient from '../api/apiClient';

const base = '/tai-chinh';

// Các hàm báo cáo là hàm cấp module (không tạo lại mỗi lần render) để hook useFinanceReport dùng làm dependency.
export const taiChinhService = {
  doanhThu: (params) => apiClient.get(`${base}/doanh-thu`, { params }),
  thue: (params) => apiClient.get(`${base}/thue`, { params }),
  loiNhuan: (params) => apiClient.get(`${base}/loi-nhuan`, { params }),

  chiPhi: (params) => apiClient.get(`${base}/chi-phi`, { params }),
  taoChiPhi: (data) => apiClient.post(`${base}/chi-phi`, data),
  suaChiPhi: (id, data) => apiClient.put(`${base}/chi-phi/${id}`, data),
  xoaChiPhi: (id) => apiClient.delete(`${base}/chi-phi/${id}`),

  mucTieu: (nam) => apiClient.get(`${base}/muc-tieu`, { params: { nam } }),
  datMucTieu: (data) => apiClient.post(`${base}/muc-tieu`, data),
  xoaMucTieu: (id) => apiClient.delete(`${base}/muc-tieu/${id}`),
};