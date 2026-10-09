import { useCallback, useEffect, useState } from 'react';
import { hoaDonApi } from '../api/hoaDonApi';
import { notifyError } from '../utils/notify';

/** Danh sách hóa đơn. trangThai để trống = tất cả. */
export function useReceipts(trangThai) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const res = await hoaDonApi.danhSach(trangThai);
      setData(res.data ?? []);
    } catch (err) {
      notifyError(err);
    } finally {
      setLoading(false);
    }
  }, [trangThai]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { data, loading, reload };
}