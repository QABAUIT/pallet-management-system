import React from 'react';
import { Button, Radio, Flex, Card, Badge } from 'antd';
import { DownloadOutlined, PlusOutlined } from '@ant-design/icons';
import SearchInput from '../common/SearchInput';
import OtherTypeFilter from '../common/OtherTypeFilter';

export default function PalletHeader({ searchText, setSearchText, filter, setFilter, filterTab, setFilterTab, defaultFilter, totalPallets, onAdd }) {
  return (
    <Card
      styles={{ body: { padding: '16px 24px' } }}
      style={{ marginBottom: 24, borderRadius: 12, boxShadow: '0 1px 3px rgba(0,0,0,0.05)', border: 'none' }}
    >
      <Flex vertical gap="middle">
        {/* Row 1: Search (Left) - RadioGroup (Right) */}
        <Flex justify="space-between" align="center" wrap="wrap" gap="middle">
          <div style={{ width: 320, maxWidth: '100%' }}>
            <SearchInput
              value={searchText}
              onChange={setSearchText}
              placeholder="Tìm kiếm pallet..."
            />
          </div>

          <Radio.Group 
            value={filterTab} 
            onChange={(e) => setFilterTab(e.target.value)}
            style={{ overflowX: 'auto', whiteSpace: 'nowrap' }}
          >
            <Radio.Button value="all">Tất cả Pallet <Badge count={totalPallets} style={{ backgroundColor: '#1677ff', marginLeft: 8 }} /></Radio.Button>
            <Radio.Button value="standard">Tiêu chuẩn</Radio.Button>
            <Radio.Button value="euro">Euro(EPAL)</Radio.Button>
            <Radio.Button value="heavy">Tải trọng nặng</Radio.Button>
            <Radio.Button value="plastic">Pallet nhựa</Radio.Button>
          </Radio.Group>
        </Flex>

        {/* Row 2: Actions (Left) */}
        <Flex gap="small" wrap="wrap">
          <Button icon={<DownloadOutlined />}>Xuất danh mục</Button>
          <Button type="primary" icon={<PlusOutlined />} style={{ background: '#d97706' }} onClick={onAdd}>Thêm pallet mới</Button>
        </Flex>

        {/* Row 3: Filter (Left) */}
        <div >
          <OtherTypeFilter
            label="Bộ lọc nâng cao"
            title="Lọc theo thuộc tính"
            groups={[
              {
                key: "loaiMatHang",
                label: "Loại mặt hàng",
                type: "checkbox",
                options: [
                  { value: "pallet", label: "Pallet" },
                  { value: "linh_kien", label: "Linh kiện" },
                  { value: "cho_tai_che", label: "Chờ tái chế" }
                ]
              },
              {
                key: "chatLieu",
                label: "Chất liệu",
                type: "checkbox",
                options: [
                  { value: "go", label: "Gỗ" },
                  { value: "nhua", label: "Nhựa" },
                  { value: "sat", label: "Sắt" },
                  { value: "khac", label: "Khác" }
                ]
              }
            ]}
            value={filter}
            defaultValue={defaultFilter}
            onApply={setFilter}
          />
        </div>
      </Flex>
    </Card>
  );
}
