import { useCallback, useEffect, useMemo, useState } from 'react';
import { message } from 'antd';
import { hoaDonApi } from '../api/hoaDonApi';
import { notifyError } from '../utils/notify';

export const VAT_RATE = 8; // %

/** Toàn bộ trạng thái + tính tiền của form "Tạo hóa đơn bán pallet". */
export function useReceiptCreate(onCreated) {
  const [dm, setDm] = useState({ khachHang: [], matHang: [], tenKho: '' });
  const [khachHangId, setKhachHangId] = useState(null);
  const [items, setItems] = useState([]); // [{ matHangId, soLuong }]
  const [phuongThuc, setPhuongThuc] = useState('tien_mat');
  const [tienKhachDua, setTienKhachDua] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const loadCatalog = useCallback(async () => {
    try {
      const res = await hoaDonApi.danhMucBanHang();
      setDm(res.data);
    } catch (err) {
      notifyError(err);
    }
  }, []);

  useEffect(() => {
    loadCatalog();
  }, [loadCatalog]);

  const mhMap = useMemo(() => new Map(dm.matHang.map((m) => [m.id, m])), [dm.matHang]);
  const khach = dm.khachHang.find((k) => k.id === khachHangId);

  const lines = items
    .filter((it) => mhMap.has(it.matHangId))
    .map((it) => {
      const mh = mhMap.get(it.matHangId);
      return { ...it, mh, thanhTien: mh.donGia * it.soLuong };
    });
  const tongHang = lines.reduce((s, l) => s + l.thanhTien, 0);
  const vat = Math.round((tongHang * VAT_RATE) / 100);
  const tong = tongHang + vat;
  const thoiLai = phuongThuc === 'tien_mat' ? Math.max(0, (tienKhachDua || 0) - tong) : 0;

  const addItem = (matHangId) => setItems((cur) => [...cur, { matHangId, soLuong: 1 }]);
  const setQty = (matHangId, soLuong) =>
    setItems((cur) => cur.map((i) => (i.matHangId === matHangId ? { ...i, soLuong: soLuong || 1 } : i)));
  const removeItem = (matHangId) => setItems((cur) => cur.filter((i) => i.matHangId !== matHangId));

  const submit = async (thanhToanNgay) => {
    if (!khachHangId) return message.warning('Vui lòng chọn khách hàng');
    if (items.length === 0) return message.warning('Vui lòng thêm ít nhất 1 loại pallet');
    if (thanhToanNgay && phuongThuc === 'tien_mat' && (tienKhachDua || 0) < tong) {
      return message.warning('Số tiền khách đưa chưa đủ để thanh toán');
    }
    setSubmitting(true);
    try {
      const res = await hoaDonApi.taoMoi({
        khachHangId,
        chiTiet: items.map(({ matHangId, soLuong }) => ({ matHangId, soLuong })),
        tyLeVat: VAT_RATE,
        thanhToanNgay,
        phuongThucThanhToan: thanhToanNgay ? phuongThuc : null,
        tienKhachDua: thanhToanNgay && phuongThuc === 'tien_mat' ? tienKhachDua : null,
      });
      message.success(`${res.message}: ${res.data.maHoaDon}`);
      setItems([]);
      setKhachHangId(null);
      setTienKhachDua(null);
      await loadCatalog();
      onCreated?.(res.data, thanhToanNgay);
    } catch (err) {
      notifyError(err);
    } finally {
      setSubmitting(false);
    }
  };

  return {
    dm, khach, khachHangId, setKhachHangId,
    items, lines, addItem, setQty, removeItem,
    phuongThuc, setPhuongThuc, tienKhachDua, setTienKhachDua,
    tongHang, vat, tong, thoiLai,
    submitting, submit,
  };
}