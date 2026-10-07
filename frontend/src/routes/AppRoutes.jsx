import { Routes, Route } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import MainLayout from "../components/layout/Layout";

import Login from "../pages/Login";

import AboutUs from "../pages/AboutUs";

// Hoá đơn
import ReceiptDetail from "../pages/Receipt/ReceiptDetail";
import ReceiptCreate from "../pages/Receipt/ReceiptCreate";
import TransactionHistory from "../pages/Receipt/TransactionHistory";

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
        <Route path="/hoa-don" element={<TransactionHistory />} />
        <Route path="/hoa-don/tao-moi" element={<ReceiptCreate />} />
        <Route path="/hoa-don/:id" element={<ReceiptDetail />} />

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

        {/* Tài chính */}
        <Route path="/tai-chinh" element={<Revenue />} />
        <Route path="/tai-chinh/thue" element={<Tax />} />
        <Route path="/tai-chinh/loi-nhuan" element={<Profit />} />
      </Route>
    </Routes>
  );
}
