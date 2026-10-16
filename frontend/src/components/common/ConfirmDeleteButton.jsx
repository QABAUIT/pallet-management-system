import React from "react";
import { Button, Popconfirm } from "antd";
import { DeleteOutlined } from "@ant-design/icons";

/**
 * Nút xoá có xác nhận, dùng thống nhất trong cột "Thao tác" của mọi bảng.
 * Dùng: <ConfirmDeleteButton onConfirm={() => handleDelete(record.id)} />
 * Tùy chọn: title, okText (đổi chữ xác nhận), disabled (mờ nút, vd bản ghi đã ngừng hoạt động).
 */
const ConfirmDeleteButton = ({
  onConfirm,
  title = "Xác nhận xoá bản ghi này?",
  okText = "Xoá",
  disabled = false,
}) => (
  <Popconfirm
    title={title}
    okText={okText}
    cancelText="Huỷ"
    onConfirm={onConfirm}
    disabled={disabled}
  >
    <Button danger icon={<DeleteOutlined />} size="small" disabled={disabled} />
  </Popconfirm>
);

export default ConfirmDeleteButton;
