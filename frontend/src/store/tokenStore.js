// Lưu access/refresh token. Đơn giản hóa bằng localStorage cho bản scaffold này.
// LƯU Ý BẢO MẬT: localStorage có thể bị đọc bởi XSS. Khi lên production nên cân nhắc
// chuyển refreshToken sang httpOnly cookie do backend set (cần chỉnh AuthController phát
// cookie thay vì trả JSON) - việc này để lại cho bước hoàn thiện sau, không chặn tiến độ demo.

const ACCESS_TOKEN_KEY = 'pallettrack_access_token'
const REFRESH_TOKEN_KEY = 'pallettrack_refresh_token'
const USER_INFO_KEY = 'pallettrack_user_info'

export const tokenStore = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  getUserInfo: () => {
    const raw = localStorage.getItem(USER_INFO_KEY)
    return raw ? JSON.parse(raw) : null
  },
  setSession: ({ accessToken, refreshToken, nhanVienId, hoTen, maVaiTro }) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(USER_INFO_KEY, JSON.stringify({ nhanVienId, hoTen, maVaiTro }))
  },
  clear: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  },
}
