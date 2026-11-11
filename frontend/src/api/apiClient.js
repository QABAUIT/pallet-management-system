import axios from 'axios'
import { tokenStore } from '../store/tokenStore'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const token = tokenStore.getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let dangLamMoiToken = false
let hangDoi = [] // các request 401 chờ refresh xong

function giaiPhongHangDoi(loi, tokenMoi) {
  hangDoi.forEach(({ resolve, reject }) => (loi ? reject(loi) : resolve(tokenMoi)))
  hangDoi = []
}

function veTrangDangNhap() {
  tokenStore.clear()
  if (window.location.pathname !== '/login') window.location.href = '/login'
}

function chuanHoaLoi(error) {
  return {
    message: error.response?.data?.message || 'Không kết nối được máy chủ, vui lòng thử lại!',
    fieldErrors: error.response?.data?.data,
    status: error.response?.status,
  }
}

// Tự giải nén ApiResponse + tự refresh token khi access token hết hạn (401)
apiClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const original = error.config
    const status = error.response?.status
    const laUrlAuth = original?.url?.includes('/auth/')

    if (status === 401 && original && !original._daThuLai && !laUrlAuth) {
      const refreshToken = tokenStore.getRefreshToken()
      if (!refreshToken) {
        veTrangDangNhap()
        return Promise.reject(chuanHoaLoi(error))
      }

      // Đã có request khác đang refresh -> xếp hàng chờ
      if (dangLamMoiToken) {
        return new Promise((resolve, reject) => {
          hangDoi.push({ resolve, reject })
        }).then((tokenMoi) => {
          original._daThuLai = true
          original.headers = original.headers || {}
          original.headers.Authorization = `Bearer ${tokenMoi}`
          return apiClient(original)
        })
      }

      original._daThuLai = true
      dangLamMoiToken = true
      try {
        // Dùng axios gốc (không qua interceptor) để tránh vòng lặp refresh vô hạn.
        const res = await axios.post(`${apiClient.defaults.baseURL}/auth/refresh`, { refreshToken })
        // Hỗ trợ cả ApiResponse bọc ngoài lẫn response thô.
        const moi = res.data?.data ?? res.data
        if (!moi?.accessToken) {
          throw new Error('Refresh token không hợp lệ')
        }
        tokenStore.setSession({ ...tokenStore.getUserInfo(), ...moi })
        giaiPhongHangDoi(null, moi.accessToken)
        original.headers = original.headers || {}
        original.headers.Authorization = `Bearer ${moi.accessToken}`
        return apiClient(original)
      } catch {
        const loi = { message: 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại', status: 401 }
        giaiPhongHangDoi(loi)
        veTrangDangNhap()
        return Promise.reject(loi)
      } finally {
        dangLamMoiToken = false
      }
    }

    return Promise.reject(chuanHoaLoi(error))
  },
)

export default apiClient