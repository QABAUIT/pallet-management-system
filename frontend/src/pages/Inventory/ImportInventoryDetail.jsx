import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Card, Descriptions, Spin, Tag, Typography } from 'antd'

import InventoryPage from '../../components/inventory/InventoryPage'
import { phieuNhapApi } from '../../api/phieuNhapApi'
import { fmtDateTime, fmtNumber } from '../../utils/format'
import { notifyError } from '../../utils/notify'
import { PHIEU_KHO_TRANG_THAI, resolveStatus } from '../../utils/statusTag'

// Nút "Quay lại lịch sử nhập kho" đã có sẵn ở Header (DETAIL_ROUTES).
export default function ImportInventoryDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let ignore = false
    setLoading(true)
    phieuNhapApi
      .chiTiet(id)
      .then((res) => {
        if (!ignore) setData(res)
      })
      .catch((err) => {
        if (!ignore) {
          notifyError(err)
          navigate('/kho/nhap-kho', { replace: true })
        }
      })
      .finally(() => {
        if (!ignore) setLoading(false)
      })
    return () => {
      ignore = true
    }
  }, [id, navigate])

  const status = resolveStatus(PHIEU_KHO_TRANG_THAI, data?.trangThai)

  return (
    <InventoryPage showTabs={false}>
      {loading || !data ? (
        <div style={{ textAlign: 'center', padding: 64 }}>
          <Spin />
        </div>
      ) : (
        <Card style={{ borderRadius: 8, border: '1px solid #f0f0f0' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
            <Typography.Title level={3} style={{ margin: 0 }}>
              Phiếu nhập kho: {data.maPhieu}
            </Typography.Title>
            <Tag color={status.color}>{status.label}</Tag>
          </div>

          <Descriptions bordered column={2} styles={{ label: { width: 200, fontWeight: 600 } }}>
            <Descriptions.Item label="Mã phiếu nhập">{data.maPhieu}</Descriptions.Item>
            <Descriptions.Item label="Trạng thái">{status.label}</Descriptions.Item>
            <Descriptions.Item label="Nhà cung cấp" span={2}>
              {data.tenNhaCungCap || '—'}
            </Descriptions.Item>
            <Descriptions.Item label="Nhân viên tiếp nhận">{data.tenNhanVienTiepNhan || '—'}</Descriptions.Item>
            <Descriptions.Item label="Ngày nhập kho">{fmtDateTime(data.ngayNhapKho)}</Descriptions.Item>
            <Descriptions.Item label="Tổng số lượng pallet" span={2}>
              <b>{fmtNumber(data.tongSoLuongPallet)}</b> cái
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}
    </InventoryPage>
  )
}
