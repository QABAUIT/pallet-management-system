import { useCallback, useEffect, useMemo, useState } from "react";
import { notifyError } from "../utils/notify";

/**
 * Danh sách do BE trả về MẢNG (không phân trang): tải 1 lần, tìm kiếm / lọc trạng thái /
 * phân trang ngay trên trình duyệt (giống Stocktaking.jsx).
 * fetchFn phải ổn định (vd taiCheApi.danhSach), searchFields nên là hằng số ngoài component.
 */
export function useLocalList(fetchFn, { searchFields = [], pageSize = 8 } = {}) {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const [trangThai, setTrangThai] = useState(null);
  const [page, setPage] = useState(1);

  const reload = useCallback(() => {
    setLoading(true);
    return fetchFn()
      .then((list) => setRows(Array.isArray(list) ? list : []))
      .catch(notifyError)
      .finally(() => setLoading(false));
  }, [fetchFn]);

  useEffect(() => {
    reload();
  }, [reload]);

  useEffect(() => {
    setPage(1);
  }, [keyword, trangThai]);

  const filtered = useMemo(() => {
    const kw = keyword.trim().toLowerCase();
    return rows.filter((r) => {
      if (trangThai && r.trangThai !== trangThai) return false;
      if (!kw) return true;
      return searchFields.some((f) =>
        String(r[f] ?? "")
          .toLowerCase()
          .includes(kw),
      );
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rows, keyword, trangThai]);

  const paged = filtered.slice((page - 1) * pageSize, page * pageSize);

  return {
    rows,
    filtered,
    paged,
    loading,
    reload,
    keyword,
    setKeyword,
    trangThai,
    setTrangThai,
    page,
    setPage,
    pageSize,
  };
}
