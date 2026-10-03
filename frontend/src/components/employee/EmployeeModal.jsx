import React from 'react';
import { Modal, Form, Input, Select, Upload, Button, Avatar, Tag, Space, Typography, Row, Col, DatePicker } from 'antd';
import { UserOutlined, CloudUploadOutlined, SaveOutlined, DeleteOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;
const { Option } = Select;

export default function EmployeeModal({
  isModalVisible,
  setIsModalVisible,
  modalMode,
  form,
  handleModalSubmit
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
            {modalMode === 'add' ? 'Thêm nhân viên mới' : 'Cập nhật nhân viên'}
          </Title>
          <Text style={{ color: 'rgba(255,255,255,0.85)', fontSize: '13px' }}>Lưu trữ thông tin nhân viên</Text>
        </div>
        <Button type="text" icon={<span style={{ fontSize: 20, color: 'white' }}>×</span>} onClick={() => setIsModalVisible(false)} style={{ color: 'white', padding: 0 }} />
      </div>

      <div style={{ padding: '24px' }}>
        <Form form={form} layout="vertical" onFinish={handleModalSubmit}>
          <Row gutter={16}>
            {/* Hàng 1: Thông tin định danh */}
            <Col span={8}>
              <Form.Item label={<span>MÃ NHÂN VIÊN {modalMode === 'add' && <span style={{ color: 'red' }}>*</span>}</span>} name="maNv">
                <Input placeholder={modalMode === 'add' ? 'Tự động tạo' : ''} disabled style={{ backgroundColor: '#fafafa' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>TÊN NHÂN VIÊN <span style={{ color: 'red' }}>*</span></span>} name="hoTen" rules={[{ required: true, message: 'Vui lòng nhập tên nhân viên' }]}>
                <Input placeholder="Nguyễn Văn A" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="NGÀY SINH" name="ngaySinh">
                <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" placeholder="Chọn ngày sinh" />
              </Form.Item>
            </Col>

            {/* Hàng 2: Giấy tờ & Liên hệ */}
            <Col span={8}>
              <Form.Item label={<span>CCCD</span>} name="cccd" rules={[{ message: 'Vui lòng nhập CCCD' }]}>
                <Input placeholder="027306001423" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="SỐ ĐIỆN THOẠI" name="sdt">
                <Input placeholder="0123456789" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="EMAIL" name="email" rules={[{ type: 'email', message: 'Email không hợp lệ' }]}>
                <Input placeholder="nguyenvana@gmail.com" />
              </Form.Item>
            </Col>

            {/* Hàng 3: Công việc 1 */}
            <Col span={8}>
              <Form.Item label={<span>ĐỊA CHỈ </span>} name="diaChi" rules={[{ message: 'Vui lòng nhập địa chỉ' }]}>
                <Input placeholder="79e, đường Võ Thị Sáu, tỉnh Đồng Nai" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>VAI TRÒ <span style={{ color: 'red' }}>*</span></span>} name="vaiTroId" rules={[{ required: true, message: 'Vui lòng chọn vai trò' }]}>
                <Select placeholder="NHÂN VIÊN KHO">
                  <Option value={1}>Giám đốc</Option>
                  <Option value={2}>Phó giám đốc</Option>
                  <Option value={3}>Nhân viên kho</Option>
                  <Option value={4}>Nhân viên bán hàng</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="CHỨC VỤ" name="chucVu">
                <Input placeholder="Ví dụ: Tổ trưởng" />
              </Form.Item>
            </Col>

            {/* Hàng 4: Công việc 2 & Hệ thống */}
            <Col span={8}>
              <Form.Item label="NGÀY VÀO LÀM" name="ngayVaoLam">
                <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" placeholder="Chọn ngày vào làm" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={<span>TÊN ĐĂNG NHẬP <span style={{ color: 'red' }}>*</span></span>} name="tenDangNhap" rules={[{ required: true, message: 'Vui lòng nhập tên đăng nhập' }]}>
                <Input placeholder="Ví dụ: nguyenvana" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="TRẠNG THÁI LÀM VIỆC" name="trangThai">
                <Select placeholder="Chọn trạng thái">
                  <Option value="dang_lam_viec">Đang làm việc</Option>
                  <Option value="da_nghi_viec">Đã nghỉ việc</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item label="HÌNH ẢNH NHÂN VIÊN" name="anhDaiDien">
            <div style={{ border: '1px dashed #d9d9d9', borderRadius: '8px', padding: '16px', display: 'flex', alignItems: 'center', gap: '16px' }}>
              <Avatar shape="square" size={64} icon={<UserOutlined />} style={{ backgroundColor: '#f5f5f5', color: '#bfbfbf' }} />
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                  <Text strong>anh_the.jpg</Text>
                  <Tag color="success">Đã tải lên</Tag>
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
          </Form.Item>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '32px' }}>
            <Button onClick={() => setIsModalVisible(false)} size="large" style={{ borderRadius: '6px' }}>Hủy</Button>
            <Button type="primary" htmlType="submit" size="large" icon={<SaveOutlined />} style={{ backgroundColor: '#001529', borderColor: '#001529', borderRadius: '6px' }}>
              Lưu nhân viên {modalMode === 'add' ? 'mới' : ''}
            </Button>
          </div>
        </Form>
      </div>
    </Modal>
  );
}
