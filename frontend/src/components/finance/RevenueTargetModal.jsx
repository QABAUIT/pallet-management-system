import { useState } from "react";
import { Form, Input, InputNumber, Modal, Radio, Select } from "antd";
import AppTable from "../common/AppTable";
import ConfirmDeleteButton from "../common/ConfirmDeleteButton";
import { useRevenueTargets } from "../../hooks/useRevenueTargets";
import { formatMoney } from "../../utils/receiptFormat";

const formatTien = (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
const THANG = Array.from({ length: 12 }, (_, i) => ({ value: i + 1, label: `Tháng ${i + 1}` }));

/** Xem mục tiêu doanh thu theo năm; GD/PGD (canEdit) đặt hoặc xóa mục tiêu tháng / năm. */
export default function RevenueTargetModal({ open, onClose, canEdit, onChanged }) {
  const [nam, setNam] = useState(new Date().getFullYear());
  const [form] = Form.useForm();
  const [ky, setKy] = useState("thang");
  const { data, loading, saving, save, remove } = useRevenueTargets({ open, nam, onChanged });

  const columns = [
    {
      key: "ky",
      header: "Kỳ",
      render: (r) => (r.ky === "thang" ? `Tháng ${r.thangKy}/${r.namKy}` : `Cả năm ${r.namKy}`),
    },
    { key: "soTien", header: "Mục tiêu", align: "right", render: (r) => `${formatMoney(r.soTienMucTieu)} đ` },
    { key: "thucTe", header: "Thực tế", align: "right", render: (r) => `${formatMoney(r.doanhThuThucTe)} đ` },
    { key: "pt", header: "Đạt", align: "right", render: (r) => <b>{r.phanTramDat}%</b> },
    ...(canEdit
      ? [{
          key: "act",
          header: "",
          align: "center",
          width: "60px",
          render: (r) => <ConfirmDeleteButton title="Xóa mục tiêu này?" onConfirm={() => remove(r.id)} />,
        }]
      : []),
  ];

  const submit = async (values) => {
    const ok = await save({
      ky: values.ky,
      namKy: nam,
      thangKy: values.ky === "thang" ? values.thangKy : null,
      soTienMucTieu: values.soTienMucTieu,
      ghiChu: values.ghiChu,
    });
    if (ok) form.resetFields(["soTienMucTieu", "ghiChu"]);
  };

  return (
    <Modal title="Mục tiêu doanh thu" open={open} onCancel={onClose} footer={null} width={720} destroyOnHidden>
      <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 12 }}>
        <span className="rc-label" style={{ margin: 0 }}>Năm</span>
        <InputNumber min={2000} max={2100} value={nam} onChange={(v) => v && setNam(v)} />
      </div>

      {loading ? <p className="muted-text">Đang tải...</p> : <AppTable columns={columns} data={data} emptyText="Chưa đặt mục tiêu nào cho năm này" />}

      {canEdit && (
        <Form
          form={form}
          layout="vertical"
          style={{ marginTop: 16 }}
          initialValues={{ ky: "thang", thangKy: new Date().getMonth() + 1 }}
          onFinish={submit}
        >
          <p className="section-label">ĐẶT / CẬP NHẬT MỤC TIÊU (cùng kỳ sẽ ghi đè)</p>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
            <Form.Item name="ky" label="Kỳ">
              <Radio.Group onChange={(e) => setKy(e.target.value)}>
                <Radio value="thang">Theo tháng</Radio>
                <Radio value="nam">Cả năm</Radio>
              </Radio.Group>
            </Form.Item>
            {ky === "thang" && (
              <Form.Item name="thangKy" label="Tháng" rules={[{ required: true, message: "Chọn tháng" }]}>
                <Select options={THANG} />
              </Form.Item>
            )}
          </div>
          <Form.Item
            name="soTienMucTieu"
            label="Số tiền mục tiêu"
            rules={[{ required: true, message: "Nhập số tiền mục tiêu" }]}
          >
            <InputNumber min={0} style={{ width: "100%" }} formatter={formatTien}
              parser={(v) => v.replace(/\./g, "")} addonAfter="VND" />
          </Form.Item>
          <Form.Item name="ghiChu" label="Ghi chú">
            <Input maxLength={255} />
          </Form.Item>
          <button type="submit" className="btn-solid" disabled={saving}>Lưu mục tiêu</button>
        </Form>
      )}
    </Modal>
  );
}