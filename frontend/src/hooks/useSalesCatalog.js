import { useCallback, useEffect, useState } from 'react';
import { hoaDonService } from '../services/hoaDonService';
import { notifyError } from '../utils/notify';

const EMPTY = { khachHang: [], matHang: [], tenKho: '' };

/** Danh mục khách hàng + mặt hàng kèm tồn khả dụng của kho. */
export function useSalesCatalog(enabled = true) {
  const [dm, setDm] = useState(EMPTY);

  const reload = useCallback(async () => {
    try {
      const res = await hoaDonService.danhMucBanHang();
      setDm(res.data ?? EMPTY);
    } catch (err) {
      notifyError(err);
    }
  }, []);

  useEffect(() => {
    if (enabled) reload();
  }, [enabled, reload]);

  return { dm, reload };
}