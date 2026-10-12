import { useEffect, useMemo, useState } from "react";
import { BadgeInfo, Banknote, AlertTriangle, Eye, UserPlus, Pencil } from "lucide-react";
import StatCard from "../components/common/StatCard";
import SearchInput from "../components/common/SearchInput";
import OtherTypeFilter from "../components/common/OtherTypeFilter";
import AppTable from "../components/common/AppTable";
import Pagination from "../components/common/Pagination";
import AddCustomerModal from "../components/common/AddCustomerModal";
import { khachHangApi } from "../api/khachHangApi";
import "../components/layout/Customer.css";

// Tình trạng công nợ - backend tự tính từ hóa đơn
const STATUS_LABEL = {
  tat_toan: "Tất toán",
  dang_no: "Đang nợ",
  qua_han: "Quá hạn",
  chua_phat_sinh: "Chưa phát sinh",
};
const STATUS_TONE = {
  tat_toan: "green",
  dang_no: "amber",
  qua_han: "red",
  chua_phat_sinh: "blue",
};
const HOP_TAC_LABEL = { dang_hop_tac: "Đang hợp tác", ngung_hop_tac: "Ngừng hợp tác" };
const LOAI_KH_LABEL = { doanh_nghiep: "Doanh nghiệp", ca_nhan: "Cá nhân" };

const DEFAULT_FILTERS = { status: null };

const formatMoney = (n) => (n ?? 0).toLocaleString("vi-VN") + " đ";

// Đổi khách hàng đang lưu trong danh sách -> field cho form sửa
const customerToFormValues = (c) => ({
  ten_kh: c.name || "",
  loai_kh: c.loaiKh || "doanh_nghiep",
  mst: c.mst || "",
  dia_chi: c.address || "",
  sdt: c.phone || "",
  email: c.email || "",
  nguoi_dai_dien: c.representative || "",
  trang_thai: c.trangThai || "dang_hop_tac",
});

// Lấy thông báo lỗi từ apiClient: ưu tiên lỗi từng field (vd "Số điện thoại không hợp lệ")
const getErrorMessage = (err) => {
  const details =
    err?.fieldErrors && typeof err.fieldErrors === "object"
      ? Object.values(err.fieldErrors).join("\n")
      : "";
  return details || err?.message || "Có lỗi xảy ra, vui lòng thử lại";
};

