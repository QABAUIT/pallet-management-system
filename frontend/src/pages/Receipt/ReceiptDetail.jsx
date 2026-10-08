import { useCallback, useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { DatePicker, Input, InputNumber, message, Modal, Select } from "antd";
import dayjs from "dayjs";
import { ArrowLeft, CheckCircle2, FileSpreadsheet, ListChecks, Pencil, Printer, StickyNote } from "lucide-react";
import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import { useAuth } from "../../store/AuthContext";
import { hoaDonApi } from "../../api/hoaDonApi";
import {
  PHUONG_THUC,
  docTienBangChu,
  formatDate,
  formatDateTime,
  formatMoney,
  trangThaiHienThi,
  xuatCsv,
} from "../../utils/receiptFormat";
import "./Receipt.css";

const tienMat = (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

export default function ReceiptDetail() {
  const { id } = useParams();
  const location = useLocation();
  const { user } = useAuth();
  const laQuanLy = ["GD", "PGD"].includes(user?.maVaiTro);

  const [hd, setHd] = useState(null);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'sua' | 'tra' | 'huy'
  const [saving, setSaving] = useState(false);
  const [ghiChu, setGhiChu] = useState("");
  const [han, setHan] = useState(null);
  const [phuongThuc, setPhuongThuc] = useState("tien_mat");
  const [tienKhachDua, setTienKhachDua] = useState(null);
  const [lyDo, setLyDo] = useState("");

  const tai = useCallback(() => {
    setLoading(true);
    return hoaDonApi
      .chiTiet(id)
      .then((res) => setHd(res.data))
      .catch((err) => message.error(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    tai();
  }, [tai]);

  useEffect(() => {
    if (hd && location.state?.moSua) moSua();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hd?.id]);

  if (loading && !hd) return <div className="rc-page"><div className="rc-loading">Đang tải hóa đơn...</div></div>;
  if (!hd) return <div className="rc-page"><div className="rc-loading">Không tìm thấy hóa đơn</div></div>;

  const t = trangThaiHienThi(hd);
  const dangCho = hd.trangThai === "cho_thanh_toan";
  const khoaSua = hd.daChotSo || hd.trangThai === "da_huy";
  const thoiLai = phuongThuc === "tien_mat" ? Math.max(0, (tienKhachDua || 0) - hd.tongThanhToan) : 0;

  function moSua() {
    setGhiChu(hd.ghiChu || "");
    setHan(hd.hanThanhToan ? dayjs(hd.hanThanhToan) : null);
    setModal("sua");
  }

  async function chay(fn, thongBao) {
    setSaving(true);
    try {
      await fn();
      message.success(thongBao);
      setModal(null);
      await tai();
    } catch (err) {
      message.error(err.message);
    } finally {
      setSaving(false);
    }
  }

  const luuSua = () =>
    chay(
      () => hoaDonApi.capNhat(hd.id, { ghiChu, hanThanhToan: han ? han.format("YYYY-MM-DD") : null }),
      "Đã cập nhật hóa đơn",
    );
  const thanhToan = () =>
    chay(
      () => hoaDonApi.thanhToan(hd.id, { phuongThuc, tienKhachDua: phuongThuc === "tien_mat" ? tienKhachDua : null }),
      "Thanh toán thành công",
    );
  const huy = () => chay(() => hoaDonApi.huy(hd.id, lyDo), "Đã hủy hóa đơn");

  const xuatExcel = () =>
    xuatCsv(`${hd.maHoaDon}.csv`, [
      ["STT", "Mã mặt hàng", "Tên mặt hàng", "Số lượng", "Đơn giá", "Chiết khấu %", "Thành tiền"],
      ...hd.chiTiet.map((d) => [d.stt, d.maMatHang, d.tenMatHang, d.soLuong, d.donGia, d.tyLeChietKhau, d.thanhTien]),
      [],
      ["", "", "", "", "", "Tiền trước thuế", hd.tienTruocThue],
      ["", "", "", "", "", `VAT ${hd.tyLeVat}%`, hd.tienVat],
      ["", "", "", "", "", "Tổng thanh toán", hd.tongThanhToan],
    ]);

  const dt = hd.doiTac;
  const nv = hd.nhanVienLap;

  return (
    <div className="rc-page">
      <ReceiptTabs />
      <Link to="/hoa-don" className="rc-back rc-no-print">
        <ArrowLeft size={14} /> Quay lại lịch sử giao dịch
      </Link>

      <div className="rc-body" style={{ marginTop: 12 }}>
        <div className="rc-doc">
          <div className="rc-doc-head">
            <div>
              <span className={`rc-status rc-${t.tone}`}>
                <i /> {t.label}
              </span>
              <h1 className="rc-doc-title">Hóa đơn : {hd.maHoaDon}</h1>
              <div className="rc-doc-meta">
                <span>Thời gian lập: <b>{formatDateTime(hd.ngayLap)}</b></span>
                <span>Hạn thanh toán: <b>{formatDate(hd.hanThanhToan)}</b></span>
                {hd.ngayThanhToan && <span>Đã thanh toán lúc: <b>{formatDateTime(hd.ngayThanhToan)}</b></span>}
              </div>
            </div>
            <div className="rc-doc-actions rc-no-print">
              <button className="rc-btn" onClick={() => window.print()}>
                <Printer size={15} /> In chi tiết hóa đơn (PDF)
              </button>
              <button className="rc-btn" onClick={xuatExcel}>
                <FileSpreadsheet size={15} /> Xuất Excel chi tiết
              </button>
            </div>
          </div>

          <div className="rc-info">
            <div className="rc-info-card" style={{ "--accent": "#904d00" }}>
              <div className="rc-info-label">{dt?.loai === "nha_cung_cap" ? "Nhà cung cấp" : "Khách hàng"}</div>
              <div className="rc-info-name">{dt?.ten || "—"}</div>
              <div className="rc-kv"><span>Mã:</span><b>{dt?.ma || "—"}</b></div>
              <div className="rc-kv"><span>MST:</span><b>{dt?.mst || "—"}</b></div>
              <div className="rc-kv"><span>Đại diện:</span><b>{dt?.nguoiDaiDien || "—"}</b></div>
              <div className="rc-kv"><span>Hotline:</span><b>{dt?.sdt || "—"}</b></div>
              <div className="rc-info-foot">{dt?.diaChi || "Chưa có địa chỉ"}</div>
            </div>
            <div className="rc-info-card" style={{ "--accent": "#0f2942" }}>
              <div className="rc-info-label">Nhân viên lập hóa đơn</div>
              <div className="rc-info-name">{nv.hoTen}</div>
              <div className="rc-kv"><span>Mã nhân viên:</span><b>{nv.maNv}</b></div>
              <div className="rc-kv"><span>Chức vụ:</span><b>{nv.chucVu || "—"}</b></div>
              <div className="rc-kv"><span>Bộ phận:</span><b>{nv.tenBoPhan || "—"}</b></div>
              <div className="rc-kv"><span>Kho xuất:</span><b>{hd.tenKho || "—"}</b></div>
              <div className="rc-info-foot" style={{ color: "#047857", display: "flex", gap: 4, alignItems: "center" }}>
                <CheckCircle2 size={12} /> Hóa đơn được lập trên hệ thống
              </div>
            </div>
          </div>

          <div className="rc-section">
            <div className="rc-section-head">
              <h2 className="rc-section-title"><ListChecks size={16} /> Danh mục pallet trong hóa đơn</h2>
              {!khoaSua && (
                <button className="rc-link rc-no-print" onClick={moSua}>
                  <Pencil size={11} /> Chỉnh sửa
                </button>
              )}
            </div>
            <div style={{ overflowX: "auto" }}>
              <table className="rc-items">
                <thead>
                  <tr>
                    <th>STT</th>
                    <th>Mã pallet</th>
                    <th className="l">Tên &amp; mô tả chủng loại</th>
                    <th>Kích thước<br />(DxRxC mm)</th>
                    <th className="r">Số lượng<br />(cái)</th>
                    <th className="r">Đơn giá<br />(VNĐ)</th>
                    <th className="r">Thành tiền (VNĐ)</th>
                  </tr>
                </thead>
                <tbody>
                  {hd.chiTiet.map((d) => (
                    <tr key={d.stt}>
                      <td>{String(d.stt).padStart(2, "0")}</td>
                      <td style={{ fontWeight: 700 }}>{d.maMatHang}</td>
                      <td className="l">
                        <div className="rc-item-name">{d.tenMatHang}</div>
                        {d.moTa && <div className="rc-item-desc">{d.moTa}</div>}
                      </td>
                      <td>{d.kichThuoc || "—"}</td>
                      <td className="r">{d.soLuong}</td>
                      <td className="r">{formatMoney(d.donGia)}</td>
                      <td className="r" style={{ fontWeight: 700 }}>{formatMoney(d.thanhTien)} đ</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="rc-bottom">
            <div className="rc-note">
              <div className="rc-note-title"><StickyNote size={12} /> Ghi chú giao dịch</div>
              <p>{hd.ghiChu || "Không có ghi chú."}</p>
              <div className="rc-kv"><span>Phương thức:</span><b>{PHUONG_THUC[hd.phuongThucThanhToan] || "Chưa thanh toán"}</b></div>
              {hd.lichSu?.length > 0 && (
                <div className="rc-log">
                  {hd.lichSu.slice(0, 4).map((l, i) => (
                    <div key={i}>
                      {formatDateTime(l.thoiGian)} - {l.nguoiSua}: {l.lyDo || `${l.hanhDong} ${l.truongThayDoi || ""}`}
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="rc-ledger">
              <div className="rc-ledger-row"><span>Tiền hàng:</span><b>{formatMoney(hd.tongTienHang)} đ</b></div>
              {Number(hd.tienGiamGiaTong) > 0 && (
                <div className="rc-ledger-row"><span>Giảm giá ({hd.tyLeGiamGiaTong}%):</span><b>-{formatMoney(hd.tienGiamGiaTong)} đ</b></div>
              )}
              {Number(hd.phiGiaoHang) > 0 && (
                <div className="rc-ledger-row"><span>Phí giao hàng:</span><b>{formatMoney(hd.phiGiaoHang)} đ</b></div>
              )}
              <div className="rc-ledger-row"><span>Tiền trước thuế:</span><b>{formatMoney(hd.tienTruocThue)} đ</b></div>
              <div className="rc-ledger-row"><span>Thuế VAT ({Number(hd.tyLeVat)}%):</span><b>{formatMoney(hd.tienVat)} đ</b></div>
              <div className="rc-ledger-total">
                <strong>Tổng tiền thanh toán:</strong>
                <span>{formatMoney(hd.tongThanhToan)} đ</span>
              </div>
              {hd.trangThai === "da_thanh_toan" && hd.phuongThucThanhToan === "tien_mat" && (
                <div className="rc-ledger-row">
                  <span>Khách đưa / thối lại:</span>
                  <b>{formatMoney(hd.tienKhachDua)} / {formatMoney(hd.tienThoiLai)} đ</b>
                </div>
              )}
              <div className="rc-words">
                Số tiền viết bằng chữ:
                <em>{docTienBangChu(hd.tongThanhToan)}</em>
              </div>
            </div>
          </div>

          {!khoaSua && (
            <div className="rc-doc-foot rc-no-print">
              {laQuanLy && dangCho && (
                <button className="rc-btn rc-btn-danger" onClick={() => { setLyDo(""); setModal("huy"); }}>
                  Hủy hóa đơn
                </button>
              )}
              {dangCho && (
                <button
                  className="rc-btn-accent rc-btn"
                  onClick={() => { setPhuongThuc("tien_mat"); setTienKhachDua(Number(hd.tongThanhToan)); setModal("tra"); }}
                >
                  Thanh toán ngay
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      <Modal title="Chỉnh sửa hóa đơn" open={modal === "sua"} onCancel={() => setModal(null)} onOk={luuSua}
        confirmLoading={saving} okText="Lưu" cancelText="Đóng">
        <div style={{ display: "grid", gap: 12 }}>
          <div>
            <label className="rc-label">Hạn thanh toán</label>
            <DatePicker value={han} onChange={setHan} format="DD/MM/YYYY" style={{ width: "100%" }} />
          </div>
          <div>
            <label className="rc-label">Ghi chú</label>
            <Input.TextArea rows={4} value={ghiChu} onChange={(e) => setGhiChu(e.target.value)} maxLength={2000} />
          </div>
          <div className="rc-muted" style={{ fontSize: 12 }}>
            Muốn đổi mặt hàng hoặc số lượng: hủy hóa đơn này và tạo hóa đơn mới.
          </div>
        </div>
      </Modal>

      <Modal title={`Thanh toán ${hd.maHoaDon}`} open={modal === "tra"} onCancel={() => setModal(null)}
        onOk={thanhToan} confirmLoading={saving} okText="Xác nhận thanh toán" cancelText="Đóng">
        <div style={{ display: "grid", gap: 12 }}>
          <div className="rc-ledger-total"><strong>Cần thanh toán:</strong><span>{formatMoney(hd.tongThanhToan)} đ</span></div>
          <div>
            <label className="rc-label">Phương thức</label>
            <Select value={phuongThuc} onChange={setPhuongThuc} style={{ width: "100%" }}
              options={Object.entries(PHUONG_THUC).map(([value, label]) => ({ value, label }))} />
          </div>
          {phuongThuc === "tien_mat" && (
            <>
              <div>
                <label className="rc-label">Số tiền khách đưa</label>
                <InputNumber value={tienKhachDua} onChange={setTienKhachDua} min={0} style={{ width: "100%" }}
                  formatter={tienMat} parser={(v) => v.replace(/\./g, "")} addonAfter="VND" />
              </div>
              <div className="rc-change"><small>Tiền thối lại</small><strong>{formatMoney(thoiLai)} đ</strong></div>
            </>
          )}
        </div>
      </Modal>

      <Modal title={`Hủy ${hd.maHoaDon}`} open={modal === "huy"} onCancel={() => setModal(null)} onOk={huy}
        confirmLoading={saving} okText="Xác nhận hủy" okButtonProps={{ danger: true, disabled: !lyDo.trim() }} cancelText="Đóng">
        <label className="rc-label">Lý do hủy</label>
        <Input.TextArea rows={3} value={lyDo} onChange={(e) => setLyDo(e.target.value)} maxLength={255} />
      </Modal>
    </div>
  );
}