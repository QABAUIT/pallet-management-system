import { useEffect, useState } from 'react'
import { matHangApi } from '../api/matHangApi'
import { unwrap } from '../utils/unwrap'
import { notifyError } from '../utils/notify'

/** Danh sách mặt hàng (pallet) đang kinh doanh cho dropdown trong form tạo phiếu. */
export function useMatHangOptions() {
  const [options, setOptions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let ignore = false
    matHangApi
      .danhSach()
      .then(unwrap)
      .then((list) => {
        if (ignore) return
        const arr = Array.isArray(list) ? list : []
        setOptions(
          arr
            .filter((m) => m.trangThai === 'dang_kinh_doanh' && m.loaiMatHang !== 'cho_tai_che')
            .map((m) => ({ value: m.id, label: `${m.maMatHang} - ${m.tenMatHang}` })),
        )
      })
      .catch((err) => {
        if (!ignore) notifyError(err)
      })
      .finally(() => {
        if (!ignore) setLoading(false)
      })
    return () => {
      ignore = true
    }
  }, [])

  return { options, loading }
}
