import { Link, useLocation } from "react-router-dom";
import "../layout/style.css";

const TABS = [
  { to: "/tai-chinh", label: "Doanh thu" },
  { to: "/tai-chinh/thue", label: "Thuế" },
  { to: "/tai-chinh/loi-nhuan", label: "Lợi nhuận" },
];

export default function FinanceTabs() {
  const { pathname } = useLocation();
  return (
    <nav className="rc-tabs rc-no-print">
      {TABS.map((t) => (
        <Link key={t.to} to={t.to} className={"rc-tab" + (pathname === t.to ? " active" : "")}>
          {t.label}
        </Link>
      ))}
    </nav>
  );
}