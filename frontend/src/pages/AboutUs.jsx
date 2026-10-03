import {
  Award,
  BadgeCheck,
  Recycle,
  Layers,
  Radio,
  Network,
  Building2,
  Warehouse,
  Ship,
  Phone,
  Mail,
  ShieldCheck,
  Download,
  Pencil,
} from "lucide-react";
import "../components/layout/style.css";

const CERTIFICATIONS = [
  { icon: Award, title: "Top 10 VLA 2024", subtitle: "Logistics Tin Cậy" },
  { icon: BadgeCheck, title: "EPAL / EUR-1", subtitle: "Chuẩn Pallet Châu Âu" },
];

const LEADERS = [
  {
    initials: "TQT",
    name: "Ông Trần Quốc Toàn",
    role: "Giám đốc điều hành (CEO)",
    email: "toan.tran@pallettrackpro.vn",
  },
  {
    initials: "ML",
    name: "Bà Nguyễn Thị Mai Lan",
    role: "Đại diện pháp luật / Phó TGĐ",
    email: "lan.nguyen@pallettrackpro.vn",
  },
];

const BUSINESS_LINES = [
  {
    icon: Recycle,
    title: "Sản xuất & Tái chế Pallet gỗ EPAL",
    desc: "Nguyên liệu gỗ thông nhập khẩu sấy đạt chuẩn IPPC/ISPM-15 tiêu chuẩn xuất khẩu châu Âu.",
  },
  {
    icon: Layers,
    title: "Pallet nhựa HDPE nguyên sinh",
    desc: "Chống tĩnh điện, kháng hóa chất, tải trọng tĩnh lên đến 5,000 kg dành cho ngành dược & kho lạnh.",
  },
  {
    icon: Radio,
    title: "Giải pháp RFID Smart Pallet",
    desc: "Gắn chíp RFID UHF theo dõi hành trình pallet, kết nối trực tiếp với cổng ERP PalletTrack Pro Cloud.",
  },
  {
    icon: Network,
    title: "Quản trị Logistics chuỗi cung ứng",
    desc: "Dịch vụ cho thuê pallet tuần hoàn (Pallet Pooling), thu hồi vỏ rỗng và bảo dưỡng định kỳ trên toàn quốc.",
  },
];

const BRANCHES = [
  {
    icon: Building2,
    isMain: true,
    tag: "Trụ sở",
    name: "Trụ sở chính & Điều hành Trung tâm",
    address:
      "Tòa nhà PalletTrack Tower, Lô CN1, KCN Tân Bình, Phường Tây Thạnh, Quận Tân Phú, TP. Hồ Chí Minh",
    note: "Điện thoại: 028.7300.9898 · Diện tích văn phòng: 1,800 m²",
  },
  {
    icon: Warehouse,
    tag: "20,000 m²",
    name: "Kho trung chuyển Dĩ An (Bình Dương HUB)",
    address: "Khu công nghiệp Sóng Thần 2, Thành phố Dĩ An, Tỉnh Bình Dương",
    note: "Phụ trách: Pallet gỗ EPAL, Pallet tái sinh & Trạm luân chuyển đường sắt",
  },
  {
    icon: Ship,
    tag: "15,000 m²",
    name: "Kho Cảng Quốc tế Cái Mép (Bà Rịa Vũng Tàu)",
    address: "Cụm ICD Tân Cảng Cái Mép, Thị xã Phú Mỹ, Tỉnh Bà Rịa - Vũng Tàu",
    note: "Phụ trách: Đóng hàng container xuất khẩu, Hun trùng đạt chuẩn ISPM-15",
  },
];

