import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button, Card, Table, Tag } from "antd";
import { EyeOutlined } from "@ant-design/icons";

import InventoryPage from "../../components/inventory/InventoryPage";
import PhieuKhoToolbar from "../../components/inventory/PhieuKhoToolbar";
import CreateImportModal from "../../components/inventory/CreateImportModal";
import StatCard, { StatGrid } from "../../components/inventory/StatCard";
import { phieuNhapApi } from "../../api/phieuNhapApi";
import { usePhieuKhoList } from "../../hooks/usePhieuKhoList";
import { useStats } from "../../hooks/useStats";
import { fmtDateTime, fmtNumber, fmtVnd } from "../../utils/format";
import { PHIEU_KHO_TRANG_THAI, resolveStatus } from "../../utils/statusTag";

export default function ImportInventory() {
  const navigate = useNavigate();
  const [openCreate, setOpenCreate] = useState(false);

  const list = usePhieuKhoList({
    fetchList: phieuNhapApi.danhSach,
    exportExcel: phieuNhapApi.xuatExcel,
    fileName: "Lich_Su_Nhap_Kho.xlsx",
  });
  const stats = useStats(phieuNhapApi.thongKe);
  const { table } = list;

  const columns = [
    {
      title: "MÃ PHIẾU NHẬP",
      dataIndex: "maPhieu",
      width: 160,
      render: (t) => <b>{t}</b>,
    },
    {
      title: "NHÀ CUNG CẤP",
      dataIndex: "tenNhaCungCap",
      render: (t) => t || "—",
    },
    {
      title: "NHÂN VIÊN TIẾP NHẬN",
      dataIndex: "tenNhanVienTiepNhan",
      render: (t) => t || "—",
    },
    {
      title: "NGÀY NHẬP KHO",
      dataIndex: "ngayNhapKho",
      width: 160,
      render: fmtDateTime,
    },
    {
      title: "SL PALLET",
      dataIndex: "tongSoLuongPallet",
      width: 110,
      align: "right",
      render: fmtNumber,
    },
    {
      title: "TRẠNG THÁI",
      dataIndex: "trangThai",
      width: 150,
      render: (v) => {
        const s = resolveStatus(PHIEU_KHO_TRANG_THAI, v);
        return <Tag color={s.color}>{s.label}</Tag>;
      },
    },
    {
      title: "THAO TÁC",
      key: "action",
      width: 90,
      align: "center",
      render: (_, record) => (
        <Button
          type="text"
          icon={<EyeOutlined />}
          style={{ color: "#8c8c8c" }}
          onClick={() => navigate(`/kho/nhap-kho/${record.id}`)}
        />
      ),
    },
  ];

  const handleCreated = () => {
    setOpenCreate(false);
    table.reload();
    stats.reload();
  };

  const s = stats.data;

  return (
    <InventoryPage>
      <StatGrid columns={4}>
        <StatCard
          label="Nhập kho trong ngày"
          value={fmtNumber(s?.nhapKhoTrongNgay)}
          unit="pallet"
          note="Chỉ tính phiếu đã hoàn thành"
          color="#1677ff"
          loading={stats.loading}
        />
        <StatCard
          label="Đang kiểm định"
          value={fmtNumber(s?.dangKiemDinh)}
          unit="pallet"
          note="Phiếu đang chờ xử lý"
          color="#cf1322"
          loading={stats.loading}
        />
        <StatCard
          label="Tổng giá trị nhập tháng"
          value={fmtVnd(s?.tongGiaTriNhapThang)}
          color="#389e0d"
          loading={stats.loading}
        />
        <StatCard
          label="Sức chứa khả dụng"
          value={`${s?.sucChuaKhaDungPhanTram ?? 0}%`}
          unit={`/ ${fmtNumber(s?.tongSoSlot)} slots`}
          note={`Hệ thống còn ${fmtNumber(s?.soSlotTrong)} vị trí trống`}
          color="#595959"
          loading={stats.loading}
        />
      </StatGrid>

      <PhieuKhoToolbar
        placeholder="Tìm theo mã phiếu nhập... (nhấn Enter)"
        keyword={list.keyword}
        onKeywordChange={list.handleKeywordChange}
        onSearch={list.handleSearch}
        trangThai={list.trangThai}
        onTrangThaiChange={list.handleTrangThaiChange}
        range={list.range}
        onRangeChange={list.handleRangeChange}
        onExport={list.handleExport}
        exporting={list.exporting}
        onCreate={() => setOpenCreate(true)}
        createLabel="Tạo phiếu nhập kho"
      />

      <Card
        styles={{ body: { padding: 0 } }}
        style={{
          borderRadius: 8,
          overflow: "hidden",
          border: "1px solid #f0f0f0",
        }}
      >
        <Table
          columns={columns}
          dataSource={table.data}
          loading={table.loading}
          rowKey="id"
          scroll={{ x: 900 }}
          pagination={{
            ...table.pagination,
            showTotal: (total, range) =>
              `Hiển thị ${range[0]} - ${range[1]} trên tổng số ${total} phiếu nhập kho`,
          }}
          onChange={table.handleTableChange}
          locale={{ emptyText: "Chưa có phiếu nhập kho nào" }}
        />
      </Card>

      {openCreate && (
        <CreateImportModal
          onCancel={() => setOpenCreate(false)}
          onCreated={handleCreated}
        />
      )}
    </InventoryPage>
  );
}
