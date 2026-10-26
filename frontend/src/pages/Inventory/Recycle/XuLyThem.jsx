import { CheckCircle2, ClipboardList, Recycle } from "lucide-react";

import PhieuListPage from "../../../components/recycle/PhieuListPage";
import XuLyThemFormModal from "../../../components/recycle/XuLyThemFormModal";
import { LOAI_NGUON, fmtDate, statusColumn } from "../../../components/recycle/helpers";
import { xuLyThemApi } from "../../../api/recycleApi";
import { fmtNumber } from "../../../utils/format";
import { PHIEU_TAI_CHE_TRANG_THAI } from "../../../utils/statusTag";

const SEARCH_FIELDS = ["maPhieu", "tenMatHangNguon", "tenMatHangDich", "lyDo"];

const COLUMNS = [
  { key: "maPhieu", header: "MÃ PHIẾU", render: (r) => <b>{r.maPhieu}</b> },
  {
    key: "loaiNguon",
    header: "NHÓM XỬ LÝ",
    render: (r) => LOAI_NGUON[r.loaiNguon] ?? r.loaiNguon,
  },
  {
    key: "soDatLai",
    header: "DÙNG LẠI",
    align: "right",
    render: (r) => fmtNumber(r.soDatLai),
  },
  {
    key: "soPheLieu",
    header: "BỎ ĐI (CHIẾC / KG)",
    align: "right",
    render: (r) => `${fmtNumber(r.soPheLieu)} (${fmtNumber(r.kgPheLieu)} kg)`,
  },
  { key: "lyDo", header: "LÝ DO" },
  { key: "ngayThucHien", header: "NGÀY", render: (r) => fmtDate(r.ngayThucHien) },
  statusColumn(PHIEU_TAI_CHE_TRANG_THAI),
];

const buildStats = (rows) => {
  const xong = rows.filter((r) => r.trangThai === "da_duyet");
  return [
    {
      label: "PHIẾU CHỜ DUYỆT",
      value: fmtNumber(rows.filter((r) => r.trangThai === "nhap").length),
      icon: ClipboardList,
      tone: "amber",
      badge: "phiếu",
      caption: "Chờ GD/PGD duyệt",
    },
    {
      label: "CHIẾC DÙNG LẠI",
      value: fmtNumber(xong.reduce((a, r) => a + r.soDatLai, 0)),
      icon: Recycle,
      tone: "green",
      badge: "chiếc",
      caption: "Đã quay lại làm linh kiện",
    },
    {
      label: "PHẾ LIỆU PHÁT SINH",
      value: fmtNumber(xong.reduce((a, r) => a + r.kgPheLieu, 0)),
      icon: CheckCircle2,
      tone: "red",
      badge: "kg",
      caption: "Các phiếu đã duyệt",
    },
  ];
};

// Bước 3 tái chế: xử lý thanh/cục cần xử lý thêm (route /kho/tai-che/xu-ly-them)
export default function XuLyThem() {
  return (
    <PhieuListPage
      api={xuLyThemApi}
      searchFields={SEARCH_FIELDS}
      columns={COLUMNS}
      buildStats={buildStats}
      FormModal={XuLyThemFormModal}
      createLabel="Tạo phiếu xử lý"
      placeholder="Tìm theo mã phiếu, lý do..."
      itemLabel="phiếu xử lý"
      emptyText="Chưa có phiếu xử lý nào"
    />
  );
}
