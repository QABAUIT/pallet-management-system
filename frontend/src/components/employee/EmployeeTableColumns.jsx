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
      let color = 'default';
      let bg = '#f5f5f5';
      let borderColor = '#d9d9d9';

      if (role === 'Quản lí kho' || role === 'Nhân viên kho') { color = '#1d39c4'; bg = '#f0f5ff'; borderColor = '#adc6ff'; }
      else if (role === 'Admin' || role === 'Giám đốc' || role === 'Phó giám đốc') { color = '#531dab'; bg = '#f9f0ff'; borderColor = '#d3adf7'; }
      else if (role === 'Bán hàng' || role === 'Nhân viên bán hàng' || role === 'Sales') { color = '#08979c'; bg = '#e6fffb'; borderColor = '#87e8de'; }
      else if (role) { color = '#d46b08'; bg = '#fff7e6'; borderColor = '#ffd591'; }

      return <Tag style={{ color, background: bg, borderColor, borderRadius: 4, fontWeight: 500 }}>{role || 'N/A'}</Tag>;
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
      let color = '#595959';
      let bg = '#fafafa';
      let borderColor = '#d9d9d9';

      if (status === 'Đang làm việc' || status === 'dang_lam_viec') {
        color = '#389e0d'; bg = '#f6ffed'; borderColor = '#b7eb8f';
        status = 'Đang làm việc';
      } else if (status === 'Đã nghỉ việc' || status === 'da_nghi_viec') {
        color = '#cf1322'; bg = '#fff1f0'; borderColor = '#ffa39e';
        status = 'Đã nghỉ việc';
      } else if (status === 'Nghỉ phép') {
        color = '#d48806'; bg = '#fffbe6'; borderColor = '#ffe58f';
      }

      return <Tag style={{ color, background: bg, borderColor, borderRadius: 4, fontWeight: 500 }}>{status || 'N/A'}</Tag>;
    },
  },
  {
    key: 'action',
    header: 'THAO TÁC',
    align: 'center',
    render: (record) => (
      <Space size="middle">
        <Button type="text" icon={<EditOutlined />} style={{ color: '#8c8c8c' }} onClick={() => handleEdit(record)} />
        <ConfirmDeleteButton onConfirm={() => handleDelete(record.id)} />
      </Space>
    ),
  },
];
