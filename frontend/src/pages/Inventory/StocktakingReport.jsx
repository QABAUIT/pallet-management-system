import { useMemo, useState } from 'react'
import { Button, Card, Input, Radio, Table, Tag, Typography } from 'antd'
import { DownloadOutlined, SearchOutlined } from '@ant-design/icons'

import InventoryPage from '../../components/inventory/InventoryPage'
import StatCard, { StatGrid } from '../../components/inventory/StatCard'
import { tonKhoApi } from '../../api/tonKhoApi'
import { useServerTable } from '../../hooks/useServerTable'
import { useStats } from '../../hooks/useStats'
import { downloadBlob } from '../../utils/downloadFile'
import { fmtNumber, fmtVnd } from '../../utils/format'
import { notifyError } from '../../utils/notify'
import { TINH_TRANG_TON_KHO } from '../../utils/statusTag'

const { Text } = Typography

// Giá trị chatLieu khớp CHECK constraint mat_hang.chat_lieu
const CHAT_LIEU_TABS = [
  { value: '', label: 'Tất cả' },
  { value: 'go', label: 'Pallet gỗ' },
  { value: 'nhua', label: 'Pallet nhựa' },
  { value: 'sat', label: 'Pallet kim loại' },
  { value: 'khac', label: 'Khác' },
]

// Trang "Báo cáo tồn kho" (route /kho)
export default function StocktakingReport() {
  const [chatLieu, setChatLieu] = useState('')
  const [keyword, setKeyword] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [exporting, setExporting] = useState(false)

  const stats = useStats(tonKhoApi.thongKe)

  const extraParams = useMemo(
    () => ({ keyword: searchTerm || undefined, chatLieu: chatLieu || undefined }),
    [searchTerm, chatLieu],
  )
  const table = useServerTable(tonKhoApi.danhSach, extraParams)
  const { pageSize } = table.pagination
  const goToFirstPage = () => table.handleTableChange({ current: 1, pageSize })

  const handleSearch = () => {
    setSearchTerm(keyword.trim())
    goToFirstPage()
  }

  const handleKeywordChange = (value) => {
    setKeyword(value)
    if (value === '' && searchTerm !== '') {
      setSearchTerm('')
      goToFirstPage()
    }
  }

  const handleChatLieuChange = (e) => {
    setChatLieu(e.target.value)
    goToFirstPage()
  }

  const handleExport = async () => {
    setExporting(true)
    try {
      const blob = await tonKhoApi.xuatExcel(extraParams)
      downloadBlob(blob, 'Bao_Cao_Ton_Kho.xlsx')
    } catch (err) {
      notifyError(err)
    } finally {
      setExporting(false)
    }
  }

  const columns = [
    { title: 'MÃ SKU', dataIndex: 'maSku', width: 130, render: (t) => <b>{t}</b> },
    {
      title: 'PALLET',
      key: 'pallet',
      render: (_, r) => (
        <div>
          <div style={{ fontWeight: 600 }}>{r.tenPallet}</div>
          {r.phanLoai && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              {r.phanLoai}
            </Text>
          )}
        </div>
      ),
    },
    { title: 'KÍCH THƯỚC (D x R x C)', dataIndex: 'kichThuoc', width: 180, render: (t) => t || '—' },
    { title: 'SỐ LƯỢNG TỒN', dataIndex: 'soLuongTon', width: 130, align: 'right', render: (v) => <b>{fmtNumber(v)}</b> },
    { title: 'ĐƠN GIÁ ĐỊNH MỨC', dataIndex: 'donGiaDinhMuc', width: 160, align: 'right', render: fmtVnd },
    {
      title: 'TÌNH TRẠNG',
      dataIndex: 'tinhTrang',
      width: 130,
      render: (v) => <Tag color={TINH_TRANG_TON_KHO[v] || 'default'}>{v || '—'}</Tag>,
    },
  ]

  const s = stats.data

  return (
    <InventoryPage>
      <StatGrid columns={3}>
        <StatCard
          label="Tổng số lượng tồn kho"
          value={fmtNumber(s?.tongSoLuongTon)}
          unit="cái"
          color="#1677ff"
          loading={stats.loading}
        />
        <StatCard
          label="Tổng giá trị ước tính"
          value={fmtVnd(s?.tongGiaTriUocTinh)}
          note="Số lượng tồn x đơn giá bán"
          color="#d46b08"
          loading={stats.loading}
        />
        <StatCard
          label="Cảnh báo hết hàng"
          value={fmtNumber(s?.soChungLoaiCanhBao)}
          unit="chủng loại"
          note="Tồn kho thấp hơn hoặc bằng mức tối thiểu"
          color="#cf1322"
          loading={stats.loading}
        />
      </StatGrid>

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
        <Radio.Group
          optionType="button"
          buttonStyle="solid"
          size="large"
          value={chatLieu}
          onChange={handleChatLieuChange}
          options={CHAT_LIEU_TABS}
        />
        <div style={{ display: 'flex', gap: 12 }}>
          <Input
            placeholder="Tìm theo SKU hoặc tên pallet... (nhấn Enter)"
            prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
            style={{ width: 340 }}
            size="large"
            allowClear
            value={keyword}
            onChange={(e) => handleKeywordChange(e.target.value)}
            onPressEnter={handleSearch}
          />
          <Button
            type="primary"
            size="large"
            icon={<DownloadOutlined />}
            loading={exporting}
            onClick={handleExport}
            style={{ backgroundColor: '#d46b08', borderColor: '#d46b08', fontWeight: 500 }}
          >
            Xuất Excel báo cáo
          </Button>
        </div>
      </div>

      <Card
        styles={{ body: { padding: 0 } }}
        style={{ borderRadius: 8, overflow: 'hidden', border: '1px solid #f0f0f0' }}
      >
        <Table
          columns={columns}
          dataSource={table.data}
          loading={table.loading}
          rowKey="matHangId"
          scroll={{ x: 900 }}
          pagination={{
            ...table.pagination,
            showTotal: (total, range) =>
              `Hiển thị ${range[0]} - ${range[1]} trên tổng số ${total} loại pallet`,
          }}
          onChange={table.handleTableChange}
          locale={{ emptyText: 'Chưa có dữ liệu tồn kho' }}
        />
      </Card>
    </InventoryPage>
  )
}
