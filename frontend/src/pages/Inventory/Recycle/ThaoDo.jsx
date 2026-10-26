import { CheckCircle2, ClipboardList, Hammer } from "lucide-react";

import PhieuListPage from "../../../components/recycle/PhieuListPage";
import ThaoDoFormModal from "../../../components/recycle/ThaoDoFormModal";
import { fmtDate, statusColumn } from "../../../components/recycle/helpers";
import { thaoDoApi } from "../../../api/recycleApi";
import { fmtNumber } from "../../../utils/format";
import { PHIEU_TAI_CHE_TRANG_THAI } from "../../../utils/statusTag";

const SEARCH_FIELDS = ["maPhieu", "maPhieuTaiChe", "maLo", "tenKho"];

const COLUMNS = [
  { key: "maPhieu", header: "MÃ ĐỢT THÁO DỠ", render: (r) => <b>{r.maPhieu}</b> },
  {
    key: "nguon",
    header: "PHIẾU PHÂN LOẠI / LÔ",
    render: (r) => `${r.maPhieuTaiChe} / ${r.maLo}`,
  },
  {
    key: "soPalletThaoDo",
    header: "PALLET THÁO",
    align: "right",
    render: (r) => fmtNumber(r.soPalletThaoDo),
  },
  {
    key: "dat",
    header: "THANH NGUYÊN / CỤC VUÔNG",
    render: (r) => `${r.soThanhNguyen} / ${r.soCucVuong}`,
  },
  {
    key: "canXuLy",
    header: "THANH / CỤC CẦN XỬ LÝ",
    render: (r) => `${r.soThanhCanXuLy} / ${r.soCucCanXuLy}`,
  },
  {
    key: "kgPheLieu",
    header: "PHẾ LIỆU (KG)",
    align: "right",
    render: (r) => fmtNumber(r.kgPheLieu),
  },
  { key: "ngayThucHien", header: "NGÀY", render: (r) => fmtDate(r.ngayThucHien) },
  statusColumn(PHIEU_TAI_CHE_TRANG_THAI),
];

const buildStats = (rows) => {
  const cho = rows.filter((r) => r.trangThai === "nhap");
  const xong = rows.filter((r) => r.trangThai === "da_duyet");
  return [
    {
      label: "ĐỢT CHỜ DUYỆT",
      value: fmtNumber(cho.length),
      icon: ClipboardList,
      tone: "amber",
      badge: "đợt",
      caption: "Chờ GD/PGD duyệt",
    },
    {
      label: "PALLET ĐÃ THÁO DỠ",
      value: fmtNumber(xong.reduce((a, r) => a + r.soPalletThaoDo, 0)),
      icon: Hammer,
      tone: "blue",
      badge: "pallet",
      caption: "Các đợt đã duyệt",
    },
    {
      label: "PHẾ LIỆU THU ĐƯỢC",
      value: fmtNumber(xong.reduce((a, r) => a + r.kgPheLieu, 0)),
      icon: CheckCircle2,
      tone: "green",
      badge: "kg",
      caption: "Các đợt đã duyệt",
    },
  ];
};

// Bước 2 tái chế: tháo dỡ pallet thành thanh/cục (route /kho/tai-che/thao-do)
export default function ThaoDo() {
  return (
    <PhieuListPage
      api={thaoDoApi}
      searchFields={SEARCH_FIELDS}
      columns={COLUMNS}
      buildStats={buildStats}
      FormModal={ThaoDoFormModal}
      createLabel="Tạo đợt tháo dỡ"
      placeholder="Tìm theo mã đợt, mã phiếu, mã lô..."
      itemLabel="đợt tháo dỡ"
      emptyText="Chưa có đợt tháo dỡ nào"
    />
  );
}