export default function CustomerManagement() {
  const [customerList, setCustomerList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [search, setSearch] = useState("");
  const [filters, setFilters] = useState(DEFAULT_FILTERS);
  const [page, setPage] = useState(1);
  const [selectedId, setSelectedId] = useState(null); // chỉ set khi bấm icon con mắt
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState(null);
  const perPage = 6;

  const editInitialValues = useMemo(
    () => (editingCustomer ? customerToFormValues(editingCustomer) : null),
    [editingCustomer]
  );

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await khachHangApi.layDanhSach();
      setCustomerList(data);
    } catch (err) {
      console.error("Lỗi lấy danh sách khách hàng:", err);
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (payload) => {
    try {
      await khachHangApi.taoMoi(payload);
      setShowAddModal(false);
      setPage(1);
      await loadCustomers();
    } catch (err) {
      alert(getErrorMessage(err));
    }
  };

  const handleUpdate = async (payload) => {
    try {
      await khachHangApi.capNhat(editingCustomer.id, payload);
      setEditingCustomer(null);
      await loadCustomers();
    } catch (err) {
      alert(getErrorMessage(err));
    }
  };

  // ----- Lọc dữ liệu -----
  const filteredCustomers = customerList.filter((c) => {
    const matchStatus = filters.status == null || c.status === filters.status;
    if (!matchStatus) return false;

    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      String(c.code ?? "").toLowerCase().includes(q) ||
      String(c.name ?? "").toLowerCase().includes(q) ||
      String(c.mst ?? "").toLowerCase().includes(q) ||
      String(c.phone ?? "").toLowerCase().includes(q)
    );
  });

  const pagedCustomers = filteredCustomers.slice((page - 1) * perPage, page * perPage);
  const selectedCustomer = customerList.find((c) => c.id === selectedId) || null;

  // ----- Số liệu 3 thẻ thống kê, tính từ danh sách thật -----
  const overdueCustomers = customerList.filter((c) => c.status === "qua_han");
  const totalRevenue = customerList.reduce((sum, c) => sum + (c.totalSpend ?? 0), 0);
  const overdueAmount = overdueCustomers.reduce((sum, c) => sum + (c.totalDebt ?? 0), 0);

  // ----- Cột bảng -----
  const columns = [
    {
      key: "code",
      header: "Mã KH",
      width: "110px",
      render: (row) => <span className="cust-code">{row.code}</span>,
    },
    {
      key: "name",
      header: "Tên doanh nghiệp / Đối tác",
      render: (row) => (
        <div>
          <p className="cust-name">
            {row.name}
            {row.trangThai === "ngung_hop_tac" && (
              <span className="tag-inactive">Ngừng hợp tác</span>
            )}
          </p>
          <p className="cust-mst">MST: {row.mst || "—"}</p>
        </div>
      ),
    },
    {
      key: "contact",
      header: "Liên hệ",
      render: (row) => (
        <div>
          <p className="cust-contact-line">{row.phone}</p>
          <p className="cust-contact-line muted">{row.email}</p>
        </div>
      ),
    },
    { key: "totalOrders", header: "Tổng đơn", align: "center", width: "90px" },
    {
      key: "totalSpend",
      header: "Tổng chi tiêu (VNĐ)",
      align: "right",
      width: "150px",
      render: (row) => formatMoney(row.totalSpend),
    },
    {
      key: "status",
      header: "Tình trạng",
      width: "120px",
      render: (row) => (
        <span className={`stat-badge tone-${STATUS_TONE[row.status] ?? "blue"}`}>
          {STATUS_LABEL[row.status] ?? row.status}
        </span>
      ),
    },
    {
      key: "actions",
      header: "Thao tác",
      align: "center",
      width: "80px",
      render: (row) => (
        <button
          type="button"
          className="icon-only-btn"
          onClick={() => setSelectedId(row.id)}
          aria-label={`Xem chi tiết ${row.name}`}
        >
          <Eye size={16} />
        </button>
      ),
    },
  ];

  if (loading && customerList.length === 0) {
    return <div>Đang tải danh sách khách hàng...</div>;
  }

  if (error && customerList.length === 0) {
    return <div>Lỗi: {error}</div>;
  }

  return (
    <div>
      {/* 3 thẻ thống kê */}
      <div className="stat-row">
        <StatCard
          label="Tổng khách hàng B2B"
          value={`${customerList.length} đối tác`}
          icon={BadgeInfo}
          tone="blue"
          caption="Tổng số khách hàng trong hệ thống"
        />
        <StatCard
          label="Tổng doanh số lũy kế"
          value={formatMoney(totalRevenue)}
          icon={Banknote}
          tone="green"
          caption="Tính từ các hóa đơn đã thanh toán"
        />
        <StatCard
          label="Khách nợ quá hạn"
          value={`${overdueCustomers.length} đối tác cảnh báo`}
          icon={AlertTriangle}
          tone="red"
          badge={`Cần thu hồi: ${formatMoney(overdueAmount)}`}
        />
      </div>

      {/* Thanh tìm kiếm + lọc + nút thêm */}
      <div className="toolbar-row">
        <SearchInput
          value={search}
          onChange={(v) => {
            setSearch(v);
            setPage(1);
          }}
          placeholder="Tìm mã KH, tên doanh nghiệp, MST, SĐT..."
        />

        <OtherTypeFilter
          label="Lọc tình trạng"
          groups={[
            {
              key: "status",
              type: "radio",
              options: [
                { value: "tat_toan", label: "Tất toán" },
                { value: "dang_no", label: "Đang nợ" },
                { value: "qua_han", label: "Quá hạn" },
                { value: "chua_phat_sinh", label: "Chưa phát sinh" },
              ],
            },
          ]}
          value={filters}
          defaultValue={DEFAULT_FILTERS}
          onApply={(next) => {
            setFilters(next);
            setPage(1);
          }}
        />

        <button
          type="button"
          className="btn-solid toolbar-add-btn"
          onClick={() => setShowAddModal(true)}
        >
          <UserPlus size={16} />
          Thêm Khách Hàng
        </button>
      </div>

      {/* Bảng + panel chi tiết: chỉ tách 2 cột khi đã chọn 1 khách hàng */}
      <div className={"cust-layout" + (selectedCustomer ? " with-detail" : "")}>
        <div className="cust-table-col">
          <AppTable columns={columns} data={pagedCustomers} rowKey={(row) => row.id} />

          <div style={{ marginTop: 16 }}>
            <Pagination
              currentPage={page}
              totalItems={filteredCustomers.length}
              itemsPerPage={perPage}
              itemLabel="doanh nghiệp"
              onPageChange={setPage}
            />
          </div>
        </div>

        {selectedCustomer && (
          <CustomerDetailPanel
            customer={selectedCustomer}
            onClose={() => setSelectedId(null)}
            onEdit={() => setEditingCustomer(selectedCustomer)}
          />
        )}
      </div>

      <AddCustomerModal
        open={showAddModal}
        mode="add"
        onClose={() => setShowAddModal(false)}
        onSave={handleCreate}
      />

      <AddCustomerModal
        open={!!editingCustomer}
        mode="edit"
        initialValues={editInitialValues}
        onClose={() => setEditingCustomer(null)}
        onSave={handleUpdate}
      />
    </div>
  );
}

