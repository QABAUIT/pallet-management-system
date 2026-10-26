import { Tag } from "antd";
import { AlertTriangle, CheckCircle2, ClipboardList, PackagePlus } from "lucide-react";

import PhieuListPage from "../../../components/recycle/PhieuListPage";
import DongPalletFormModal from "../../../components/recycle/DongPalletFormModal";
import PhieuDongPalletDrawer from "../../../components/recycle/PhieuDongPalletDrawer";
import { fmtDate, statusColumn } from "../../../components/recycle/helpers";
import { dongPalletApi } from "../../../api/recycleApi";
import { fmtNumber } from "../../../utils/format";
import { PHIEU_TAI_CHE_TRANG_THAI } from "../../../utils/statusTag";

const SEARCH_FIELDS = ["maPhieu", "tenDinhMuc", "tenThanhPham", "tenKho"];

const COLUMNS = [
  { key: "maPhieu", header: "MÃ PHIẾU", render: (r) => <b>{r.maPhieu}</b> },
  { key: "tenDinhMuc", header: "ĐỊNH MỨC" },
  { key: "tenThanhPham", header: "THÀNH PHẨM" },
  { key: "tenKho", header: "KHO" },
  {
    key: "soLuongSanXuat",
    header: "SL SẢN XUẤT",
    align: "right",
    render: (r) => fmtNumber(r.soLuongSanXuat),
  },
  {
    key: "soDongChenhLech",
    header: "LỆCH ĐỊNH MỨC",
    render: (r) =>
      r.soDongChenhLech > 0 ? <Tag color="red">{r.soDongChenhLech} dòng lệch</Tag> : <Tag color="green">Đúng ĐM</Tag>,
  },
  { key: "ngayThucHien", header: "NGÀY", render: (r) => fmtDate(r.ngayThucHien) },
  statusColumn(PHIEU_TAI_CHE_TRANG_THAI),
];

const buildStats = (rows) => {
  const cho = rows.filter((r) => r.trangThai === "nhap");
  const xong = rows.filter((r) => r.trangThai === "da_duyet");
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
      label: "PALLET ĐÃ ĐÓNG",
      value: fmtNumber(xong.reduce((a, r) => a + r.soLuongSanXuat, 0)),
      icon: PackagePlus,
      tone: "green",
      badge: "pallet",
      caption: "Các phiếu đã duyệt",
    },
    {
      label: "PHIẾU LỆCH ĐỊNH MỨC",
      value: fmtNumber(rows.filter((r) => r.soDongChenhLech > 0 && r.trangThai !== "da_huy").length),
      icon: AlertTriangle,
      tone: "red",
      badge: "phiếu",
      caption: "Cần xem lý do chênh lệch",
    },
    {
      label: "TỔNG PHIẾU ĐÃ DUYỆT",
      value: fmtNumber(xong.length),
      icon: CheckCircle2,
      tone: "blue",
      badge: "phiếu",
    },
  ];
};

// Quy trình đóng pallet từ linh kiện theo định mức (route /kho/tai-che/dong-pallet)
export default function DongPallet() {
  return (
    <PhieuListPage
      api={dongPalletApi}
      searchFields={SEARCH_FIELDS}
      columns={COLUMNS}
      buildStats={buildStats}
      FormModal={DongPalletFormModal}
      DetailDrawer={PhieuDongPalletDrawer}
      createLabel="Lập phiếu đóng pallet"
      placeholder="Tìm theo mã phiếu, định mức, thành phẩm..."
      itemLabel="phiếu đóng pallet"
      emptyText="Chưa có phiếu đóng pallet nào"
    />
  );
}
