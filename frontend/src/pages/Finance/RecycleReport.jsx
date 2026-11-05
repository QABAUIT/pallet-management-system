import { useMemo, useState } from "react";
import { Banknote, Layers, Package, Scale } from "lucide-react";
import StatCard from "../../components/common/StatCard";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import FinanceTabs from "../../components/finance/FinanceTabs";
import FinanceToolbar from "../../components/finance/FinanceToolbar";
import RecycleBarChart from "../../components/finance/RecycleBarChart";
import { useFinanceReport } from "../../hooks/useFinanceReport";
import { baoCaoTaiCheService } from "../../services/baoCaoTaiCheService";
import { nhanKy } from "../../utils/matchTime";
import { formatDate, formatDateTime, formatMoney } from "../../utils/receiptFormat";
import "../../components/layout/style.css";

const PER_PAGE = 5;
const num = (v) => Number(v) || 0;
const pct = (v) => `${formatMoney(num(v))}%`;
const LOAI_NGUON = { thanh: "Thanh gỗ cần xử lý", cuc: "Cục gỗ cần xử lý" };

// Chênh lệch = thực tế - định mức: dương là dùng vượt (đỏ), âm là tiết kiệm (xanh)
const lech = (v) => {
  const n = num(v);
  if (n === 0) return <span className="muted-text">0</span>;
  return <b style={{ color: n > 0 ? "#dc2626" : "#16a34a" }}>{n > 0 ? `+${formatMoney(n)}` : formatMoney(n)}</b>;
};

const Row = ({ label, children }) => (
  <div className="rc-ledger-row">
    <span>{label}</span>
    <b>{children}</b>
  </div>
);

