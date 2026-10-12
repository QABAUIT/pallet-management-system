// Form (snake_case, từ AddCustomerModal) -> field backend cần (camelCase)
export function formToApiRequest(form) {
  return {
    tenKh: form.ten_kh,
    loaiKh: form.loai_kh,
    mst: form.mst,
    diaChi: form.dia_chi,
    sdt: form.sdt,
    email: form.email,
    nguoiDaiDien: form.nguoi_dai_dien,
    trangThai: form.trang_thai,
  };
}

// Response backend (camelCase) -> field UI đang dùng trong CustomerManagement/CustomerDetailPanel
export function apiToUiCustomer(res) {
  return {
    id: res.id, // số thật (PK), dùng để gọi API chi tiết/sửa - KHÔNG hiện lên UI
    code: res.maKh, // chuỗi "KH-001" hiện ở cột "Mã KH"
    name: res.tenKh,
    mst: res.mst,
    phone: res.sdt,
    email: res.email,
    representative: res.nguoiDaiDien,
    address: res.diaChi,
    loaiKh: res.loaiKh,
    trangThai: res.trangThai, // dang_hop_tac | ngung_hop_tac
    status: res.tinhTrangCongNo, // tat_toan | dang_no | qua_han | chua_phat_sinh
    description: "", // backend chưa có field này, để trống
    totalOrders: res.tongDonHang ?? 0,
    totalSpend: res.tongChiTieu ?? 0,
    debtLimit: 0, // backend chưa có hạn mức nợ, tạm 0 - ẩn phần này ở panel nếu cần
    onTimePaymentPercent: 0, // backend chưa tính field này
    palletsCirculated: 0, // backend chưa có
    totalDebt: res.tongNo ?? 0,
    unpaidInvoices: (res.hoaDonChuaThanhToan || []).map((inv) => ({
      code: inv.maHoaDon,
      issueDate: formatDate(inv.ngayLap),
      dueDate: formatDate(inv.hanThanhToan),
      amount: inv.tongThanhToan,
    })),
  };
}

function formatDate(value) {
  if (!value) return "";
  return new Date(value).toLocaleDateString("vi-VN");
}