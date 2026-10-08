import { useState } from "react";
import { Col, Form, Input, Modal, Row, Select } from "antd";
import { phieuXuatApi } from "../../api/phieuXuatApi";
import { useAuth } from "../../store/AuthContext";
import { useMatHangOptions } from "../../hooks/useMatHangOptions";
import { KHO_ID } from "../../config/kho";
import { LOAI_XUAT_KHO } from "../../utils/statusTag";
import { notifyError, notifySuccess } from "../../utils/notify";
import PhieuKhoItemsField from "./PhieuKhoItemsField";

const TRANG_THAI_OPTIONS = [
  { value: "cho_xu_ly", label: "Chờ xử lý (chưa trừ tồn kho)" },
  { value: "da_hoan_thanh", label: "Hoàn thành (trừ tồn kho ngay)" },
];

/**
 * Modal tạo phiếu xuất kho. Khớp TaoPhieuXuatRequest:
 * khoId, nhanVienXuatId, loaiXuat, lyDoXuat, trangThai, bienSoXe, chiTiet[{matHangId, soLuong, ghiChu}]
 * (loId để trống: BE cho phép bỏ qua, khi đó chỉ trừ tồn kho chứ không trừ lô)
 */
export default function CreateExportModal({ onCancel, onCreated }) {
  const { user } = useAuth();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const { options: matHangOptions, loading: matHangLoading } =
    useMatHangOptions();

  const handleFinish = async (values) => {
    const payload = {
      khoId: KHO_ID,
      nhanVienXuatId: user?.nhanVienId,
      loaiXuat: values.loaiXuat,
      lyDoXuat: values.lyDoXuat.trim(),
      trangThai: values.trangThai,
      bienSoXe: values.bienSoXe?.trim() || null,
      chiTiet: values.chiTiet.map((r) => ({
        matHangId: r.matHangId,
        soLuong: r.soLuong,
        ghiChu: r.ghiChu?.trim() || null,
      })),
    };

    setSaving(true);
    try {
      await phieuXuatApi.tao(payload);
      notifySuccess("Tạo phiếu xuất kho thành công");
      onCreated();
    } catch (err) {
      notifyError(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open
      centered
      width={760}
      title="Tạo phiếu xuất kho pallet"
      okText="Xác nhận tạo phiếu xuất"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          loaiXuat: LOAI_XUAT_KHO[0],
          trangThai: "cho_xu_ly",
          chiTiet: [{}],
        }}
        onFinish={handleFinish}
        style={{ marginTop: 16 }}
      >
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item label="Mã phiếu xuất">
              <Input disabled placeholder="Hệ thống tự tạo khi lưu" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Nhân viên xuất kho">
              <Input disabled value={user?.hoTen || ""} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              label="Loại xuất kho"
              name="loaiXuat"
              rules={[{ required: true }]}
            >
              <Select
                options={LOAI_XUAT_KHO.map((v) => ({ value: v, label: v }))}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              label="Trạng thái phiếu xuất"
              name="trangThai"
              rules={[{ required: true }]}
            >
              <Select options={TRANG_THAI_OPTIONS} />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item
              label="Lý do xuất & thông tin đơn hàng"
              name="lyDoXuat"
              rules={[
                {
                  required: true,
                  whitespace: true,
                  message: "Vui lòng nhập lý do xuất",
                },
                { max: 500, message: "Tối đa 500 ký tự" },
              ]}
            >
              <Input.TextArea
                rows={2}
                placeholder="Xuất giao đơn hàng #HD-9841 cho khách hàng..."
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              label="Biển số xe"
              name="bienSoXe"
              rules={[{ max: 20, message: "Tối đa 20 ký tự" }]}
            >
              <Input placeholder="51D-892.41" />
            </Form.Item>
          </Col>
        </Row>

        <PhieuKhoItemsField
          matHangOptions={matHangOptions}
          loading={matHangLoading}
        />
      </Form>
    </Modal>
  );
}
