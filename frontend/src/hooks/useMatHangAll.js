import { useEffect, useState } from "react";
import { matHangApi } from "../api/matHangApi";
import { unwrap } from "../utils/unwrap";
import { notifyError } from "../utils/notify";

/**
 * Toàn bộ mặt hàng đang kinh doanh (kể cả linh kiện, vật tư), để form định mức tự lọc theo loại.
 * (useMatHangOptions cũ cố tình bỏ hàng tái chế nên không dùng lại được.)
 */
export function useMatHangAll() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    matHangApi
      .danhSach()
      .then(unwrap)
      .then((arr) => {
        if (!ignore)
          setList((Array.isArray(arr) ? arr : []).filter((m) => m.trangThai === "dang_kinh_doanh"));
      })
      .catch((err) => !ignore && notifyError(err))
      .finally(() => !ignore && setLoading(false));
    return () => {
      ignore = true;
    };
  }, []);

  return { list, loading };
}
