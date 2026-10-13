import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Banknote, Building2, FileText, Package, Target } from "lucide-react";
import StatCard from "../../components/common/StatCard";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import OtherTypeFilter from "../../components/common/OtherTypeFilter";
import ReceiptStatusBadge from "../../components/receipt/ReceiptStatusBadge";
import FinanceTabs from "../../components/finance/FinanceTabs";
import FinanceToolbar from "../../components/finance/FinanceToolbar";
import RevenueBarChart from "../../components/finance/RevenueBarChart";
import PalletPieChart from "../../components/finance/PalletPieChart";
import RevenueTargetModal from "../../components/finance/RevenueTargetModal";
import { useAuth } from "../../store/AuthContext";
import { useFinanceReport } from "../../hooks/useFinanceReport";
import { taiChinhService } from "../../services/taiChinhService";
import { nhanKy } from "../../utils/matchTime";
import { toTrend } from "../../utils/financeFormat";
import { formatDate, formatMoney, formatTime } from "../../utils/receiptFormat";
import "../../components/layout/style.css";

const PER_PAGE = 5;
const DEFAULT_TT = { trangThai: null };

export default function Revenue() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const canEdit = ["GD", "PGD"].includes(user?.maVaiTro);

  const [time, setTime] = useState({ preset: "month", from: null, to: null });
  const { data, loading, reload, range } = useFinanceReport(taiChinhService.doanhThu, time);
  const [ttFilter, setTtFilter] = useState(DEFAULT_TT);
  const [page, setPage] = useState(1);
  const [openTarget, setOpenTarget] = useState(false);
  const label = nhanKy(time, range);

  const rows = useMemo(() => {
    const t = ttFilter.trangThai;
    return (data?.giaoDichGanDay ?? []).filter((r) => {
      if (!t) return true;
      return t === "qua_han" ? r.quaHan : r.trangThai === t && !r.quaHan;
    });
  }, [data, ttFilter]);
  const pagedRows = rows.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  // Tăng trưởng trung bình tháng-sau-so-tháng-trước của biểu đồ 6 tháng
  const tangTruongTB = useMemo(() => {
    const ds = (data?.bieuDoThang ?? []).map((d) => Number(d.doanhThu));
    const g = [];
    for (let i = 1; i < ds.length; i++) if (ds[i - 1] > 0) g.push(((ds[i] - ds[i - 1]) / ds[i - 1]) * 100);
    return g.length ? (g.reduce((a, b) => a + b, 0) / g.length).toFixed(1) : null;
  }, [data]);

  const columns = [
    { key: "ma", header: "Mã hóa đơn", width: "150px", render: (r) => <b>#{r.maHoaDon}</b> },
    {
      key: "kh",
      header: "Khách hàng B2B",
      render: (r) => (
        <div>
          <div className="rc-name">{r.tenKhachHang}</div>
          <div className="muted-text">{r.mstKhachHang ? `MST: ${r.mstKhachHang}` : ""}</div>
        </div>
      ),
    },
    {
      key: "tg",
      header: "Thời gian",
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
      header: "Chủng loại & số lượng",
      render: (r) => (
        <div>
          <div className="rc-name">{r.tenMatHang || "—"}</div>
          <div className="muted-text">
            {r.soLuong != null ? `${formatMoney(r.soLuong)} cái` : ""}
            {r.soLoaiKhac > 0 ? ` · +${r.soLoaiKhac} loại khác` : ""}
          </div>
        </div>
      ),
    },
    { key: "donGia", header: "Đơn giá (VND)", align: "right", render: (r) => (r.donGia != null ? formatMoney(r.donGia) : "—") },
    { key: "tong", header: "Tổng thanh toán", align: "right", render: (r) => <b>{formatMoney(r.tongThanhToan)}</b> },
    { key: "tt", header: "Trạng thái", align: "center", width: "140px", render: (r) => <ReceiptStatusBadge hd={r} /> },
  ];

  const apply = (setter) => (next) => {
    setter(next);
    setPage(1);
  };

  return (
    <div>
      <FinanceTabs />

      <FinanceToolbar time={time} onTimeChange={setTime}>
        <button type="button" className="btn-outline" onClick={() => setOpenTarget(true)}>
          <Target size={16} /> {canEdit ? "Đặt mục tiêu" : "Xem mục tiêu"}
        </button>
      </FinanceToolbar>

      {!data ? (
        <p className="muted-text">{loading ? "Đang tải dữ liệu..." : "Không có dữ liệu"}</p>
      ) : (
        <>
          <div className="stat-row">
            <StatCard
              label={`DOANH THU ${label.toUpperCase()}`}
              value={`${formatMoney(data.doanhThu)} đ`}
              icon={Banknote}
              tone="blue"
              trend={toTrend(data.phanTramDoanhThu)}
              caption="so với kỳ trước"
            />
            <StatCard
              label="SỐ LƯỢNG HÓA ĐƠN"
              value={`${formatMoney(data.soHoaDon)} đơn hàng`}
              icon={FileText}
              tone="amber"
              trend={toTrend(data.phanTramSoHoaDon)}
              caption="đã thanh toán trong kỳ"
            />
            <StatCard
              label="TỒN KHO PALLET"
              value={`${formatMoney(data.tonKhoPallet)} cái`}
              icon={Package}
              tone="purple"
              badge={`Đang giữ chỗ ${formatMoney(data.daGiuCho)} cái`}
            />
            <StatCard
              label="KHÁCH HÀNG B2B"
              value={`${formatMoney(data.tongKhachHang)} đối tác`}
              icon={Building2}
              tone="green"
              badge={`+${data.khachHangMoi} đối tác mới`}
              caption="trong kỳ"
            />
          </div>

          <div className="card fn-target">
            {data.mucTieu ? (
              <>
                <div className="fn-target-head">
                  <span className="section-label" style={{ margin: 0 }}>MỤC TIÊU DOANH THU {label.toUpperCase()}</span>
                  <b>{formatMoney(data.mucTieu.soTien)} đ</b>
                </div>
                <div className="fn-progress">
                  <span style={{ width: `${Math.min(100, data.mucTieu.phanTramDat ?? 0)}%` }} />
                </div>
                <p className="muted-text" style={{ margin: "8px 0 0" }}>
                  Đã đạt {data.mucTieu.phanTramDat}% ({formatMoney(data.doanhThu)} đ)
                </p>
              </>
            ) : (
              <p className="muted-text" style={{ margin: 0 }}>
                {time.preset === "month" || time.preset === "year"
                  ? `Chưa đặt mục tiêu doanh thu cho ${label}.`
                  : "Mục tiêu doanh thu chỉ hiển thị khi chọn khung Tháng này hoặc Năm nay."}
              </p>
            )}
          </div>

          <div className="fn-grid-2">
            <div className="card">
              <h2 className="card-title">Biểu đồ doanh thu 6 tháng gần nhất</h2>
              <p className="muted-text" style={{ margin: "4px 0 14px" }}>Xu hướng luân chuyển pallet và thu hồi tiền mặt</p>
              <RevenueBarChart data={data.bieuDoThang} />
              <div className="fn-chart-foot">
                <span>Dữ liệu cập nhật đến {new Date().toLocaleDateString("vi-VN")}</span>
                {tangTruongTB != null && <b>Tăng trưởng trung bình: {tangTruongTB >= 0 ? "+" : ""}{tangTruongTB}%/tháng</b>}
              </div>
            </div>

            <div className="card">
              <h2 className="card-title">Cơ cấu pallet {label}</h2>
              <p className="muted-text" style={{ margin: "4px 0 14px" }}>Tỷ trọng doanh thu theo từng chủng loại</p>
              <PalletPieChart data={data.coCauPallet} />
              {data.sanPhamBanChayNhat && (
                <div className="fn-chart-foot">
                  <span>Dòng bán chạy nhất:</span>
                  <b>{data.sanPhamBanChayNhat}</b>
                </div>
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header" style={{ marginBottom: 14 }}>
              <h2 className="card-title">Giao dịch luân chuyển pallet gần đây</h2>
              <OtherTypeFilter
                label="Lọc trạng thái"
                align="right"
                groups={[{
                  key: "trangThai",
                  type: "radio",
                  options: [
                    { value: "da_thanh_toan", label: "Đã thanh toán" },
                    { value: "cho_thanh_toan", label: "Chờ thanh toán" },
                    { value: "qua_han", label: "Quá hạn" },
                  ],
                }]}
                value={ttFilter}
                defaultValue={DEFAULT_TT}
                onApply={apply(setTtFilter)}
              />
            </div>
            <AppTable columns={columns} data={pagedRows} onRowClick={(r) => navigate(`/hoa-don/${r.id}`)} />
            <div style={{ marginTop: 16 }}>
              <Pagination
                currentPage={page}
                totalItems={rows.length}
                itemsPerPage={PER_PAGE}
                itemLabel="giao dịch"
                onPageChange={setPage}
              />
            </div>
          </div>
        </>
      )}

      <RevenueTargetModal open={openTarget} onClose={() => setOpenTarget(false)} canEdit={canEdit} onChanged={reload} />
    </div>
  );
}