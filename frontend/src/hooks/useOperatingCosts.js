import { useCallback, useEffect, useState } from 'react';
import { taiChinhService } from '../services/taiChinhService';
import { notifyError, notifySuccess } from '../utils/notify';

/** Danh sách + thêm/sửa/xóa chi phí vận hành trong khoảng ngày. */
export function useOperatingCosts({ open, tuNgay, denNgay, onChanged }) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    if (!open || !tuNgay || !denNgay) return;
    setLoading(true);
    try {
      const res = await taiChinhService.chiPhi({ tuNgay, denNgay });
      setData(res.data ?? []);
    } catch (err) {
      notifyError(err);
    } finally {
      setLoading(false);
    }
  }, [open, tuNgay, denNgay]);

  useEffect(() => {
    load();
  }, [load]);

  // Chạy 1 thao tác ghi; trả true nếu thành công
  const run = async (fn, okMsg) => {
    setSaving(true);
    try {
      await fn();
      notifySuccess(okMsg);
      await load();
      onChanged?.();
      return true;
    } catch (err) {
      notifyError(err);
      return false;
    } finally {
      setSaving(false);
    }
  };

  return {
    data,
    loading,
    saving,
    create: (payload) => run(() => taiChinhService.taoChiPhi(payload), 'Đã thêm khoản chi'),
    update: (id, payload) => run(() => taiChinhService.suaChiPhi(id, payload), 'Đã cập nhật khoản chi'),
    remove: (id) => run(() => taiChinhService.xoaChiPhi(id), 'Đã xóa khoản chi'),
  };
}