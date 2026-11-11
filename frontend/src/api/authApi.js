import apiClient from './apiClient'

export const authApi = {
  login: (tenDangNhap, matKhau, ghiNhoDangNhap) =>
    apiClient.post('/auth/login', { tenDangNhap, matKhau, ghiNhoDangNhap }),
  refresh: (refreshToken) => apiClient.post('/auth/refresh', { refreshToken }),
  logout: (refreshToken) => apiClient.post('/auth/logout', { refreshToken }),
}
