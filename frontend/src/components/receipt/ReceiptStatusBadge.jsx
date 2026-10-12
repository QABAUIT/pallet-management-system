import { Tag } from "antd";
import { HOA_DON_TRANG_THAI, resolveStatus } from "../../utils/statusTag";

// Quá hạn không lưu trong DB mà tính từ hạn thanh toán (backend trả cờ quaHan)
export default function ReceiptStatusBadge({ hd }) {
  const s = resolveStatus(HOA_DON_TRANG_THAI, hd.quaHan ? "qua_han" : hd.trangThai);
  return <Tag color={s.color} style={{ marginInlineEnd: 0 }}>{s.label}</Tag>;
}