import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { message, Select } from "antd";
import { Eye, ListFilter, Pencil, TrendingDown, TrendingUp, Wallet } from "lucide-react";
import TimeFilter from "../../components/common/TimeFilter";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import SearchInput from "../../components/common/SearchInput";
import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import { matchTime } from "../../utils/matchTime";
import { hoaDonApi } from "../../api/hoaDonApi";
import {
  LOAI_GD,
  PHUONG_THUC,
  TRANG_THAI,
  formatDate,
  formatMoney,
  formatTime,
  trangThaiHienThi,
} from "../../utils/receiptFormat";
import "./Receipt.css";

function StatCard({ label, value, icon, accent, soft, chip, note }) {
  return (
    <div className="rc-stat" style={{ "--accent": accent, "--soft": soft }}>
      <div className="rc-stat-top">
        <div>
          <div className="rc-stat-label">{label}</div>
          <div className="rc-stat-value">
            {value}
            <small>đ</small>
          </div>
        </div>
        <div className="rc-stat-icon">{icon}</div>
      </div>
      <div className="rc-stat-foot">
        <span className="rc-chip">{chip}</span>
        <span>{note}</span>
      </div>
    </div>
  );
}

export default function TransactionHistory() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [time, setTime] = useState({ preset: "month", from: null, to: null });
  const [loai, setLoai] = useState("all"); // all | thu | chi
  const [trangThai, setTrangThai] = useState(undefined);
  const [page, setPage] = useState(1);
  const perPage = 8;

  useEffect(() => {
    hoaDonApi
      .danhSach()
      .then((res) => setRows(res.data || []))
      .catch((err) => message.error(err.message))
      .finally(() => setLoading(false));
  }, []);

  const trongKy = useMemo(() => rows.filter((r) => matchTime(r.ngayLap, time)), [rows, time]);

  const thongKe = useMemo(() => {
    const daThu = trongKy.filter((r) => r.trangThai === "da_thanh_toan");
    const thu = daThu.filter((r) => r.laThu);
    const chi = daThu.filter((r) => !r.laThu);
    const tongThu = thu.reduce((s, r) => s + Number(r.tongThanhToan), 0);
    const tongChi = chi.reduce((s, r) => s + Number(r.tongThanhToan), 0);
    const rong = tongThu - tongChi;
    return {
      tongThu,
      tongChi,
      rong,
      soThu: thu.length,
      soChi: chi.length,
      bienDo: tongThu > 0 ? Math.round((rong / tongThu) * 100) : 0,
    };
  }, [trongKy]);

  const filteredRows = useMemo(() => {
    const q = search.trim().toLowerCase();
    return trongKy
      .filter((r) => (loai === "all" ? true : loai === "thu" ? r.laThu : !r.laThu))
      .filter((r) => (trangThai ? (trangThai === "qua_han" ? r.quaHan : r.trangThai === trangThai) : true))
      .filter(
        (r) =>
          !q ||
          [r.maHoaDon, r.tenDoiTac, r.sdtDoiTac, r.mstDoiTac].some((v) => v && v.toLowerCase().includes(q)),
      );
  }, [trongKy, loai, trangThai, search]);

  const pagedRows = filteredRows.slice((page - 1) * perPage, page * perPage);
  const reset = () => setPage(1);

  const columns = [
    { key: "maHoaDon", header: "Mã GD", width: "150px", render: (r) => <span className="rc-cell-main">{r.maHoaDon}</span> },
    {
      key: "ngayLap",
      header: "Ngày & giờ",
      width: "110px",
      render: (r) => (
        <div>
          <div className="rc-mono" style={{ fontSize: 12 }}>{formatDate(r.ngayLap)}</div>
          <div className="rc-cell-sub">{formatTime(r.ngayLap)}</div>
        </div>
      ),
    },
    {
      key: "loai",
      header: "Loại GD",
      align: "center",
      width: "130px",
      render: (r) => (
        <span className={`rc-badge ${r.laThu ? "rc-green" : "rc-red"}`}>{LOAI_GD[r.loaiGiaoDich] || r.loaiGiaoDich}</span>
      ),
    },
    {
      key: "doiTac",
      header: "Khách hàng / Đối tác",
      render: (r) => (
        <div>
          <div className="rc-cell-name">{r.tenDoiTac || "—"}</div>
          <div className="rc-cell-sub">{r.mstDoiTac ? `MST: ${r.mstDoiTac}` : r.sdtDoiTac || ""}</div>
        </div>
      ),
    },
    {
      key: "phuongThuc",
      header: "Phương thức",
      width: "130px",
      render: (r) => PHUONG_THUC[r.phuongThucThanhToan] || <span className="rc-muted">Chưa thanh toán</span>,
    },
    {
      key: "tongThanhToan",
      header: "Số tiền (VND)",
      align: "right",
      width: "150px",
      render: (r) => (
        <span className={r.trangThai === "da_huy" ? "rc-mono rc-muted" : r.laThu ? "rc-up" : "rc-down"}>
          {r.laThu ? "+" : "-"}
          {formatMoney(r.tongThanhToan)}
        </span>
      ),
    },
    {
      key: "trangThai",
      header: "Trạng thái",
      align: "center",
      width: "130px",
      render: (r) => {
        const t = trangThaiHienThi(r);
        return <span className={`rc-badge rc-${t.tone}`}>{t.label}</span>;
      },
    },
    {
      key: "thaoTac",
      header: "Thao tác",
      align: "center",
      width: "90px",
      render: (r) => (
        <div style={{ display: "flex", justifyContent: "center", gap: 4 }}>
          <button className="rc-icon-btn" title="Xem chi tiết" onClick={() => navigate(`/hoa-don/${r.id}`)}>
            <Eye size={16} />
          </button>
          {r.trangThai !== "da_huy" && !r.daChotSo && (
            <button
              className="rc-icon-btn"
              title="Chỉnh sửa"
              onClick={() => navigate(`/hoa-don/${r.id}`, { state: { moSua: true } })}
            >
              <Pencil size={15} />
            </button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="rc-page">
      <ReceiptTabs count={rows.length} />

      <div className="rc-body">
        <div className="rc-stats">
          <StatCard
            label="Tổng thu (kỳ này)"
            value={formatMoney(thongKe.tongThu)}
            icon={<TrendingUp size={20} />}
            accent="#059669"
            soft="#d1fae5"
            chip={`${thongKe.soThu} giao dịch`}
            note="đã thu trong kỳ"
          />
          <StatCard
            label="Tổng chi (kỳ này)"
            value={formatMoney(thongKe.tongChi)}
            icon={<TrendingDown size={20} />}
            accent="#ba1a1a"
            soft="#fef2f2"
            chip={`${thongKe.soChi} giao dịch`}
            note="đã chi trong kỳ"
          />
          <StatCard
            label="Số dư ròng đối soát"
            value={`${thongKe.rong > 0 ? "+" : ""}${formatMoney(thongKe.rong)}`}
            icon={<Wallet size={20} />}
            accent="#0f2942"
            soft="#d1e4ff"
            chip={`Biên độ ${thongKe.bienDo}%`}
            note="Tỷ suất lợi nhuận ròng"
          />
        </div>

        <div className="rc-card rc-toolbar">
          <TimeFilter
            value={time}
            onChange={(next) => {
              setTime(next);
              reset();
            }}
          />
          <div className="rc-seg">
            {[
              ["all", "Tất cả"],
              ["thu", "Thu (Bán hàng)"],
              ["chi", "Chi (Mua, sửa, vận chuyển)"],
            ].map(([k, label]) => (
              <button
                key={k}
                className={loai === k ? "is-on" : ""}
                onClick={() => {
                  setLoai(k);
                  reset();
                }}
              >
                {label}
              </button>
            ))}
          </div>
          <SearchInput
            value={search}
            onChange={(v) => {
              setSearch(v);
              reset();
            }}
            placeholder="Tìm mã HD, đối tác, SĐT..."
          />
          <Select
            allowClear
            value={trangThai}
            placeholder={
              <span>
                <ListFilter size={12} style={{ verticalAlign: -2, marginRight: 4 }} />
                Lọc trạng thái
              </span>
            }
            style={{ width: 170 }}
            onChange={(v) => {
              setTrangThai(v);
              reset();
            }}
            options={["cho_thanh_toan", "da_thanh_toan", "qua_han", "da_huy"].map((k) => ({
              value: k,
              label: TRANG_THAI[k].label,
            }))}
          />
        </div>

        <div className="rc-card">
          {loading ? (
            <div className="rc-loading">Đang tải dữ liệu...</div>
          ) : (
            <div className="rc-table-wrap">
              <AppTable columns={columns} data={pagedRows} />
            </div>
          )}
          <div style={{ padding: 16 }}>
            <Pagination
              currentPage={page}
              totalItems={filteredRows.length}
              itemsPerPage={perPage}
              itemLabel="giao dịch"
              onPageChange={setPage}
            />
          </div>
        </div>
      </div>
    </div>
  );
}