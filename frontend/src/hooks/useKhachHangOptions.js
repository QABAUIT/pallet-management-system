import { useEffect, useState } from "react";
import { khachHangApi } from "../api/khachHangApi";
import { notifyError } from "../utils/notify";

/** Danh sách khách hàng đang hợp tác cho dropdown (dùng lại khachHangApi, trường code/name đã được map sẵn). */
export function useKhachHangOptions() {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    khachHangApi
      .layDanhSach("dang_hop_tac")
      .then((list) => {
        if (ignore) return;
        setOptions(
          (list ?? []).map((k) => ({
            value: k.id,
            label: k.code ? `${k.code} - ${k.name}` : k.name,
          })),
        );
      })
      .catch((err) => !ignore && notifyError(err))
      .finally(() => !ignore && setLoading(false));
    return () => {
      ignore = true;
    };
  }, []);

  return { options, loading };
}
