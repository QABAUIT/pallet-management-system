import { useCallback, useEffect, useState } from 'react';
import { taiChinhService } from '../services/taiChinhService';
import { notifyError, notifySuccess } from '../utils/notify';

/** Mục tiêu doanh thu của 1 năm (các tháng + mục tiêu cả năm). */
export function useRevenueTargets({ open, nam, onChanged }) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    if (!open) return;
    setLoading(true);
    try {
      const res = await taiChinhService.mucTieu(nam);
      setData(res.data ?? []);
    } catch (err) {
      notifyError(err);
    } finally {
      setLoading(false);
    }
  }, [open, nam]);

  useEffect(() => {
    load();
  }, [load]);

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
    save: (payload) => run(() => taiChinhService.datMucTieu(payload), 'Đã lưu mục tiêu doanh thu'),
    remove: (id) => run(() => taiChinhService.xoaMucTieu(id), 'Đã xóa mục tiêu'),
  };
}