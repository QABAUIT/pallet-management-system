import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from '../components/MainLayout'
import ProtectedRoute from './ProtectedRoute'

import LoginPage from '../features/auth/LoginPage'
import MatHangPage from '../features/mathang/MatHangPage'
import HoaDonLichSuPage from '../features/hoadon/HoaDonLichSuPage'
import HoaDonTaoMoiPage from '../features/hoadon/HoaDonTaoMoiPage'
import NhanVienPage from '../features/nhanvien/NhanVienPage'
import KhachHangPage from '../features/khachhang/KhachHangPage'
import NhaCungCapPage from '../features/nhacungcap/NhaCungCapPage'
import KhoLichSuNhapPage from '../features/kho/KhoLichSuNhapPage'
import KhoTaoPhieuNhapPage from '../features/kho/KhoTaoPhieuNhapPage'
import KhoLichSuXuatPage from '../features/kho/KhoLichSuXuatPage'
import KhoKiemKePage from '../features/kho/KhoKiemKePage'
import KhoBaoCaoTonPage from '../features/kho/KhoBaoCaoTonPage'
import TaiChinhDoanhThuPage from '../features/taichinh/TaiChinhDoanhThuPage'
import TaiChinhThuePage from '../features/taichinh/TaiChinhThuePage'
import TaiChinhLoiNhuanPage from '../features/taichinh/TaiChinhLoiNhuanPage'
import ThongTinCongTyPage from '../features/company/ThongTinCongTyPage'
import ThongTinNhanVienPage from '../features/company/ThongTinNhanVienPage'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Vào "/" thì mặc định chuyển tới màn Pallet */}
        <Route index element={<Navigate to="/pallet" replace />} />

        <Route path="pallet" element={<MatHangPage />} />
        <Route path="pallet/them-moi" element={<MatHangPage />} />

        <Route path="hoa-don/lich-su" element={<HoaDonLichSuPage />} />
        <Route path="hoa-don/tao-moi" element={<HoaDonTaoMoiPage />} />

        <Route path="nhan-vien" element={<NhanVienPage />} />
        <Route path="khach-hang" element={<KhachHangPage />} />
        <Route path="nha-cung-cap" element={<NhaCungCapPage />} />

        <Route path="kho/lich-su-nhap" element={<KhoLichSuNhapPage />} />
        <Route path="kho/tao-phieu-nhap" element={<KhoTaoPhieuNhapPage />} />
        <Route path="kho/lich-su-xuat" element={<KhoLichSuXuatPage />} />
        <Route path="kho/kiem-ke" element={<KhoKiemKePage />} />
        <Route path="kho/bao-cao-ton" element={<KhoBaoCaoTonPage />} />

        <Route path="tai-chinh/doanh-thu" element={<TaiChinhDoanhThuPage />} />
        <Route path="tai-chinh/thue" element={<TaiChinhThuePage />} />
        <Route path="tai-chinh/loi-nhuan" element={<TaiChinhLoiNhuanPage />} />

        <Route path="thong-tin/cong-ty" element={<ThongTinCongTyPage />} />
        <Route path="thong-tin/nhan-vien" element={<ThongTinNhanVienPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
