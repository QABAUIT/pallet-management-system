import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Eye, Pencil, TrendingDown, TrendingUp, Wallet } from "lucide-react";
import TimeFilter from "../../components/common/TimeFilter";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import SearchInput from "../../components/common/SearchInput";
import StatCard from "../../components/common/StatCard";
import OtherTypeFilter from "../../components/common/OtherTypeFilter";
import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import ReceiptStatusBadge from "../../components/receipt/ReceiptStatusBadge";
import { useReceipts } from "../../hooks/useReceipts";
import { matchTime } from "../../utils/matchTime";
import { LOAI_GD, PHUONG_THUC, formatDate, formatMoney, formatTime } from "../../utils/receiptFormat";

const DEFAULT_LOAI = { loai: null };
const DEFAULT_TT = { trangThai: null, sort: "newest" };

export default function TransactionHistory() {
  const navigate = useNavigate();
  const { data: rows, loading } = useReceipts();
  const [search, setSearch] = useState("");
  const [time, setTime] = useState({ preset: "month", from: null, to: null });
  const [loaiFilter, setLoaiFilter] = useState(DEFAULT_LOAI);
  const [ttFilter, setTtFilter] = useState(DEFAULT_TT);
  const [page, setPage] = useState(1);
  const perPage = 8;

  const apply = (setter) => (next) => {
    setter(next);
    setPage(1);
  };

  const trongKy = useMemo(() => rows.filter((r) => matchTime(r.ngayLap, time)), [rows, time]);

  const stats = useMemo(() => {
    const daTT = trongKy.filter((r) => r.trangThai === "da_thanh_toan");
    const thu = daTT.filter((r) => r.laThu);
    const chi = daTT.filter((r) => !r.laThu);
    const sum = (list) => list.reduce((s, r) => s + Number(r.tongThanhToan), 0);
    const tongThu = sum(thu);
    const tongChi = sum(chi);
    const rong = tongThu - tongChi;
    return {
      tongThu, tongChi, rong,
      soThu: thu.length, soChi: chi.length,
      bienDo: tongThu > 0 ? Math.round((rong / tongThu) * 100) : 0,
    };
  }, [trongKy]);

  const filteredRows = useMemo(() => {
    const q = search.trim().toLowerCase();
    const list = trongKy
      .filter((r) => !loaiFilter.loai || (loaiFilter.loai === "thu" ? r.laThu : !r.laThu))
      .filter((r) => {
        const t = ttFilter.trangThai;
        if (!t) return true;
        return t === "qua_han" ? r.quaHan : r.trangThai === t;
      })
      .filter(
        (r) =>
          !q || [r.maHoaDon, r.tenDoiTac, r.sdtDoiTac, r.mstDoiTac].some((v) => v && v.toLowerCase().includes(q)),
      );
    // API trả mới nhất trước
    return ttFilter.sort === "oldest" ? [...list].reverse() : list;
  }, [trongKy, loaiFilter, ttFilter, search]);

  const pagedRows = filteredRows.slice((page - 1) * perPage, page * perPage);

  const columns = [
    { key: "maHoaDon", header: "Mã GD", width: "150px", render: (r) => <span className="rc-name">{r.maHoaDon}</span> },
    {
      key: "ngayLap",
      header: "Ngày & giờ",
      width: "110px",
      render: (r) => (
        <div>
          <div>{formatDate(r.ngayLap)}</div>
          <div className="muted-text">{formatTime(r.ngayLap)}</div>
        </div>
      ),
    },
    {
      key: "loai",
      header: "Loại GD",
      align: "center",
      width: "150px",
      render: (r) => (
        <span className={`stat-badge tone-${r.laThu ? "green" : "red"}`}>{LOAI_GD[r.loaiGiaoDich] || r.loaiGiaoDich}</span>
      ),
    },
    {
      key: "doiTac",
      header: "Khách hàng / Đối tác",
      render: (r) => (
        <div>
          <div className="rc-name">{r.tenDoiTac || "—"}</div>
          <div className="muted-text">{r.mstDoiTac ? `MST: ${r.mstDoiTac}` : r.sdtDoiTac || ""}</div>
        </div>
      ),
    },
    {
      key: "phuongThuc",
      header: "Phương thức",
      width: "130px",
      render: (r) => PHUONG_THUC[r.phuongThucThanhToan] || <span className="muted-text">Chưa thanh toán</span>,
    },
    {
      key: "tongThanhToan",
      header: "Số tiền (VND)",
      align: "right",
      width: "150px",
      render: (r) =>
        r.trangThai === "da_huy" ? (
          <span className="muted-text">{formatMoney(r.tongThanhToan)}</span>
        ) : (
          <span className={r.laThu ? "rc-in" : "rc-out"}>
            {r.laThu ? "+" : "-"}
            {formatMoney(r.tongThanhToan)}
          </span>
        ),
    },
    {
      key: "trangThai",
      header: "Trạng thái",
      align: "center",
      width: "140px",
      render: (r) => <ReceiptStatusBadge hd={r} />,
    },
    {
      key: "thaoTac",
      header: "Thao tác",
      align: "center",
      width: "100px",
      render: (r) => (
        <div style={{ display: "flex", justifyContent: "center", gap: 2 }}>
          <button className="icon-btn" title="Xem chi tiết" onClick={() => navigate(`/hoa-don/${r.id}`)}>
            <Eye size={16} />
          </button>
          {r.trangThai !== "da_huy" && !r.daChotSo && (
            <button
              className="icon-btn"
              title="Chỉnh sửa"
              onClick={() => navigate(`/hoa-don/${r.id}`, { state: { moSua: true } })}
            >
              <Pencil size={16} />
            </button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <ReceiptTabs count={rows.length} />

      <div className="stat-row">
        <StatCard
          label="TỔNG THU (KỲ NÀY)"
          value={`${formatMoney(stats.tongThu)} đ`}
          icon={TrendingUp}
          tone="green"
          badge={`${stats.soThu} giao dịch`}
          caption="đã thu trong kỳ"
        />
        <StatCard
          label="TỔNG CHI (KỲ NÀY)"
          value={`${formatMoney(stats.tongChi)} đ`}
          icon={TrendingDown}
          tone="red"
          badge={`${stats.soChi} giao dịch`}
          caption="đã chi trong kỳ"
        />
        <StatCard
          label="SỐ DƯ RÒNG ĐỐI SOÁT"
          value={`${stats.rong > 0 ? "+" : ""}${formatMoney(stats.rong)} đ`}
          icon={Wallet}
          tone="blue"
          badge={`Biên độ ${stats.bienDo}%`}
          caption="tỷ suất lợi nhuận ròng"
        />
      </div>

      <div className="rc-toolbar">
        <SearchInput
          value={search}
          onChange={(v) => {
            setSearch(v);
            setPage(1);
          }}
          placeholder="Tìm mã HD, đối tác, SĐT..."
        />
        <TimeFilter value={time} onChange={apply(setTime)} />
        <OtherTypeFilter
          label="Lọc loại giao dịch"
          groups={[
            {
              key: "loai",
              type: "radio",
              options: [
                { value: "thu", label: "Thu (bán hàng)" },
                { value: "chi", label: "Chi (mua, sửa chữa, vận chuyển)" },
              ],
            },
          ]}
          value={loaiFilter}
          defaultValue={DEFAULT_LOAI}
          onApply={apply(setLoaiFilter)}
        />
        <OtherTypeFilter
          label="Lọc trạng thái"
          groups={[
            {
              key: "trangThai",
              type: "radio",
              options: [
                { value: "cho_thanh_toan", label: "Chờ thanh toán" },
                { value: "da_thanh_toan", label: "Đã thanh toán" },
                { value: "qua_han", label: "Quá hạn" },
                { value: "da_huy", label: "Đã hủy" },
              ],
            },
          ]}
          sortOptions={[
            { value: "newest", label: "Mới nhất" },
            { value: "oldest", label: "Cũ nhất" },
          ]}
          value={ttFilter}
          defaultValue={DEFAULT_TT}
          onApply={apply(setTtFilter)}
        />
      </div>

      {loading ? <p className="muted-text">Đang tải dữ liệu...</p> : <AppTable columns={columns} data={pagedRows} />}

      <div style={{ marginTop: 16 }}>
        <Pagination
          currentPage={page}
          totalItems={filteredRows.length}
          itemsPerPage={perPage}
          itemLabel="giao dịch"
          onPageChange={setPage}
        />
      </div>
    </div>
  );
}