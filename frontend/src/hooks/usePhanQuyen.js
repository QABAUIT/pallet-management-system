import { useAuth } from "../store/AuthContext";

const QUAN_LY = ["GD", "PGD"];
const KHO = ["GD", "PGD", "NVKHO"];

/**
 * Quyền theo vai trò đăng nhập (user.maVaiTro). Chỉ để ẩn/hiện nút cho gọn,
 * BE vẫn là nơi chặn thật (@PreAuthorize).
 */
export function usePhanQuyen() {
  const { user } = useAuth();
  const ma = String(user?.maVaiTro ?? "").replace(/^ROLE_/, "");
  return {
    maVaiTro: ma,
    laQuanLy: QUAN_LY.includes(ma), // GD, PGD: duyệt / xác nhận / huỷ đơn mua
    duocThaoTacKho: KHO.includes(ma), // GD, PGD, NVKHO: lập phiếu
  };
}
