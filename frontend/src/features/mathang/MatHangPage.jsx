import React, { useEffect, useState } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, Select, Tag, message, Space } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { matHangApi } from '../../api/matHangApi'

const { Option } = Select

export default function MatHangPage() {
  const [danhSach, setDanhSach] = useState([])
  const [dangTai, setDangTai] = useState(false)
  const [mo, setMo] = useState(false)
  const [dangSua, setDangSua] = useState(null) // null = thêm mới, object = đang sửa
  const [form] = Form.useForm()

  const taiDanhSach = async () => {
    setDangTai(true)
    try {
      const res = await matHangApi.danhSach()
      setDanhSach(res.data)
    } catch (err) {
      message.error(err.message)
    } finally {
      setDangTai(false)
    }
  }

  useEffect(() => {
    taiDanhSach()
  }, [])

  const moModalThemMoi = () => {
    setDangSua(null)
    form.resetFields()
    setMo(true)
  }

  const moModalSua = (record) => {
    setDangSua(record)
    form.setFieldsValue(record)
    setMo(true)
  }

  const onSubmit = async () => {
    try {
      const values = await form.validateFields()
      if (dangSua) {
        await matHangApi.capNhat(dangSua.id, values)
        message.success('Cập nhật thành công')
      } else {
        await matHangApi.taoMoi(values)
        message.success('Tạo mặt hàng thành công')
      }
      setMo(false)
      taiDanhSach()
    } catch (err) {
      if (err?.errorFields) return // lỗi validate form, antd tự hiển thị
      message.error(err.message || 'Có lỗi xảy ra')
    }
  }

  const ngungKinhDoanh = async (id) => {
    try {
      await matHangApi.ngungKinhDoanh(id)
      message.success('Đã ngừng kinh doanh')
      taiDanhSach()
    } catch (err) {
      message.error(err.message)
    }
  }

  const columns = [
    { title: 'Mã', dataIndex: 'maMatHang', width: 110 },
    { title: 'Tên mặt hàng', dataIndex: 'tenMatHang' },
    {
      title: 'Loại',
      dataIndex: 'loaiMatHang',
      width: 100,
      render: (v) => <Tag color={v === 'pallet' ? 'blue' : 'orange'}>{v}</Tag>,
    },
    {
      title: 'Đơn giá bán',
      dataIndex: 'donGiaBan',
      align: 'right',
      render: (v) => v?.toLocaleString('vi-VN') + ' đ',
    },
    {
      title: 'Tồn kho / Khả dụng',
      key: 'ton',
      align: 'right',
      render: (_, r) => `${r.tongTonKho ?? 0} / ${r.tongKhaDung ?? 0}`,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'trangThai',
      render: (v) => (
        <Tag color={v === 'dang_kinh_doanh' ? 'green' : 'default'}>
          {v === 'dang_kinh_doanh' ? 'Đang kinh doanh' : 'Ngừng kinh doanh'}
        </Tag>
      ),
    },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <a onClick={() => moModalSua(record)}>Sửa</a>
          {record.trangThai === 'dang_kinh_doanh' && (
            <a onClick={() => ngungKinhDoanh(record.id)} style={{ color: '#cf1322' }}>
              Ngừng KD
            </a>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>Quản lí Pallet</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={moModalThemMoi}>
          Thêm pallet mới
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={danhSach}
        loading={dangTai}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={dangSua ? 'Sửa mặt hàng' : 'Thêm pallet mới'}
        open={mo}
        onCancel={() => setMo(false)}
        onOk={onSubmit}
        okText={dangSua ? 'Lưu' : 'Tạo mới'}
        width={640}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="tenMatHang" label="Tên pallet" rules={[{ required: true }]}>
            <Input placeholder="VD: Pallet Gỗ" />
          </Form.Item>

          <Space size="middle" style={{ display: 'flex' }}>
            <Form.Item name="loaiMatHang" label="Loại mặt hàng" rules={[{ required: true }]} initialValue="pallet" style={{ flex: 1 }}>
              <Select>
                <Option value="pallet">Pallet</Option>
                <Option value="linh_kien">Linh kiện (ván, thanh gỗ...)</Option>
              </Select>
            </Form.Item>
            <Form.Item name="chatLieu" label="Chất liệu" style={{ flex: 1 }}>
              <Select allowClear>
                <Option value="go">Gỗ</Option>
                <Option value="nhua">Nhựa</Option>
                <Option value="sat">Sắt</Option>
                <Option value="khac">Khác</Option>
              </Select>
            </Form.Item>
          </Space>

          <Space size="middle" style={{ display: 'flex' }}>
            <Form.Item name="kichThuocDai" label="Dài (mm)" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
            <Form.Item name="kichThuocRong" label="Rộng (mm)" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
            <Form.Item name="kichThuocCao" label="Cao (mm)" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Space>

          <Space size="middle" style={{ display: 'flex' }}>
            <Form.Item name="taiTrongTinh" label="Tải trọng tĩnh (kg)" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
            <Form.Item name="taiTrongDong" label="Tải trọng động (kg)" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Space>

          <Form.Item name="donGiaBan" label="Đơn giá niêm yết (VNĐ)" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0} step={1000}
              formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>

          {!dangSua && (
            <Space size="middle" style={{ display: 'flex' }}>
              <Form.Item name="soLuongBanDau" label="Số lượng ban đầu" style={{ flex: 1 }}>
                <InputNumber style={{ width: '100%' }} min={1} placeholder="Bỏ trống nếu chưa nhập hàng" />
              </Form.Item>
              <Form.Item name="khoId" label="Nhập vào kho" style={{ flex: 1 }}>
                {/* TODO: thay danh sách cứng này bằng gọi API GET /kho thật khi bạn làm feature Kho */}
                <Select placeholder="Chọn kho" allowClear>
                  <Option value={1}>Kho Tổng Tân Bình</Option>
                  <Option value={2}>Kho Chi nhánh Bình Dương</Option>
                </Select>
              </Form.Item>
            </Space>
          )}

          <Form.Item name="moTa" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
