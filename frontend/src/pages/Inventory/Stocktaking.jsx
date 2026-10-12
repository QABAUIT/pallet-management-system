import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Card, Empty, Input, Popconfirm, Radio, Table, Tag, message } from 'antd'
import { CheckOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'

import InventoryPage from '../../components/inventory/InventoryPage'
import StatCard, { StatGrid } from '../../components/inventory/StatCard'
import { CreateStocktakeModal, EditStocktakeModal } from '../../components/inventory/StocktakeModals'
import { kiemKeApi } from '../../api/kiemKeApi'
import { useAuth } from '../../store/AuthContext'
import { fmtNumber } from '../../utils/format'
import { notifyError, notifySuccess } from '../../utils/notify'
import { KIEM_KE_TRANG_THAI, resolveStatus } from '../../utils/statusTag'

// BE chưa có API "lấy phiên đang diễn ra", nên nhớ mã phiên vừa tạo ngay trên trình duyệt.
const STORAGE_KEY = 'kiem-ke-phien-id'

const readPhienId = () => {
  try {
    return window.localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}
const savePhienId = (id) => {
  try {
    if (id) window.localStorage.setItem(STORAGE_KEY, String(id))
    else window.localStorage.removeItem(STORAGE_KEY)
  } catch {
    // trình duyệt chặn localStorage thì chỉ giữ trong state
  }
}

export default function Stocktaking() {
  const { user } = useAuth()
  const [phienId, setPhienId] = useState(readPhienId)
  const [rows, setRows] = useState([])
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(false)
  const [filter, setFilter] = useState('all')
  const [search, setSearch] = useState('')
  const [openCreate, setOpenCreate] = useState(false)
  const [editing, setEditing] = useState(null)
  const [finishing, setFinishing] = useState(false)

  const load = useCallback(async () => {
    if (!phienId) {
      setRows([])
      setStats(null)
      return
    }
    setLoading(true)
    try {
      const [chiTiet, thongKe] = await Promise.all([kiemKeApi.chiTiet(phienId), kiemKeApi.thongKe(phienId)])
      setRows(Array.isArray(chiTiet) ? chiTiet : [])
      setStats(thongKe)
    } catch (err) {
      notifyError(err)
    } finally {
      setLoading(false)
    }
  }, [phienId])

  useEffect(() => {
    load()
  }, [load])

  const counts = useMemo(() => {
    const c = { all: rows.length, khop: 0, lech: 0, cho_kiem: 0 }
    rows.forEach((r) => {
      if (c[r.trangThaiKiemKe] !== undefined) c[r.trangThaiKiemKe] += 1
    })
    return c
  }, [rows])

  // Lọc + tìm kiếm ở FE (BE trả đủ cả danh sách của phiên, không phân trang)
  const filteredRows = useMemo(() => {
    const kw = search.trim().toLowerCase()
    return rows.filter((r) => {
      if (filter !== 'all' && r.trangThaiKiemKe !== filter) return false
      if (!kw) return true
      return [r.maSku, r.tenPallet, r.viTriLuuTru].some((v) => (v || '').toLowerCase().includes(kw))
    })
  }, [rows, filter, search])

  const handleCreated = (phien) => {
    savePhienId(phien.id)
    setPhienId(String(phien.id))
    setFilter('all')
    setSearch('')
    setOpenCreate(false)
  }

  const handleFinish = async () => {
    if (counts.cho_kiem > 0) {
      message.warning(`Còn ${counts.cho_kiem} mã đang ở trạng thái Chờ kiểm, cần kiểm xong trước khi chốt.`)
      return
    }
    setFinishing(true)
    try {
      await kiemKeApi.hoanTat(phienId, user?.nhanVienId)
      notifySuccess('Chốt phiên kiểm kê thành công')
      savePhienId(null)
      setPhienId(null)
    } catch (err) {
      notifyError(err)
    } finally {
      setFinishing(false)
    }
  }

  const columns = [
    { title: 'MÃ SKU', dataIndex: 'maSku', width: 130, render: (t) => <b>{t}</b> },
    { title: 'TÊN PALLET', dataIndex: 'tenPallet' },
    { title: 'VỊ TRÍ', dataIndex: 'viTriLuuTru', width: 180 },
    { title: 'TỒN HỆ THỐNG', dataIndex: 'tonHeThong', width: 130, align: 'right', render: fmtNumber },
    {
      title: 'TỒN THỰC TẾ',
      dataIndex: 'tonThucTe',
      width: 130,
      align: 'right',
      render: (v) => (v == null ? '—' : <b>{fmtNumber(v)}</b>),
    },
    {
      title: 'CHÊNH LỆCH',
      dataIndex: 'chenhLech',
      width: 120,
      align: 'right',
      render: (v) => {
        if (v == null) return '—'
        if (v === 0) return '0'
        return <b style={{ color: v > 0 ? '#389e0d' : '#cf1322' }}>{v > 0 ? `+${v}` : v}</b>
      },
    },
    {
      title: 'TRẠNG THÁI',
      dataIndex: 'trangThaiKiemKe',
      width: 120,
      render: (v) => {
        const s = resolveStatus(KIEM_KE_TRANG_THAI, v)
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
          icon={<EditOutlined />}
          style={{ color: '#8c8c8c' }}
          onClick={() => setEditing(record)}
        />
      ),
    },
  ]

  const filterOptions = [
    { value: 'all', label: `Tất cả (${counts.all})` },
    { value: 'khop', label: `Khớp (${counts.khop})` },
    { value: 'lech', label: `Lệch (${counts.lech})` },
    { value: 'cho_kiem', label: `Chờ kiểm (${counts.cho_kiem})` },
  ]

  const createButton = (
    <Button
      size="large"
      icon={<PlusOutlined />}
      onClick={() => setOpenCreate(true)}
      style={{ fontWeight: 500 }}
    >
      Tạo phiên kiểm kê mới
    </Button>
  )

  return (
    <InventoryPage>
      {!phienId ? (
        <Card style={{ borderRadius: 8, border: '1px solid #f0f0f0' }}>
          <Empty description="Chưa có phiên kiểm kê nào đang mở trên máy này">
            <Button
              type="primary"
              size="large"
              icon={<PlusOutlined />}
              onClick={() => setOpenCreate(true)}
              style={{ backgroundColor: '#d46b08', borderColor: '#d46b08' }}
            >
              Tạo phiên kiểm kê mới
            </Button>
          </Empty>
        </Card>
      ) : (
        <>
          <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 12, marginBottom: 16 }}>
            <Tag color="blue">Phiên #{phienId}</Tag>
            <Popconfirm
              title="Tạo phiên mới?"
              description="Màn hình sẽ chuyển sang phiên mới. Phiên hiện tại vẫn còn trong cơ sở dữ liệu nhưng không hiển thị ở đây nữa."
              okText="Tạo phiên mới"
              cancelText="Hủy"
              onConfirm={() => setOpenCreate(true)}
            >
              {createButton}
            </Popconfirm>
            <Popconfirm
              title="Hoàn tất kiểm kê?"
              description="Tồn kho sẽ được điều chỉnh theo số thực tế đối với các mã bị lệch. Không thể hoàn tác."
              okText="Hoàn tất"
              cancelText="Hủy"
              onConfirm={handleFinish}
            >
              <Button
                type="primary"
                size="large"
                icon={<CheckOutlined />}
                loading={finishing}
                style={{ backgroundColor: '#d46b08', borderColor: '#d46b08', fontWeight: 500 }}
              >
                Hoàn tất kiểm kê
              </Button>
            </Popconfirm>
          </div>

          <StatGrid columns={3}>
            <StatCard
              label="Tổng pallet trong hệ thống"
              value={fmtNumber(stats?.tongPalletHeThong)}
              unit="cái"
              note="Theo ảnh chụp tồn kho lúc tạo phiên"
              color="#0f2942"
              loading={loading && !stats}
            />
            <StatCard
              label="Đã kiểm tra thực tế"
              value={fmtNumber(stats?.daKiemTraThucTe)}
              unit="cái"
              note={stats ? `Tiến độ: ${stats.tienDoPhanTram}% số mã đã kiểm` : undefined}
              color="#389e0d"
              loading={loading && !stats}
            />
            <StatCard
              label="Phát hiện chênh lệch"
              value={fmtNumber(stats?.phatHienChenhLech)}
              unit="mã"
              color="#cf1322"
              loading={loading && !stats}
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
            <Input
              placeholder="Tìm theo mã SKU, tên pallet, vị trí..."
              prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
              style={{ width: 360 }}
              size="large"
              allowClear
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <Radio.Group
              optionType="button"
              buttonStyle="solid"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              options={filterOptions}
            />
          </div>

          <Card
            styles={{ body: { padding: 0 } }}
            style={{ borderRadius: 8, overflow: 'hidden', border: '1px solid #f0f0f0' }}
          >
            <Table
              columns={columns}
              dataSource={filteredRows}
              loading={loading}
              rowKey="id"
              scroll={{ x: 950 }}
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showTotal: (total, range) => `Hiển thị ${range[0]} - ${range[1]} trên tổng số ${total} mã pallet kiểm kê`,
              }}
              locale={{ emptyText: 'Không có mã nào phù hợp' }}
            />
          </Card>
        </>
      )}

      {openCreate && <CreateStocktakeModal onCancel={() => setOpenCreate(false)} onCreated={handleCreated} />}
      {editing && (
        <EditStocktakeModal
          row={editing}
          onCancel={() => setEditing(null)}
          onSaved={() => {
            setEditing(null)
            load()
          }}
        />
      )}
    </InventoryPage>
  )
}
