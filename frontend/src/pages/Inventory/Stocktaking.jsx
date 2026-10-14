import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Empty, Popconfirm, Tag, message } from "antd";
import { CheckOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { Boxes, ClipboardCheck, PackageX } from "lucide-react";

import InventoryPage from "../../components/inventory/InventoryPage";
import {
  CreateStocktakeModal,
  EditStocktakeModal,
} from "../../components/inventory/StocktakeModals";
import StatCard from "../../components/common/StatCard";
import SearchInput from "../../components/common/SearchInput";
import OtherTypeFilter from "../../components/common/OtherTypeFilter";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import { kiemKeApi } from "../../api/kiemKeApi";
import { useAuth } from "../../store/AuthContext";
import { fmtNumber } from "../../utils/format";
import { notifyError, notifySuccess } from "../../utils/notify";
import { KIEM_KE_TRANG_THAI, resolveStatus } from "../../utils/statusTag";

import "../../components/layout/style.css";

// BE chưa có API "lấy phiên đang diễn ra", nên nhớ mã phiên vừa tạo ngay trên trình duyệt.
const STORAGE_KEY = "kiem-ke-phien-id";
const ITEMS_PER_PAGE = 8;
const DEFAULT_FILTER = { status: null };

const readPhienId = () => {
  try {
    return window.localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
};
const savePhienId = (id) => {
  try {
    if (id) window.localStorage.setItem(STORAGE_KEY, String(id));
    else window.localStorage.removeItem(STORAGE_KEY);
  } catch {
    // trình duyệt chặn localStorage thì chỉ giữ trong state
  }
};

export default function Stocktaking() {
  const { user } = useAuth();
  const [phienId, setPhienId] = useState(readPhienId);
  const [rows, setRows] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [filterStatus, setFilterStatus] = useState(null); // null = tất cả
  const [search, setSearch] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [openCreate, setOpenCreate] = useState(false);
  const [editing, setEditing] = useState(null);
  const [finishing, setFinishing] = useState(false);

  const load = useCallback(async () => {
    if (!phienId) {
      setRows([]);
      setStats(null);
      return;
    }
    setLoading(true);
    try {
      const [chiTiet, thongKe] = await Promise.all([
        kiemKeApi.chiTiet(phienId),
        kiemKeApi.thongKe(phienId),
      ]);
      setRows(Array.isArray(chiTiet) ? chiTiet : []);
      setStats(thongKe);
    } catch (err) {
      notifyError(err);
    } finally {
      setLoading(false);
    }
  }, [phienId]);

  useEffect(() => {
    load();
  }, [load]);

  const counts = useMemo(() => {
    const c = { all: rows.length, khop: 0, lech: 0, cho_kiem: 0 };
    rows.forEach((r) => {
      if (c[r.trangThaiKiemKe] !== undefined) c[r.trangThaiKiemKe] += 1;
    });
    return c;
  }, [rows]);

  // Lọc + tìm kiếm + phân trang ở FE (BE trả đủ cả danh sách của phiên, không phân trang)
  const filteredRows = useMemo(() => {
    const kw = search.trim().toLowerCase();
    return rows.filter((r) => {
      if (filterStatus && r.trangThaiKiemKe !== filterStatus) return false;
      if (!kw) return true;
      return [r.maSku, r.tenPallet, r.viTriLuuTru].some((v) =>
        (v || "").toLowerCase().includes(kw),
      );
    });
  }, [rows, filterStatus, search]);

  const pagedRows = filteredRows.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE,
  );

  // Khi search/lọc/đổi phiên thì quay về trang 1
  useEffect(() => {
    setCurrentPage(1);
  }, [search, filterStatus, phienId]);

  const handleCreated = (phien) => {
    savePhienId(phien.id);
    setPhienId(String(phien.id));
    setFilterStatus(null);
    setSearch("");
    setOpenCreate(false);
  };

  const handleFinish = async () => {
    if (counts.cho_kiem > 0) {
      message.warning(
        `Còn ${counts.cho_kiem} mã đang ở trạng thái Chờ kiểm, cần kiểm xong trước khi chốt.`,
      );
      return;
    }
    setFinishing(true);
    try {
      await kiemKeApi.hoanTat(phienId, user?.nhanVienId);
      notifySuccess("Chốt phiên kiểm kê thành công");
      savePhienId(null);
      setPhienId(null);
    } catch (err) {
      notifyError(err);
    } finally {
      setFinishing(false);
    }
  };

  const columns = [
    { key: "maSku", header: "MÃ SKU", render: (r) => <b>{r.maSku}</b> },
    { key: "tenPallet", header: "TÊN PALLET" },
    { key: "viTriLuuTru", header: "VỊ TRÍ" },
    {
      key: "tonHeThong",
      header: "TỒN HỆ THỐNG",
      align: "right",
      render: (r) => fmtNumber(r.tonHeThong),
    },
    {
      key: "tonThucTe",
      header: "TỒN THỰC TẾ",
      align: "right",
      render: (r) =>
        r.tonThucTe == null ? "—" : <b>{fmtNumber(r.tonThucTe)}</b>,
    },
    {
      key: "chenhLech",
      header: "CHÊNH LỆCH",
      align: "right",
      render: (r) => {
        const v = r.chenhLech;
        if (v == null) return "—";
        if (v === 0) return "0";
        return (
          <b style={{ color: v > 0 ? "#389e0d" : "#cf1322" }}>
            {v > 0 ? `+${v}` : v}
          </b>
        );
      },
    },
    {
      key: "trangThaiKiemKe",
      header: "TRẠNG THÁI",
      render: (r) => {
        const s = resolveStatus(KIEM_KE_TRANG_THAI, r.trangThaiKiemKe);
        return <Tag color={s.color}>{s.label}</Tag>;
      },
    },
    {
      key: "action",
      header: "THAO TÁC",
      align: "center",
      render: (r) => (
        <Button
          type="text"
          icon={<EditOutlined />}
          style={{ color: "#8c8c8c" }}
          onClick={() => setEditing(r)}
        />
      ),
    },
  ];

  const filterGroups = [
    {
      key: "status",
      label: "Trạng thái",
      type: "radio",
      options: [
        { value: "khop", label: `Khớp (${counts.khop})` },
        { value: "lech", label: `Lệch (${counts.lech})` },
        { value: "cho_kiem", label: `Chờ kiểm (${counts.cho_kiem})` },
      ],
    },
  ];

  const createButton = (
    <Button
      size="large"
      icon={<PlusOutlined />}
      onClick={() => setOpenCreate(true)}
      style={{ borderRadius: 6, fontWeight: 500 }}
    >
      Tạo phiên kiểm kê mới
    </Button>
  );

  const show = (v) => (loading && !stats ? "…" : v);

  return (
    <InventoryPage>
      {!phienId ? (
        <Card style={{ borderRadius: 8, border: "1px solid #f0f0f0" }}>
          <Empty description="Chưa có phiên kiểm kê nào đang mở trên máy này">
            <Button
              type="primary"
              size="large"
              icon={<PlusOutlined />}
              onClick={() => setOpenCreate(true)}
              style={{
                backgroundColor: "#d46b08",
                borderColor: "#d46b08",
                borderRadius: 6,
              }}
            >
              Tạo phiên kiểm kê mới
            </Button>
          </Empty>
        </Card>
      ) : (
        <>
          <div className="stat-row">
            <StatCard
              label="TỔNG PALLET TRONG HỆ THỐNG"
              value={show(fmtNumber(stats?.tongPalletHeThong))}
              icon={Boxes}
              tone="blue"
              badge="cái"
              caption="Theo ảnh chụp lúc tạo phiên"
            />
            <StatCard
              label="ĐÃ KIỂM TRA THỰC TẾ"
              value={show(fmtNumber(stats?.daKiemTraThucTe))}
              icon={ClipboardCheck}
              tone="green"
              badge="cái"
              caption={
                stats ? `Tiến độ ${stats.tienDoPhanTram}% số mã` : undefined
              }
            />
            <StatCard
              label="PHÁT HIỆN CHÊNH LỆCH"
              value={show(fmtNumber(stats?.phatHienChenhLech))}
              icon={PackageX}
              tone="red"
              badge="mã"
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
                value={search}
                onChange={setSearch}
                placeholder="Tìm theo mã SKU, tên pallet, vị trí..."
              />
              <OtherTypeFilter
                label="Lọc trạng thái"
                groups={filterGroups}
                value={{ status: filterStatus }}
                defaultValue={DEFAULT_FILTER}
                onApply={(val) => setFilterStatus(val.status ?? null)}
              />
            </div>

            <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
              <Tag color="blue">Phiên #{phienId}</Tag>
              <Popconfirm
                title="Tạo phiên mới?"
                description="Màn hình sẽ chuyển sang phiên mới. Phiên hiện tại vẫn còn trong cơ sở dữ liệu nhưng không hiển thị ở đây nữa."
                okText="Tạo phiên mới"
                cancelText="Hủy"
                onConfirm={() => setOpenCreate(true)}
              >
                {createButton}
              </Popconfirm>
              <Popconfirm
                title="Hoàn tất kiểm kê?"
                description="Tồn kho sẽ được điều chỉnh theo số thực tế đối với các mã bị lệch. Không thể hoàn tác."
                okText="Hoàn tất"
                cancelText="Hủy"
                onConfirm={handleFinish}
              >
                <Button
                  type="primary"
                  size="large"
                  icon={<CheckOutlined />}
                  loading={finishing}
                  style={{
                    backgroundColor: "#d46b08",
                    borderColor: "#d46b08",
                    borderRadius: 6,
                    fontWeight: 500,
                  }}
                >
                  Hoàn tất kiểm kê
                </Button>
              </Popconfirm>
            </div>
          </div>

          <AppTable
            columns={columns}
            data={pagedRows}
            rowKey={(r) => r.id}
            emptyText={
              loading ? "Đang tải dữ liệu..." : "Không có mã nào phù hợp"
            }
          />

          <Pagination
            currentPage={currentPage}
            totalItems={filteredRows.length}
            itemsPerPage={ITEMS_PER_PAGE}
            itemLabel="mã pallet kiểm kê"
            onPageChange={setCurrentPage}
          />
        </>
      )}

      {openCreate && (
        <CreateStocktakeModal
          onCancel={() => setOpenCreate(false)}
          onCreated={handleCreated}
        />
      )}
      {editing && (
        <EditStocktakeModal
          row={editing}
          onCancel={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            load();
          }}
        />
      )}
    </InventoryPage>
  );
}
