export const formatMoney = (n) => Number(n ?? 0).toLocaleString('vi-VN')
export const formatDate = (iso) => (iso ? new Date(iso).toLocaleDateString('vi-VN') : '—')
export const formatTime = (iso) =>
  iso ? new Date(iso).toLocaleTimeString('vi-VN', { hour12: false }) : ''
export const formatDateTime = (iso) => (iso ? `${formatDate(iso)} ${formatTime(iso)}` : '—')

export const LOAI_GD = {
  thu_ban_pallet: 'Thu bán pallet',
  thu_ban_linh_kien: 'Thu bán linh kiện',
  thu_khac: 'Thu khác',
  chi_mua_pallet_cu: 'Chi mua pallet cũ',
  chi_nguyen_lieu_phu_tro: 'Chi nguyên liệu',
  chi_sua_chua: 'Chi sửa chữa',
  chi_van_chuyen: 'Chi vận chuyển',
  chi_khac: 'Chi khác',
}

export const PHUONG_THUC = {
  tien_mat: 'Tiền mặt',
  chuyen_khoan: 'Chuyển khoản',
  qr_code: 'Quét mã QR',
}

export const TRANG_THAI = {
  nhap: { label: 'Nháp', tone: 'gray' },
  cho_thanh_toan: { label: 'Chờ thanh toán', tone: 'amber' },
  cho_duyet: { label: 'Chờ duyệt', tone: 'amber' },
  da_thanh_toan: { label: 'Đã thanh toán', tone: 'green' },
  qua_han: { label: 'Quá hạn', tone: 'red' },
  da_huy: { label: 'Đã hủy', tone: 'gray' },
}

// Trạng thái hiển thị: quá hạn (tính từ hạn thanh toán) được ưu tiên hơn "chờ thanh toán"
export const trangThaiHienThi = (hd) => TRANG_THAI[hd.quaHan ? 'qua_han' : hd.trangThai] || TRANG_THAI.nhap

const CHU_SO = ['không', 'một', 'hai', 'ba', 'bốn', 'năm', 'sáu', 'bảy', 'tám', 'chín']
const DON_VI = ['', 'nghìn', 'triệu', 'tỷ', 'nghìn tỷ', 'triệu tỷ']

function docBaSo(so, daCoNhomTruoc) {
  const tram = Math.floor(so / 100)
  const chuc = Math.floor((so % 100) / 10)
  const donvi = so % 10
  let s = ''
  if (tram > 0 || daCoNhomTruoc) s += `${CHU_SO[tram]} trăm`
  if (chuc > 1) {
    s += ` ${CHU_SO[chuc]} mươi`
    if (donvi === 1) s += ' mốt'
    else if (donvi === 5) s += ' lăm'
    else if (donvi > 0) s += ` ${CHU_SO[donvi]}`
  } else if (chuc === 1) {
    s += ' mười'
    if (donvi === 5) s += ' lăm'
    else if (donvi > 0) s += ` ${CHU_SO[donvi]}`
  } else if (donvi > 0) {
    if (tram > 0 || daCoNhomTruoc) s += ' lẻ'
    s += ` ${CHU_SO[donvi]}`
  }
  return s.trim()
}

export function docTienBangChu(so) {
  let n = Math.round(Number(so) || 0)
  if (n === 0) return 'Không đồng.'
  const nhom = []
  while (n > 0) {
    nhom.push(n % 1000)
    n = Math.floor(n / 1000)
  }
  const parts = []
  for (let i = nhom.length - 1; i >= 0; i--) {
    if (nhom[i] === 0) continue
    parts.push(`${docBaSo(nhom[i], parts.length > 0)} ${DON_VI[i]}`.trim())
  }
  const s = parts.join(' ')
  return `${s.charAt(0).toUpperCase()}${s.slice(1)} đồng chẵn.`
}

// Xuất CSV (mở bằng Excel được, có BOM để không lỗi tiếng Việt)
export function xuatCsv(tenFile, hang) {
  const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const noiDung = hang.map((r) => r.map(esc).join(',')).join('\r\n')
  const blob = new Blob(['\uFEFF' + noiDung], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = tenFile
  a.click()
  URL.revokeObjectURL(url)
}