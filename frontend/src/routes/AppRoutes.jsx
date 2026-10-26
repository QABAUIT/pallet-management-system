import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import MainLayout from "../components/layout/Layout";

import Login from "../pages/Login";

import AboutUs from "../pages/AboutUs";

// Hoá đơn
import ReceiptDetail from "../pages/Receipt/ReceiptDetail";
import ReceiptCreate from "../pages/Receipt/ReceiptCreate";
import TransactionHistory from "../pages/Receipt/TransactionHistory";
import BaoGia from "../pages/Receipt/BaoGia";
import BanPheLieu from "../pages/Receipt/BanPheLieu";
// Pallet - chỉ có 1 trang duy nhất
import PalletManagement from "../pages/PalletManagement";

// Đối tác / nhân sự / khách hàng
import SupplierManagement from "../pages/SupplierManagement";
import EmployeeManagement from "../pages/EmployeeManagement";
import CustomerManagement from "../pages/CustomerManagement";

// Kho vận
import ImportInventory from "../pages/Inventory/ImportInventory";
import ImportInventoryDetail from "../pages/Inventory/ImportInventoryDetail";
import ExportInventory from "../pages/Inventory/ExportInventory";
import ExportInventoryDetail from "../pages/Inventory/ExportInventoryDetail";
import Stocktaking from "../pages/Inventory/Stocktaking";
import StocktakingReport from "../pages/Inventory/StocktakingReport";

// Tài chính
import Revenue from "../pages/Finance/Revenue";
import Tax from "../pages/Finance/Tax";
import Profit from "../pages/Finance/Profit";

// Thông báo
import Notifications from "../pages/Notifications";

import DonMua from "../pages/Inventory/DonMua";
import PhanLoai from "../pages/Inventory/Recycle/PhanLoai";
import ThaoDo from "../pages/Inventory/Recycle/ThaoDo";
import XuLyThem from "../pages/Inventory/Recycle/XuLyThem";
import DinhMuc from "../pages/Inventory/Recycle/DinhMuc";
import DongPallet from "../pages/Inventory/Recycle/DongPallet";
import DichVuKhach from "../pages/Inventory/Recycle/DichVuKhach";

/**
 * AppRoutes - khai báo URL path <-> component trang.
 * Nhóm Hoá đơn / Kho / Tài chính giờ có nhiều trang con, nên mỗi nhóm có
 * nhiều <Route> thay vì 1 route như trước.
 */
export default function AppRoutes() {
  return (
    <Routes>
      {/* Không cần đăng nhập */}
      <Route path="/login" element={<Login />} />

      {/* Cần đăng nhập */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<AboutUs />} />

        {/* Hoá đơn */}
      <Route path="/hoa-don" element={<Navigate to="/hoa-don/lich-su" replace />} />
      <Route path="/hoa-don/lich-su" element={<TransactionHistory />} />
      <Route path="/hoa-don/tao-moi" element={<ReceiptCreate />} />
      <Route path="/hoa-don/:id" element={<ReceiptDetail />} />
      <Route path="/hoa-don/bao-gia" element={<BaoGia />} />
      <Route path="/hoa-don/ban-phe-lieu" element={<BanPheLieu />} /> 

        {/* Pallet */}
        <Route path="/pallet" element={<PalletManagement />} />

        {/* Đối tác / nhân sự / khách hàng */}
        <Route path="/nha-cung-cap" element={<SupplierManagement />} />
        <Route path="/nhan-vien" element={<EmployeeManagement />} />
        <Route path="/khach-hang" element={<CustomerManagement />} />

        {/* Kho vận */}
        <Route path="/kho" element={<StocktakingReport />} />
        <Route path="/kho/nhap-kho" element={<ImportInventory />} />
        <Route path="/kho/nhap-kho/:id" element={<ImportInventoryDetail />} />
        <Route path="/kho/xuat-kho" element={<ExportInventory />} />
        <Route path="/kho/xuat-kho/:id" element={<ExportInventoryDetail />} />
        <Route path="/kho/kiem-ke" element={<Stocktaking />} />

        <Route path="/kho/don-mua" element={<DonMua />} />
        <Route path="/kho/tai-che" element={<Navigate to="/kho/tai-che/phan-loai" replace />} />
        <Route path="/kho/tai-che/phan-loai" element={<PhanLoai />} />
        <Route path="/kho/tai-che/thao-do" element={<ThaoDo />} />
        <Route path="/kho/tai-che/xu-ly-them" element={<XuLyThem />} />

        <Route path="/kho/tai-che/dinh-muc" element={<DinhMuc />} />
        <Route path="/kho/tai-che/dong-pallet" element={<DongPallet />} />

        <Route path="/kho/tai-che/dich-vu-khach" element={<DichVuKhach />} /> 

        {/* Tài chính */}
        <Route path="/tai-chinh" element={<Revenue />} />
        <Route path="/tai-chinh/thue" element={<Tax />} />
        <Route path="/tai-chinh/loi-nhuan" element={<Profit />} />

        {/* Thông báo */}
        <Route path="/notifications" element={<Notifications />} />
        
      </Route>
    </Routes>
  );
}
