import { useState } from "react";
import { DatePicker, Form, Input, InputNumber, Modal, Select } from "antd";
import dayjs from "dayjs";
import { Pencil } from "lucide-react";
import AppTable from "../common/AppTable";
import ConfirmDeleteButton from "../common/ConfirmDeleteButton";
import { useOperatingCosts } from "../../hooks/useOperatingCosts";
import { LOAI_CHI_PHI, LOAI_CHI_PHI_OPTIONS } from "../../utils/financeFormat";
import { formatDate, formatMoney } from "../../utils/receiptFormat";

const formatTien = (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
const MAC_DINH = { loaiChiPhi: "van_tai_logistics", ngayChi: dayjs() };

/** Chi phí vận hành trong khung thời gian đang chọn. GD/PGD (canEdit) được thêm, sửa, xóa. */
export default function OperatingCostModal({ open, onClose, tuNgay, denNgay, canEdit, onChanged }) {
  const [form] = Form.useForm();
  const [editingId, setEditingId] = useState(null);
  const { data, loading, saving, create, update, remove } =
    useOperatingCosts({ open, tuNgay, denNgay, onChanged });

  const batDauSua = (r) => {
    setEditingId(r.id);
    form.setFieldsValue({
      loaiChiPhi: r.loaiChiPhi, soTien: Number(r.soTien), ngayChi: dayjs(r.ngayChi),
      moTa: r.moTa, ghiChu: r.ghiChu,
    });
  };

  const reset = () => {
    setEditingId(null);
    form.resetFields();
    form.setFieldsValue(MAC_DINH);
  };

  const submit = async (values) => {
    const payload = { ...values, ngayChi: values.ngayChi.format("YYYY-MM-DD") };
    const ok = editingId ? await update(editingId, payload) : await create(payload);
    if (ok) reset();
  };

  const columns = [
    { key: "ma", header: "Mã", width: "130px", render: (r) => <b>{r.maChiPhi}</b> },
    { key: "ngay", header: "Ngày chi", width: "100px", render: (r) => formatDate(r.ngayChi) },
    { key: "loai", header: "Loại", render: (r) => LOAI_CHI_PHI[r.loaiChiPhi] || r.loaiChiPhi },
    { key: "moTa", header: "Mô tả", render: (r) => r.moTa || "—" },
    { key: "tien", header: "Số tiền", align: "right", render: (r) => <b>{formatMoney(r.soTien)} đ</b> },
    ...(canEdit
      ? [{
          key: "act",
          header: "",
          align: "center",
          width: "90px",
          render: (r) => (
            <div style={{ display: "flex", justifyContent: "center", gap: 4 }}>
              <button className="icon-btn" title="Sửa" onClick={() => batDauSua(r)}><Pencil size={15} /></button>
              <ConfirmDeleteButton title="Xóa khoản chi này?" onConfirm={() => remove(r.id)} />
            </div>
          ),
        }]
      : []),
  ];

  return (
    <Modal title="Chi phí vận hành" open={open} onCancel={onClose} footer={null} width={860} destroyOnHidden
      afterOpenChange={(o) => o && form.setFieldsValue(MAC_DINH)}>
      {loading ? <p className="muted-text">Đang tải...</p> : <AppTable columns={columns} data={data} emptyText="Chưa có khoản chi nào trong kỳ này" />}

      {canEdit && (
        <Form form={form} layout="vertical" style={{ marginTop: 16 }} initialValues={MAC_DINH} onFinish={submit}>
          <p className="section-label">{editingId ? "SỬA KHOẢN CHI" : "THÊM KHOẢN CHI"}</p>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12 }}>
            <Form.Item name="loaiChiPhi" label="Loại chi phí" rules={[{ required: true }]}>
              <Select options={LOAI_CHI_PHI_OPTIONS} />
            </Form.Item>
            <Form.Item name="soTien" label="Số tiền" rules={[{ required: true, message: "Nhập số tiền" }]}>
              <InputNumber min={1} style={{ width: "100%" }} formatter={formatTien}
                parser={(v) => v.replace(/\./g, "")} addonAfter="VND" />
            </Form.Item>
            <Form.Item name="ngayChi" label="Ngày chi" rules={[{ required: true, message: "Chọn ngày chi" }]}>
              <DatePicker format="DD/MM/YYYY" style={{ width: "100%" }} />
            </Form.Item>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
            <Form.Item name="moTa" label="Mô tả"><Input maxLength={255} /></Form.Item>
            <Form.Item name="ghiChu" label="Ghi chú"><Input maxLength={255} /></Form.Item>
          </div>
          <div style={{ display: "flex", gap: 8 }}>
            <button type="submit" className="btn-solid" disabled={saving}>
              {editingId ? "Lưu thay đổi" : "Thêm khoản chi"}
            </button>
            {editingId && <button type="button" className="btn-outline" onClick={reset}>Hủy sửa</button>}
          </div>
        </Form>
      )}
    </Modal>
  );
}