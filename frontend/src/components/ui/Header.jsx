import { ArrowLeft, Bell, ChevronDown, LogOut } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import "../layout/style.css";
import { useAuth } from "../../store/AuthContext";


const titles = {
  "/": "Hồ sơ doanh nghiệp và năng lực vận hành",

  "/hoa-don": "Quản lí hóa đơn",
  "/hoa-don/tao-moi": "Quản lí hóa đơn",
  "/hoa-don/lich-su": "Quản lí hóa đơn",

  "/pallet": "Quản lí pallet",
  "/nha-cung-cap": "Quản lí nhà cung cấp",
  "/nhan-vien": "Quản lí nhân viên",
  "/khach-hang": "Quản lí khách hàng",

  "/kho": "Quản lí kho",
  "/kho/nhap-kho": "Quản lí kho",
  "/kho/xuat-kho": "Quản lí kho",
  "/kho/kiem-ke": "Quản lí kho",

  "/tai-chinh": "Quản lí tài chính",
  "/tai-chinh/thue": "Quản lí tài chính",
  "/tai-chinh/loi-nhuan": "Quản lí tài chính",

  "/notifications": "Thông báo",
};

const subtitles = {
  "/": "Hệ thống quản lí doanh nghiệp pallet",

  "/hoa-don": "Lập phiếu bán hàng pallet, tính chiết khấu và theo dõi các đơn hàng công nợ chưa thanh toán",
  "/hoa-don/tao-moi": "Lập phiếu bán hàng pallet, tính chiết khấu và theo dõi các đơn hàng công nợ chưa thanh toán",
  "/hoa-don/lich-su": "Theo dõi lịch sử dòng tiền, đối soát giao dịch",

  "/pallet": "Quản lý danh mục quy cách, tiêu chuẩn kỹ thuật và số lượng pallet trong kho",
  "/nha-cung-cap": "Quản lý đối tác cung ứng",
  "/nhan-vien": "Danh sách nhân sự, phân quyền vai trò vận hành và thông tin liên hệ",
  "/khach-hang": "Hồ sơ doanh nghiệp đối tác, liên kết dữ liệu tự động từ hóa đơn và theo dõi công nợ pallet",

  "/kho": "Giám sát số lượng lưu kho thực tế, giá trị định mức và trạng thái định lượng an toàn.",
  "/kho/nhap-kho": "Điều phối luân chuyển pallet, nhập xuất kho và kiểm kê",
  "/kho/xuat-kho": "Điều phối luân chuyển pallet, nhập xuất kho và kiểm kê",
  "/kho/kiem-ke": "Điều phối luân chuyển pallet, nhập xuất kho và kiểm kê",

  "/tai-chinh": "Theo dõi doanh thu và dòng tiền thu về theo từng kỳ",
  "/tai-chinh/thue": "Quản lý thuế phải nộp và theo dõi hạn kê khai",
  "/tai-chinh/loi-nhuan": "Phân tích lợi nhuận và hiệu quả kinh doanh theo kỳ",
};

// Các trang chi tiết (URL có thêm /:id). Muốn thêm trang chi tiết mới
// chỉ cần thêm 1 dòng vào đây.
const DETAIL_ROUTES = [
  {
    prefix: "/hoa-don/",
    title: "Chi tiết hóa đơn",
    backTo: "/hoa-don/lich-su",
    backLabel: "Quay lại lịch sử giao dịch",
  },
  {
    prefix: "/kho/nhap-kho/",
    title: "Chi tiết phiếu nhập kho",
    backTo: "/kho/nhap-kho",
    backLabel: "Quay lại lịch sử nhập kho",
  },
  {
    prefix: "/kho/xuat-kho/",
    title: "Chi tiết phiếu xuất kho",
    backTo: "/kho/xuat-kho",
    backLabel: "Quay lại lịch sử xuất kho",
  },
];

// Trả về cấu hình trang chi tiết nếu pathname là trang chi tiết.
// Phải loại trừ các path khớp chính xác (vd /hoa-don/tao-moi cũng bắt đầu
// bằng "/hoa-don/" nhưng KHÔNG phải trang chi tiết).
const findDetailRoute = (pathname) => {
  if (titles[pathname]) return undefined;
  return DETAIL_ROUTES.find((r) => pathname.startsWith(r.prefix));
};

const getPageTitle = (pathname) => {
  if (titles[pathname]) return titles[pathname];
  return findDetailRoute(pathname)?.title ?? "Pallet Út Xíu";
};

// Trang chi tiết không có subtitle dạng chữ - dòng đó là link quay lại (getBackLink)
const getPageSubtitle = (pathname) => subtitles[pathname] || undefined;

// Trả về { to, label } cho trang chi tiết, hoặc null nếu không phải.
// Hỗ trợ ?returnTo=... để quay về đúng trang danh sách kèm bộ lọc/phân trang,
// chỉ chấp nhận returnTo nằm trong đúng trang danh sách (tránh redirect lung tung).
const getBackLink = (pathname, search) => {
  const route = findDetailRoute(pathname);
  if (!route) return null;

  const requested = new URLSearchParams(search).get("returnTo");
  const to =
    requested === route.backTo || requested?.startsWith(route.backTo + "?")
      ? requested
      : route.backTo;

  return { to, label: route.backLabel };
};

export default function Header({
  user = {
    name: "Trần Hải Nam",
    role: "Quản trị viên kho",
    avatarUrl: "",
  },
  notificationCount = 0,
  notificationPath = "/notifications",
}) {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();
  const [showMenu, setShowMenu] = useState(false);

  const title = getPageTitle(location.pathname);
  const subtitle = getPageSubtitle(location.pathname);
  const backLink = getBackLink(location.pathname, location.search);

  const handleLogout = async () => {
    await logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="header">
      <div className="header-left">
        <h1 className="header-title">{title}</h1>
        {backLink ? (
          <Link to={backLink.to} className="back-link">
            <ArrowLeft size={16} aria-hidden />
            <span>{backLink.label}</span>
          </Link>
        ) : (
          subtitle && <p className="header-sub">{subtitle}</p>
        )}
      </div>

      <div className="header-right">
        <button
          type="button"
          aria-label="Thông báo"
          onClick={() => navigate(notificationPath)}
          className="icon-btn"
        >
          <Bell size={20} />
          {notificationCount > 0 && <span className="notif-dot" />}
        </button>

        <div className="divider" />

        <div className="user-menu">
          <button
            type="button"
            onClick={() => setShowMenu(!showMenu)}
            className="user-btn"
          >
            {user.avatarUrl ? (
              <img
                src={user.avatarUrl}
                alt={user.name}
                className="avatar"
              />
            ) : (
              <div className="avatar-fallback">
                {user.name?.charAt(0)}
              </div>
            )}

            <div className="user-info">
              <p className="user-name">{user.name}</p>
              <p className="user-role">{user.role}</p>
            </div>

            <ChevronDown size={16} className="chevron" />
          </button>

          {showMenu && (
            <div className="user-dropdown">
              <button
                type="button"
                onClick={handleLogout}
                className="logout-btn"
              >
                <LogOut size={16} />
                <span>Đăng xuất</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}