import React, { useState } from "react";
import { Modal, Form, Input, Select, Button, Typography, Row, Col } from "antd";
import { SaveOutlined } from "@ant-design/icons";
import { notifyError } from "../../utils/notify";

const { Title, Text } = Typography;
const { Option } = Select;

/**
 * Modal Thêm / Cập nhật nhà cung cấp - dựng giống EmployeeModal
 * (header cam, lưới 3 cột, nút Hủy + Lưu ở cuối).
 * Trang cha chỉ render khi cần mở: {modal.open && <SupplierModal ... />}
 *
 * Props:
 * - mode: 'add' | 'edit'
 * - record: dòng đang sửa (khi mode = 'edit')
 * - nhomHangOptions: mảng chuỗi cho ô Nhóm hàng
 * - onCancel(): đóng modal
 * - onSubmit(payload): gọi API; ném lỗi nếu thất bại (modal sẽ hiện lỗi)
 */
export default function SupplierModal({
  mode,
  record,
  nhomHangOptions = [],
  onCancel,
  onSubmit,
}) {
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const isEdit = mode === "edit";

  const initialValues = isEdit
    ? {
        maNcc: record.maNcc ?? "",
        tenNcc: record.tenNcc ?? "",
        mst: record.mst ?? "",
        sdt: record.sdt ?? "",
        email: record.email ?? "",
        nguoiLienHe: record.nguoiLienHe ?? "",
        diaChi: record.diaChi ?? "",
        nhomHang: record.nhomHang || undefined,
        trangThai: record.trangThai || "dang_hop_tac",
        ghiChu: record.ghiChu ?? "",
      }
    : { trangThai: "dang_hop_tac" };

  const handleFinish = async (values) => {
    // Chuỗi rỗng -> null cho gọn dữ liệu
    const payload = Object.fromEntries(
      Object.entries(values)
        .filter(([k]) => k !== "maNcc") // mã do backend tự sinh, không gửi lên
        .map(([k, v]) => [k, typeof v === "string" ? v.trim() || null : v]),
    );

    setSaving(true);
    try {
      await onSubmit(payload);
    } catch (err) {
      // apiClient trả { message, fieldErrors } - fieldErrors = response.data.data
      const fe = err?.fieldErrors;
      if (fe && typeof fe === "object" && !Array.isArray(fe)) {
        form.setFields(
          Object.entries(fe).map(([name, msg]) => ({
            name,
            errors: [String(msg)],
          })),
        );
      } else {
        notifyError(err);
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open
      centered
      onCancel={onCancel}
      footer={null}
      closable={false}
      maskClosable={false}
      width={900}
      bodyStyle={{ padding: 0 }}
      destroyOnClose
    >
      <div
        style={{
          backgroundColor: "#d46b08",
          padding: "16px 24px",
          color: "white",
          borderTopLeftRadius: "8px",
          borderTopRightRadius: "8px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div>
          <Title
            level={4}
            style={{ color: "white", margin: 0, fontWeight: 600 }}
          >
            {isEdit ? "Cập nhật nhà cung cấp" : "Thêm nhà cung cấp mới"}
          </Title>
          <Text style={{ color: "rgba(255,255,255,0.85)", fontSize: "13px" }}>
            Lưu trữ thông tin nhà cung cấp
          </Text>
        </div>
        <Button
          type="text"
          icon={<span style={{ fontSize: 20, color: "white" }}>×</span>}
          onClick={onCancel}
          style={{ color: "white", padding: 0 }}
        />
      </div>

      <div style={{ padding: "24px" }}>
        <Form
          form={form}
          layout="vertical"
          initialValues={initialValues}
          onFinish={handleFinish}
        >
          <Row gutter={16}>
            {/* Hàng 1: Thông tin định danh */}
            <Col span={8}>
              <Form.Item label="MÃ NHÀ CUNG CẤP" name="maNcc">
                <Input
                  placeholder="Tự động tạo"
                  disabled
                  style={{ backgroundColor: "#fafafa" }}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label={
                  <span>
                    TÊN NHÀ CUNG CẤP <span style={{ color: "red" }}>*</span>
                  </span>
                }
                name="tenNcc"
                rules={[
                  { required: true, message: "Vui lòng nhập tên nhà cung cấp" },
                  { max: 200, message: "Tối đa 200 ký tự" },
                ]}
              >
                <Input placeholder="Công ty TNHH ..." />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label="MÃ SỐ THUẾ"
                name="mst"
                rules={[{ max: 20, message: "Tối đa 20 ký tự" }]}
              >
                <Input placeholder="3604012606" />
              </Form.Item>
            </Col>

            {/* Hàng 2: Liên hệ */}
            <Col span={8}>
              <Form.Item
                label="SỐ ĐIỆN THOẠI"
                name="sdt"
                rules={[{ max: 20, message: "Tối đa 20 ký tự" }]}
              >
                <Input placeholder="0123456789" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label="EMAIL"
                name="email"
                rules={[
                  { type: "email", message: "Email không hợp lệ" },
                  { max: 150, message: "Tối đa 150 ký tự" },
                ]}
              >
                <Input placeholder="lienhe@congty.vn" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label="NGƯỜI LIÊN HỆ"
                name="nguoiLienHe"
                rules={[{ max: 150, message: "Tối đa 150 ký tự" }]}
              >
                <Input placeholder="Nguyễn Văn A" />
              </Form.Item>
            </Col>

            {/* Hàng 3: Địa chỉ & Hợp tác */}
            <Col span={8}>
              <Form.Item
                label={<span>ĐỊA CHỈ </span>}
                name="diaChi"
                rules={[{ max: 255, message: "Tối đa 255 ký tự" }]}
              >
                <Input placeholder="Số 29, đường 7, xã Bình Minh, Đồng Nai" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="NHÓM HÀNG" name="nhomHang">
                <Select allowClear placeholder="Chọn nhóm hàng">
                  {nhomHangOptions.map((v) => (
                    <Option key={v} value={v}>
                      {v}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label="TRẠNG THÁI HỢP TÁC"
                name="trangThai"
                rules={[
                  { required: true, message: "Vui lòng chọn trạng thái" },
                ]}
              >
                <Select placeholder="Chọn trạng thái">
                  <Option value="dang_hop_tac">Đang hợp tác</Option>
                  <Option value="ngung_hop_tac">Ngừng hợp tác</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item label="GHI CHÚ" name="ghiChu">
            <Input.TextArea
              rows={3}
              placeholder="Ghi chú thêm về nhà cung cấp"
            />
          </Form.Item>

          <div
            style={{
              display: "flex",
              justifyContent: "flex-end",
              gap: "8px",
              marginTop: "32px",
            }}
          >
            <Button
              onClick={onCancel}
              size="large"
              style={{ borderRadius: "6px" }}
            >
              Hủy
            </Button>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              icon={<SaveOutlined />}
              loading={saving}
              style={{
                backgroundColor: "#001529",
                borderColor: "#001529",
                borderRadius: "6px",
              }}
            >
              Lưu nhà cung cấp {isEdit ? "" : "mới"}
            </Button>
          </div>
        </Form>
      </div>
    </Modal>
  );
}
