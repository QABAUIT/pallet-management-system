import React from 'react';
import { Modal, Form, Input, Select, InputNumber, Row, Col, Divider } from 'antd';

export default function PalletModal({ isModalVisible, setIsModalVisible, modalMode, form, handleModalSubmit }) {
  return (
    <Modal
      title={modalMode === 'add' ? 'Thêm mới pallet' : 'Cập nhật thông tin pallet'}
      open={isModalVisible}
      onOk={() => form.submit()}
      onCancel={() => setIsModalVisible(false)}
      okText="Lưu"
      cancelText="Hủy"
      width={700}
      style={{ top: 20 }}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleModalSubmit}
      >
        <Row gutter={12}>
          {modalMode === 'edit' && (
            <Col span={8}>
              <Form.Item name="maMatHang" label="Mã hệ thống">
                <Input disabled />
              </Form.Item>
            </Col>
          )}
          <Col span={modalMode === 'edit' ? 16 : 24}>
            <Form.Item name="tenMatHang" label="Tên sản phẩm" rules={[{ required: true, message: 'Nhập tên' }]}>
              <Input placeholder="VD: Pallet Gỗ Tiêu Chuẩn" />
            </Form.Item>
          </Col>
          
          <Col span={8}>
            <Form.Item name="chatLieu" label="Chất liệu" rules={[{ required: true, message: 'Chọn' }]}>
              <Select>
                <Select.Option value="go">Gỗ</Select.Option>
                <Select.Option value="nhua">Nhựa</Select.Option>
                <Select.Option value="sat">Sắt</Select.Option>
                <Select.Option value="khac">Khác</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="loaiMatHang" label="Loại hàng" rules={[{ required: true, message: 'Chọn' }]}>
              <Select>
                <Select.Option value="pallet">Pallet</Select.Option>
                <Select.Option value="linh_kien">Linh kiện</Select.Option>
                <Select.Option value="cho_tai_che">Chờ tái chế</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="tieuChuan" label="Tiêu chuẩn (ISO...)">
              <Input placeholder="VD: ISPM 15" />
            </Form.Item>
          </Col>

          <Col span={8}>
            <Form.Item name="kichThuocDai" label="Dài (mm)">
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="kichThuocRong" label="Rộng (mm)">
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="kichThuocCao" label="Cao (mm)">
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Col>

          <Col span={8}>
            <Form.Item name="taiTrongTinh" label="Tải tĩnh (kg)">
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="taiTrongDong" label="Tải động (kg)">
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="donGiaBan" label="Giá bán (đ)" rules={[{ required: true, message: 'Nhập giá' }]}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Col>

          <Col span={12}>
            <Form.Item name="hinhAnh" label="URL Hình ảnh" style={{ marginBottom: 12 }}>
              <Input placeholder="https://..." />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="moTa" label="Mô tả chi tiết" style={{ marginBottom: 12 }}>
              <Input rows={1} />
            </Form.Item>
          </Col>
        </Row>

        {modalMode === 'add' && (
          <>
            <Divider style={{ margin: '12px 0' }} />
            <div style={{ marginBottom: 8, fontWeight: 500 }}>Khởi tạo tồn kho ban đầu</div>
            <Row gutter={12}>
              <Col span={12}>
                <Form.Item name="soLuongBanDau" label="Số lượng" style={{ marginBottom: 0 }}>
                  <InputNumber style={{ width: '100%' }} min={1} placeholder="Nhập số lượng" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="khoId" label="Kho lưu trữ" style={{ marginBottom: 0 }}>
                  <Select placeholder="Chọn kho (ID)">
                    {/* Hardcode temporary options since we don't have khoApi integrated yet */}
                    <Select.Option value={1}>Kho Tổng (ID: 1)</Select.Option>
                    <Select.Option value={2}>Kho Miền Nam (ID: 2)</Select.Option>
                  </Select>
                </Form.Item>
              </Col>
            </Row>
          </>
        )}
      </Form>
    </Modal>
  );
}
