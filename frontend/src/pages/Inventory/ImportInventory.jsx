import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button, Space, Tag, Tooltip } from "antd";
import {
  EditOutlined,
  EyeOutlined,
  AppstoreAddOutlined,
} from "@ant-design/icons";
import { PackagePlus, ClipboardList, Wallet, Warehouse } from "lucide-react";

import InventoryPage from "../../components/inventory/InventoryPage";
import PhieuKhoToolbar from "../../components/inventory/PhieuKhoToolbar";
import CreateImportModal from "../../components/inventory/CreateImportModal";
import ArrangeLocationModal from "../../components/inventory/ArrangeLocationModal";
import StatCard from "../../components/common/StatCard";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import { phieuNhapApi } from "../../api/phieuNhapApi";
import { usePhieuKhoList } from "../../hooks/usePhieuKhoList";
import { useStats } from "../../hooks/useStats";
import { fmtDateTime, fmtNumber, fmtVnd } from "../../utils/format";
import { PHIEU_KHO_TRANG_THAI, resolveStatus } from "../../utils/statusTag";

import "../../components/layout/style.css";

export default function ImportInventory() {
  const navigate = useNavigate();
  const [openCreate, setOpenCreate] = useState(false);
  const [editing, setEditing] = useState(null); // phiếu đang sửa
  const [arranging, setArranging] = useState(null); // phiếu đang được xếp vị trí

  const list = usePhieuKhoList({
    fetchList: phieuNhapApi.danhSach,
    exportExcel: phieuNhapApi.xuatExcel,
    fileName: "Lich_Su_Nhap_Kho.xlsx",
  });
  const stats = useStats(phieuNhapApi.thongKe);
  const { table } = list;
  const s = stats.data;
  const show = (v) => (stats.loading ? "…" : v);

  const columns = [
    {
      key: "maPhieu",
      header: "MÃ PHIẾU NHẬP",
      render: (r) => <b>{r.maPhieu}</b>,
    },
    {
      key: "tenNhaCungCap",
      header: "NHÀ CUNG CẤP",
      render: (r) => r.tenNhaCungCap || "—",
    },
    {
      key: "tenNhanVienTiepNhan",
      header: "NHÂN VIÊN TIẾP NHẬN",
      render: (r) => r.tenNhanVienTiepNhan || "—",
    },
    {
      key: "ngayNhapKho",
      header: "NGÀY NHẬP KHO",
      render: (r) => fmtDateTime(r.ngayNhapKho),
    },
    {
      key: "tongSoLuongPallet",
      header: "SL PALLET",
      align: "right",
      render: (r) => fmtNumber(r.tongSoLuongPallet),
    },
    {
      key: "trangThai",
      header: "TRẠNG THÁI",
      render: (r) => {
        const st = resolveStatus(PHIEU_KHO_TRANG_THAI, r.trangThai);
        return <Tag color={st.color}>{st.label}</Tag>;
      },
    },
    {
      key: "action",
      header: "THAO TÁC",
      align: "center",
      render: (r) => (
        <Space size="middle">
          <Tooltip title="Xem chi tiết">
            <Button
              type="text"
              icon={<EyeOutlined />}
              style={{ color: "#8c8c8c" }}
              onClick={() => navigate(`/kho/nhap-kho/${r.id}`)}
            />
          </Tooltip>

          <Tooltip
            title={
              r.trangThai === "cho_xu_ly"
                ? "Chỉnh sửa"
                : "Chỉ sửa được phiếu đang chờ xử lý"
            }
          >
            <Button
              type="text"
              icon={<EditOutlined />}
              style={{ color: "#8c8c8c" }}
              disabled={r.trangThai !== "cho_xu_ly"}
              onClick={() => setEditing(r)}
            />
          </Tooltip>

          <Tooltip
            title={
              r.trangThai === "da_hoan_thanh"
                ? "Xếp vị trí lưu trữ (Lên kệ)"
                : "Phiếu phải hoàn tất mới có thể xếp vị trí"
            }
          >
            <Button
              type="text"
              icon={<AppstoreAddOutlined />}
              style={{
                color: r.trangThai === "da_hoan_thanh" ? "#1677ff" : "#8c8c8c",
              }}
              disabled={r.trangThai !== "da_hoan_thanh"}
              onClick={() => setArranging(r)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const closeModal = () => {
    setOpenCreate(false);
    setEditing(null);
    setArranging(null);
  };

  const handleCreated = () => {
    setOpenCreate(false);
    setEditing(null);
    setArranging(null);
    table.reload();
    stats.reload();
  };

  return (
    <InventoryPage>
      <div className="stat-row">
        <StatCard
          label="NHẬP KHO TRONG NGÀY"
          value={show(fmtNumber(s?.nhapKhoTrongNgay))}
          icon={PackagePlus}
          tone="blue"
          badge="pallet"
          caption="Chỉ tính phiếu đã hoàn thành"
        />
        <StatCard
          label="ĐANG KIỂM ĐỊNH"
          value={show(fmtNumber(s?.dangKiemDinh))}
          icon={ClipboardList}
          tone="red"
          badge="pallet"
          caption="Phiếu đang chờ xử lý"
        />
        <StatCard
          label="TỔNG GIÁ TRỊ NHẬP THÁNG"
          value={show(fmtVnd(s?.tongGiaTriNhapThang))}
          icon={Wallet}
          tone="green"
          caption="Phiếu hoàn thành trong tháng"
        />
        <StatCard
          label="SỨC CHỨA KHẢ DỤNG"
          value={show(`${s?.sucChuaKhaDungPhanTram ?? 0}%`)}
          icon={Warehouse}
          tone="purple"
          badge={`/ ${fmtNumber(s?.tongSoSlot)} slots hệ thống`}
          caption={`Còn ${fmtNumber(s?.soSlotTrong)} vị trí trống`}
        />
      </div>

      <PhieuKhoToolbar
        placeholder="Tìm theo mã phiếu nhập..."
        keyword={list.keyword}
        onKeywordChange={list.setKeyword}
        trangThai={list.trangThai}
        onTrangThaiChange={list.handleTrangThaiChange}
        time={list.time}
        onTimeChange={list.handleTimeChange}
        onExport={list.handleExport}
        exporting={list.exporting}
        onCreate={() => setOpenCreate(true)}
        createLabel="Tạo phiếu nhập kho"
      />

      <AppTable
        columns={columns}
        data={table.data}
        rowKey={(r) => r.id}
        emptyText={
          table.loading ? "Đang tải dữ liệu..." : "Chưa có phiếu nhập kho nào"
        }
      />

      <Pagination
        currentPage={table.pagination.current}
        totalItems={table.pagination.total}
        itemsPerPage={table.pagination.pageSize}
        itemLabel="phiếu nhập kho"
        onPageChange={list.goToPage}
      />

      {(openCreate || editing) && (
        <CreateImportModal
          key={editing?.id ?? "new"}
          record={editing}
          onCancel={closeModal}
          onCreated={handleCreated}
        />
      )}

      {arranging && (
        <ArrangeLocationModal
          key={`arr_${arranging.id}`}
          record={arranging}
          onCancel={closeModal}
          onSuccess={handleCreated}
        />
      )}
    </InventoryPage>
  );
}
