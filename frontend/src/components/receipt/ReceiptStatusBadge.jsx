import "../layout/style.css";
import { trangThaiHienThi } from "../../utils/receiptFormat";

export default function ReceiptStatusBadge({ hd }) {
  const t = trangThaiHienThi(hd);
  return <span className={`stat-badge tone-${t.tone}`}>{t.label}</span>;
}