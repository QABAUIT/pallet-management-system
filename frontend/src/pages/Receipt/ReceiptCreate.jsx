import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { InputNumber, Input, Select } from "antd";
import { Plus, RefreshCw, X } from "lucide-react";
import AppTable from "../../components/common/AppTable";
import SearchInput from "../../components/common/SearchInput";
import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import ReceiptStatusBadge from "../../components/receipt/ReceiptStatusBadge";
import { useReceipts } from "../../hooks/useReceipts";
import { useReceiptCreate, VAT_RATE } from "../../hooks/useReceiptCreate";
import { PHUONG_THUC, formatDate, formatMoney } from "../../utils/receiptFormat";

const formatTien = (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

export default function ReceiptCreate() {
  const navigate = useNavigate();
  const { data: choTT, reload: reloadChoTT } = useReceipts("cho_thanh_toan");
  const [dangThem, setDangThem] = useState(false);
  const [search, setSearch] = useState("");

  const f = useReceiptCreate((created, daThanhToan) => {
    reloadChoTT();
    if (daThanhToan) navigate(`/hoa-don/${created.id}`);
  });

  const chuaTT = choTT.filter((r) => r.laThu);
  const q = search.trim().toLowerCase();
  const dsHienThi = chuaTT.filter(
    (r) => !q || [r.maHoaDon, r.tenDoiTac, r.sdtDoiTac].some((v) => v && v.toLowerCase().includes(q)),
  );
  const tongCongNo = chuaTT.reduce((s, r) => s + Number(r.tongThanhToan), 0);

  const columns = [
    {
      key: "ma",
      header: "Mã HĐ",
      width: "130px",
      render: (r) => (
        <div>
          <div className="rc-name">{r.maHoaDon}</div>
          <div className="muted-text">{formatDate(r.ngayLap)}</div>
        </div>
      ),
    },
    {
      key: "kh",
      header: "Khách hàng & SĐT",
      render: (r) => (
        <div>
          <div className="rc-name">{r.tenDoiTac}</div>
          <div className="muted-text">{r.sdtDoiTac} · Hạn: {formatDate(r.hanThanhToan)}</div>
        </div>
      ),
    },
    {
      key: "tong",
      header: "Tổng tiền",
      align: "right",
      render: (r) => (
        <div>
          <b>{formatMoney(r.tongThanhToan)}</b>
          <div className="muted-text" style={r.quaHan ? { color: "#dc2626" } : undefined}>
            {r.quaHan ? `Quá hạn ${r.soNgayQuaHan} ngày` : "Trong kỳ hạn"}
          </div>
        </div>
      ),
    },
    { key: "tt", header: "Trạng thái", align: "center", render: (r) => <ReceiptStatusBadge hd={r} /> },
    {
      key: "act",
      header: "",
      align: "center",
      width: "90px",
      render: (r) => (
        <button className="btn-outline rc-btn-sm" onClick={() => navigate(`/hoa-don/${r.id}`)}>
          Chi tiết
        </button>
      ),
    },
  ];

  return (
    <div>
      <ReceiptTabs />

      <div className="rc-create">
        {/* ===== Trái: form tạo hóa đơn ===== */}
        <section className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">Tạo hóa đơn bán pallet mới</h2>
              <p className="muted-text" style={{ margin: "4px 0 0" }}>
                Phiếu xuất kho tích hợp đối soát tự động{f.dm.tenKho ? ` · ${f.dm.tenKho}` : ""}
              </p>
            </div>
            <span className="pill pill-blue">Mã chứng từ: tự động</span>
          </div>

          <div className="card-divider" />

          <div className="rc-cust-row rc-field">
            <div>
              <span className="rc-label">Tên khách hàng / doanh nghiệp đối tác</span>
              <Select
                showSearch
                allowClear
                placeholder="Chọn khách hàng"
                style={{ width: "100%" }}
                value={f.khachHangId}
                onChange={f.setKhachHangId}
                optionFilterProp="label"
                options={f.dm.khachHang.map((k) => ({ value: k.id, label: `${k.maKh} - ${k.tenKh}` }))}
              />
            </div>
            <div>
              <span className="rc-label">Số điện thoại</span>
              <Input readOnly value={f.khach?.sdt ?? ""} />
            </div>
          </div>

          <div className="card-top-row">
            <p className="section-label" style={{ margin: 0 }}>
              CHI TIẾT PALLET XUẤT BÁN <span className="pill pill-blue">{f.items.length} hạng mục</span>
            </p>
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
                f.addItem(v);
                setDangThem(false);
              }}
              onBlur={() => setDangThem(false)}
              options={f.dm.matHang
                .filter((m) => !f.items.some((i) => i.matHangId === m.id))
                .map((m) => ({
                  value: m.id,
                  disabled: m.tonKhaDung <= 0,
                  label: `${m.tenMatHang} (${m.maMatHang}) · tồn ${m.tonKhaDung}`,
                }))}
            />
          )}

          {f.lines.length === 0 && <div className="rc-empty">Chưa có mặt hàng nào. Bấm "Thêm loại pallet".</div>}

          {f.lines.map((l) => (
            <div className="rc-line" key={l.matHangId}>
              <div>
                <div className="rc-line-name">{l.mh.tenMatHang}</div>
                <div className="muted-text">{l.mh.kichThuoc ? `${l.mh.kichThuoc} mm` : l.mh.maMatHang}</div>
              </div>
              <div className="rc-stock">Tồn<br />{l.mh.tonKhaDung}</div>
              <InputNumber
                size="small"
                min={1}
                max={Math.max(1, l.mh.tonKhaDung)}
                value={l.soLuong}
                onChange={(v) => f.setQty(l.matHangId, v)}
              />
              <div className="rc-col">Đơn giá<b>{formatMoney(l.mh.donGia)} đ</b></div>
              <div className="rc-col">Thành tiền<b>{formatMoney(l.thanhTien)} đ</b></div>
              <button className="icon-btn" title="Bỏ dòng này" onClick={() => f.removeItem(l.matHangId)}>
                <X size={14} />
              </button>
            </div>
          ))}

          <div className="rc-summary">
            <div className="rc-ledger">
              <div className="rc-ledger-row"><span>Tổng tiền hàng ({f.items.length} loại):</span><b>{formatMoney(f.tongHang)} đ</b></div>
              <div className="rc-ledger-row"><span>Thuế VAT ({VAT_RATE}%):</span><b>+{formatMoney(f.vat)} đ</b></div>
              <div className="rc-ledger-total"><strong>Tổng cộng thanh toán</strong><span>{formatMoney(f.tong)} đ</span></div>
            </div>

            <div className="rc-cash">
              <div>
                <span className="rc-label">Phương thức thanh toán</span>
                <Select
                  value={f.phuongThuc}
                  onChange={f.setPhuongThuc}
                  style={{ width: "100%" }}
                  options={Object.entries(PHUONG_THUC).map(([value, label]) => ({ value, label }))}
                />
              </div>
              <div>
                <span className="rc-label">Số tiền khách đưa / nhận</span>
                <InputNumber
                  value={f.phuongThuc === "tien_mat" ? f.tienKhachDua : f.tong}
                  disabled={f.phuongThuc !== "tien_mat"}
                  onChange={f.setTienKhachDua}
                  min={0}
                  style={{ width: "100%" }}
                  formatter={formatTien}
                  parser={(v) => v.replace(/\./g, "")}
                  addonAfter="VND"
                />
              </div>
            </div>
            <div className="rc-change" style={{ marginTop: 14 }}>
              <small>Tiền thối lại khách hàng</small>
              <strong>{formatMoney(f.thoiLai)} đ</strong>
            </div>
          </div>

          <div className="rc-foot">
            <button className="btn-outline" disabled={f.submitting} onClick={() => f.submit(false)}>
              Xác nhận thêm hóa đơn (Chờ thanh toán)
            </button>
            <button className="btn-solid" disabled={f.submitting} onClick={() => f.submit(true)}>
              Xác nhận thanh toán ngay
            </button>
          </div>
        </section>

        {/* ===== Phải: hóa đơn chưa thanh toán ===== */}
        <section className="card">
          <div className="card-header">
            <h2 className="card-title">Danh sách hóa đơn chưa thanh toán</h2>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span className="pill pill-amber">{chuaTT.length} đơn chờ</span>
              <button className="icon-btn" title="Tải lại" onClick={reloadChoTT}><RefreshCw size={15} /></button>
            </div>
          </div>

          <div style={{ margin: "14px 0" }}>
            <SearchInput value={search} onChange={setSearch} placeholder="Tìm theo mã HD, tên đối tác..." />
          </div>

          {dsHienThi.length === 0 ? (
            <div className="rc-empty">Không có hóa đơn nào chờ thanh toán.</div>
          ) : (
            <AppTable columns={columns} data={dsHienThi} />
          )}

          <div className="rc-debt">
            <span>Tổng công nợ cần thu:</span>
            <b>{formatMoney(tongCongNo)} đ</b>
          </div>
        </section>
      </div>
    </div>
  );
}