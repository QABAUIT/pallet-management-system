import React, { useEffect, useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuth } from '../store/AuthContext'

// Bật = true để TỰ ĐỘNG đăng nhập ngầm bằng tài khoản demo khi đang dev,
// KHÔNG bỏ qua việc lấy token - API vẫn cần token thật để backend chấp nhận.
// Bật qua biến môi trường VITE_DEV_AUTO_LOGIN=true trong .env (KHÔNG hard-code true
// thẳng trong code như bản cũ), để tránh quên tắt lúc build production.
const DEV_AUTO_LOGIN = import.meta.env.VITE_DEV_AUTO_LOGIN === 'true'
const DEV_TEN_DANG_NHAP = import.meta.env.VITE_DEV_USERNAME || 'au.hoanghai'
const DEV_MAT_KHAU = import.meta.env.VITE_DEV_PASSWORD || 'Password@123'

export default function ProtectedRoute({ children }) {
  const { daDangNhap, login } = useAuth()
  const location = useLocation()
  const [dangTuDangNhap, setDangTuDangNhap] = useState(false)
  const [loiTuDangNhap, setLoiTuDangNhap] = useState(null)

  useEffect(() => {
    if (DEV_AUTO_LOGIN && !daDangNhap && !dangTuDangNhap) {
      setDangTuDangNhap(true)
      login(DEV_TEN_DANG_NHAP, DEV_MAT_KHAU, true)
        .catch((err) => setLoiTuDangNhap(err.message || 'Tự đăng nhập demo thất bại'))
        .finally(() => setDangTuDangNhap(false))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [daDangNhap])

  if (DEV_AUTO_LOGIN && !daDangNhap) {
    if (loiTuDangNhap) {
      return (
        <div style={{ padding: 24, color: '#cf1322' }}>
          Tự đăng nhập demo thất bại: {loiTuDangNhap}. Kiểm tra lại
          VITE_DEV_USERNAME/VITE_DEV_PASSWORD trong .env, hoặc backend chưa chạy.
        </div>
      )
    }
    return (
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 100 }}>
        <Spin tip="Đang tự đăng nhập (dev mode)..." size="large" />
      </div>
    )
  }

  if (!daDangNhap) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }
  return children
}