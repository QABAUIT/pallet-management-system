import { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { InputNumber, Modal, Select } from "antd";
import { FileSpreadsheet, Pencil, Printer, Trash2 } from "lucide-react";
import AppTable from "../../components/common/AppTable";
import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import ReceiptStatusBadge from "../../components/receipt/ReceiptStatusBadge";
import ReceiptEditModal from "../../components/receipt/ReceiptEditModal";
import ReceiptCancelModal from "../../components/receipt/ReceiptCancelModal";
import { useAuth } from "../../store/AuthContext";
import { useReceiptDetail } from "../../hooks/useReceiptDetail";
import { PHUONG_THUC, docTienBangChu, formatDate, formatDateTime, formatMoney, xuatCsv } from "../../utils/receiptFormat";

const formatTien = (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

const itemColumns = [
  { key: "stt", header: "STT", width: "60px", align: "center", render: (d) => String(d.stt).padStart(2, "0") },
  { key: "maMatHang", header: "Mã pallet", width: "110px", render: (d) => <b>{d.maMatHang}</b> },
  {
    key: "ten",
    header: "Tên & mô tả chủng loại",
    render: (d) => (
      <div>
        <div className="rc-name">{d.tenMatHang}</div>
        {d.moTa && <div className="muted-text">{d.moTa}</div>}
      </div>
    ),
  },
  { key: "kichThuoc", header: "Kích thước (mm)", align: "center", render: (d) => d.kichThuoc || "—" },
  { key: "soLuong", header: "Số lượng", align: "right", width: "90px", render: (d) => d.soLuong },
  { key: "donGia", header: "Đơn giá", align: "right", render: (d) => formatMoney(d.donGia) },
  { key: "thanhTien", header: "Thành tiền", align: "right", render: (d) => <b>{formatMoney(d.thanhTien)} đ</b> },
];

export default function ReceiptDetail() {
  const { id } = useParams();
  const location = useLocation();
  const { user } = useAuth();
  const laQuanLy = ["GD", "PGD"].includes(user?.maVaiTro);
  const { hd, loading, saving, capNhat, thanhToan, xoa } = useReceiptDetail(id);

  const [modal, setModal] = useState(null); // 'sua' | 'tra' | 'xoa'
  const [phuongThuc, setPhuongThuc] = useState("tien_mat");
  const [tienKhachDua, setTienKhachDua] = useState(null);

  // Bấm bút chì ở lịch sử -> mở thẳng modal sửa
  useEffect(() => {
    if (hd && location.state?.moSua) setModal("sua");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hd?.id]);

  if (loading && !hd) return <p className="muted-text">Đang tải hóa đơn...</p>;
  if (!hd) return <p className="muted-text">Không tìm thấy hóa đơn</p>;

  const dangCho = hd.trangThai === "cho_thanh_toan";
  const khoaSua = hd.daChotSo || hd.trangThai === "da_huy";
  const thoiLai = phuongThuc === "tien_mat" ? Math.max(0, (tienKhachDua || 0) - hd.tongThanhToan) : 0;
  const dt = hd.doiTac;
  const nv = hd.nhanVienLap;

  const xuatExcel = () =>
    xuatCsv(`${hd.maHoaDon}.csv`, [
      ["STT", "Mã mặt hàng", "Tên mặt hàng", "Số lượng", "Đơn giá", "Chiết khấu %", "Thành tiền"],
      ...hd.chiTiet.map((d) => [d.stt, d.maMatHang, d.tenMatHang, d.soLuong, d.donGia, d.tyLeChietKhau, d.thanhTien]),
      [],
      ["", "", "", "", "", "Tiền trước thuế", hd.tienTruocThue],
      ["", "", "", "", "", `VAT ${hd.tyLeVat}%`, hd.tienVat],
      ["", "", "", "", "", "Tổng thanh toán", hd.tongThanhToan],
    ]);

  return (
    <div>
      <ReceiptTabs />

      <div className="card">
        <div className="card-header">
          <div>
            <ReceiptStatusBadge hd={hd} />
            <h2 className="card-title" style={{ marginTop: 8, fontSize: 22 }}>Hóa đơn: {hd.maHoaDon}</h2>
            <p className="muted-text" style={{ margin: "4px 0 0" }}>
              Lập lúc {formatDateTime(hd.ngayLap)} · Hạn thanh toán {formatDate(hd.hanThanhToan)}
              {hd.ngayThanhToan && ` · Đã thanh toán ${formatDateTime(hd.ngayThanhToan)}`}
            </p>
          </div>
          <div className="rc-actions rc-no-print">
            <button className="btn-outline" onClick={() => window.print()}>
              <Printer size={16} /> In chi tiết (PDF)
            </button>
            <button className="btn-outline" onClick={xuatExcel}>
              <FileSpreadsheet size={16} /> Xuất Excel
            </button>
          </div>
        </div>

        <div className="card-divider" />

        <div className="info-box-grid">
          <div className="info-box">
            <p className="info-box-label">{dt?.loai === "nha_cung_cap" ? "NHÀ CUNG CẤP" : "KHÁCH HÀNG"}</p>
            <p className="info-box-value">{dt?.ten || "—"}</p>
            <p className="info-box-sub">Mã: {dt?.ma || "—"} · MST: {dt?.mst || "—"}</p>
            <p className="info-box-sub">Đại diện: {dt?.nguoiDaiDien || "—"} · {dt?.sdt || "—"}</p>
            <p className="info-box-sub">{dt?.diaChi || "Chưa có địa chỉ"}</p>
          </div>
          <div className="info-box">
            <p className="info-box-label">NHÂN VIÊN LẬP HÓA ĐƠN</p>
            <p className="info-box-value">{nv.hoTen}</p>
            <p className="info-box-sub">Mã NV: {nv.maNv} · {nv.chucVu || "—"}</p>
            <p className="info-box-sub">Bộ phận: {nv.tenBoPhan || "—"}</p>
            <p className="info-box-sub">Kho xuất: {hd.tenKho || "—"}</p>
          </div>
        </div>

        <div className="card-divider" />

        <div className="card-top-row">
          <p className="section-label" style={{ margin: 0 }}>DANH MỤC PALLET TRONG HÓA ĐƠN</p>
          {!khoaSua && (
            <button className="btn-outline rc-btn-sm rc-no-print" onClick={() => setModal("sua")}>
              <Pencil size={13} /> Chỉnh sửa
            </button>
          )}
        </div>
        <AppTable columns={itemColumns} data={hd.chiTiet} />

        <div className="rc-bottom">
          <div>
            <p className="section-label">GHI CHÚ GIAO DỊCH</p>
            <div className="rc-note">{hd.ghiChu || "Không có ghi chú."}</div>
            <p className="rc-log" style={{ margin: "10px 0 0" }}>
              Phương thức: {PHUONG_THUC[hd.phuongThucThanhToan] || "Chưa thanh toán"}
            </p>
            {hd.lichSu?.slice(0, 4).map((l, i) => (
              <div className="rc-log" key={i}>
                {formatDateTime(l.thoiGian)} · {l.nguoiSua}: {l.lyDo || `${l.hanhDong} ${l.truongThayDoi || ""}`}
              </div>
            ))}
          </div>

          <div className="rc-ledger">
            <div className="rc-ledger-row"><span>Tiền hàng</span><b>{formatMoney(hd.tongTienHang)} đ</b></div>
            {Number(hd.tienGiamGiaTong) > 0 && (
              <div className="rc-ledger-row"><span>Giảm giá ({hd.tyLeGiamGiaTong}%)</span><b>-{formatMoney(hd.tienGiamGiaTong)} đ</b></div>
            )}
            {Number(hd.phiGiaoHang) > 0 && (
              <div className="rc-ledger-row"><span>Phí giao hàng</span><b>{formatMoney(hd.phiGiaoHang)} đ</b></div>
            )}
            <div className="rc-ledger-row"><span>Tiền trước thuế</span><b>{formatMoney(hd.tienTruocThue)} đ</b></div>
            <div className="rc-ledger-row"><span>Thuế VAT ({Number(hd.tyLeVat)}%)</span><b>{formatMoney(hd.tienVat)} đ</b></div>
            <div className="rc-ledger-total">
              <strong>Tổng thanh toán</strong>
              <span>{formatMoney(hd.tongThanhToan)} đ</span>
            </div>
            {hd.trangThai === "da_thanh_toan" && hd.phuongThucThanhToan === "tien_mat" && (
              <div className="rc-ledger-row">
                <span>Khách đưa / thối lại</span>
                <b>{formatMoney(hd.tienKhachDua)} / {formatMoney(hd.tienThoiLai)} đ</b>
              </div>
            )}
            <div className="rc-words">Bằng chữ: {docTienBangChu(hd.tongThanhToan)}</div>
          </div>
        </div>

        {!khoaSua && dangCho && (
          <div className="rc-foot rc-no-print">
            {laQuanLy && (
              <button className="btn-danger" onClick={() => setModal("xoa")}>
                <Trash2 size={16} /> Xóa hóa đơn
              </button>
            )}
            <button
              className="btn-solid"
              onClick={() => {
                setPhuongThuc("tien_mat");
                setTienKhachDua(Number(hd.tongThanhToan));
                setModal("tra");
              }}
            >
              Thanh toán ngay
            </button>
          </div>
        )}
      </div>

      <ReceiptEditModal
        open={modal === "sua"}
        hd={hd}
        saving={saving}
        onCancel={() => setModal(null)}
        onSubmit={async (payload) => {
          if (await capNhat(payload)) setModal(null);
        }}
      />

      <ReceiptCancelModal
        open={modal === "xoa"}
        ma={hd.maHoaDon}
        loading={saving}
        onCancel={() => setModal(null)}
        onOk={async (lyDo) => {
          if (await xoa(lyDo)) setModal(null);
        }}
      />

      <Modal
        title={`Thanh toán ${hd.maHoaDon}`}
        open={modal === "tra"}
        onCancel={() => setModal(null)}
        confirmLoading={saving}
        okText="Xác nhận thanh toán"
        cancelText="Đóng"
        onOk={async () => {
          const ok = await thanhToan({ phuongThuc, tienKhachDua: phuongThuc === "tien_mat" ? tienKhachDua : null });
          if (ok) setModal(null);
        }}
      >
        <div style={{ display: "grid", gap: 12 }}>
          <div className="rc-ledger-total"><strong>Cần thanh toán</strong><span>{formatMoney(hd.tongThanhToan)} đ</span></div>
          <div>
            <span className="rc-label">Phương thức</span>
            <Select
              value={phuongThuc}
              onChange={setPhuongThuc}
              style={{ width: "100%" }}
              options={Object.entries(PHUONG_THUC).map(([value, label]) => ({ value, label }))}
            />
          </div>
          {phuongThuc === "tien_mat" && (
            <>
              <div>
                <span className="rc-label">Số tiền khách đưa</span>
                <InputNumber
                  value={tienKhachDua}
                  onChange={setTienKhachDua}
                  min={0}
                  style={{ width: "100%" }}
                  formatter={formatTien}
                  parser={(v) => v.replace(/\./g, "")}
                  addonAfter="VND"
                />
              </div>
              <div className="rc-change"><small>Tiền thối lại</small><strong>{formatMoney(thoiLai)} đ</strong></div>
            </>
          )}
        </div>
      </Modal>
    </div>
  );
}