import { useCallback, useEffect, useState } from 'react';
import { hoaDonService } from '../services/hoaDonService';
import { notifyError, notifySuccess } from '../utils/notify';

/** Danh sách hóa đơn. trangThai để trống = tất cả. */
export function useReceipts(trangThai) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const res = await hoaDonService.getAll(trangThai ? { trangThai } : undefined);
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

  // Xóa mềm: trả true nếu thành công
  const xoa = async (id, lyDo) => {
    setDeleting(true);
    try {
      await hoaDonService.xoa(id, lyDo);
      notifySuccess('Đã xóa hóa đơn');
      await reload();
      return true;
    } catch (err) {
      notifyError(err);
      return false;
    } finally {
      setDeleting(false);
    }
  };

  return { data, loading, deleting, reload, xoa };
}