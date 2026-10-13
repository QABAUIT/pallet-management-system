import { useState } from "react";
import { Bolt, MoreHorizontal, ShoppingCart, TrendingDown, TrendingUp, Truck, Users, Wallet, Wrench, Settings2 } from "lucide-react";
import StatCard from "../../components/common/StatCard";
import FinanceTabs from "../../components/finance/FinanceTabs";
import FinanceToolbar from "../../components/finance/FinanceToolbar";
import ProfitLineChart from "../../components/finance/ProfitLineChart";
import OperatingCostModal from "../../components/finance/OperatingCostModal";
import { useAuth } from "../../store/AuthContext";
import { useFinanceReport } from "../../hooks/useFinanceReport";
import { taiChinhService } from "../../services/taiChinhService";
import { nhanKy } from "../../utils/matchTime";
import { toTrend } from "../../utils/financeFormat";
import { formatMoney } from "../../utils/receiptFormat";
import "../../components/layout/style.css";

// Biểu tượng + màu cho từng hạng mục chi phí
const HANG_MUC_STYLE = {
  van_tai_logistics: { icon: Truck, bg: "#dbeafe", color: "#1d4ed8" },
  nhan_su: { icon: Users, bg: "#fef3c7", color: "#b45309" },
  bao_tri_thiet_bi: { icon: Wrench, bg: "#dcfce7", color: "#16a34a" },
  dien_nang_luong: { icon: Bolt, bg: "#ede9fe", color: "#7c3aed" },
  khac: { icon: MoreHorizontal, bg: "#f1f5f9", color: "#64748b" },
  mua_hang_dich_vu: { icon: ShoppingCart, bg: "#ffe4e6", color: "#e11d48" },
};

// Chi phí tăng là xấu: badge luôn màu đỏ của thẻ, kèm mũi tên
const badgeChiPhi = (p) => (p == null ? undefined : `${p >= 0 ? "↑" : "↓"} ${Math.abs(p)}%`);

export default function Profit() {
  const { user } = useAuth();
  const canEdit = ["GD", "PGD"].includes(user?.maVaiTro);

  const [time, setTime] = useState({ preset: "month", from: null, to: null });
  const { data, loading, reload, range, tuNgay, denNgay } = useFinanceReport(taiChinhService.loiNhuan, time);
  const [openCost, setOpenCost] = useState(false);
  const label = nhanKy(time, range);

  const hangMuc = (data?.chiPhiTheoHangMuc ?? []).filter((h) => h.ma !== "mua_hang_dich_vu" || Number(h.soTien) > 0);

  return (
    <div>
      <FinanceTabs />

      <FinanceToolbar time={time} onTimeChange={setTime}>
        <button type="button" className="btn-outline" onClick={() => setOpenCost(true)}>
          <Settings2 size={16} /> {canEdit ? "Quản lý chi phí" : "Xem chi phí"}
        </button>
      </FinanceToolbar>

      {!data ? (
        <p className="muted-text">{loading ? "Đang tải dữ liệu..." : "Không có dữ liệu"}</p>
      ) : (
        <>
          <div className="stat-row">
            <StatCard
              label="TỔNG DOANH THU"
              value={`${formatMoney(data.doanhThu)} đ`}
              icon={TrendingUp}
              tone="blue"
              trend={toTrend(data.phanTramDoanhThu)}
              caption="so với kỳ trước"
            />
            <StatCard
              label="TỔNG CHI PHÍ"
              value={`${formatMoney(data.chiPhi)} đ`}
              icon={TrendingDown}
              tone="red"
              badge={badgeChiPhi(data.phanTramChiPhi)}
              caption="so với kỳ trước"
            />
            <StatCard
              label="LỢI NHUẬN RÒNG"
              value={`${formatMoney(data.loiNhuan)} đ`}
              icon={Wallet}
              tone="green"
              trend={toTrend(data.phanTramLoiNhuan)}
              caption="so với kỳ trước"
            />
          </div>

          <div className="fn-grid-2">
            <div className="card">
              <h2 className="card-title">So sánh Doanh thu &amp; Chi phí</h2>
              <p className="muted-text" style={{ margin: "4px 0 14px" }}>Biểu đồ biến động tài chính trong {label}</p>
              <ProfitLineChart data={data.bieuDo} />
            </div>

            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Chi phí Vận hành Chính</h2>
                <span className="muted-text">{label}</span>
              </div>
              <div className="card-divider" />
              {hangMuc.map((h) => {
                const s = HANG_MUC_STYLE[h.ma] ?? HANG_MUC_STYLE.khac;
                const Icon = s.icon;
                return (
                  <div className="fn-cost-row" key={h.ma}>
                    <span className="fn-cost-icon" style={{ background: s.bg, color: s.color }}><Icon size={17} /></span>
                    <span>{h.ten}</span>
                    <b>{formatMoney(h.soTien)}</b>
                  </div>
                );
              })}
              <div className="fn-cost-total">
                <strong>TỔNG CỘNG:</strong>
                <span>{formatMoney(data.chiPhi)} đ</span>
              </div>
            </div>
          </div>
        </>
      )}

      <OperatingCostModal
        open={openCost}
        onClose={() => setOpenCost(false)}
        tuNgay={tuNgay}
        denNgay={denNgay}
        canEdit={canEdit}
        onChanged={reload}
      />
    </div>
  );
}