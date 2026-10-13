import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { FileSpreadsheet } from "lucide-react";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import FinanceTabs from "../../components/finance/FinanceTabs";
import FinanceToolbar from "../../components/finance/FinanceToolbar";
import { useFinanceReport } from "../../hooks/useFinanceReport";
import { taiChinhService } from "../../services/taiChinhService";
import { nhanKy } from "../../utils/matchTime";
import { notifyWarning } from "../../utils/notify";
import { formatDate, formatMoney, xuatCsv } from "../../utils/receiptFormat";
import "../../components/layout/style.css";

const PER_PAGE = 8;

export default function Tax() {
  const [time, setTime] = useState({ preset: "month", from: null, to: null });
  const { data, loading, range } = useFinanceReport(taiChinhService.thue, time);
  const [page, setPage] = useState(1);
  const label = nhanKy(time, range);
  const tyLe = data ? Number(data.tyLeVatMacDinh) : 0;

  const rows = data?.chiTiet ?? [];
  const pagedRows = useMemo(() => rows.slice((page - 1) * PER_PAGE, page * PER_PAGE), [rows, page]);

  const xuatExcel = () => {
    if (rows.length === 0) return notifyWarning("Không có hóa đơn nào để xuất");
    xuatCsv(`thue-gtgt-${label.replace(/[\/\s]/g, "-")}.csv`, [
      ["Số HĐ", "Ngày lập", "Khách hàng", "Doanh thu (chưa thuế)", "Thuế suất %", "Thuế GTGT"],
      ...rows.map((r) => [r.maHoaDon, formatDate(r.ngayLap), r.tenKhachHang, r.doanhThuChuaThue, r.tyLeVat, r.tienVat]),
      [],
      ["Tổng cộng", "", "", data.tongDoanhThuChuaThue, "", data.tongThue],
    ]);
  };

  const columns = [
    { key: "ma", header: "Số HĐ", width: "170px", render: (r) => <Link to={`/hoa-don/${r.id}`}><b>#{r.maHoaDon}</b></Link> },
    { key: "ngay", header: "Ngày lập", width: "120px", render: (r) => formatDate(r.ngayLap) },
    { key: "kh", header: "Khách hàng", render: (r) => r.tenKhachHang },
    { key: "dt", header: "Doanh thu (chưa thuế)", align: "right", render: (r) => `${formatMoney(r.doanhThuChuaThue)} đ` },
    {
      key: "thue",
      header: `Thuế GTGT (${tyLe}%)`,
      align: "right",
      render: (r) => (
        <div>
          <span className="fn-blue">{formatMoney(r.tienVat)} đ</span>
          {Number(r.tyLeVat) !== tyLe && <div className="muted-text">thuế suất {Number(r.tyLeVat)}%</div>}
        </div>
      ),
    },
  ];

  return (
    <div>
      <FinanceTabs />
      <FinanceToolbar time={time} onTimeChange={(next) => { setTime(next); setPage(1); }} />

      {!data ? (
        <p className="muted-text">{loading ? "Đang tải dữ liệu..." : "Không có dữ liệu"}</p>
      ) : (
        <>
          <div className="fn-banner">
            <div>
              <div className="fn-banner-title">Số thuế GTGT phải nộp</div>
              <div className="fn-banner-sub">Kỳ {label}</div>
            </div>
            <div className="fn-banner-amount">{formatMoney(data.tongThue)} đ</div>
          </div>

          <div className="card">
            <div className="card-header" style={{ marginBottom: 14 }}>
              <h2 className="card-title">Chi tiết thuế GTGT</h2>
              <button type="button" className="btn-accent rc-no-print" onClick={xuatExcel}>
                <FileSpreadsheet size={16} /> Xuất Excel
              </button>
            </div>

            <AppTable columns={columns} data={pagedRows} emptyText="Không có hóa đơn bán nào trong kỳ này" />

            <div className="fn-total-row">
              <span>Tổng cộng:</span>
              <span>{formatMoney(data.tongDoanhThuChuaThue)} đ</span>
              <span className="fn-blue">{formatMoney(data.tongThue)} đ</span>
            </div>

            <div style={{ marginTop: 16 }}>
              <Pagination
                currentPage={page}
                totalItems={rows.length}
                itemsPerPage={PER_PAGE}
                itemLabel="hóa đơn"
                onPageChange={setPage}
              />
            </div>
          </div>
        </>
      )}
    </div>
  );
}