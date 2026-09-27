import React, { createContext, useContext, useState, useCallback } from 'react'
import { tokenStore } from './tokenStore'
import { authApi } from '../api/authApi'

const AuthContext = createContext(null)

// DEV MODE
const IS_DEV_MODE = true

export function AuthProvider({ children }) {

  const [user, setUser] = useState(() => {
    if (IS_DEV_MODE) {
      return {
        nhanVienId: 1,
        hoTen: 'Developer',
        maVaiTro: 'GD'
      }
    }

    return tokenStore.getUserInfo()
  })

  const login = useCallback(async (tenDangNhap, matKhau, ghiNhoDangNhap) => {

    // DEV MODE: không gọi API login
    if (IS_DEV_MODE) {
      const userDev = {
        nhanVienId: 1,
        hoTen: 'Developer',
        maVaiTro: 'GD'
      }

      setUser(userDev)

      return userDev
    }

    // PRODUCTION: login thật
    const res = await authApi.login(
      tenDangNhap,
      matKhau,
      ghiNhoDangNhap
    )

    const {
      accessToken,
      refreshToken,
      nhanVienId,
      hoTen,
      maVaiTro
    } = res.data

    tokenStore.setSession({
      accessToken,
      refreshToken,
      nhanVienId,
      hoTen,
      maVaiTro
    })

    setUser({
      nhanVienId,
      hoTen,
      maVaiTro
    })

    return res.data
  }, [])

  const logout = useCallback(async () => {

    if (IS_DEV_MODE) {
      setUser({
        nhanVienId: 1,
        hoTen: 'Developer',
        maVaiTro: 'GD'
      })

      return
    }

    const refreshToken = tokenStore.getRefreshToken()

    try {
      if (refreshToken) {
        await authApi.logout(refreshToken)
      }
    } finally {
      tokenStore.clear()
      setUser(null)
    }

  }, [])

  const daDangNhap = !!user

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        logout,
        daDangNhap
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)

  if (!ctx) {
    throw new Error(
      'useAuth phải dùng bên trong <AuthProvider>'
    )
  }

  return ctx
}