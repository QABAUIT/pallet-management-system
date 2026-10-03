const ACCESS_TOKEN_KEY = 'pallettrack_access_token'
const REFRESH_TOKEN_KEY = 'pallettrack_refresh_token'
const USER_INFO_KEY = 'pallettrack_user_info'

// Token đang nằm ở kho nào thì dùng kho đó
const kho = () =>
  sessionStorage.getItem(REFRESH_TOKEN_KEY) ? sessionStorage : localStorage

export const tokenStore = {
  getAccessToken: () => kho().getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => kho().getItem(REFRESH_TOKEN_KEY),
  getUserInfo: () => {
    try {
      const raw = kho().getItem(USER_INFO_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  },
  // ghiNho: true -> localStorage, false -> sessionStorage, undefined -> giữ kho hiện tại
  setSession: ({ accessToken, refreshToken, nhanVienId, hoTen, maVaiTro }, ghiNho) => {
    let dich = kho()
    if (ghiNho !== undefined) {
      tokenStore.clear()
      dich = ghiNho ? localStorage : sessionStorage
    }
    dich.setItem(ACCESS_TOKEN_KEY, accessToken)
    dich.setItem(REFRESH_TOKEN_KEY, refreshToken)
    dich.setItem(USER_INFO_KEY, JSON.stringify({ nhanVienId, hoTen, maVaiTro }))
  },
  clear: () => {
    ;[localStorage, sessionStorage].forEach((s) => {
      s.removeItem(ACCESS_TOKEN_KEY)
      s.removeItem(REFRESH_TOKEN_KEY)
      s.removeItem(USER_INFO_KEY)
    })
  },
}