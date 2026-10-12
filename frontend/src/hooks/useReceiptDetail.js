import { useCallback, useEffect, useState } from 'react';
import { hoaDonService } from '../services/hoaDonService';
import { notifyError, notifySuccess } from '../utils/notify';

export function useReceiptDetail(id) {
  const [hd, setHd] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const res = await hoaDonService.getById(id);
      setHd(res.data);
    } catch (err) {
      notifyError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    reload();
  }, [reload]);

  // Chạy 1 thao tác ghi; trả true nếu thành công để trang đóng modal
  const run = async (fn, okMsg) => {
    setSaving(true);
    try {
      await fn();
      notifySuccess(okMsg);
      await reload();
      return true;
    } catch (err) {
      notifyError(err);
      return false;
    } finally {
      setSaving(false);
    }
  };

  return {
    hd,
    loading,
    saving,
    capNhat: (data) => run(() => hoaDonService.update(id, data), 'Đã cập nhật hóa đơn'),
    thanhToan: (data) => run(() => hoaDonService.thanhToan(id, data), 'Thanh toán thành công'),
    xoa: (lyDo) => run(() => hoaDonService.xoa(id, lyDo), 'Đã xóa hóa đơn'),
  };
}