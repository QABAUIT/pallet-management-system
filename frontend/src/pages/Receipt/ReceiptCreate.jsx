import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { InputNumber, message, Select } from "antd";
import { FilePlus2, Plus, RefreshCw, Search, X } from "lucide-react";
import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import { hoaDonApi } from "../../api/hoaDonApi";
import { PHUONG_THUC, formatDate, formatMoney } from "../../utils/receiptFormat";
import "./Receipt.css";

const VAT = 8; // %
const tienMat = (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

export default function ReceiptCreate() {
  const navigate = useNavigate();
  const [dm, setDm] = useState({ khachHang: [], matHang: [], tenKho: "" });
  const [khachHangId, setKhachHangId] = useState(null);
  const [items, setItems] = useState([]); // [{ matHangId, soLuong }]
  const [dangThem, setDangThem] = useState(false);
  const [phuongThuc, setPhuongThuc] = useState("tien_mat");
  const [tienKhachDua, setTienKhachDua] = useState(null);
  const [dangGui, setDangGui] = useState(false);
  const [chuaTT, setChuaTT] = useState([]);
  const [tuKhoa, setTuKhoa] = useState("");

  const taiDanhMuc = useCallback(
    () =>
      hoaDonApi
        .danhMucBanHang()
        .then((res) => setDm(res.data))
        .catch((err) => message.error(err.message)),
    [],
  );
  const taiChuaTT = useCallback(
    () =>
      hoaDonApi
        .danhSach("cho_thanh_toan")
        .then((res) => setChuaTT((res.data || []).filter((r) => r.laThu)))
        .catch((err) => message.error(err.message)),
    [],
  );

  useEffect(() => {
    taiDanhMuc();
    taiChuaTT();
  }, [taiDanhMuc, taiChuaTT]);

  const mhMap = useMemo(() => new Map(dm.matHang.map((m) => [m.id, m])), [dm.matHang]);
  const khach = dm.khachHang.find((k) => k.id === khachHangId);

  const dong = items.map((it) => {
    const mh = mhMap.get(it.matHangId);
    return { ...it, mh, thanhTien: mh.donGia * it.soLuong };
  });
  const tongHang = dong.reduce((s, d) => s + d.thanhTien, 0);
  const vat = Math.round((tongHang * VAT) / 100);
  const tong = tongHang + vat;
  const thoiLai = phuongThuc === "tien_mat" ? Math.max(0, (tienKhachDua || 0) - tong) : 0;

  const chonThem = (matHangId) => {
    setItems((cur) => [...cur, { matHangId, soLuong: 1 }]);
    setDangThem(false);
  };
  const doiSoLuong = (matHangId, soLuong) =>
    setItems((cur) => cur.map((i) => (i.matHangId === matHangId ? { ...i, soLuong: soLuong || 1 } : i)));
  const xoaDong = (matHangId) => setItems((cur) => cur.filter((i) => i.matHangId !== matHangId));

  async function gui(thanhToanNgay) {
    if (!khachHangId) return message.warning("Vui lòng chọn khách hàng");
    if (items.length === 0) return message.warning("Vui lòng thêm ít nhất 1 loại pallet");
    if (thanhToanNgay && phuongThuc === "tien_mat" && (tienKhachDua || 0) < tong) {
      return message.warning("Số tiền khách đưa chưa đủ để thanh toán");
    }
    setDangGui(true);
    try {
      const res = await hoaDonApi.taoMoi({
        khachHangId,
        chiTiet: items.map(({ matHangId, soLuong }) => ({ matHangId, soLuong })),
        tyLeVat: VAT,
        thanhToanNgay,
        phuongThucThanhToan: thanhToanNgay ? phuongThuc : null,
        tienKhachDua: thanhToanNgay && phuongThuc === "tien_mat" ? tienKhachDua : null,
      });
      message.success(`${res.message}: ${res.data.maHoaDon}`);
      setItems([]);
      setKhachHangId(null);
      setTienKhachDua(null);
      await Promise.all([taiDanhMuc(), taiChuaTT()]);
      if (thanhToanNgay) navigate(`/hoa-don/${res.data.id}`);
    } catch (err) {
      message.error(err.message);
    } finally {
      setDangGui(false);
    }
  }

  const q = tuKhoa.trim().toLowerCase();
  const dsChuaTT = chuaTT.filter(
    (r) => !q || [r.maHoaDon, r.tenDoiTac, r.sdtDoiTac].some((v) => v && v.toLowerCase().includes(q)),
  );
  const tongCongNo = chuaTT.reduce((s, r) => s + Number(r.tongThanhToan), 0);

  return (
    <div className="rc-page">
      <ReceiptTabs />

      <div className="rc-create">
        {/* ===== Cột trái: form tạo hóa đơn ===== */}
        <section className="rc-panel">
          <div className="rc-panel-head">
            <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <div className="rc-logo"><FilePlus2 size={16} /></div>
              <div>
                <h2>Tạo hóa đơn bán pallet mới</h2>
                <p>Phiếu xuất kho tích hợp đối soát tự động{dm.tenKho ? ` · ${dm.tenKho}` : ""}</p>
              </div>
            </div>
            <div className="rc-code">Mã chứng từ:<b>Tự động</b></div>
          </div>

          <div className="rc-row2">
            <div>
              <label className="rc-label">Tên khách hàng / doanh nghiệp đối tác</label>
              <Select
                showSearch
                allowClear
                placeholder="Chọn khách hàng"
                style={{ width: "100%" }}
                value={khachHangId}
                onChange={setKhachHangId}
                optionFilterProp="label"
                options={dm.khachHang.map((k) => ({ value: k.id, label: `${k.maKh} - ${k.tenKh}` }))}
              />
            </div>
            <div>
              <label className="rc-label">Số điện thoại đối tác</label>
              <div className="rc-code" style={{ background: "#fff", padding: "6px 9px", fontSize: 12, color: "#0b1c30" }}>
                {khach?.sdt || "—"}
              </div>
            </div>
          </div>

          <div className="rc-sub-head">
            <div style={{ display: "flex", alignItems: "center" }}>
              <h3>Chi tiết pallet xuất bán</h3>
              <span className="rc-pill">{items.length} hạng mục đã chọn</span>
            </div>
            <button className="rc-link" onClick={() => setDangThem(true)}>
              <Plus size={12} /> Thêm loại pallet
            </button>
          </div>

          {dangThem && (
            <Select
              autoFocus
              open
              showSearch
              placeholder="Chọn loại pallet / linh kiện"
              style={{ width: "100%", marginBottom: 8 }}
              optionFilterProp="label"
              onChange={chonThem}
              onBlur={() => setDangThem(false)}
              options={dm.matHang
                .filter((m) => !items.some((i) => i.matHangId === m.id))
                .map((m) => ({
                  value: m.id,
                  disabled: m.tonKhaDung <= 0,
                  label: `${m.tenMatHang} (${m.maMatHang}) · tồn ${m.tonKhaDung}`,
                }))}
            />
          )}

          {dong.length === 0 && <div className="rc-empty">Chưa có mặt hàng nào. Bấm "Thêm loại pallet".</div>}

          {dong.map((d) => (
            <div className="rc-line" key={d.matHangId}>
              <div>
                <div className="rc-line-name">{d.mh.tenMatHang}</div>
                <div className="rc-line-desc">{d.mh.kichThuoc ? `${d.mh.kichThuoc} mm` : d.mh.maMatHang}</div>
              </div>
              <div className="rc-stock">Tồn:<br />{d.mh.tonKhaDung}<br />cái</div>
              <div>
                <div className="rc-col-label" style={{ textAlign: "left" }}>Số lượng</div>
                <InputNumber
                  size="small"
                  min={1}
                  max={Math.max(1, d.mh.tonKhaDung)}
                  value={d.soLuong}
                  onChange={(v) => doiSoLuong(d.matHangId, v)}
                  style={{ width: "100%" }}
                />
              </div>
              <div>
                <div className="rc-col-label">Đơn giá</div>
                <div className="rc-col-val">{formatMoney(d.mh.donGia)} đ</div>
              </div>
              <div>
                <div className="rc-col-label">Thành tiền</div>
                <div className="rc-col-val strong">{formatMoney(d.thanhTien)} đ</div>
              </div>
              <button className="rc-icon-btn" title="Bỏ dòng này" onClick={() => xoaDong(d.matHangId)}>
                <X size={13} />
              </button>
            </div>
          ))}

          <div className="rc-summary">
            <div className="rc-summary-rows">
              <div className="rc-sum-row"><span>Tổng tiền hàng ({items.length} loại pallet):</span><b>{formatMoney(tongHang)} đ</b></div>
              <div className="rc-sum-row"><span>Thuế suất VAT ({VAT}%):</span><b style={{ color: "#904d00" }}>+{formatMoney(vat)} đ</b></div>
              <div className="rc-sum-total"><span style={{ fontSize: 14, fontFamily: "inherit" }}>Tổng cộng thanh toán:</span><span>{formatMoney(tong)} đ</span></div>
            </div>

            <div style={{ paddingTop: 16 }}>
              <label className="rc-label">Phương thức thanh toán</label>
              <Select
                value={phuongThuc}
                onChange={setPhuongThuc}
                style={{ width: "100%" }}
                options={Object.entries(PHUONG_THUC).map(([value, label]) => ({ value, label }))}
              />
            </div>

            <div className="rc-cash">
              <div>
                <label className="rc-label">Số tiền khách đưa/nhận:</label>
                <InputNumber
                  value={phuongThuc === "tien_mat" ? tienKhachDua : tong}
                  disabled={phuongThuc !== "tien_mat"}
                  onChange={setTienKhachDua}
                  min={0}
                  style={{ width: "100%" }}
                  formatter={tienMat}
                  parser={(v) => v.replace(/\./g, "")}
                  addonAfter="VND"
                />
              </div>
              <div className="rc-change">
                <small>Tiền thối lại khách hàng:</small>
                <strong>{formatMoney(thoiLai)} đ</strong>
              </div>
            </div>
          </div>

          <div className="rc-actions">
            <button className="rc-btn" disabled={dangGui} onClick={() => gui(false)}>
              Xác nhận thêm hóa đơn<br />(Chờ thanh toán)
            </button>
            <button className="rc-btn rc-btn-primary" disabled={dangGui} onClick={() => gui(true)}>
              Xác nhận thanh toán ngay
            </button>
          </div>
        </section>

        {/* ===== Cột phải: hóa đơn chưa thanh toán ===== */}
        <section className="rc-panel">
          <div className="rc-panel-head" style={{ marginBottom: 12 }}>
            <h2>Danh sách hóa đơn chưa thanh toán</h2>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span className="rc-badge rc-amber">{chuaTT.length} đơn chờ</span>
              <button className="rc-icon-btn" title="Tải lại" onClick={taiChuaTT}><RefreshCw size={13} /></button>
            </div>
          </div>

          <div style={{ position: "relative", marginBottom: 12 }}>
            <Search size={12} style={{ position: "absolute", left: 10, top: 11, color: "#74777e" }} />
            <input
              value={tuKhoa}
              onChange={(e) => setTuKhoa(e.target.value)}
              placeholder="Tìm theo mã HD, tên đối tác..."
              style={{ width: "100%", boxSizing: "border-box", border: "1px solid #c3c6ce", borderRadius: 2, padding: "7px 9px 8px 33px", fontSize: 12 }}
            />
          </div>

          {dsChuaTT.length === 0 ? (
            <div className="rc-empty">Không có hóa đơn nào chờ thanh toán.</div>
          ) : (
            <table className="rc-unpaid">
              <thead>
                <tr>
                  <th>Mã HĐ</th>
                  <th>Khách hàng &amp; SĐT</th>
                  <th className="r">Tổng tiền</th>
                  <th>Trạng thái</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {dsChuaTT.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <div className="rc-cell-main" style={{ fontSize: 10 }}>{r.maHoaDon}</div>
                      <div className="rc-cell-sub">{formatDate(r.ngayLap)}</div>
                    </td>
                    <td>
                      <div className="rc-cell-name">{r.tenDoiTac}</div>
                      <div className="rc-cell-sub">{r.sdtDoiTac}</div>
                      <div className="rc-cell-sub">(Hạn: {formatDate(r.hanThanhToan)})</div>
                    </td>
                    <td className="r">
                      <div className="rc-mono" style={{ fontSize: 11, fontWeight: 700 }}>{formatMoney(r.tongThanhToan)}</div>
                      <div style={{ fontSize: 10, color: r.quaHan ? "#ba1a1a" : "#43474d" }}>
                        {r.quaHan ? `Quá hạn ${r.soNgayQuaHan} ngày` : "Trong kỳ hạn"}
                      </div>
                    </td>
                    <td><span className="rc-badge rc-amber">Chờ thanh toán</span></td>
                    <td>
                      <button className="rc-pay-btn" onClick={() => navigate(`/hoa-don/${r.id}`)}>Chi tiết</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <div className="rc-debt-foot">
            <span>Tổng công nợ cần thu:</span>
            <b>{formatMoney(tongCongNo)} đ</b>
          </div>
        </section>
      </div>
    </div>
  );
}