import React from 'react';
import { Button, Popconfirm } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';

/**
 * Nút xoá có xác nhận, dùng thống nhất trong cột "Thao tác" của mọi bảng.
 * Dùng: <ConfirmDeleteButton onConfirm={() => handleDelete(record.id)} />
 */
const ConfirmDeleteButton = ({ onConfirm, title = 'Xác nhận xoá bản ghi này?' }) => (
  <Popconfirm title={title} okText="Xoá" cancelText="Huỷ" onConfirm={onConfirm}>
    <Button danger icon={<DeleteOutlined />} size="small" />
  </Popconfirm>
);

export default ConfirmDeleteButton;
