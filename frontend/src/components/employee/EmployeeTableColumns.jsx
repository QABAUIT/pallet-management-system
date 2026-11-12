import React from 'react';
import { Space, Typography, Avatar, Tag, Button } from 'antd';
import { UserOutlined, EditOutlined } from '@ant-design/icons';
import ConfirmDeleteButton from '../common/ConfirmDeleteButton';

const { Text } = Typography;

export const getEmployeeColumns = (handleEdit, handleDelete) => [
  {
    key: 'maNv',
    header: 'MÃ NHÂN VIÊN',
    render: (record) => (
      <div>
        <div style={{ fontWeight: 600 }}>{record.maNv || 'N/A'}</div>
        <Text type="secondary" style={{ fontSize: 12 }}>{record.chucVu || ''}</Text>
      </div>
    ),
  },
  {
    key: 'hoTen',
    header: 'HỌ VÀ TÊN',
    render: (record) => (
      <Space>
        <Avatar src={record.anhDaiDien} icon={!record.anhDaiDien && <UserOutlined />} />
        <div>
          <div style={{ fontWeight: 600 }}>{record.hoTen}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.tenBoPhan || record.chucVu}</Text>
        </div>
      </Space>
    ),
  },
  {
    key: 'tenVaiTro',
    header: 'VAI TRÒ',
    render: (record) => {
      const role = record.tenVaiTro;
      let tone = 'slate';

      if (role === 'Quản lí kho' || role === 'Nhân viên kho') tone = 'blue';
      else if (role === 'Admin' || role === 'Giám đốc' || role === 'Phó giám đốc') tone = 'purple';
      else if (role === 'Bán hàng' || role === 'Nhân viên bán hàng' || role === 'Sales') tone = 'green';
      else if (role) tone = 'amber';

      return <Tag className={`status-tag status-${tone}`}>{role || 'N/A'}</Tag>;
    },
  },
  {
    key: 'sdt',
    header: 'SỐ ĐIỆN THOẠI',
    render: (record) => <Text>{record.sdt}</Text>
  },
  {
    key: 'email',
    header: 'EMAIL CÔNG VIỆC',
    render: (record) => <Text>{record.email}</Text>
  },
  {
    key: 'ngayVaoLam',
    header: 'NGÀY VÀO LÀM',
    render: (record) => {
      if (!record.ngayVaoLam) return null;
      const date = new Date(record.ngayVaoLam);
      return <Text>{date.toLocaleDateString('vi-VN')}</Text>;
    }
  },
  {
    key: 'trangThai',
    header: 'TRẠNG THÁI',
    render: (record) => {
      let status = record.trangThai;
      let tone = 'slate';

      if (status === 'Đang làm việc' || status === 'dang_lam_viec') {
        tone = 'green';
        status = 'Đang làm việc';
      } else if (status === 'Đã nghỉ việc' || status === 'da_nghi_viec') {
        tone = 'red';
        status = 'Đã nghỉ việc';
      } else if (status === 'Nghỉ phép') {
        tone = 'amber';
      }

      return <Tag className={`status-tag status-${tone}`}>{status || 'N/A'}</Tag>;
    },
  },
  {
    key: 'action',
    header: 'THAO TÁC',
    align: 'center',
    render: (record) => (
      <Space size="middle">
        <Button type="text" className="table-action-btn" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
        <ConfirmDeleteButton onConfirm={() => handleDelete(record.id)} />
      </Space>
    ),
  },
];
