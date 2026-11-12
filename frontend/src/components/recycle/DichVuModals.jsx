import { useState } from "react";
import { Alert, Col, DatePicker, Form, Input, InputNumber, Modal, Row, Select } from "antd";
import dayjs from "dayjs";
import { dichVuApi } from "../../api/recycleApi";
import { KHO_ID } from "../../config/kho";
import { useKhachHangOptions } from "../../hooks/useKhachHangOptions";
import { fmtVnd } from "../../utils/format";
import { notifyError, notifySuccess } from "../../utils/notify";
import { PHUONG_THUC_TT, vndInput } from "./dichVu";

const NUM = { min: 0, precision: 0, style: { width: "100%" } };

/** Khung modal dùng chung: xử lý submit, loading, thông báo. */
function useSubmit(action, okMsg, onDone) {
  const [saving, setSaving] = useState(false);
  const submit = async (payload) => {
    setSaving(true);
    try {
      await action(payload);
      notifySuccess(okMsg);
      onDone();
    } catch (err) {
      notifyError(err);
    } finally {
      setSaving(false);
    }
  };
  return { saving, submit };
}

/** Tiếp nhận pallet của khách. Khớp TiepNhanDichVuRequest. */
export function TiepNhanModal({ onCancel, onSaved }) {
  const [form] = Form.useForm();
  const { options, loading } = useKhachHangOptions();
  const { saving, submit } = useSubmit(dichVuApi.tiepNhan, "Đã tiếp nhận pallet của khách", onSaved);

  return (
    <Modal
      open
      centered
      title="Tiếp nhận pallet của khách để tháo dỡ"
      okText="Tiếp nhận"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <Alert
        style={{ margin: "12px 0" }}
        type="info"
        showIcon
        message="Pallet của khách được giữ riêng, không cộng vào tồn kho công ty."
      />
      <Form
        form={form}
        layout="vertical"
        onFinish={(v) =>
          submit({
            khachHangId: v.khachHangId,
            khoId: KHO_ID,
            soPalletNhan: v.soPalletNhan,
            donGiaDichVu: v.donGiaDichVu,
            ghiChu: v.ghiChu?.trim() || null,
          })
        }
      >
        <Form.Item label="Khách hàng" name="khachHangId" rules={[{ required: true, message: "Chọn khách hàng" }]}>
          <Select showSearch optionFilterProp="label" placeholder="Chọn khách hàng" options={options} loading={loading} />
        </Form.Item>
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item label="Số pallet nhận" name="soPalletNhan" rules={[{ required: true, message: "Nhập số pallet" }]}>
              <InputNumber {...NUM} min={1} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              label="Đơn giá dịch vụ (₫/pallet)"
              name="donGiaDichVu"
              rules={[{ required: true, message: "Nhập đơn giá" }]}
            >
              <InputNumber {...NUM} {...vndInput} />
            </Form.Item>
          </Col>
        </Row>
        <Form.Item label="Ghi chú" name="ghiChu" rules={[{ max: 255, message: "Tối đa 255 ký tự" }]}>
          <Input.TextArea rows={2} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

/** Ghi kết quả tháo dỡ (chờ GD/PGD duyệt). Khớp KetQuaDichVuRequest. */
export function KetQuaModal({ record, onCancel, onSaved }) {
  const [form] = Form.useForm();
  const { saving, submit } = useSubmit(
    (p) => dichVuApi.ketQua(record.id, p),
    "Đã ghi nhận kết quả, chờ duyệt",
    onSaved,
  );

  return (
    <Modal
      open
      centered
      width={640}
      title={`Kết quả tháo dỡ - ${record.maPhieu}`}
      okText="Ghi nhận kết quả"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <div style={{ color: "var(--text-muted)", margin: "8px 0 16px" }}>
        Khách: <b>{record.tenKhachHang}</b> · Đã nhận <b>{record.soPalletNhan}</b> pallet
      </div>
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          soPalletThaoDo: record.soPalletThaoDo || undefined,
          soThanhNguyen: record.soThanhNguyen,
          soCucVuong: record.soCucVuong,
          soThanhCanXuLy: record.soThanhCanXuLy,
          soCucCanXuLy: record.soCucCanXuLy,
          kgPheLieu: record.kgPheLieu,
        }}
        onFinish={(v) => {
          const p = {
            soPalletThaoDo: v.soPalletThaoDo,
            soThanhNguyen: v.soThanhNguyen ?? 0,
            soCucVuong: v.soCucVuong ?? 0,
            soThanhCanXuLy: v.soThanhCanXuLy ?? 0,
            soCucCanXuLy: v.soCucCanXuLy ?? 0,
            kgPheLieu: v.kgPheLieu ?? 0,
          };
          if (p.soThanhNguyen + p.soCucVuong + p.soThanhCanXuLy + p.soCucCanXuLy + p.kgPheLieu <= 0) {
            notifyError("Cần nhập ít nhất một loại kết quả tháo dỡ");
            return;
          }
          submit(p);
        }}
      >
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              label={`Số pallet đã tháo dỡ (tối đa ${record.soPalletNhan})`}
              name="soPalletThaoDo"
              rules={[{ required: true, message: "Nhập số pallet" }]}
            >
              <InputNumber {...NUM} min={1} max={record.soPalletNhan} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Phế liệu (kg)" name="kgPheLieu">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Thanh nguyên" name="soThanhNguyen">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Cục vuông" name="soCucVuong">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Thanh cần xử lý thêm" name="soThanhCanXuLy">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Cục cần xử lý thêm" name="soCucCanXuLy">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    </Modal>
  );
}

