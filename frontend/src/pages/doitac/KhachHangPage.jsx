import React, { useState } from 'react';
import { Table, Button, Input, Select, Space, Tag, Modal, Form, message } from 'antd';
import { PlusOutlined, EditOutlined, SearchOutlined } from '@ant-design/icons';
import PageHeader from '../../components/common/PageHeader';
import ConfirmDeleteButton from '../../components/common/ConfirmDeleteButton';
import { useServerTable } from '../../hooks/useServerTable';
import { createCrudService } from '../../services/createCrudService';
import { notifySuccess, notifyError } from '../../utils/notify';
import { TRANG_THAI_HOAT_DONG, resolveStatus } from '../../utils/statusTag';

// Đổi '/khach-hang' thành đúng path Controller BE của bạn (ví dụ '/api/v1/khach-hang'
// đã được apiClient tự thêm sẵn phần '/api/v1', chỉ cần khai path còn lại).
const khachHangService = createCrudService('/khach-hang');

/**
 * TRANG MẪU ĐẦY ĐỦ - copy file này làm khung cho trang của bạn, đổi:
 *  1. khachHangService -> service module của bạn (đổi basePath)
 *  2. columns -> field của entity bạn
 *  3. FormModal (bên dưới) -> field nhập của entity bạn
 * Các phần còn lại (search, phân trang, xoá có xác nhận, modal thêm/sửa)
 * giữ nguyên pattern để đồng nhất trải nghiệm giữa các màn hình.
 */
const KhachHangPage = () => {
  const [tuKhoa, setTuKhoa] = useState('');
  const [trangThai, setTrangThai] = useState(undefined);
  const [modalOpen, setModalOpen] = useState(false);
  const [dangSua, setDangSua] = useState(null); // null = thêm mới, có giá trị = đang sửa
  const [form] = Form.useForm();

  const table = useServerTable(khachHangService.getAll, {
    tenKhChua: tuKhoa || undefined,
    trangThai,
  });

  const moModalThem = () => {
    setDangSua(null);
    form.resetFields();
    setModalOpen(true);
  };

  const moModalSua = (record) => {
    setDangSua(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const xuLyLuu = async () => {
    try {
      const values = await form.validateFields();
      if (dangSua) {
        await khachHangService.update(dangSua.id, values);
        notifySuccess('Cập nhật khách hàng thành công');
      } else {
        await khachHangService.create(values);
        notifySuccess('Thêm khách hàng thành công');
      }
      setModalOpen(false);
      table.reload();
    } catch (err) {
      if (err?.errorFields) return; // lỗi validate form, antd tự hiển thị
      notifyError(err);
    }
  };

  const xuLyXoa = async (id) => {
    try {
      await khachHangService.remove(id);
      notifySuccess('Đã xoá khách hàng');
      table.reload();
    } catch (err) {
      notifyError(err);
    }
  };

  const columns = [
    { title: 'Mã KH', dataIndex: 'maKh', width: 120 },
    { title: 'Tên khách hàng', dataIndex: 'tenKh' },
    { title: 'Loại KH', dataIndex: 'loaiKh', width: 130 },
    { title: 'SĐT', dataIndex: 'sdt', width: 130 },
    { title: 'Email', dataIndex: 'email' },
    {
      title: 'Trạng thái',
      dataIndex: 'trangThai',
      width: 150,
      render: (value) => {
        const { label, color } = resolveStatus(TRANG_THAI_HOAT_DONG, value);
        return <Tag color={color}>{label}</Tag>;
      },
    },
    {
      title: 'Thao tác',
      width: 110,
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} size="small" onClick={() => moModalSua(record)} />
          <ConfirmDeleteButton onConfirm={() => xuLyXoa(record.id)} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Quản lý Khách hàng"
        actions={
          <Button type="primary" icon={<PlusOutlined />} onClick={moModalThem}>
            Thêm khách hàng
          </Button>
        }
      />

      <Space style={{ marginBottom: 16 }}>
        <Input
          placeholder="Tìm theo tên khách hàng..."
          prefix={<SearchOutlined />}
          value={tuKhoa}
          onChange={(e) => setTuKhoa(e.target.value)}
          allowClear
          style={{ width: 260 }}
        />
        <Select
          placeholder="Trạng thái"
          allowClear
          style={{ width: 180 }}
          value={trangThai}
          onChange={setTrangThai}
          options={[
            { value: 'dang_hop_tac', label: 'Đang hợp tác' },
            { value: 'ngung_hop_tac', label: 'Ngừng hợp tác' },
          ]}
        />
      </Space>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={table.data}
        loading={table.loading}
        pagination={table.pagination}
        onChange={table.handleTableChange}
      />

      <Modal
        title={dangSua ? 'Sửa khách hàng' : 'Thêm khách hàng'}
        open={modalOpen}
        onOk={xuLyLuu}
        onCancel={() => setModalOpen(false)}
        okText="Lưu"
        cancelText="Huỷ"
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="maKh" label="Mã khách hàng" rules={[{ required: true, message: 'Bắt buộc nhập mã KH' }]}>
            <Input disabled={!!dangSua} />
          </Form.Item>
          <Form.Item name="tenKh" label="Tên khách hàng" rules={[{ required: true, message: 'Bắt buộc nhập tên KH' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="loaiKh" label="Loại khách hàng" initialValue="doanh_nghiep">
            <Select
              options={[
                { value: 'ca_nhan', label: 'Cá nhân' },
                { value: 'doanh_nghiep', label: 'Doanh nghiệp' },
              ]}
            />
          </Form.Item>
          <Form.Item name="sdt" label="Số điện thoại">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ type: 'email', message: 'Email không hợp lệ' }]}>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default KhachHangPage;
