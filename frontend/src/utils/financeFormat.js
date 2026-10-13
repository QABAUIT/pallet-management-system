// Loại chi phí vận hành (khớp CHECK của cột chi_phi_van_hanh.loai_chi_phi trong DB)
export const LOAI_CHI_PHI = {
  van_tai_logistics: "Vận tải & Logistics",
  nhan_su: "Nhân sự kho bãi",
  bao_tri_thiet_bi: "Bảo trì thiết bị",
  dien_nang_luong: "Điện & Năng lượng",
  khac: "Chi phí khác",
};

export const LOAI_CHI_PHI_OPTIONS = Object.entries(LOAI_CHI_PHI).map(([value, label]) => ({ value, label }));

// Màu cột/biểu đồ tròn
export const PALETTE = ["#8b00ff", "#38a8f5", "#fcd22f", "#2fb67c", "#f97316", "#94a3b8"];

// Đổi phần trăm so kỳ trước (số hoặc null) sang props `trend` của StatCard
export const toTrend = (p) =>
  p == null ? undefined : { direction: p >= 0 ? "up" : "down", text: `${Math.abs(p)}%` };