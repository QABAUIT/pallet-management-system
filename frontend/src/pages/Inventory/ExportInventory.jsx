import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button, Space, Tag, Tooltip, Typography } from "antd";
import { EditOutlined, EyeOutlined } from "@ant-design/icons";
import { PackageMinus, Truck, Wrench, Gauge } from "lucide-react";

import InventoryPage from "../../components/inventory/InventoryPage";
import PhieuKhoToolbar from "../../components/inventory/PhieuKhoToolbar";
import CreateExportModal from "../../components/inventory/CreateExportModal";
import StatCard from "../../components/common/StatCard";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import { phieuXuatApi } from "../../api/phieuXuatApi";
import { usePhieuKhoList } from "../../hooks/usePhieuKhoList";
import { useStats } from "../../hooks/useStats";
import { fmtDateTime, fmtNumber } from "../../utils/format";
import {
  LOAI_XUAT_MAU,
  PHIEU_KHO_TRANG_THAI,
  resolveStatus,
} from "../../utils/statusTag";

import "../../components/layout/style.css";

const { Text } = Typography;

export default function ExportInventory() {
  const navigate = useNavigate();
  const [openCreate, setOpenCreate] = useState(false);
  const [editing, setEditing] = useState(null); // phiếu đang sửa

  const list = usePhieuKhoList({
    fetchList: phieuXuatApi.danhSach,
    exportExcel: phieuXuatApi.xuatExcel,
    fileName: "Lich_Su_Xuat_Kho.xlsx",
  });
  const stats = useStats(phieuXuatApi.thongKe);
  const { table } = list;
  const s = stats.data;
  const show = (v) => (stats.loading ? "…" : v);

  const columns = [
    {
      key: "maPhieuXuat",
      header: "MÃ PHIẾU XUẤT",
      render: (r) => <b>{r.maPhieuXuat}</b>,
    },
    {
      key: "nhanVien",
      header: "NHÂN VIÊN XUẤT",
      render: (r) => (
        <div>
          <div style={{ fontWeight: 600 }}>{r.maNhanVienXuat || "—"}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {r.tenNhanVienXuat}
          </Text>
        </div>
      ),
    },
    {
      key: "ngayXuatKho",
      header: "NGÀY XUẤT KHO",
      render: (r) => fmtDateTime(r.ngayXuatKho),
    },
    {
      key: "loaiXuatKho",
      header: "LOẠI XUẤT",
      render: (r) => (
        <Tag color={LOAI_XUAT_MAU[r.loaiXuatKho] || "default"}>
          {r.loaiXuatKho || "—"}
        </Tag>
      ),
    },
    {
      key: "lyDoXuat",
      header: "LÝ DO XUẤT KHO",
      render: (r) => r.lyDoXuat || "—",
    },
    {
      key: "tongSoLuong",
      header: "SỐ LƯỢNG",
      align: "right",
      render: (r) => fmtNumber(r.tongSoLuong),
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
          <Button
            type="text"
            icon={<EyeOutlined />}
            style={{ color: "#8c8c8c" }}
            onClick={() => navigate(`/kho/xuat-kho/${r.id}`)}
          />
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
        </Space>
      ),
    },
  ];

  const closeModal = () => {
    setOpenCreate(false);
    setEditing(null);
  };

  const handleCreated = () => {
    setOpenCreate(false);
    setEditing(null);
    table.reload();
    stats.reload();
  };

  return (
    <InventoryPage>
      <div className="stat-row">
        <StatCard
          label="TỔNG XUẤT HÔM NAY"
          value={show(fmtNumber(s?.tongXuatHomNay))}
          icon={PackageMinus}
          tone="blue"
          badge="pallet"
          caption="Chỉ tính phiếu đã hoàn thành"
        />
        <StatCard
          label="ĐANG BỐC DỠ & XẾP XE"
          value={show(fmtNumber(s?.dangBocDoXepXe))}
          icon={Truck}
          tone="amber"
          badge="pallet"
          caption="Phiếu đang chờ xử lý"
        />
        <StatCard
          label="XUẤT BẢO DƯỠNG ĐỊNH KỲ"
          value={show(fmtNumber(s?.xuatBaoDuongDinhKy))}
          icon={Wrench}
          tone="purple"
          badge="pallet"
          caption="Loại Bảo trì, hoàn thành trong tháng"
        />
        <StatCard
          label="TỶ LỆ XUẤT ĐÚNG TIẾN ĐỘ"
          value={show(
            s?.tyLeXuatDungTienDo != null ? `${s.tyLeXuatDungTienDo}%` : "—",
          )}
          icon={Gauge}
          tone="green"
          caption="Phiếu tháng này, trễ nếu chờ quá 24h"
        />
      </div>

      <PhieuKhoToolbar
        placeholder="Tìm theo mã phiếu xuất..."
        keyword={list.keyword}
        onKeywordChange={list.setKeyword}
        trangThai={list.trangThai}
        onTrangThaiChange={list.handleTrangThaiChange}
        time={list.time}
        onTimeChange={list.handleTimeChange}
        onExport={list.handleExport}
        exporting={list.exporting}
        onCreate={() => setOpenCreate(true)}
        createLabel="Tạo phiếu xuất kho"
      />

      <AppTable
        columns={columns}
        data={table.data}
        rowKey={(r) => r.id}
        emptyText={
          table.loading ? "Đang tải dữ liệu..." : "Chưa có phiếu xuất kho nào"
        }
      />

      <Pagination
        currentPage={table.pagination.current}
        totalItems={table.pagination.total}
        itemsPerPage={table.pagination.pageSize}
        itemLabel="phiếu xuất kho"
        onPageChange={list.goToPage}
      />

      {(openCreate || editing) && (
        <CreateExportModal
          key={editing?.id ?? "new"}
          record={editing}
          onCancel={closeModal}
          onCreated={handleCreated}
        />
      )}
    </InventoryPage>
  );
}
