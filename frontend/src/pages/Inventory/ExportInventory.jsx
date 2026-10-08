import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, Table, Tag, Typography } from 'antd'
import { EyeOutlined } from '@ant-design/icons'

import InventoryPage from '../../components/inventory/InventoryPage'
import PhieuKhoToolbar from '../../components/inventory/PhieuKhoToolbar'
import CreateExportModal from '../../components/inventory/CreateExportModal'
import StatCard, { StatGrid } from '../../components/inventory/StatCard'
import { phieuXuatApi } from '../../api/phieuXuatApi'
import { usePhieuKhoList } from '../../hooks/usePhieuKhoList'
import { useStats } from '../../hooks/useStats'
import { fmtDateTime, fmtNumber } from '../../utils/format'
import { LOAI_XUAT_MAU, PHIEU_KHO_TRANG_THAI, resolveStatus } from '../../utils/statusTag'

const { Text } = Typography

export default function ExportInventory() {
  const navigate = useNavigate()
  const [openCreate, setOpenCreate] = useState(false)

  const list = usePhieuKhoList({
    fetchList: phieuXuatApi.danhSach,
    exportExcel: phieuXuatApi.xuatExcel,
    fileName: 'Lich_Su_Xuat_Kho.xlsx',
  })
  const stats = useStats(phieuXuatApi.thongKe)
  const { table } = list

  const columns = [
    {
      title: 'MÃ PHIẾU XUẤT',
      dataIndex: 'maPhieuXuat',
      width: 160,
      render: (t) => <b>{t}</b>,
    },
    {
      title: 'NHÂN VIÊN XUẤT',
      key: 'nhanVien',
      width: 190,
      render: (_, r) => (
        <div>
          <div style={{ fontWeight: 600 }}>{r.maNhanVienXuat || '—'}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {r.tenNhanVienXuat}
          </Text>
        </div>
      ),
    },
    { title: 'NGÀY XUẤT KHO', dataIndex: 'ngayXuatKho', width: 160, render: fmtDateTime },
    {
      title: 'LOẠI XUẤT',
      dataIndex: 'loaiXuatKho',
      width: 130,
      render: (v) => <Tag color={LOAI_XUAT_MAU[v] || 'default'}>{v || '—'}</Tag>,
    },
    { title: 'LÝ DO XUẤT KHO', dataIndex: 'lyDoXuat', ellipsis: true, render: (t) => t || '—' },
    { title: 'SỐ LƯỢNG', dataIndex: 'tongSoLuong', width: 110, align: 'right', render: fmtNumber },
    {
      title: 'TRẠNG THÁI',
      dataIndex: 'trangThai',
      width: 150,
      render: (v) => {
        const s = resolveStatus(PHIEU_KHO_TRANG_THAI, v)
        return <Tag color={s.color}>{s.label}</Tag>
      },
    },
    {
      title: 'THAO TÁC',
      key: 'action',
      width: 90,
      align: 'center',
      render: (_, record) => (
        <Button
          type="text"
          icon={<EyeOutlined />}
          style={{ color: '#8c8c8c' }}
          onClick={() => navigate(`/kho/xuat-kho/${record.id}`)}
        />
      ),
    },
  ]

  const handleCreated = () => {
    setOpenCreate(false)
    table.reload()
    stats.reload()
  }

  const s = stats.data

  return (
    <InventoryPage>
      <StatGrid columns={4}>
        <StatCard
          label="Tổng xuất hôm nay"
          value={fmtNumber(s?.tongXuatHomNay)}
          unit="pallet"
          note="Chỉ tính phiếu đã hoàn thành"
          color="#1677ff"
          loading={stats.loading}
        />
        <StatCard
          label="Đang bốc dỡ & xếp xe"
          value={fmtNumber(s?.dangBocDoXepXe)}
          unit="pallet"
          note="Phiếu đang chờ xử lý"
          color="#d46b08"
          loading={stats.loading}
        />
        <StatCard
          label="Xuất bảo dưỡng định kỳ"
          value={fmtNumber(s?.xuatBaoDuongDinhKy)}
          unit="pallet"
          color="#722ed1"
          loading={stats.loading}
        />
        <StatCard
          label="Tỷ lệ xuất đúng tiến độ"
          value={s?.tyLeXuatDungTienDo != null ? `${s.tyLeXuatDungTienDo}%` : '—'}
          color="#389e0d"
          loading={stats.loading}
        />
      </StatGrid>

      <PhieuKhoToolbar
        placeholder="Tìm theo mã phiếu xuất... (nhấn Enter)"
        keyword={list.keyword}
        onKeywordChange={list.handleKeywordChange}
        onSearch={list.handleSearch}
        trangThai={list.trangThai}
        onTrangThaiChange={list.handleTrangThaiChange}
        range={list.range}
        onRangeChange={list.handleRangeChange}
        onExport={list.handleExport}
        exporting={list.exporting}
        onCreate={() => setOpenCreate(true)}
        createLabel="Tạo phiếu xuất kho"
      />

      <Card
        styles={{ body: { padding: 0 } }}
        style={{ borderRadius: 8, overflow: 'hidden', border: '1px solid #f0f0f0' }}
      >
        <Table
          columns={columns}
          dataSource={table.data}
          loading={table.loading}
          rowKey="id"
          scroll={{ x: 1000 }}
          pagination={{
            ...table.pagination,
            showTotal: (total, range) =>
              `Hiển thị ${range[0]} - ${range[1]} trên tổng số ${total} phiếu xuất kho`,
          }}
          onChange={table.handleTableChange}
          locale={{ emptyText: 'Chưa có phiếu xuất kho nào' }}
        />
      </Card>

      {openCreate && <CreateExportModal onCancel={() => setOpenCreate(false)} onCreated={handleCreated} />}
    </InventoryPage>
  )
}
