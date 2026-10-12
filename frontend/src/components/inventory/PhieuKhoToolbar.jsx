import { Button, DatePicker, Input, Select, Space } from 'antd'
import { DownloadOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import { PHIEU_KHO_TRANG_THAI } from '../../utils/statusTag'

const { RangePicker } = DatePicker

const TRANG_THAI_OPTIONS = Object.entries(PHIEU_KHO_TRANG_THAI).map(([value, { label }]) => ({
  value,
  label,
}))

/** Thanh tìm kiếm / lọc / xuất Excel / tạo phiếu dùng chung cho Lịch sử nhập & xuất kho. */
export default function PhieuKhoToolbar({
  placeholder,
  keyword,
  onKeywordChange,
  onSearch,
  trangThai,
  onTrangThaiChange,
  range,
  onRangeChange,
  onExport,
  exporting,
  onCreate,
  createLabel,
}) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: 12,
        marginBottom: 16,
      }}
    >
      <Space size="middle" wrap>
        <Input
          placeholder={placeholder}
          prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
          style={{ width: 320 }}
          size="large"
          allowClear
          value={keyword}
          onChange={(e) => onKeywordChange(e.target.value)}
          onPressEnter={onSearch}
        />
        <Select
          placeholder="Tất cả trạng thái"
          size="large"
          style={{ width: 190 }}
          allowClear
          value={trangThai}
          onChange={onTrangThaiChange}
          options={TRANG_THAI_OPTIONS}
        />
        <RangePicker
          size="large"
          format="DD/MM/YYYY"
          placeholder={['Từ ngày', 'Đến ngày']}
          value={range}
          onChange={onRangeChange}
        />
      </Space>

      <Space size="middle">
        <Button size="large" icon={<DownloadOutlined />} loading={exporting} onClick={onExport}>
          Xuất file Excel
        </Button>
        <Button
          type="primary"
          size="large"
          icon={<PlusOutlined />}
          onClick={onCreate}
          style={{ backgroundColor: '#d46b08', borderColor: '#d46b08', fontWeight: 500 }}
        >
          {createLabel}
        </Button>
      </Space>
    </div>
  )
}