// Báo cáo tái chế (route /tai-chinh/tai-che): hiệu suất phân loại/tháo dỡ, giá mua theo lô, chênh lệch định mức
export default function RecycleReport() {
  const [time, setTime] = useState({ preset: "month", from: null, to: null });
  const { data, loading, range } = useFinanceReport(baoCaoTaiCheService.tongHop, time);
  const [pageLo, setPageLo] = useState(1);
  const [pageLech, setPageLech] = useState(1);
  const label = nhanKy(time, range);

  const onTimeChange = (t) => {
    setTime(t);
    setPageLo(1);
    setPageLech(1);
  };

  const hs = data?.hieuSuat;
  const pl = hs?.phanLoai ?? {};
  const td = hs?.thaoDo ?? {};
  const dv = hs?.dichVuChoKhach ?? {};
  const xuLyThem = useMemo(() => hs?.xuLyThem ?? [], [hs]);
  const theoLo = useMemo(() => data?.theoLo ?? [], [data]);
  const cl = data?.chenhLech;
  const theoVatTu = useMemo(() => cl?.theoVatTu ?? [], [cl]);
  const dongLech = useMemo(() => cl?.cacDongLech ?? [], [cl]);

  const tongKgPheLieu = num(pl.kgPheLieuLoaiBo) + num(td.kgPheLieu) + xuLyThem.reduce((s, r) => s + num(r.kg), 0);
  const vatTuLech = theoVatTu.filter((r) => num(r.chenh_lech) !== 0).slice(0, 8);

  const colXuLy = [
    { key: "nguon", header: "Nguồn xử lý thêm", render: (r) => <b>{LOAI_NGUON[r.loai_nguon] || r.loai_nguon}</b> },
    { key: "datLai", header: "Dùng lại được", align: "right", render: (r) => formatMoney(r.dat_lai) },
    { key: "pheLieu", header: "Bị bỏ (chiếc)", align: "right", render: (r) => formatMoney(r.phe_lieu) },
    { key: "kg", header: "Phế liệu (kg)", align: "right", render: (r) => formatMoney(r.kg) },
  ];

  const colLo = [
    { key: "lo", header: "Mã lô", width: "120px", render: (r) => <b>{r.ma_lo}</b> },
    { key: "ncc", header: "Nhà cung cấp", render: (r) => r.ten_ncc || "—" },
    { key: "ngay", header: "Ngày nhập", width: "110px", render: (r) => formatDate(r.ngay_nhap) },
    { key: "sl", header: "SL nhập", align: "right", render: (r) => formatMoney(r.so_luong_nhap) },
    { key: "gia", header: "Giá mua / pallet", align: "right", render: (r) => (r.don_gia_nhap != null ? `${formatMoney(r.don_gia_nhap)} đ` : "—") },
    { key: "von", header: "Tổng vốn", align: "right", render: (r) => <b>{formatMoney(r.tong_von)} đ</b> },
    { key: "xl", header: "Đã xử lý", align: "right", render: (r) => formatMoney(r.so_pallet_da_xu_ly) },
    { key: "bl", header: "Bán lại", align: "right", render: (r) => pct(r.ty_le_ban_lai) },
    { key: "td", header: "Tháo dỡ", align: "right", render: (r) => pct(r.ty_le_thao_do) },
    { key: "lb", header: "Loại bỏ", align: "right", render: (r) => pct(r.ty_le_loai_bo) },
    { key: "kg", header: "Phế liệu (kg)", align: "right", render: (r) => formatMoney(r.kg_phe_lieu) },
  ];

  const colVatTu = [
    { key: "vt", header: "Vật tư", render: (r) => (<div><div className="rc-name">{r.ten_mat_hang}</div><div className="muted-text">{r.ma_mat_hang}</div></div>) },
    { key: "dm", header: "Định mức", align: "right", render: (r) => formatMoney(r.dinh_muc) },
    { key: "tt", header: "Thực tế", align: "right", render: (r) => formatMoney(r.thuc_te) },
    { key: "cl", header: "Chênh lệch", align: "right", render: (r) => lech(r.chenh_lech) },
    { key: "ty", header: "Tỷ lệ", align: "right", render: (r) => pct(r.ty_le_chenh_lech) },
  ];

  const colDongLech = [
    { key: "phieu", header: "Phiếu đóng pallet", width: "150px", render: (r) => <b>{r.ma_phieu}</b> },
    { key: "tg", header: "Duyệt lúc", width: "150px", render: (r) => formatDateTime(r.thoi_gian_duyet) },
    { key: "vt", header: "Vật tư", render: (r) => r.ma_mat_hang },
    { key: "dm", header: "Định mức", align: "right", render: (r) => formatMoney(r.so_luong_dinh_muc) },
    { key: "tt", header: "Thực tế", align: "right", render: (r) => formatMoney(r.so_luong_thuc_te) },
    { key: "cl", header: "Chênh lệch", align: "right", render: (r) => lech(r.chenh_lech) },
    { key: "ly", header: "Lý do", render: (r) => r.ly_do_chenh_lech || "—" },
  ];

  return (
    <div>
      <FinanceTabs />
      <FinanceToolbar time={time} onTimeChange={onTimeChange} />

      {!data ? (
        <p className="muted-text">{loading ? "Đang tải dữ liệu..." : "Không có dữ liệu"}</p>
      ) : (
        <>
          <div className="stat-row">
            <StatCard
              label="PALLET ĐÃ PHÂN LOẠI"
              value={`${formatMoney(pl.tongPalletXuLy)} pallet`}
              icon={Layers}
              tone="blue"
              badge={`Bán lại ${pct(pl.tyLeBanLai)}`}
              caption={`phiếu đã duyệt ${label}`}
            />
            <StatCard
              label="PALLET ĐÃ THÁO DỠ"
              value={`${formatMoney(td.palletDaThaoDo)} pallet`}
              icon={Package}
              tone="amber"
              badge={`${formatMoney(td.thanhNguyenTrenPallet)} thanh nguyên / pallet`}
              caption="không tính dịch vụ cho khách"
            />
            <StatCard
              label="PHẾ LIỆU THU VỀ"
              value={`${formatMoney(tongKgPheLieu)} kg`}
              icon={Scale}
              tone="purple"
              caption="từ phân loại, tháo dỡ và xử lý thêm"
            />
            <StatCard
              label="PHÍ DỊCH VỤ THÁO DỠ"
              value={`${formatMoney(dv.doanhThuDichVuTruocVat)} đ`}
              icon={Banknote}
              tone="green"
              badge={`${formatMoney(dv.palletThaoDoChoKhach)} pallet của khách`}
              caption="chưa gồm VAT"
            />
          </div>

          <div className="fn-grid-2">
            <div className="card">
              <h2 className="card-title">Kết quả phân loại pallet cũ</h2>
              <p className="muted-text" style={{ margin: "4px 0 14px" }}>Số pallet theo hướng xử lý sau phân loại</p>
              {num(pl.tongPalletXuLy) === 0 ? (
                <p className="muted-text">Chưa có phiếu phân loại đã duyệt trong kỳ.</p>
              ) : (
                <>
                  <RecycleBarChart
                    labels={["Bán lại nguyên trạng", "Chuyển tháo dỡ", "Loại bỏ"]}
                    values={[num(pl.banLai), num(pl.thaoDo), num(pl.loaiBo)]}
                    colors={["#2fb67c", "#38a8f5", "#f97316"]}
                    unit="pallet"
                  />
                  <div className="fn-chart-foot">
                    <span>Tỷ lệ: bán lại {pct(pl.tyLeBanLai)} · tháo dỡ {pct(pl.tyLeThaoDo)} · loại bỏ {pct(pl.tyLeLoaiBo)}</span>
                    <b>{formatMoney(pl.kgPheLieuLoaiBo)} kg phế liệu từ pallet loại bỏ</b>
                  </div>
                </>
              )}
            </div>

            <div className="card">
              <h2 className="card-title">Hiệu suất tháo dỡ</h2>
              <p className="muted-text" style={{ margin: "4px 0 14px" }}>Thu hồi được gì từ {formatMoney(td.palletDaThaoDo)} pallet</p>
              {num(td.palletDaThaoDo) === 0 ? (
                <p className="muted-text">Chưa có đợt tháo dỡ đã duyệt trong kỳ.</p>
              ) : (
                <div className="rc-ledger">
                  <Row label="Thanh gỗ nguyên">{formatMoney(td.thanhNguyen)} ({formatMoney(td.thanhNguyenTrenPallet)} / pallet)</Row>
                  <Row label="Cục gỗ vuông">{formatMoney(td.cucVuong)} ({formatMoney(td.cucVuongTrenPallet)} / pallet)</Row>
                  <Row label="Thanh cần xử lý thêm">{formatMoney(td.thanhCanXuLy)}</Row>
                  <Row label="Cục cần xử lý thêm">{formatMoney(td.cucCanXuLy)}</Row>
                  <Row label="Tỷ lệ cần xử lý thêm">{pct(td.tyLeCanXuLy)}</Row>
                  <Row label="Phế liệu">{formatMoney(td.kgPheLieu)} kg ({formatMoney(td.kgPheLieuTrenPallet)} kg / pallet)</Row>
                </div>
              )}
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">Xử lý thêm thanh / cục gỗ</h2>
            <p className="muted-text" style={{ margin: "4px 0 14px" }}>Kết quả các phiếu xử lý thêm đã duyệt trong kỳ</p>
            <AppTable columns={colXuLy} data={xuLyThem} emptyText="Chưa có phiếu xử lý thêm đã duyệt trong kỳ" />
          </div>

          <div className="card">
            <h2 className="card-title">Giá mua và hiệu suất theo lô</h2>
            <p className="muted-text" style={{ margin: "4px 0 14px" }}>Các lô pallet cũ nhập về để tái chế trong kỳ, kèm tỷ lệ xử lý trên số pallet nhập</p>
            <AppTable columns={colLo} data={theoLo.slice((pageLo - 1) * PER_PAGE, pageLo * PER_PAGE)} emptyText="Chưa có lô tái chế nào nhập trong kỳ" />
            <div style={{ marginTop: 16 }}>
              <Pagination currentPage={pageLo} totalItems={theoLo.length} itemsPerPage={PER_PAGE} itemLabel="lô" onPageChange={setPageLo} />
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">Chênh lệch định mức đóng pallet</h2>
            <p className="muted-text" style={{ margin: "4px 0 14px" }}>
              {formatMoney(cl?.palletDaSanXuat)} pallet đã sản xuất từ {formatMoney(cl?.soPhieuDaDuyet)} phiếu đã duyệt. Dương là dùng vượt định mức, âm là tiết kiệm.
            </p>
            {vatTuLech.length > 0 && (
              <RecycleBarChart
                labels={vatTuLech.map((r) => r.ma_mat_hang)}
                values={vatTuLech.map((r) => num(r.chenh_lech))}
                colors={vatTuLech.map((r) => (num(r.chenh_lech) > 0 ? "#dc2626" : "#16a34a"))}
                unit="đơn vị"
              />
            )}
            <div style={{ marginTop: 16 }}>
              <AppTable columns={colVatTu} data={theoVatTu} emptyText="Chưa có phiếu đóng pallet đã duyệt trong kỳ" />
            </div>
            <h3 className="section-label" style={{ margin: "20px 0 10px" }}>CÁC DÒNG VẬT TƯ BỊ LỆCH</h3>
            <AppTable columns={colDongLech} data={dongLech.slice((pageLech - 1) * PER_PAGE, pageLech * PER_PAGE)} emptyText="Không có dòng vật tư nào lệch định mức" />
            <div style={{ marginTop: 16 }}>
              <Pagination currentPage={pageLech} totalItems={dongLech.length} itemsPerPage={PER_PAGE} itemLabel="dòng" onPageChange={setPageLech} />
            </div>
          </div>
        </>
      )}
    </div>
  );
}
