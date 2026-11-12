import { Link, useLocation } from "react-router-dom";
import { FilePlus, FileText, History, Scale } from "lucide-react";
import "../layout/style.css";

const TABS = [
  { to: "/hoa-don/tao-moi", label: "Thanh toán & Tạo hóa đơn", icon: FilePlus },
  { to: "/hoa-don/bao-gia", label: "Báo giá", icon: FileText },
  { to: "/hoa-don/ban-phe-lieu", label: "Bán phế liệu", icon: Scale },
];

export default function ReceiptTabs({ count }) {
  const { pathname } = useLocation();
  // Các trang còn lại (lịch sử, chi tiết hóa đơn /hoa-don/:id) thuộc tab "Lịch sử giao dịch"
  const lichSu = !TABS.some((t) => pathname.startsWith(t.to));

  return (
    <nav className="rc-tabs rc-no-print">
      <Link to="/hoa-don/lich-su" className={"rc-tab" + (lichSu ? " active" : "")}>
        <History size={16} aria-hidden />
        Lịch sử giao dịch
        {count != null && <span className="tf-count">{count}</span>}
      </Link>
      {TABS.map(({ to, label, icon: Icon }) => (
        <Link
          key={to}
          to={to}
          className={"rc-tab" + (pathname.startsWith(to) ? " active" : "")}
        >
          <Icon size={16} aria-hidden />
          {label}
        </Link>
      ))}
    </nav>
  );
}
