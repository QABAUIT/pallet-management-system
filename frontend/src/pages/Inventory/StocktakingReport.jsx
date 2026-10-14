import { useEffect, useMemo, useState } from "react";
import { Button, Tag, Typography } from "antd";
import { DownloadOutlined } from "@ant-design/icons";
import { Boxes, Wallet, PackageX } from "lucide-react";

import InventoryPage from "../../components/inventory/InventoryPage";
import StatCard from "../../components/common/StatCard";
import SearchInput from "../../components/common/SearchInput";
import OtherTypeFilter from "../../components/common/OtherTypeFilter";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import { tonKhoApi } from "../../api/tonKhoApi";
import { useServerTable } from "../../hooks/useServerTable";
import { useStats } from "../../hooks/useStats";
import { downloadBlob } from "../../utils/downloadFile";
import { fmtNumber, fmtVnd } from "../../utils/format";
import { notifyError } from "../../utils/notify";
import { TINH_TRANG_TON_KHO } from "../../utils/statusTag";

import "../../components/layout/style.css";

const { Text } = Typography;
const PAGE_SIZE = 8;

// Giá trị chatLieu khớp CHECK constraint mat_hang.chat_lieu
const CHAT_LIEU_GROUPS = [
  {
    key: "chatLieu",
    label: "Chất liệu",
    type: "radio",
    options: [
      { value: "go", label: "Pallet gỗ" },
      { value: "nhua", label: "Pallet nhựa" },
      { value: "sat", label: "Pallet kim loại" },
      { value: "khac", label: "Khác" },
    ],
  },
];
const DEFAULT_FILTER = { chatLieu: null };

// Trang "Báo cáo tồn kho" (route /kho)
export default function StocktakingReport() {
  const [chatLieu, setChatLieu] = useState(null); // null = tất cả
  const [keyword, setKeyword] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [exporting, setExporting] = useState(false);

  const stats = useStats(tonKhoApi.thongKe);

  const extraParams = useMemo(
    () => ({
      keyword: searchTerm || undefined,
      chatLieu: chatLieu || undefined,
    }),
    [searchTerm, chatLieu],
  );
  const table = useServerTable(tonKhoApi.danhSach, extraParams, PAGE_SIZE);
  const goToPage = (page) =>
    table.handleTableChange({ current: page, pageSize: PAGE_SIZE });

  // Tự tìm sau khi ngừng gõ 0,4s
  useEffect(() => {
    const t = setTimeout(() => {
      const next = keyword.trim();
      if (next !== searchTerm) {
        setSearchTerm(next);
        goToPage(1);
      }
    }, 400);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword]);

  const handleChatLieuApply = (val) => {
    setChatLieu(val.chatLieu ?? null);
    goToPage(1);
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      const blob = await tonKhoApi.xuatExcel(extraParams);
      downloadBlob(blob, "Bao_Cao_Ton_Kho.xlsx");
    } catch (err) {
      notifyError(err);
    } finally {
      setExporting(false);
    }
  };

  const columns = [
    { key: "maSku", header: "MÃ SKU", render: (r) => <b>{r.maSku}</b> },
    {
      key: "pallet",
      header: "PALLET",
      render: (r) => (
        <div>
          <div style={{ fontWeight: 600 }}>{r.tenPallet}</div>
          {r.phanLoai && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              {r.phanLoai}
            </Text>
          )}
        </div>
      ),
    },
    {
      key: "kichThuoc",
      header: "KÍCH THƯỚC (D x R x C)",
      render: (r) => r.kichThuoc || "—",
    },
    {
      key: "soLuongTon",
      header: "SỐ LƯỢNG TỒN",
      align: "right",
      render: (r) => <b>{fmtNumber(r.soLuongTon)}</b>,
    },
    {
      key: "donGiaDinhMuc",
      header: "ĐƠN GIÁ ĐỊNH MỨC",
      align: "right",
      render: (r) => fmtVnd(r.donGiaDinhMuc),
    },
    {
      key: "tinhTrang",
      header: "TÌNH TRẠNG",
      render: (r) => (
        <Tag color={TINH_TRANG_TON_KHO[r.tinhTrang] || "default"}>
          {r.tinhTrang || "—"}
        </Tag>
      ),
    },
  ];

  const s = stats.data;
  const show = (v) => (stats.loading ? "…" : v);

  return (
    <InventoryPage>
      <div className="stat-row">
        <StatCard
          label="TỔNG SỐ LƯỢNG TỒN KHO"
          value={show(fmtNumber(s?.tongSoLuongTon))}
          icon={Boxes}
          tone="blue"
          badge="cái"
        />
        <StatCard
          label="TỔNG GIÁ TRỊ ƯỚC TÍNH"
          value={show(fmtVnd(s?.tongGiaTriUocTinh))}
          icon={Wallet}
          tone="amber"
          caption="Số lượng tồn x đơn giá bán"
        />
        <StatCard
          label="CẢNH BÁO HẾT HÀNG"
          value={show(fmtNumber(s?.soChungLoaiCanhBao))}
          icon={PackageX}
          tone="red"
          badge="chủng loại"
          caption="Tồn ≤ mức tối thiểu"
        />
      </div>

      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          flexWrap: "wrap",
          gap: 16,
          marginBottom: 16,
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            flexWrap: "wrap",
            gap: 16,
          }}
        >
          <SearchInput
            value={keyword}
            onChange={setKeyword}
            placeholder="Tìm theo SKU hoặc tên pallet..."
          />
          <OtherTypeFilter
            label="Lọc chất liệu"
            groups={CHAT_LIEU_GROUPS}
            value={{ chatLieu }}
            defaultValue={DEFAULT_FILTER}
            onApply={handleChatLieuApply}
          />
        </div>
        <Button
          type="primary"
          size="large"
          icon={<DownloadOutlined />}
          loading={exporting}
          onClick={handleExport}
          style={{
            backgroundColor: "#d46b08",
            borderColor: "#d46b08",
            borderRadius: 6,
            fontWeight: 500,
          }}
        >
          Xuất Excel báo cáo
        </Button>
      </div>

      <AppTable
        columns={columns}
        data={table.data}
        rowKey={(r) => r.matHangId}
        emptyText={
          table.loading ? "Đang tải dữ liệu..." : "Chưa có dữ liệu tồn kho"
        }
      />

      <Pagination
        currentPage={table.pagination.current}
        totalItems={table.pagination.total}
        itemsPerPage={table.pagination.pageSize}
        itemLabel="loại pallet"
        onPageChange={goToPage}
      />
    </InventoryPage>
  );
}
