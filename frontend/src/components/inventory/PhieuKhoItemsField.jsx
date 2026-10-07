import { Button, Form, Input, InputNumber, Select } from 'antd'
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons'

const ROW = {
  display: 'grid',
  gridTemplateColumns: 'minmax(0, 2fr) 110px minmax(0, 1.3fr) 32px',
  gap: 8,
  alignItems: 'start',
}

/**
 * Danh sách dòng hàng của phiếu (chiTiet[]: matHangId, soLuong, ghiChu).
 * Phải đặt bên trong <Form>.
 */
export default function PhieuKhoItemsField({ matHangOptions, loading }) {
  return (
    <>
      <div style={{ fontWeight: 600, margin: '4px 0 8px' }}>Danh sách pallet</div>
      <Form.List
        name="chiTiet"
        rules={[
          {
            validator: async (_, value) => {
              if (!value || value.length === 0) {
                return Promise.reject(new Error('Cần ít nhất 1 mặt hàng'))
              }
            },
          },
        ]}
      >
        {(fields, { add, remove }, { errors }) => (
          <>
            {fields.map(({ key, name, ...rest }) => (
              <div key={key} style={ROW}>
                <Form.Item
                  {...rest}
                  name={[name, 'matHangId']}
                  rules={[{ required: true, message: 'Chọn mặt hàng' }]}
                  style={{ marginBottom: 12 }}
                >
                  <Select
                    showSearch
                    optionFilterProp="label"
                    placeholder="Chọn pallet"
                    options={matHangOptions}
                    loading={loading}
                  />
                </Form.Item>
                <Form.Item
                  {...rest}
                  name={[name, 'soLuong']}
                  rules={[{ required: true, message: 'Nhập SL' }]}
                  style={{ marginBottom: 12 }}
                >
                  <InputNumber min={1} precision={0} placeholder="Số lượng" style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item {...rest} name={[name, 'ghiChu']} style={{ marginBottom: 12 }}>
                  <Input placeholder="Ghi chú" maxLength={255} />
                </Form.Item>
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  disabled={fields.length === 1}
                  onClick={() => remove(name)}
                />
              </div>
            ))}
            <Button type="dashed" icon={<PlusOutlined />} onClick={() => add()} block>
              Thêm pallet
            </Button>
            <Form.ErrorList errors={errors} />
          </>
        )}
      </Form.List>
    </>
  )
}
