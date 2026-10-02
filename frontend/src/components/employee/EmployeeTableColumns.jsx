import React from 'react';
import { Space, Typography, Avatar, Tag, Button, Popconfirm } from 'antd';
import { UserOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

const { Text } = Typography;

export const getEmployeeColumns = (handleEdit, handleDelete) => [
  {
    title: 'MÃ NHÂN VIÊN',
    dataIndex: 'maNv',
    key: 'maNv',
    render: (text, record) => (
      <div>
        <div style={{ fontWeight: 600 }}>{text || 'N/A'}</div>
        <Text type="secondary" style={{ fontSize: 12 }}>{record.chucVu || ''}</Text>
      </div>
    ),
  },
  {
    title: 'HỌ VÀ TÊN',
    dataIndex: 'hoTen',
    key: 'hoTen',
    render: (text, record) => (
      <Space>
        <Avatar src={record.anhDaiDien} icon={!record.anhDaiDien && <UserOutlined />} />
        <div>
          <div style={{ fontWeight: 600 }}>{text}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.tenBoPhan || record.chucVu}</Text>
        </div>
      </Space>
    ),
  },
  {
    title: 'VAI TRÒ',
    dataIndex: 'tenVaiTro',
    key: 'tenVaiTro',
    render: (role) => {
      let color = 'default';
      let bg = '#f5f5f5';
      let borderColor = '#d9d9d9';

      if (role === 'Quản lí kho' || role === 'Nhân viên kho') { color = '#1d39c4'; bg = '#f0f5ff'; borderColor = '#adc6ff'; }
      else if (role === 'Admin' || role === 'Giám đốc') { color = '#531dab'; bg = '#f9f0ff'; borderColor = '#d3adf7'; }
      else if (role === 'Bán hàng' || role === 'Nhân viên bán hàng' || role === 'Sales') { color = '#08979c'; bg = '#e6fffb'; borderColor = '#87e8de'; }
      else if (role) { color = '#d46b08'; bg = '#fff7e6'; borderColor = '#ffd591'; }

      return <Tag style={{ color, background: bg, borderColor, borderRadius: 4, fontWeight: 500 }}>{role || 'N/A'}</Tag>;
    },
  },
  {
    title: 'SỐ ĐIỆN THOẠI',
    dataIndex: 'sdt',
    key: 'sdt',
    render: (text) => <Text>{text}</Text>
  },
  {
    title: 'EMAIL CÔNG VIỆC',
    dataIndex: 'email',
    key: 'email',
    render: (text) => <Text>{text}</Text>
  },
  {
    title: 'NGÀY VÀO LÀM',
    dataIndex: 'ngayVaoLam',
    key: 'ngayVaoLam',
    render: (text) => {
      if (!text) return null;
      const date = new Date(text);
      return <Text>{date.toLocaleDateString('vi-VN')}</Text>;
    }
  },
  {
    title: 'TRẠNG THÁI',
    dataIndex: 'trangThai',
    key: 'trangThai',
    render: (status) => {
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
    title: 'THAO TÁC',
    key: 'action',
    render: (_, record) => (
      <Space size="middle">
        <Button type="text" icon={<EditOutlined />} style={{ color: '#8c8c8c' }} onClick={() => handleEdit(record)} />
        <Popconfirm title="Bạn có chắc muốn xóa nhân viên này?" onConfirm={() => handleDelete(record.id)}>
          <Button type="text" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      </Space>
    ),
  },
];
