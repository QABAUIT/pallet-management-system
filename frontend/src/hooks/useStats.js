import { useCallback, useEffect, useState } from 'react'
import { notifyError } from '../utils/notify'

/**
 * Tải các thẻ thống kê (GET .../thong-ke).
 * fetchFn phải là hàm ỔN ĐỊNH (vd phieuNhapApi.thongKe), không tạo lại mỗi lần render.
 */
export function useStats(fetchFn) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  const reload = useCallback(() => {
    setLoading(true)
    return fetchFn()
      .then(setData)
      .catch(notifyError)
      .finally(() => setLoading(false))
  }, [fetchFn])

  useEffect(() => {
    reload()
  }, [reload])

  return { data, loading, reload }
}
