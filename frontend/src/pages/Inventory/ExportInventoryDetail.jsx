import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Card, Descriptions, Spin, Tag, Typography } from 'antd'

import InventoryPage from '../../components/inventory/InventoryPage'
import { phieuXuatApi } from '../../api/phieuXuatApi'
import { fmtDateTime, fmtNumber } from '../../utils/format'
import { notifyError } from '../../utils/notify'
import { LOAI_XUAT_MAU, PHIEU_KHO_TRANG_THAI, resolveStatus } from '../../utils/statusTag'

// Nút "Quay lại lịch sử xuất kho" đã có sẵn ở Header (DETAIL_ROUTES).
export default function ExportInventoryDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let ignore = false
    setLoading(true)
    phieuXuatApi
      .chiTiet(id)
      .then((res) => {
        if (!ignore) setData(res)
      })
      .catch((err) => {
        if (!ignore) {
          notifyError(err)
          navigate('/kho/xuat-kho', { replace: true })
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
              Phiếu xuất kho: {data.maPhieuXuat}
            </Typography.Title>
            <Tag color={status.color}>{status.label}</Tag>
          </div>

          <Descriptions bordered column={2} styles={{ label: { width: 200, fontWeight: 600 } }}>
            <Descriptions.Item label="Mã phiếu xuất">{data.maPhieuXuat}</Descriptions.Item>
            <Descriptions.Item label="Trạng thái">{status.label}</Descriptions.Item>
            <Descriptions.Item label="Nhân viên xuất kho">
              {data.tenNhanVienXuat || '—'}
              {data.maNhanVienXuat ? ` (${data.maNhanVienXuat})` : ''}
            </Descriptions.Item>
            <Descriptions.Item label="Ngày xuất kho">{fmtDateTime(data.ngayXuatKho)}</Descriptions.Item>
            <Descriptions.Item label="Loại xuất">
              <Tag color={LOAI_XUAT_MAU[data.loaiXuatKho] || 'default'}>{data.loaiXuatKho || '—'}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Tổng số lượng">
              <b>{fmtNumber(data.tongSoLuong)}</b> cái
            </Descriptions.Item>
            <Descriptions.Item label="Lý do xuất" span={2}>
              {data.lyDoXuat || '—'}
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}
    </InventoryPage>
  )
}
