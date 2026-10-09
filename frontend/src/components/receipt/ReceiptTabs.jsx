import { Link, useLocation } from "react-router-dom";
import { FilePlus, History } from "lucide-react";
import "../layout/style.css";

export default function ReceiptTabs({ count }) {
  const { pathname } = useLocation();
  const taoMoi = pathname.startsWith("/hoa-don/tao-moi");

  return (
    <nav className="rc-tabs rc-no-print">
      <Link to="/hoa-don/lich-su" className={"rc-tab" + (taoMoi ? "" : " active")}>
        <History size={16} aria-hidden />
        Lịch sử giao dịch
        {count != null && <span className="tf-count">{count}</span>}
      </Link>
      <Link to="/hoa-don/tao-moi" className={"rc-tab" + (taoMoi ? " active" : "")}>
        <FilePlus size={16} aria-hidden />
        Thanh toán &amp; Tạo hóa đơn
      </Link>
    </nav>
  );
}