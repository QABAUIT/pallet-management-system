import React from 'react';
import { Space, Typography } from 'antd';

const { Title } = Typography;

/**
 * Header chuẩn cho mọi trang danh sách: tiêu đề bên trái + nút hành động
 * (VD nút "Thêm mới") bên phải. Dùng để mọi màn hình có bố cục đồng nhất.
 */
const PageHeader = ({ title, actions }) => (
  <div
    style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginBottom: 16,
    }}
  >
    <Title level={4} style={{ margin: 0 }}>
      {title}
    </Title>
    <Space>{actions}</Space>
  </div>
);

export default PageHeader;