export default function AboutUs() {
  return (
    <div>
      {/* Hàng nút: Xuất PDF / Chỉnh sửa */}
      <div className="toolbar-right">
        <button type="button" className="btn-outline">
          <Download size={16} />
          Xuất hồ sơ năng lực PDF
        </button>
        <button type="button" className="btn-solid">
          <Pencil size={16} />
          Chỉnh sửa thông tin
        </button>
      </div>

      <div className="two-col-layout">
        {/* ========== CỘT TRÁI ========== */}
        <div className="col-stack">
          {/* Thẻ: Hồ sơ công ty (chỉ text, không có ảnh) */}
          <div className="card">
            <div className="card-top-row">
              <span className="pill pill-amber">
                <ShieldCheck size={13} />
                MÃ DOANH NGHIỆP: PLT-VNM-2018
              </span>
              <span className="muted-text">Cập nhật: 01/11/2024</span>
            </div>

            <h2 className="company-name">
              CÔNG TY CỔ PHẦN LOGISTICS &amp; GIẢI PHÁP PALLET VIỆT NAM (PALLET ÚT XÍU)
            </h2>
            <p className="company-intl-name">
              Tên thương mại quốc tế: <b>Pallet Ut Xiu Logistics JSC</b>
            </p>

            <div className="quote-box">
              “Chuẩn hóa kho vận – Kết nối chuỗi cung ứng công nghiệp”
            </div>

            <div className="card-divider" />

            <p className="section-label">CHỨNG NHẬN TIÊU CHUẨN KỸ THUẬT &amp; DANH HIỆU</p>
            <div className="cert-list">
              {CERTIFICATIONS.map((c) => (
                <div className="cert-item" key={c.title}>
                  <c.icon size={18} />
                  <div>
                    <p className="cert-item-title">{c.title}</p>
                    <p className="cert-item-sub">{c.subtitle}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Thẻ: Thông tin pháp lý & Hồ sơ đăng ký kinh doanh */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Thông tin pháp lý &amp; Hồ sơ đăng ký kinh doanh</h3>
              <span className="pill pill-blue">Cấp bởi Sở KH&amp;ĐT TP.HCM</span>
            </div>
            <div className="card-divider" />

            <div className="info-box-grid">
              <div className="info-box">
                <p className="info-box-label">MÃ SỐ THUẾ (MST)</p>
                <p className="info-box-value">0316892415</p>
                <p className="info-box-sub">Ngày cấp đăng ký: 15/03/2018</p>
              </div>
              <div className="info-box">
                <p className="info-box-label">TÀI KHOẢN NGÂN HÀNG ĐỊNH DANH</p>
                <p className="info-box-value">1028.888.999</p>
                <p className="info-box-sub">Vietcombank - CN Tân Bình TP.HCM</p>
              </div>
            </div>

            <div className="card-divider" />

            <p className="section-label">BAN ĐIỀU HÀNH DOANH NGHIỆP</p>
            <div className="leader-grid">
              {LEADERS.map((l) => (
                <div className="leader-item" key={l.name}>
                  <div className="leader-avatar">{l.initials}</div>
                  <div>
                    <p className="leader-role">{l.role}</p>
                    <p className="leader-name">{l.name}</p>
                    <p className="leader-email">{l.email}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="card-divider" />

            <div className="contact-grid">
              <div className="contact-item">
                <span className="contact-icon">
                  <Phone size={16} />
                </span>
                <div>
                  <p className="contact-label">Tổng đài vận hành &amp; CSKH</p>
                  <p className="contact-value">1900 6828 · 028.7300.9898</p>
                </div>
              </div>
              <div className="contact-item">
                <span className="contact-icon">
                  <Mail size={16} />
                </span>
                <div>
                  <p className="contact-label">Hộp thư điện tử chính thức</p>
                  <p className="contact-value">contact@pallettrackpro.vn</p>
                </div>
              </div>
            </div>
          </div>

          {/* Thẻ: Mạng lưới Chi nhánh & Trung tâm Phân phối Hub */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Mạng lưới Chi nhánh &amp; Trung tâm Phân phối Hub</h3>
              <span className="pill pill-blue">03 Địa điểm vận hành</span>
            </div>
            <div className="card-divider" />

            <div className="branch-list">
              {BRANCHES.map((b) => (
                <div className={"branch-item" + (b.isMain ? " branch-item-main" : "")} key={b.name}>
                  <span className="branch-icon">
                    <b.icon size={18} />
                  </span>
                  <div className="branch-body">
                    <div className="branch-top-row">
                      <p className="branch-name">{b.name}</p>
                      <span className={"pill" + (b.isMain ? " pill-navy" : " pill-amber")}>
                        {b.tag}
                      </span>
                    </div>
                    <p className="branch-address">{b.address}</p>
                    <p className="branch-note">{b.note}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* ========== CỘT PHẢI ========== */}
        <div className="col-stack">
          {/* Thẻ: Ảnh kho hàng */}
          <div className="card image-card">
            <img
              src="https://images.unsplash.com/photo-1553413077-190dd305871c?w=800&q=80"
              alt="Kho Pallet Út Xíu"
            />
            <span className="image-caption">Út Xíu Pallet</span>
          </div>

          {/* Thẻ: Lĩnh vực kinh doanh cốt lõi */}
          <div className="card">
            <h3 className="card-title">Lĩnh vực kinh doanh cốt lõi</h3>
            <div className="card-divider" />

            <div className="business-list">
              {BUSINESS_LINES.map((b) => (
                <div className="business-item" key={b.title}>
                  <span className="business-icon">
                    <b.icon size={18} />
                  </span>
                  <div>
                    <p className="business-title">{b.title}</p>
                    <p className="business-desc">{b.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}