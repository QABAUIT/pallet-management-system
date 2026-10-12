import { useEffect, useMemo, useState } from "react";
import { DatePicker, Input, InputNumber, Modal, Select } from "antd";
import dayjs from "dayjs";
import { Plus, X } from "lucide-react";
import { useSalesCatalog } from "../../hooks/useSalesCatalog";
import { notifyWarning } from "../../utils/notify";
import { formatMoney } from "../../utils/receiptFormat";
import "../layout/style.css";

/**
 * Sửa hóa đơn.
 * - Chờ thanh toán: sửa khách hàng, mặt hàng, số lượng, hạn thanh toán, ghi chú.
 * - Đã thanh toán: chỉ sửa ghi chú.
 * Lưu ý: sửa mặt hàng thì backend tính lại theo giá bán hiện hành.
 */
export default function ReceiptEditModal({ open, hd, saving, onCancel, onSubmit }) {
  const choTT = hd.trangThai === "cho_thanh_toan";
  const { dm } = useSalesCatalog(open && choTT);
  const [khachHangId, setKhachHangId] = useState(null);
  const [items, setItems] = useState([]);
  const [han, setHan] = useState(null);
  const [ghiChu, setGhiChu] = useState("");
  const [dangThem, setDangThem] = useState(false);

  useEffect(() => {
    if (!open) return;
    setKhachHangId(hd.doiTac?.id ?? null);
    setItems(hd.chiTiet.map((d) => ({ matHangId: d.matHangId, soLuong: d.soLuong, tyLeChietKhau: d.tyLeChietKhau })));
    setHan(hd.hanThanhToan ? dayjs(hd.hanThanhToan) : null);
    setGhiChu(hd.ghiChu || "");
    setDangThem(false);
  }, [open, hd]);

  const mhMap = useMemo(() => new Map(dm.matHang.map((m) => [m.id, m])), [dm.matHang]);
  const daGiu = useMemo(() => new Map(hd.chiTiet.map((d) => [d.matHangId, d.soLuong])), [hd]);

  // Tồn có thể bán cho hóa đơn này = tồn khả dụng + phần chính hóa đơn này đang giữ chỗ
  const tonToiDa = (id) => (mhMap.get(id)?.tonKhaDung ?? 0) + (daGiu.get(id) ?? 0);

  const lines = items.map((it) => {
    const mh = mhMap.get(it.matHangId);
    const goc = hd.chiTiet.find((d) => d.matHangId === it.matHangId);
    const donGia = Number(mh ? mh.donGia : goc?.donGia ?? 0);
    const gross = donGia * it.soLuong;
    const ck = Math.round((gross * Number(it.tyLeChietKhau || 0)) / 100);
    return { ...it, ten: mh?.tenMatHang ?? goc?.tenMatHang, donGia, thanhTien: gross - ck, conBan: !!mh };
  });
  const tongHang = lines.reduce((s, l) => s + l.thanhTien, 0);
  const giam = Math.round((tongHang * Number(hd.tyLeGiamGiaTong || 0)) / 100);
  const truocThue = tongHang - giam + Number(hd.phiGiaoHang || 0);
  const tong = truocThue + Math.round((truocThue * Number(hd.tyLeVat || 0)) / 100);

  const luu = () => {
    if (!choTT) return onSubmit({ ghiChu });
    if (!khachHangId) return notifyWarning("Vui lòng chọn khách hàng");
    if (items.length === 0) return notifyWarning("Hóa đơn phải có ít nhất 1 mặt hàng");
    if (lines.some((l) => !l.conBan)) return notifyWarning("Có mặt hàng đã ngừng kinh doanh, hãy xóa dòng đó");
    return onSubmit({
      khachHangId,
      chiTiet: items.map(({ matHangId, soLuong, tyLeChietKhau }) => ({ matHangId, soLuong, tyLeChietKhau })),
      hanThanhToan: han ? han.format("YYYY-MM-DD") : null,
      ghiChu,
    });
  };

  return (
    <Modal
      title={`Chỉnh sửa hóa đơn ${hd.maHoaDon}`}
      open={open}
      onCancel={onCancel}
      onOk={luu}
      confirmLoading={saving}
      okText="Lưu thay đổi"
      cancelText="Đóng"
      width={760}
    >
      <div style={{ display: "grid", gap: 14 }}>
        {choTT ? (
          <>
            <div className="rc-cust-row">
              <div>
                <span className="rc-label">Khách hàng</span>
                <Select
                  showSearch
                  style={{ width: "100%" }}
                  value={khachHangId}
                  onChange={setKhachHangId}
                  optionFilterProp="label"
                  options={dm.khachHang.map((k) => ({ value: k.id, label: `${k.maKh} - ${k.tenKh}` }))}
                />
              </div>
              <div>
                <span className="rc-label">Hạn thanh toán</span>
                <DatePicker value={han} onChange={setHan} format="DD/MM/YYYY" style={{ width: "100%" }} />
              </div>
            </div>

            <div>
              <div className="card-top-row">
                <p className="section-label" style={{ margin: 0 }}>MẶT HÀNG</p>
                <button className="btn-outline rc-btn-sm" onClick={() => setDangThem(true)}>
                  <Plus size={13} /> Thêm loại pallet
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
                  onChange={(v) => {
                    setItems((cur) => [...cur, { matHangId: v, soLuong: 1, tyLeChietKhau: 0 }]);
                    setDangThem(false);
                  }}
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

              {lines.map((l) => (
                <div className="rc-line" key={l.matHangId}>
                  <div>
                    <div className="rc-line-name">{l.ten}</div>
                    {!l.conBan && <div style={{ color: "#dc2626", fontSize: 12 }}>Đã ngừng kinh doanh</div>}
                  </div>
                  <div className="rc-stock">Tối đa<br />{tonToiDa(l.matHangId)}</div>
                  <InputNumber
                    size="small"
                    min={1}
                    max={Math.max(1, tonToiDa(l.matHangId))}
                    value={l.soLuong}
                    onChange={(v) =>
                      setItems((cur) => cur.map((i) => (i.matHangId === l.matHangId ? { ...i, soLuong: v || 1 } : i)))
                    }
                  />
                  <div className="rc-col">Đơn giá<b>{formatMoney(l.donGia)} đ</b></div>
                  <div className="rc-col">Thành tiền<b>{formatMoney(l.thanhTien)} đ</b></div>
                  <button
                    className="icon-btn"
                    title="Bỏ dòng này"
                    onClick={() => setItems((cur) => cur.filter((i) => i.matHangId !== l.matHangId))}
                  >
                    <X size={14} />
                  </button>
                </div>
              ))}

              <div className="rc-ledger-total" style={{ marginTop: 8 }}>
                <strong>Tổng thanh toán (đã gồm VAT {Number(hd.tyLeVat)}%)</strong>
                <span>{formatMoney(tong)} đ</span>
              </div>
            </div>
          </>
        ) : (
          <p className="muted-text">Hóa đơn đã thanh toán: chỉ được sửa ghi chú.</p>
        )}

        <div>
          <span className="rc-label">Ghi chú</span>
          <Input.TextArea rows={3} value={ghiChu} onChange={(e) => setGhiChu(e.target.value)} maxLength={2000} />
        </div>
      </div>
    </Modal>
  );
}