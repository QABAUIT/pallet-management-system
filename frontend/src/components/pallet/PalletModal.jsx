import React from "react";
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
  Upload
} from "antd";
import { PictureOutlined, CloudUploadOutlined, SaveOutlined, DeleteOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;
const { Option } = Select;

export default function PalletModal({
  isModalVisible,
  setIsModalVisible,
  modalMode,
  form,
  handleModalSubmit,
}) {
  return (
    <Modal
      open={isModalVisible}
      centered
      onCancel={() => setIsModalVisible(false)}
      footer={null}
      closable={false}
      width={900}
      bodyStyle={{ padding: 0 }}
      destroyOnClose
    >
      <div style={{ backgroundColor: '#d46b08', padding: '16px 24px', color: 'white', borderTopLeftRadius: '8px', borderTopRightRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <Title level={4} style={{ color: 'white', margin: 0, fontWeight: 600 }}>
            {modalMode === 'add' ? 'Thêm mới pallet' : 'Cập nhật thông tin pallet'}
          </Title>
          <Text style={{ color: 'rgba(255,255,255,0.85)', fontSize: '13px' }}>Lưu trữ thông tin sản phẩm, kỹ thuật</Text>
        </div>
        <Button type="text" icon={<span style={{ fontSize: 20, color: 'white' }}>×</span>} onClick={() => setIsModalVisible(false)} style={{ color: 'white', padding: 0 }} />
      </div>

      <div style={{ padding: '24px' }}>
        <Form form={form} layout="vertical" onFinish={handleModalSubmit}>
          <Row gutter={16}>
            {/* Hàng 1 */}
            <Col span={8}>
              <Form.Item label={<span>MÃ HỆ THỐNG {modalMode === 'add' && <span style={{ color: 'red' }}>*</span>}</span>} name="maMatHang">
                <Input placeholder={modalMode === 'add' ? 'Tự động tạo' : ''} disabled style={{ backgroundColor: '#fafafa' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>TÊN SẢN PHẨM <span style={{ color: 'red' }}>*</span></span>} name="tenMatHang" rules={[{ required: true, message: "Vui lòng nhập tên" }]}>
                <Input placeholder="VD: Pallet Gỗ Tiêu Chuẩn" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>GIÁ BÁN (đ) <span style={{ color: 'red' }}>*</span></span>} name="donGiaBan" rules={[{ required: true, message: "Vui lòng nhập giá bán" }]}>
                <InputNumber style={{ width: "100%" }} min={0} placeholder="Nhập giá bán" />
              </Form.Item>
            </Col>

            {/* Hàng 2 */}
            <Col span={8}>
              <Form.Item label={<span>CHẤT LIỆU <span style={{ color: 'red' }}>*</span></span>} name="chatLieu" rules={[{ required: true, message: "Vui lòng chọn chất liệu" }]}>
                <Select placeholder="Chọn chất liệu">
                  <Option value="go">Gỗ</Option>
                  <Option value="nhua">Nhựa</Option>
                  <Option value="sat">Sắt</Option>
                  <Option value="khac">Khác</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>LOẠI HÀNG <span style={{ color: 'red' }}>*</span></span>} name="loaiMatHang" rules={[{ required: true, message: "Vui lòng chọn loại hàng" }]}>
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
                <InputNumber style={{ width: "100%" }} min={0} placeholder="Nhập chiều dài" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="RỘNG (mm)" name="kichThuocRong">
                <InputNumber style={{ width: "100%" }} min={0} placeholder="Nhập chiều rộng" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="CAO (mm)" name="kichThuocCao">
                <InputNumber style={{ width: "100%" }} min={0} placeholder="Nhập chiều cao" />
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
                  <Form.Item label={<span>KHO LƯU TRỮ <span style={{ color: 'red' }}>*</span></span>} name="khoId" rules={[{ required: true, message: "Vui lòng chọn kho" }]}>
                    <Select placeholder="Chọn kho (ID)">
                      <Option value={1}>Kho Tổng (ID: 1)</Option>
                      <Option value={2}>Kho Miền Nam (ID: 2)</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item label={<span>SỐ LƯỢNG BAN ĐẦU <span style={{ color: 'red' }}>*</span></span>} name="soLuongBanDau" rules={[{ required: true, message: "Nhập số lượng" }]}>
                    <InputNumber style={{ width: "100%" }} min={1} placeholder="Nhập số lượng" />
                  </Form.Item>
                </Col>
              </>
            )}
          </Row>

          <Form.Item label="HÌNH ẢNH SẢN PHẨM">
            {/* Vẫn dùng Text input để lưu URL theo form ban đầu nhưng bọc UI upload bên trên (chỉ UI mô phỏng) */}
            <div style={{ border: '1px dashed #d9d9d9', borderRadius: '8px', padding: '16px', display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
              <Avatar shape="square" size={64} icon={<PictureOutlined />} style={{ backgroundColor: '#f5f5f5', color: '#bfbfbf' }} />
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                  <Text strong>pallet_image.jpg</Text>
                  <Tag color="success">Sẵn sàng</Tag>
                </div>
                <Text type="secondary" style={{ fontSize: '12px', display: 'block', marginBottom: '8px' }}>Định dạng PNG, JPG, tối đa 5MB</Text>
                <Space>
                  <Upload showUploadList={false}>
                    <Button icon={<CloudUploadOutlined />}>Tải ảnh lên / Đổi ảnh</Button>
                  </Upload>
                  <Button type="text" danger icon={<DeleteOutlined />} />
                </Space>
              </div>
            </div>
            <Form.Item name="hinhAnh" noStyle>
              <Input placeholder="Hoặc nhập URL hình ảnh: https://..." />
            </Form.Item>
          </Form.Item>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '32px' }}>
            <Button onClick={() => setIsModalVisible(false)} size="large" style={{ borderRadius: '6px' }}>Hủy</Button>
            <Button type="primary" htmlType="submit" size="large" icon={<SaveOutlined />} style={{ backgroundColor: '#001529', borderColor: '#001529', borderRadius: '6px' }}>
              Lưu thông tin {modalMode === 'add' ? 'mới' : ''}
            </Button>
          </div>
        </Form>
      </div>
    </Modal>
  );
}
