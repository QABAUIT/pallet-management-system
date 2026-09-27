import axios from 'axios'
import { tokenStore } from '../store/tokenStore'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Gắn access token vào mọi request
apiClient.interceptors.request.use((config) => {
  const token = tokenStore.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let dangLamMoiToken = false
let hangDoiSauKhiRefresh = []

function chayLaiCacRequestDangCho(tokenMoi) {
  hangDoiSauKhiRefresh.forEach((cb) => cb(tokenMoi))
  hangDoiSauKhiRefresh = []
}

// Tự giải nén dữ liệu từ ApiResponse + tự refresh token khi accessToken hết hạn (401)
apiClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    if (status === 401 && !originalRequest._daThuLai && tokenStore.getRefreshToken()) {
      if (dangLamMoiToken) {
        // Đã có 1 request khác đang refresh - xếp hàng chờ, không gọi refresh 2 lần cùng lúc
        return new Promise((resolve) => {
          hangDoiSauKhiRefresh.push((tokenMoi) => {
            originalRequest.headers.Authorization = `Bearer ${tokenMoi}`
            originalRequest._daThuLai = true
            resolve(apiClient(originalRequest))
          })
        })
      }

      originalRequest._daThuLai = true
      dangLamMoiToken = true
      try {
        const res = await axios.post(
          `${apiClient.defaults.baseURL}/auth/refresh`,
          { refreshToken: tokenStore.getRefreshToken() },
        )
        const moi = res.data.data
        tokenStore.setSession({ ...tokenStore.getUserInfo(), ...moi })
        chayLaiCacRequestDangCho(moi.accessToken)
        originalRequest.headers.Authorization = `Bearer ${moi.accessToken}`
        return apiClient(originalRequest)
      } catch (refreshError) {
        tokenStore.clear()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        dangLamMoiToken = false
      }
    }

    const message = error.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại!'
    // Với lỗi 400 kèm chi tiết từng field (từ MethodArgumentNotValidException ở backend),
    // trả cả object lỗi để form hiển thị đúng field - xem cách dùng trong LoginPage.jsx.
    return Promise.reject({ message, fieldErrors: error.response?.data?.data, status })
  },
)

export default apiClient
