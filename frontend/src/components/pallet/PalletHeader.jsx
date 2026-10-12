import React from 'react';
import { Button, Radio, Flex, Card, Badge } from 'antd';
import { DownloadOutlined, PlusOutlined } from '@ant-design/icons';
import SearchInput from '../common/SearchInput';
import OtherTypeFilter from '../common/OtherTypeFilter';

export default function PalletHeader({ searchText, setSearchText, filter, setFilter, filterTab, setFilterTab, defaultFilter, totalPallets, onAdd, onExport }) {
  return (
    <Card
      styles={{ body: { padding: '16px 24px' } }}
      style={{ marginBottom: 24, borderRadius: 12, boxShadow: '0 1px 3px rgba(0,0,0,0.05)', border: 'none' }}
    >
      <Flex vertical gap="middle">
        {/* Row 1: Search + Filter (Left) - RadioGroup (Right) */}
        <Flex justify="space-between" align="center" gap="middle" style={{ overflow: 'hidden' }}>
          <Flex align="center" gap="small">
            <div style={{ width: 280, maxWidth: '100%' }}>
              <SearchInput
                value={searchText}
                onChange={setSearchText}
                placeholder="Tìm kiếm pallet..."
              />
            </div>
            
            <div>
              <OtherTypeFilter
                label="Bộ lọc nâng cao"
                title="Lọc theo thuộc tính"
                groups={[
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

          <Radio.Group
            value={filterTab}
            onChange={(e) => setFilterTab(e.target.value)}
            style={{ overflowX: 'auto', whiteSpace: 'nowrap', minWidth: 0 }}
          >
            <Radio.Button value="all">Tất cả <Badge count={totalPallets} style={{ backgroundColor: '#1677ff', marginLeft: 8 }} /></Radio.Button>
            <Radio.Button value="pallet">Pallet</Radio.Button>
            <Radio.Button value="linh_kien">Linh kiện</Radio.Button>
            <Radio.Button value="cho_tai_che">Chờ tái chế</Radio.Button>
          </Radio.Group>
        </Flex>

        {/* Row 2: Actions (Left) */}
        <Flex gap="small" wrap="wrap">
          <Button icon={<DownloadOutlined />} onClick={onExport}>Xuất danh mục</Button>
          <Button type="primary" icon={<PlusOutlined />} style={{ background: '#d97706' }} onClick={onAdd}>Thêm pallet mới</Button>
        </Flex>

        {/* Row 3: Filter (Left) */}

      </Flex>
    </Card>
  );
}
