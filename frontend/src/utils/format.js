import dayjs from 'dayjs'

export const fmtDateTime = (v) => (v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—')
export const fmtNumber = (v) => (v == null ? '—' : Number(v).toLocaleString('vi-VN'))
export const fmtVnd = (v) => (v == null ? '—' : `${Number(v).toLocaleString('vi-VN')} ₫`)

// BE nhận tuNgay/denNgay dạng ISO.DATE_TIME KHÔNG có múi giờ: 2026-10-07T00:00:00
// (đừng dùng toISOString() vì có chữ "Z" sẽ bị 400)
export const toApiDateTime = (d) => d.format('YYYY-MM-DDTHH:mm:ss')
