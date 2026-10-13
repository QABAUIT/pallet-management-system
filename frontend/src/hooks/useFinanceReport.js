import { useCallback, useEffect, useMemo, useState } from 'react';
import { getTimeRange, toIsoDate } from '../utils/matchTime';
import { notifyError } from '../utils/notify';

/**
 * Tải 1 báo cáo tài chính theo khung thời gian của TimeFilter.
 * fetchFn: hàm cấp module trong taiChinhService (({ tuNgay, denNgay }) => Promise).
 */
export function useFinanceReport(fetchFn, time) {
  const range = useMemo(() => getTimeRange(time), [time]);
  const tuNgay = range ? toIsoDate(range.from) : null;
  const denNgay = range ? toIsoDate(range.to) : null;

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  const reload = useCallback(async () => {
    if (!tuNgay || !denNgay) return;
    setLoading(true);
    try {
      const res = await fetchFn({ tuNgay, denNgay });
      setData(res.data);
    } catch (err) {
      notifyError(err);
    } finally {
      setLoading(false);
    }
  }, [fetchFn, tuNgay, denNgay]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { data, loading, reload, range, tuNgay, denNgay };
}