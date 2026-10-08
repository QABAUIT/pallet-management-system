import { Link, useLocation } from "react-router-dom";
import { FilePlus, History } from "lucide-react";

export default function ReceiptTabs({ count }) {
  const { pathname } = useLocation();
  const dangTaoMoi = pathname.startsWith("/hoa-don/tao-moi");

  return (
    <nav className="rc-tabs rc-no-print">
      <Link to="/hoa-don" className={`rc-tab ${dangTaoMoi ? "" : "is-active"}`}>
        <History size={15} />
        Lịch sử giao dịch
        {count != null && <span className="rc-count">{count}</span>}
      </Link>
      <Link to="/hoa-don/tao-moi" className={`rc-tab ${dangTaoMoi ? "is-active" : ""}`}>
        <FilePlus size={15} />
        Thanh toán &amp; Tạo hóa đơn
      </Link>
    </nav>
  );
}