import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, DatePicker, Input, InputNumber, Select, Switch } from "antd";
import { RotateCcw } from "lucide-react";
import dayjs from "dayjs";

import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import { DEFAULT_VAT } from "../../components/receipt/baoGia";
import { banPheLieuApi } from "../../api/banHangApi";
import { notifyError, notifySuccess, notifyWarning } from "../../utils/notify";
import { PHUONG_THUC, formatMoney } from "../../utils/receiptFormat";

const money = {
  formatter: (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, "."),
  parser: (v) => v?.replace(/\./g, ""),
};
const num = (v) => Number(v) || 0;
// BE làm tròn VAT 2 chữ số thập phân (HALF_UP), FE tính y hệt để so sánh tiền khách đưa chính xác
const r2 = (x) => Math.round(x * 100) / 100;

// Bán phế liệu gỗ theo kg cho khách (route /hoa-don/ban-phe-lieu)
export default function BanPheLieu() {
  const navigate = useNavigate();
  const [dm, setDm] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [khachHangId, setKhachHangId] = useState(null);
  const [soKg, setSoKg] = useState(null);
  const [donGiaKg, setDonGiaKg] = useState(null);
  const [tyLeVat, setTyLeVat] = useState(DEFAULT_VAT);
  const [han, setHan] = useState(dayjs().add(15, "day"));
  const [ghiChu, setGhiChu] = useState("");
  const [thanhToanNgay, setThanhToanNgay] = useState(false);
  const [phuongThuc, setPhuongThuc] = useState("tien_mat");
  const [tienKhachDua, setTienKhachDua] = useState(null);

  useEffect(() => {
    let ignore = false;
    banPheLieuApi
      .danhMuc()
      .then((d) => {
        if (ignore) return;
        setDm(d);
        setDonGiaKg(d.donGiaKgGoiY != null ? Number(d.donGiaKgGoiY) : null);
      })
      .catch((err) => !ignore && notifyError(err));
    return () => {
      ignore = true;
    };
  }, []);

  const tienHang = num(soKg) * num(donGiaKg);
  const vat = r2((tienHang * num(tyLeVat)) / 100);
  const tong = r2(tienHang + vat);
  const thoiLai = phuongThuc === "tien_mat" ? Math.max(0, r2(num(tienKhachDua) - tong)) : 0;

  const reset = () => {
    setKhachHangId(null);
    setSoKg(null);
    setDonGiaKg(dm?.donGiaKgGoiY != null ? Number(dm.donGiaKgGoiY) : null);
    setTyLeVat(DEFAULT_VAT);
    setHan(dayjs().add(15, "day"));
    setGhiChu("");
    setThanhToanNgay(false);
    setPhuongThuc("tien_mat");
    setTienKhachDua(null);
  };

  const submit = async () => {
    if (!khachHangId) return notifyWarning("Vui lòng chọn khách hàng");
    if (!soKg || soKg < 1) return notifyWarning("Vui lòng nhập số kg");
    if (soKg > dm.tonKhaDungKg) return notifyWarning(`Kho chỉ còn ${dm.tonKhaDungKg} kg phế liệu`);
    if (!donGiaKg || donGiaKg <= 0) return notifyWarning("Vui lòng nhập đơn giá mỗi kg");
    if (thanhToanNgay && phuongThuc === "tien_mat" && Math.round(num(tienKhachDua) * 100) < Math.round(tong * 100)) {
      return notifyWarning("Tiền khách đưa chưa đủ");
    }
    setSubmitting(true);
    try {
      const hd = await banPheLieuApi.ban({
        khachHangId,
        khoId: dm.khoId,
        soKg,
        donGiaKg,
        tyLeVat,
        hanThanhToan: han ? han.format("YYYY-MM-DD") : null,
        ghiChu: ghiChu.trim() || null,
        thanhToanNgay,
        phuongThucThanhToan: thanhToanNgay ? phuongThuc : null,
        tienKhachDua: thanhToanNgay && phuongThuc === "tien_mat" ? tienKhachDua : null,
      });
      notifySuccess(thanhToanNgay ? "Đã bán và thu tiền phế liệu" : "Đã lập hóa đơn bán phế liệu");
      if (hd?.id) navigate(`/hoa-don/${hd.id}`);
      else reset();
    } catch (err) {
      notifyError(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <ReceiptTabs />

      <section className="card" style={{ maxWidth: 760 }}>
        <div className="card-header">
          <div>
            <h2 className="card-title">Bán phế liệu gỗ theo kg</h2>
            <p className="muted-text" style={{ margin: "4px 0 0" }}>
              {dm?.tenKho ? `Xuất từ ${dm.tenKho}` : "Đang tải..."}
            </p>
          </div>
          <span className="pill pill-blue">Mã chứng từ: tự động</span>
        </div>

        <div className="card-divider" />

        <Alert
          type={dm && dm.tonKhaDungKg <= 0 ? "warning" : "info"}
          showIcon
          style={{ marginBottom: 16 }}
          message={dm ? `Tồn phế liệu khả dụng: ${dm.tonKhaDungKg} kg` : "Đang tải tồn phế liệu..."}
        />

        <div className="rc-field">
          <span className="rc-label">Khách hàng</span>
          <Select
            showSearch
            allowClear
            placeholder="Chọn khách hàng"
            style={{ width: "100%" }}
            value={khachHangId}
            onChange={setKhachHangId}
            optionFilterProp="label"
            loading={!dm}
            options={(dm?.khachHang ?? []).map((k) => ({ value: k.id, label: `${k.maKh} - ${k.tenKh}` }))}
          />
        </div>

        <div className="rc-cust-row rc-field">
          <div>
            <span className="rc-label">Số kg bán</span>
            <InputNumber
              min={1}
              max={dm?.tonKhaDungKg}
              precision={0}
              style={{ width: "100%" }}
              value={soKg}
              onChange={setSoKg}
              addonAfter="kg"
            />
          </div>
          <div>
            <span className="rc-label">Đơn giá mỗi kg</span>
            <InputNumber
              min={1}
              precision={0}
              style={{ width: "100%" }}
              value={donGiaKg}
              onChange={setDonGiaKg}
              addonAfter="đ/kg"
              {...money}
            />
          </div>
        </div>

        <div className="rc-cust-row rc-field">
          <div>
            <span className="rc-label">Hạn thanh toán</span>
            <DatePicker value={han} onChange={setHan} format="DD/MM/YYYY" style={{ width: "100%" }} />
          </div>
          <div>
            <span className="rc-label">VAT (%)</span>
            <InputNumber min={0} max={100} style={{ width: "100%" }} value={tyLeVat} onChange={setTyLeVat} />
          </div>
        </div>

        <div className="rc-field">
          <span className="rc-label">Ghi chú</span>
          <Input.TextArea rows={2} maxLength={500} value={ghiChu} onChange={(e) => setGhiChu(e.target.value)} />
        </div>

        <div className="rc-summary">
          <div className="rc-ledger">
            <div className="rc-ledger-row"><span>Tiền hàng ({num(soKg)} kg):</span><b>{formatMoney(tienHang)} đ</b></div>
            <div className="rc-ledger-row"><span>Thuế VAT ({num(tyLeVat)}%):</span><b>+{formatMoney(vat)} đ</b></div>
            <div className="rc-ledger-total"><strong>Tổng cộng thanh toán</strong><span>{formatMoney(tong)} đ</span></div>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: 10, marginTop: 14 }}>
            <Switch checked={thanhToanNgay} onChange={setThanhToanNgay} />
            <span style={{ fontWeight: 600 }}>Khách thanh toán ngay</span>
            <span className="muted-text">Tắt = lập hóa đơn chờ thanh toán (công nợ)</span>
          </div>

          {thanhToanNgay && (
            <>
              <div className="rc-cash">
                <div>
                  <span className="rc-label">Phương thức thanh toán</span>
                  <Select
                    value={phuongThuc}
                    onChange={setPhuongThuc}
                    style={{ width: "100%" }}
                    options={Object.entries(PHUONG_THUC).map(([value, label]) => ({ value, label }))}
                  />
                </div>
                <div>
                  <span className="rc-label">Số tiền khách đưa</span>
                  <InputNumber
                    value={phuongThuc === "tien_mat" ? tienKhachDua : tong}
                    disabled={phuongThuc !== "tien_mat"}
                    onChange={setTienKhachDua}
                    min={0}
                    style={{ width: "100%" }}
                    addonAfter="VND"
                    {...money}
                  />
                </div>
              </div>
              {phuongThuc === "tien_mat" && (
                <div className="rc-change" style={{ marginTop: 14 }}>
                  <small>Tiền thối lại khách hàng</small>
                  <strong>{formatMoney(thoiLai)} đ</strong>
                </div>
              )}
            </>
          )}
        </div>

        <div className="rc-foot">
          <button className="btn-outline" disabled={submitting} onClick={reset}>
            <RotateCcw size={15} /> Nhập lại
          </button>
          <button className="btn-solid" disabled={submitting || !dm} onClick={submit}>
            {thanhToanNgay ? "Xác nhận bán và thu tiền" : "Lập hóa đơn bán phế liệu"}
          </button>
        </div>
      </section>
    </div>
  );
}