// ========== Panel chi tiết bên phải ==========
function CustomerDetailPanel({ customer, onClose, onEdit }) {
  return (
    <div className="detail-panel">
      <div className="detail-header">
        <div className="detail-header-top">
          <span className="detail-id">{customer.code}</span>
          <span className={`stat-badge tone-${STATUS_TONE[customer.status] ?? "blue"}`}>
            {STATUS_LABEL[customer.status] ?? customer.status}
          </span>
          {customer.trangThai === "ngung_hop_tac" && (
            <span className="tag-inactive">Ngừng hợp tác</span>
          )}
          <button type="button" className="detail-close-btn" onClick={onClose} aria-label="Đóng">
            ×
          </button>
        </div>
        <h3 className="detail-company-name">{customer.name}</h3>
        {customer.description && <p className="detail-company-desc">{customer.description}</p>}
      </div>

      <div className="detail-section">
        <div className="detail-section-head">
          <p className="detail-section-title">Thông tin chung doanh nghiệp</p>
          <button type="button" className="detail-edit-link" onClick={onEdit}>
            <Pencil size={13} /> Chỉnh sửa
          </button>
        </div>
        <div className="detail-info-grid">
          <div>
            <p className="detail-field-label">Mã số thuế (MST)</p>
            <p className="detail-field-value">{customer.mst || "—"}</p>
          </div>
          <div>
            <p className="detail-field-label">Người đại diện &amp; SĐT</p>
            <p className="detail-field-value">
              {[customer.representative, customer.phone].filter(Boolean).join(" · ") || "—"}
            </p>
          </div>
          <div>
            <p className="detail-field-label">Loại khách hàng</p>
            <p className="detail-field-value">{LOAI_KH_LABEL[customer.loaiKh] ?? "—"}</p>
          </div>
          <div>
            <p className="detail-field-label">Trạng thái hợp tác</p>
            <p className="detail-field-value">{HOP_TAC_LABEL[customer.trangThai] ?? "—"}</p>
          </div>
          <div className="detail-field-full">
            <p className="detail-field-label">Email liên hệ kế toán / điều phối</p>
            <p className="detail-field-value">{customer.email || "—"}</p>
          </div>
          <div className="detail-field-full">
            <p className="detail-field-label">Địa chỉ đăng ký kinh doanh &amp; kho giao nhận</p>
            <p className="detail-field-value">{customer.address || "—"}</p>
          </div>
        </div>
      </div>

      <div className="detail-section">
        <div className="detail-section-head">
          <p className="detail-section-title">Thống kê tài chính lũy kế</p>
          {customer.debtLimit > 0 && (
            <span className="detail-muted">
              Hạn mức nợ: {(customer.debtLimit / 1000000).toFixed(0)} Tr
            </span>
          )}
        </div>
        <div className="detail-stat-grid">
          <div className="detail-stat-box">
            <p className="detail-field-label">Tổng chi tiêu đã ghi nhận</p>
            <p className="detail-stat-value">{formatMoney(customer.totalSpend)}</p>
            {customer.onTimePaymentPercent > 0 && (
              <p className="detail-stat-caption">
                {customer.onTimePaymentPercent}% thanh toán đúng hạn
              </p>
            )}
          </div>
          <div className="detail-stat-box">
            <p className="detail-field-label">Tổng đơn hàng đã xuất</p>
            <p className="detail-stat-value">{customer.totalOrders} đơn</p>
            {customer.palletsCirculated > 0 && (
              <p className="detail-stat-caption">
                {customer.palletsCirculated.toLocaleString("vi-VN")} Pallet đã luân chuyển
              </p>
            )}
          </div>
        </div>
      </div>

      {customer.unpaidInvoices.length > 0 && (
        <div className="detail-section">
          <div className="detail-section-head">
            <p className="detail-section-title">Hóa đơn chưa thanh toán (công nợ)</p>
            <span className="detail-debt-total">Tổng nợ: {formatMoney(customer.totalDebt)}</span>
          </div>
          <div className="detail-debt-table">
            {customer.unpaidInvoices.map((inv) => (
              <div className="detail-debt-row" key={inv.code}>
                <div>
                  <p className="debt-code">{inv.code}</p>
                  <p className="debt-dates">
                    Ngày lập {inv.issueDate}
                    {inv.dueDate && ` · Hạn ${inv.dueDate}`}
                  </p>
                </div>
                <p className="debt-amount">{formatMoney(inv.amount)}</p>
                <button type="button" className="pay-btn">
                  Thanh toán ngay
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}