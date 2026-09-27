import React from 'react'
import { Result } from 'antd'
import { ToolOutlined } from '@ant-design/icons'

/**
 * Dùng tạm cho các màn chưa có bạn nào nhận code. Xóa import này và viết
 * UI thật khi bắt tay vào làm feature của mình - xem features/mathang/MatHangPage.jsx
 * làm ví dụ mẫu đầy đủ (gọi API, bảng danh sách, modal thêm/sửa).
 */
export default function Placeholder({ tenManHinh, ghiChu }) {
  return (
    <Result
      icon={<ToolOutlined style={{ color: '#f5a623' }} />}
      title={tenManHinh}
      subTitle={ghiChu || 'Màn hình này chưa được code - xem features/mathang/MatHangPage.jsx làm mẫu.'}
    />
  )
}