/** Lập hóa đơn dịch vụ. Khớp LapHoaDonDichVuRequest: tyLeVat (mặc định 8), hanThanhToan (mặc định +15 ngày), ghiChu. */
export function LapHoaDonModal({ record, onCancel, onSaved }) {
  const [form] = Form.useForm();
  const { saving, submit } = useSubmit(
    (p) => dichVuApi.lapHoaDon(record.id, p),
    "Đã lập hóa đơn dịch vụ",
    onSaved,
  );

  return (
    <Modal
      open
      centered
      title={`Lập hóa đơn dịch vụ - ${record.maPhieu}`}
      okText="Lập hóa đơn"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <Alert
        style={{ margin: "12px 0" }}
        type="info"
        showIcon
        message={`Phí dịch vụ: ${fmtVnd(record.phiDichVu)} (${record.soPalletThaoDo} pallet × ${fmtVnd(record.donGiaDichVu)}), chưa gồm VAT.`}
      />
      <Form
        form={form}
        layout="vertical"
        initialValues={{ tyLeVat: 8, hanThanhToan: dayjs().add(15, "day") }}
        onFinish={(v) =>
          submit({
            tyLeVat: v.tyLeVat ?? null,
            hanThanhToan: v.hanThanhToan ? v.hanThanhToan.format("YYYY-MM-DD") : null,
            ghiChu: v.ghiChu?.trim() || null,
          })
        }
      >
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item label="Thuế VAT (%)" name="tyLeVat">
              <InputNumber min={0} max={100} style={{ width: "100%" }} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Hạn thanh toán" name="hanThanhToan">
              <DatePicker format="DD/MM/YYYY" style={{ width: "100%" }} />
            </Form.Item>
          </Col>
        </Row>
        <Form.Item label="Ghi chú" name="ghiChu" rules={[{ max: 500, message: "Tối đa 500 ký tự" }]}>
          <Input.TextArea rows={2} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

/** Công ty mua lại phế liệu của khách. Khớp MuaLaiPheLieuRequest. */
export function MuaLaiModal({ record, onCancel, onSaved }) {
  const [form] = Form.useForm();
  const soKg = Form.useWatch("soKg", form);
  const donGia = Form.useWatch("donGiaKg", form);
  const { saving, submit } = useSubmit(
    (p) => dichVuApi.muaLaiPheLieu(record.id, p),
    "Đã mua lại phế liệu của khách",
    onSaved,
  );

  return (
    <Modal
      open
      centered
      title={`Mua lại phế liệu của khách - ${record.maPhieu}`}
      okText="Xác nhận mua và chi tiền"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <Alert
        style={{ margin: "12px 0" }}
        type="info"
        showIcon
        message={`Phế liệu còn của khách: ${record.kgPheLieuConLai} kg. Khoản mua được chi ngay, phần chưa mua sẽ trả lại khách khi trả hàng.`}
      />
      <Form
        form={form}
        layout="vertical"
        initialValues={{ phuongThucThanhToan: "tien_mat" }}
        onFinish={(v) =>
          submit({
            soKg: v.soKg,
            donGiaKg: v.donGiaKg,
            phuongThucThanhToan: v.phuongThucThanhToan,
            ghiChu: v.ghiChu?.trim() || null,
          })
        }
      >
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item label="Số kg mua lại" name="soKg" rules={[{ required: true, message: "Nhập số kg" }]}>
              <InputNumber {...NUM} min={1} max={record.kgPheLieuConLai} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Đơn giá (₫/kg)" name="donGiaKg" rules={[{ required: true, message: "Nhập đơn giá" }]}>
              <InputNumber {...NUM} min={1} {...vndInput} />
            </Form.Item>
          </Col>
        </Row>
        <Form.Item label="Phương thức thanh toán" name="phuongThucThanhToan" rules={[{ required: true }]}>
          <Select options={PHUONG_THUC_TT} />
        </Form.Item>
        <Form.Item label="Ghi chú" name="ghiChu" rules={[{ max: 500, message: "Tối đa 500 ký tự" }]}>
          <Input.TextArea rows={2} />
        </Form.Item>
        <div style={{ textAlign: "right", fontSize: 15 }}>
          Số tiền chi: <b style={{ color: "var(--color-amber-strong)", fontSize: 18 }}>{fmtVnd((soKg || 0) * (donGia || 0))}</b>
        </div>
      </Form>
    </Modal>
  );
}
