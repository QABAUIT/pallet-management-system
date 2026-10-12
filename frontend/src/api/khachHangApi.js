import apiClient from "./apiClient";

// Form (snake_case) -> body backend cần (camelCase). Chỉ gửi đúng các field backend đọc.
const toRequestBody = (form) => ({
  tenKh: form.ten_kh,
  loaiKh: form.loai_kh,
  mst: form.mst,
  diaChi: form.dia_chi,
  sdt: form.sdt,
  email: form.email,
  nguoiDaiDien: form.nguoi_dai_dien,
  trangThai: form.trang_thai,
});

const formatDate = (value) =>
  value ? new Date(value).toLocaleDateString("vi-VN") : "";

// Response backend (camelCase) -> object mà giao diện đang dùng
const toCustomer = (c) => ({
  id: c.id, // khóa chính (số), dùng để gọi API xem/sửa, không hiện lên UI
  code: c.maKh, // "KH-001", hiện ở cột Mã KH
  name: c.tenKh,
  loaiKh: c.loaiKh,
  mst: c.mst ?? "",
  address: c.diaChi ?? "",
  phone: c.sdt ?? "",
  email: c.email ?? "",
  representative: c.nguoiDaiDien ?? "",

  trangThai: c.trangThai, // dang_hop_tac | ngung_hop_tac
  status: c.tinhTrangCongNo, // tat_toan | dang_no | qua_han | chua_phat_sinh

  // Backend chưa có 4 field này, để mặc định
  description: "",
  debtLimit: 0,
  onTimePaymentPercent: 0,
  palletsCirculated: 0,

  totalOrders: c.tongDonHang ?? 0,
  totalSpend: c.tongChiTieu ?? 0,
  totalDebt: c.tongNo ?? 0,
  unpaidInvoices: (c.hoaDonChuaThanhToan ?? []).map((inv) => ({
    code: inv.maHoaDon,
    issueDate: formatDate(inv.ngayLap),
    dueDate: formatDate(inv.hanThanhToan),
    amount: inv.tongThanhToan,
  })),
});

export const khachHangApi = {
  // GET trả thẳng mảng
  layDanhSach: async (trangThai) => {
    const query = trangThai ? `?trangThai=${trangThai}` : "";
    const data = await apiClient.get(`/khach-hang${query}`);
    return data.map(toCustomer);
  },

  // GET trả thẳng object
  layChiTiet: async (id) => toCustomer(await apiClient.get(`/khach-hang/${id}`)),

  // POST được backend bọc trong ApiResponse { status, message, data }
  taoMoi: async (form) => {
    const res = await apiClient.post("/khach-hang", toRequestBody(form));
    return toCustomer(res.data);
  },

  // PUT trả thẳng object
  capNhat: async (id, form) =>
    toCustomer(await apiClient.put(`/khach-hang/${id}`, toRequestBody(form))),
};