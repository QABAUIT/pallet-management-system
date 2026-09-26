import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';

// Hệ thống & Nhân sự
import NhanVienPage from './pages/nhansu/NhanVienPage';
import VaiTroPage from './pages/nhansu/VaiTroPage';
import ThongBaoPage from './pages/nhansu/ThongBaoPage';
import PheDuyetPage from './pages/nhansu/PheDuyetPage';
import NhatKyHeThongPage from './pages/nhansu/NhatKyHeThongPage';
import CauHinhHeThongPage from './pages/nhansu/CauHinhHeThongPage';

// Đối tác & Mặt hàng
import KhachHangPage from './pages/doitac/KhachHangPage';
import NhaCungCapPage from './pages/doitac/NhaCungCapPage';
import MatHangPage from './pages/doitac/MatHangPage';
import BangGiaPage from './pages/doitac/BangGiaPage';

// Kho vận
import KhoPage from './pages/khovan/KhoPage';
import TonKhoPage from './pages/khovan/TonKhoPage';
import LoHangPage from './pages/khovan/LoHangPage';
import PhieuKhoPage from './pages/khovan/PhieuKhoPage';
import PhienKiemKePage from './pages/khovan/PhienKiemKePage';
import SuaChuaPalletPage from './pages/khovan/SuaChuaPalletPage';
import PhuongTienPage from './pages/khovan/PhuongTienPage';

// Kinh doanh
import BaoGiaPage from './pages/kinhdoanh/BaoGiaPage';
import HopDongPage from './pages/kinhdoanh/HopDongPage';
import HoaDonPage from './pages/kinhdoanh/HoaDonPage';
import ThanhToanPage from './pages/kinhdoanh/ThanhToanPage';

// Tài chính
import ChiPhiVanHanhPage from './pages/taichinh/ChiPhiVanHanhPage';
import MucTieuDoanhThuPage from './pages/taichinh/MucTieuDoanhThuPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/khach-hang" replace />} />

          {/* Hệ thống & Nhân sự */}
          <Route path="nhan-vien" element={<NhanVienPage />} />
          <Route path="vai-tro" element={<VaiTroPage />} />
          <Route path="thong-bao" element={<ThongBaoPage />} />
          <Route path="phe-duyet" element={<PheDuyetPage />} />
          <Route path="nhat-ky-he-thong" element={<NhatKyHeThongPage />} />
          <Route path="cau-hinh-he-thong" element={<CauHinhHeThongPage />} />

          {/* Đối tác & Mặt hàng */}
          <Route path="khach-hang" element={<KhachHangPage />} />
          <Route path="nha-cung-cap" element={<NhaCungCapPage />} />
          <Route path="mat-hang" element={<MatHangPage />} />
          <Route path="bang-gia" element={<BangGiaPage />} />

          {/* Kho vận */}
          <Route path="kho" element={<KhoPage />} />
          <Route path="ton-kho" element={<TonKhoPage />} />
          <Route path="lo-hang" element={<LoHangPage />} />
          <Route path="phieu-kho" element={<PhieuKhoPage />} />
          <Route path="kiem-ke" element={<PhienKiemKePage />} />
          <Route path="sua-chua-pallet" element={<SuaChuaPalletPage />} />
          <Route path="phuong-tien" element={<PhuongTienPage />} />

          {/* Kinh doanh */}
          <Route path="bao-gia" element={<BaoGiaPage />} />
          <Route path="hop-dong" element={<HopDongPage />} />
          <Route path="hoa-don" element={<HoaDonPage />} />
          <Route path="thanh-toan" element={<ThanhToanPage />} />

          {/* Tài chính */}
          <Route path="chi-phi-van-hanh" element={<ChiPhiVanHanhPage />} />
          <Route path="muc-tieu-doanh-thu" element={<MucTieuDoanhThuPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
