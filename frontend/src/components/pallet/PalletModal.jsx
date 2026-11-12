import React, { useState } from "react";
import {
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Row,
  Col,
  Typography,
  Button,
  Avatar,
  Tag,
  Space,
  Upload,
  message,
} from "antd";
import { PictureOutlined, CloudUploadOutlined, SaveOutlined, DeleteOutlined } from '@ant-design/icons';
import { fileApi } from '../../api/fileApi';

const { Title, Text } = Typography;
const { Option } = Select;

export default function PalletModal({
  isModalVisible,
  setIsModalVisible,
  modalMode,
  form,
  handleModalSubmit,
}) {
  const [uploading, setUploading] = useState(false);
  const hinhAnh = Form.useWatch("hinhAnh", form);

  const handleImageUpload = async (file) => {
    setUploading(true);
    try {
      const res = await fileApi.uploadImage(file);
      const url = res?.data?.url;
      if (!url) {
        throw new Error("Máy chủ không trả về đường dẫn ảnh");
      }
      form.setFieldValue("hinhAnh", url);
      message.success("Tải ảnh lên thành công");
    } catch (error) {
      message.error(error?.message || "Tải ảnh lên thất bại");
    } finally {
      setUploading(false);
    }
  };

  const beforeUpload = (file) => {
    if (!file.type?.startsWith("image/")) {
      message.error("Chỉ chấp nhận file ảnh PNG, JPG hoặc WEBP");
      return Upload.LIST_IGNORE;
    }
    if (file.size > 5 * 1024 * 1024) {
      message.error("Ảnh tối đa 5MB");
      return Upload.LIST_IGNORE;
    }
    handleImageUpload(file);
    return false;
  };

  return (
    <Modal
      open={isModalVisible}
      centered
      onCancel={() => setIsModalVisible(false)}
      footer={null}
      closable={false}
      width={900}
      styles={{ body: { padding: 0 } }}
      destroyOnHidden
    >
      <div style={{ backgroundColor: 'var(--navy)', padding: '16px 24px', color: 'var(--text-inverse)', borderTopLeftRadius: '8px', borderTopRightRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <Title level={4} style={{ color: 'var(--text-inverse)', margin: 0, fontWeight: 600 }}>
            {modalMode === 'add' ? 'Thêm mới pallet' : 'Cập nhật thông tin pallet'}
          </Title>
          <Text style={{ color: 'var(--text-on-dark-muted)', fontSize: '13px' }}>Lưu trữ thông tin sản phẩm, kỹ thuật</Text>
        </div>
        <Button type="text" icon={<span style={{ fontSize: 20, color: 'var(--text-inverse)' }}>×</span>} onClick={() => setIsModalVisible(false)} style={{ color: 'var(--text-inverse)', padding: 0 }} />
      </div>

      <div style={{ padding: '24px' }}>
        <Form form={form} layout="vertical" onFinish={handleModalSubmit}>
          <Row gutter={16}>
            {/* Hàng 1 */}
            <Col span={8}>
              <Form.Item label={<span>MÃ HỆ THỐNG {modalMode === 'add' && <span style={{ color: 'var(--color-red)' }}>*</span>}</span>} name="maMatHang">
                <Input placeholder={modalMode === 'add' ? 'Tự động tạo' : ''} disabled style={{ backgroundColor: 'var(--bg-subtle)' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>TÊN SẢN PHẨM <span style={{ color: 'var(--color-red)' }}>*</span></span>} name="tenMatHang" rules={[{ required: true, message: "Vui lòng nhập tên" }]}>
                <Input placeholder="VD: Pallet Gỗ Tiêu Chuẩn" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>GIÁ BÁN (đ) <span style={{ color: 'var(--color-red)' }}>*</span></span>} name="donGiaBan" rules={[{ required: true, message: "Vui lòng nhập giá bán" }]}>
                <InputNumber style={{ width: "100%" }} min={0} placeholder="Nhập giá bán" />
              </Form.Item>
            </Col>

            {/* Hàng 2 */}
            <Col span={8}>
              <Form.Item label={<span>CHẤT LIỆU <span style={{ color: 'var(--color-red)' }}>*</span></span>} name="chatLieu" rules={[{ required: true, message: "Vui lòng chọn chất liệu" }]}>
                <Select placeholder="Chọn chất liệu">
                  <Option value="go">Gỗ</Option>
                  <Option value="nhua">Nhựa</Option>
                  <Option value="sat">Sắt</Option>
                  <Option value="khac">Khác</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>LOẠI HÀNG <span style={{ color: 'var(--color-red)' }}>*</span></span>} name="loaiMatHang" rules={[{ required: true, message: "Vui lòng chọn loại hàng" }]}>
                <Select placeholder="Chọn loại hàng">
                  <Option value="pallet">Pallet</Option>
                  <Option value="linh_kien">Linh kiện</Option>
                  <Option value="cho_tai_che">Chờ tái chế</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="TIÊU CHUẨN (ISO...)" name="tieuChuan">
                <Input placeholder="VD: ISPM 15" />
              </Form.Item>
            </Col>

            {/* Hàng 3: Kích thước */}
            <Col span={8}>
              <Form.Item label="DÀI (mm)" name="kichThuocDai">
                <InputNumber style={{ width: "100%" }} min={1} placeholder="Nhập chiều dài" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="RỘNG (mm)" name="kichThuocRong">
                <InputNumber style={{ width: "100%" }} min={1} placeholder="Nhập chiều rộng" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="CAO (mm)" name="kichThuocCao">
                <InputNumber style={{ width: "100%" }} min={1} placeholder="Nhập chiều cao" />
              </Form.Item>
            </Col>

            {/* Hàng 4: Tải trọng & Mô tả */}
            <Col span={8}>
              <Form.Item label="TẢI TRỌNG TĨNH (kg)" name="taiTrongTinh">
                <InputNumber style={{ width: "100%" }} min={0} placeholder="Nhập tải trọng tĩnh" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="TẢI TRỌNG ĐỘNG (kg)" name="taiTrongDong">
                <InputNumber style={{ width: "100%" }} min={0} placeholder="Nhập tải trọng động" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="MÔ TẢ CHI TIẾT" name="moTa">
                <Input placeholder="Nhập mô tả" />
              </Form.Item>
            </Col>

            {/* Hàng 5: Khởi tạo tồn kho (Chỉ khi Add) */}
            {modalMode === "add" && (
              <>
                <Col span={8}>
                  <Form.Item label={<span>KHO LƯU TRỮ <span style={{ color: 'var(--color-red)' }}>*</span></span>} name="khoId" rules={[{ required: true, message: "Vui lòng chọn kho" }]}>
                    <Select placeholder="Chọn kho (ID)">
                      <Option value={1}>Kho Tổng (ID: 1)</Option>
                      <Option value={2}>Kho Miền Nam (ID: 2)</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item label={<span>SỐ LƯỢNG BAN ĐẦU <span style={{ color: 'var(--color-red)' }}>*</span></span>} name="soLuongBanDau" rules={[{ required: true, message: "Nhập số lượng" }]}>
                    <InputNumber style={{ width: "100%" }} min={1} placeholder="Nhập số lượng" />
                  </Form.Item>
                </Col>
              </>
            )}
          </Row>

          <Form.Item label="HÌNH ẢNH SẢN PHẨM">
            <div style={{ border: '1px dashed var(--border-mid)', borderRadius: '8px', padding: '16px', display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
              <Avatar
                shape="square"
                size={64}
                src={hinhAnh || undefined}
                icon={<PictureOutlined />}
                style={{ backgroundColor: 'var(--bg-subtle)', color: 'var(--text-faint)', flexShrink: 0 }}
              />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px', flexWrap: 'wrap' }}>
                  <Text strong ellipsis style={{ maxWidth: 320 }}>
                    {hinhAnh ? 'Ảnh sản phẩm đã chọn' : 'Chưa có ảnh sản phẩm'}
                  </Text>
                  {uploading ? (
                    <Tag color="processing">Đang tải...</Tag>
                  ) : hinhAnh ? (
                    <Tag color="success">Sẵn sàng</Tag>
                  ) : null}
                </div>
                <Text type="secondary" style={{ fontSize: '12px', display: 'block', marginBottom: '8px' }}>Định dạng PNG, JPG, WEBP, tối đa 5MB</Text>
                <Space wrap>
                  <Upload
                    accept="image/png,image/jpeg,image/webp"
                    showUploadList={false}
                    beforeUpload={beforeUpload}
                    disabled={uploading}
                  >
                    <Button icon={<CloudUploadOutlined />} loading={uploading}>
                      {hinhAnh ? 'Đổi ảnh' : 'Tải ảnh lên'}
                    </Button>
                  </Upload>
                  <Button
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    disabled={!hinhAnh || uploading}
                    onClick={() => form.setFieldValue('hinhAnh', '')}
                    title="Xoá ảnh"
                  />
                </Space>
              </div>
            </div>
            <Form.Item name="hinhAnh" noStyle>
              <Input placeholder="Hoặc nhập URL hình ảnh: https://..." allowClear maxLength={255} />
            </Form.Item>
          </Form.Item>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '32px' }}>
            <Button onClick={() => setIsModalVisible(false)} size="large" style={{ borderRadius: '6px' }}>Hủy</Button>
            <Button type="primary" htmlType="submit" size="large" icon={<SaveOutlined />} style={{ backgroundColor: 'var(--navy)', borderColor: 'var(--navy)', borderRadius: '6px' }}>
              Lưu thông tin {modalMode === 'add' ? 'mới' : ''}
            </Button>
          </div>
        </Form>
      </div>
    </Modal>
  );
}
