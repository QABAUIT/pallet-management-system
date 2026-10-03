import React from 'react';
import { Space, Input, Select, Button } from 'antd';
import { SearchOutlined, PlusOutlined } from '@ant-design/icons';

const { Option } = Select;

export default function EmployeeFilterBar({
  searchText,
  setSearchText,
  filterStatus,
  setFilterStatus,
  filterRole,
  setFilterRole,
  handleAdd
}) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px', alignItems: 'center' }}>
      <Space size="middle">
        <Input
          placeholder="Tìm theo mã NV, họ tên, số điện thoại..."
          prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
          style={{ width: 320, borderRadius: '6px' }}
          size="large"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
        />
        <Select
          placeholder="Tất cả trạng thái"
          size="large"
          style={{ width: 170 }}
          allowClear
          value={filterStatus}
          onChange={setFilterStatus}
        >
          <Option value="dang_lam_viec">Đang làm việc</Option>
          <Option value="da_nghi_viec">Đã nghỉ việc</Option>
        </Select>
        <Select
          placeholder="Tất cả vai trò"
          size="large"
          style={{ width: 170 }}
          allowClear
          value={filterRole}
          onChange={setFilterRole}
        >
          <Option value="Giám đốc">Giám đốc</Option>
          <Option value="Phó giám đốc">Phó giám đốc</Option>
          <Option value="Nhân viên kho">Nhân viên kho</Option>
          <Option value="Nhân viên bán hàng">Nhân viên bán hàng</Option>
        </Select>
      </Space>

      <Button
        type="primary"
        icon={<PlusOutlined />}
        size="large"
        style={{ backgroundColor: '#d46b08', borderColor: '#d46b08', borderRadius: '6px', fontWeight: 500 }}
        onClick={handleAdd}
      >
        Thêm nhân viên mới
      </Button>
    </div>
  );
}
