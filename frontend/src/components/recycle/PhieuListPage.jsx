import { useState } from "react";
import InventoryPage from "../inventory/InventoryPage";
import RecycleTabs from "./RecycleTabs";
import ListToolbar from "./ListToolbar";
import PhieuRowActions from "./PhieuRowActions";
import StatCard from "../common/StatCard";
import AppTable from "../common/AppTable";
import Pagination from "../common/Pagination";
import { useLocalList } from "../../hooks/useLocalList";
import { usePhanQuyen } from "../../hooks/usePhanQuyen";
import { PHIEU_DUYET_TRANG_THAI } from "../../utils/statusTag";

import "../layout/style.css";

/**
 * Khung trang danh sách cho 3 bước tái chế (Phân loại / Tháo dỡ / Xử lý thêm).
 *
 * Props:
 * - api: { danhSach, duyet, huy, ... } (xem api/recycleApi.js)
 * - searchFields: mảng tên trường để tìm kiếm (khai báo hằng số ngoài component)
 * - columns: các cột dữ liệu (cột "Thao tác" được thêm tự động)
 * - buildStats(rows) -> [{ label, value, icon, tone, badge, caption }]
 * - FormModal: component form tạo/sửa, nhận { record, onCancel, onSaved }
 * - DetailDrawer: (tuỳ chọn) nhận { id, onClose }
 * - statusMap: bảng trạng thái cho bộ lọc (mặc định nhap / da_duyet / da_huy)
 */
export default function PhieuListPage({
  api,
  searchFields,
  columns,
  buildStats,
  FormModal,
  DetailDrawer,
  createLabel,
  placeholder,
  itemLabel,
  emptyText,
  statusMap = PHIEU_DUYET_TRANG_THAI,
}) {
  const list = useLocalList(api.danhSach, { searchFields });
  const { duocThaoTacKho } = usePhanQuyen();
  const [form, setForm] = useState(null); // null = đóng; { record } = mở (record rỗng = tạo mới)
  const [viewId, setViewId] = useState(null);

  const allColumns = [
    ...columns,
    {
      key: "action",
      header: "THAO TÁC",
      align: "center",
      render: (r) => (
        <PhieuRowActions
          r={r}
          api={api}
          onChanged={list.reload}
          onView={DetailDrawer ? (row) => setViewId(row.id) : undefined}
          onEdit={(row) => setForm({ record: row })}
        />
      ),
    },
  ];

  const stats = buildStats(list.rows);
  const show = (v) => (list.loading ? "…" : v);

  return (
    <InventoryPage>
      <RecycleTabs />

      <div className="stat-row">
        {stats.map((s) => (
          <StatCard key={s.label} {...s} value={show(s.value)} />
        ))}
      </div>

      <ListToolbar
        placeholder={placeholder}
        keyword={list.keyword}
        onKeywordChange={list.setKeyword}
        statusMap={statusMap}
        trangThai={list.trangThai}
        onTrangThaiChange={list.setTrangThai}
        onCreate={duocThaoTacKho ? () => setForm({}) : undefined}
        createLabel={createLabel}
      />

      <AppTable
        columns={allColumns}
        data={list.paged}
        rowKey={(r) => r.id}
        emptyText={list.loading ? "Đang tải dữ liệu..." : emptyText}
      />

      <Pagination
        currentPage={list.page}
        totalItems={list.filtered.length}
        itemsPerPage={list.pageSize}
        itemLabel={itemLabel}
        onPageChange={list.setPage}
      />

      {form && (
        <FormModal
          key={form.record?.id ?? "new"}
          record={form.record}
          onCancel={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            list.reload();
          }}
        />
      )}

      {DetailDrawer && viewId && (
        <DetailDrawer id={viewId} onClose={() => setViewId(null)} />
      )}
    </InventoryPage>
  );
}
