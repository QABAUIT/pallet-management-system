import { useState } from 'react'
import { Form, Input, InputNumber, Modal } from 'antd'
import { kiemKeApi } from '../../api/kiemKeApi'
import { useAuth } from '../../store/AuthContext'
import { KHO_ID } from '../../config/kho'
import { notifyError, notifySuccess } from '../../utils/notify'

/** Tạo phiên kiểm kê mới. BE sẽ chụp snapshot tồn kho hiện tại của kho vào phiên. */
export function CreateStocktakeModal({ onCancel, onCreated }) {
  const { user } = useAuth()
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const handleFinish = async (values) => {
    setSaving(true)
    try {
      const phien = await kiemKeApi.taoPhien({
        khoId: KHO_ID,
        nguoiTaoId: user?.nhanVienId,
        ghiChu: values.ghiChu?.trim() || null,
      })
      if (!phien?.id) throw new Error('Không nhận được mã phiên kiểm kê từ máy chủ')
      notifySuccess('Đã tạo phiên kiểm kê mới')
      onCreated(phien)
    } catch (err) {
      notifyError(err)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal
      open
      centered
      title="Tạo phiên kiểm kê mới"
      okText="Tạo phiên kiểm kê"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <Form form={form} layout="vertical" onFinish={handleFinish} style={{ marginTop: 16 }}>
        <Form.Item label="Ghi chú phiên" name="ghiChu" rules={[{ max: 255, message: 'Tối đa 255 ký tự' }]}>
          <Input.TextArea rows={3} placeholder="Ví dụ: Kiểm kê cuối tháng 10" />
        </Form.Item>
      </Form>
    </Modal>
  )
}

/** Nhập tồn thực tế cho 1 dòng kiểm kê. Để trống số lượng = đưa dòng về "Chờ kiểm". */
export function EditStocktakeModal({ row, onCancel, onSaved }) {
  const { user } = useAuth()
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const handleFinish = async (values) => {
    setSaving(true)
    try {
      await kiemKeApi.capNhatChiTiet(row.id, {
        tonThucTe: values.tonThucTe ?? null,
        ghiChuGiaiTrinh: values.ghiChuGiaiTrinh?.trim() || null,
        nguoiKiemId: user?.nhanVienId,
      })
      notifySuccess('Cập nhật tồn thực tế thành công')
      onSaved()
    } catch (err) {
      notifyError(err)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal
      open
      centered
      title={`Kiểm kê: ${row.maSku}`}
      okText="Lưu"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <div style={{ marginBottom: 4, fontWeight: 600 }}>{row.tenPallet}</div>
      <div style={{ color: '#8c8c8c', marginBottom: 16 }}>
        {row.viTriLuuTru} · Tồn hệ thống: <b>{row.tonHeThong}</b>
      </div>
      <Form
        form={form}
        layout="vertical"
        initialValues={{ tonThucTe: row.tonThucTe ?? undefined }}
        onFinish={handleFinish}
      >
        <Form.Item label="Tồn thực tế (cái)" name="tonThucTe">
          <InputNumber min={0} precision={0} style={{ width: '100%' }} placeholder="Nhập số đếm thực tế" autoFocus />
        </Form.Item>
        <Form.Item
          label="Giải trình chênh lệch"
          name="ghiChuGiaiTrinh"
          rules={[{ max: 255, message: 'Tối đa 255 ký tự' }]}
        >
          <Input.TextArea rows={3} placeholder="Chỉ cần điền khi số thực tế lệch so với hệ thống" />
        </Form.Item>
      </Form>
    </Modal>
  )
}
