import apiClient from './apiClient'

export const authApi = {
  login: (tenDangNhap, matKhau, ghiNhoDangNhap) =>
    apiClient.post('/auth/login', { tenDangNhap, matKhau, ghiNhoDangNhap }),
  logout: (refreshToken) => apiClient.post('/auth/logout', { refreshToken }),
}
