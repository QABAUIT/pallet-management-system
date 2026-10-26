import { Typography } from "antd";
import { CheckCircle2, ClipboardList, Layers } from "lucide-react";

import PhieuListPage from "../../../components/recycle/PhieuListPage";
import PhanLoaiFormModal from "../../../components/recycle/PhanLoaiFormModal";
import PhieuTaiCheDrawer from "../../../components/recycle/PhieuTaiCheDrawer";
import { fmtDate, statusColumn } from "../../../components/recycle/helpers";
import { taiCheApi } from "../../../api/recycleApi";
import { fmtNumber } from "../../../utils/format";
import { PHIEU_TAI_CHE_TRANG_THAI } from "../../../utils/statusTag";

const { Text } = Typography;
const SEARCH_FIELDS = ["maPhieu", "maLo", "tenMatHang", "tenKho"];

const COLUMNS = [
  { key: "maPhieu", header: "MÃ PHIẾU", render: (r) => <b>{r.maPhieu}</b> },
  {
    key: "lo",
    header: "LÔ HÀNG",
    render: (r) => (
      <div>
        <div style={{ fontWeight: 600 }}>{r.maLo}</div>
        <Text type="secondary" style={{ fontSize: 12 }}>
          {r.tenMatHang}
        </Text>
      </div>
    ),
  },
  { key: "tenKho", header: "KHO" },
  {
    key: "soLuongXuLy",
    header: "SL XỬ LÝ",
    align: "right",
    render: (r) => fmtNumber(r.soLuongXuLy),
  },
  {
    key: "phanLoai",
    header: "BÁN LẠI / THÁO DỠ / LOẠI BỎ",
    render: (r) =>
      `${r.soBanLai} / ${r.soThaoDo} / ${r.soLoaiBo}` +
      (r.kgPheLieuLoaiBo ? ` (${r.kgPheLieuLoaiBo} kg)` : ""),
  },
  { key: "ngayThucHien", header: "NGÀY", render: (r) => fmtDate(r.ngayThucHien) },
  statusColumn(PHIEU_TAI_CHE_TRANG_THAI),
];

const buildStats = (rows) => {
  const cho = rows.filter((r) => r.trangThai === "nhap");
  return [
    {
      label: "PHIẾU CHỜ DUYỆT",
      value: fmtNumber(cho.length),
      icon: ClipboardList,
      tone: "amber",
      badge: "phiếu",
      caption: "Chờ GD/PGD duyệt",
    },
    {
      label: "PALLET CHỜ DUYỆT",
      value: fmtNumber(cho.reduce((a, r) => a + r.soLuongXuLy, 0)),
      icon: Layers,
      tone: "blue",
      badge: "pallet",
      caption: "Chưa cộng vào tồn kho",
    },
    {
      label: "PHIẾU ĐÃ DUYỆT",
      value: fmtNumber(rows.filter((r) => r.trangThai === "da_duyet").length),
      icon: CheckCircle2,
      tone: "green",
      badge: "phiếu",
    },
  ];
};

// Bước 1 tái chế: phân loại lô pallet cũ thành bán lại / tháo dỡ / loại bỏ (route /kho/tai-che/phan-loai)
export default function PhanLoai() {
  return (
    <PhieuListPage
      api={taiCheApi}
      searchFields={SEARCH_FIELDS}
      columns={COLUMNS}
      buildStats={buildStats}
      FormModal={PhanLoaiFormModal}
      DetailDrawer={PhieuTaiCheDrawer}
      createLabel="Tạo phiếu phân loại"
      placeholder="Tìm theo mã phiếu, mã lô, tên pallet..."
      itemLabel="phiếu phân loại"
      emptyText="Chưa có phiếu phân loại nào"
    />
  );
}
