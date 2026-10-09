import { useCallback, useEffect, useState } from 'react';
import { message } from 'antd';
import { hoaDonApi } from '../api/hoaDonApi';
import { notifyError } from '../utils/notify';

export function useReceiptDetail(id) {
  const [hd, setHd] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const res = await hoaDonApi.chiTiet(id);
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

  // Chạy 1 thao tác ghi, trả true nếu thành công để trang đóng modal
  const run = async (fn, okMsg) => {
    setSaving(true);
    try {
      await fn();
      message.success(okMsg);
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
    capNhat: (data) => run(() => hoaDonApi.capNhat(id, data), 'Đã cập nhật hóa đơn'),
    thanhToan: (data) => run(() => hoaDonApi.thanhToan(id, data), 'Thanh toán thành công'),
    huy: (lyDo) => run(() => hoaDonApi.huy(id, lyDo), 'Đã hủy hóa đơn'),
  };
}