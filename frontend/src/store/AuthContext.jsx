import React, { createContext, useContext, useState, useCallback } from 'react'
import { tokenStore } from './tokenStore'
import { authApi } from '../api/authApi'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() =>
    tokenStore.getAccessToken() ? tokenStore.getUserInfo() : null,
  )

  const login = useCallback(async (tenDangNhap, matKhau, ghiNhoDangNhap) => {
    const res = await authApi.login(tenDangNhap, matKhau, ghiNhoDangNhap)
    const { accessToken, refreshToken, nhanVienId, hoTen, maVaiTro } = res.data
    tokenStore.setSession(
      { accessToken, refreshToken, nhanVienId, hoTen, maVaiTro },
      !!ghiNhoDangNhap,
    )
    setUser({ nhanVienId, hoTen, maVaiTro })
    return res.data
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = tokenStore.getRefreshToken()
    try {
      if (refreshToken) await authApi.logout(refreshToken)
    } catch {
      // lỗi mạng cũng vẫn đăng xuất phía client
    } finally {
      tokenStore.clear()
      setUser(null)
    }
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, logout, daDangNhap: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth phải dùng bên trong <AuthProvider>')
  return ctx
}