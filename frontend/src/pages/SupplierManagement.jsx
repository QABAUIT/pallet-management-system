import React, { useMemo, useState } from "react";
import { Card, Table, Typography, message } from "antd";

import { useServerTable } from "../hooks/useServerTable";
import { nhaCungCapApi } from "../api/nhaCungCapApi";
import { notifyError } from "../utils/notify";

import SupplierFilterBar from "../components/supplier/SupplierFilterBar";
import SupplierModal from "../components/supplier/SupplierModal";
import SupplierDetailModal from "../components/supplier/SupplierDetailModal";
import { getSupplierColumns } from "../components/supplier/SupplierTableColumns";

const { Title, Text } = Typography;

// Giá trị phải KHỚP CHÍNH XÁC với cột nhom_hang trong DB (backend lọc bằng dấu "=")
const NHOM_HANG = ["Gỗ tràm xẻ, thanh gỗ các loại", "Pallet nhựa"];

// apiClient đã giải nén axios (response.data) nên nhận được ApiResponse
// { status, message, data }. useServerTable cần PageResponse { content, totalElements }
// => lấy tiếp .data
const fetchSuppliers = (params) =>
  nhaCungCapApi.getAll(params).then((res) => res.data);

export default function SupplierManagement() {
  const [keyword, setKeyword] = useState(""); // chữ đang gõ
  const [searchTerm, setSearchTerm] = useState(""); // chữ đã áp dụng (Enter)
  const [nhomHang, setNhomHang] = useState(undefined); // undefined = tất cả

  // Modal xem chi tiết (lưu id đang xem, null = đóng)
  const [detailId, setDetailId] = useState(null);

  // Modal thêm / sửa
  const [modal, setModal] = useState({
    open: false,
    mode: "add",
    record: null,
  });

  // useServerTable fetch lại mỗi khi extraParams đổi
  const extraParams = useMemo(
    () => ({
      keyword: searchTerm || undefined,
      nhomHang: nhomHang || undefined,
    }),
    [searchTerm, nhomHang],
  );

  const table = useServerTable(fetchSuppliers, extraParams);
  const { current, pageSize } = table.pagination;

  // Về trang đầu khi đổi điều kiện tìm/lọc (hook dùng pageIndex 0-based nội bộ)
  const goToFirstPage = () => table.handleTableChange({ current: 1, pageSize });

  const handleSearch = () => {
    setSearchTerm(keyword.trim());
    goToFirstPage();
  };

  // Xóa hết chữ trong ô tìm kiếm thì tự quay lại danh sách đầy đủ
  const handleKeywordChange = (value) => {
    setKeyword(value);
    if (value === "" && searchTerm !== "") {
      setSearchTerm("");
      goToFirstPage();
    }
  };

  const handleNhomHangChange = (value) => {
    setNhomHang(value);
    goToFirstPage();
  };

  const openAdd = () => setModal({ open: true, mode: "add", record: null });
  const openEdit = (record) => setModal({ open: true, mode: "edit", record });
  const closeModal = () => setModal((m) => ({ ...m, open: false }));

  // Ném lỗi ra ngoài để SupplierModal tự hiện lỗi (theo field hoặc thông báo chung)
  const handleSave = async (payload) => {
    if (modal.mode === "edit") {
      await nhaCungCapApi.update(modal.record.id, payload);
      message.success("Cập nhật nhà cung cấp thành công");
    } else {
      await nhaCungCapApi.create(payload);
      message.success("Thêm nhà cung cấp thành công");
    }
    closeModal();
    table.reload();
  };

  const handleDelete = async (id) => {
    try {
      await nhaCungCapApi.delete(id);
      message.success("Đã chuyển nhà cung cấp sang trạng thái Ngừng hợp tác");
      table.reload();
    } catch (err) {
      notifyError(err);
    }
  };

  const columns = getSupplierColumns({
    offset: (current - 1) * pageSize,
    onView: (record) => setDetailId(record.id),
    onEdit: openEdit,
    onDelete: handleDelete,
  });

  return (
    <div
      style={{
        padding: "24px",
        backgroundColor: "#fcfcfc",
        minHeight: "100vh",
        textAlign: "left",
      }}
    >
      <SupplierFilterBar
        keyword={keyword}
        onKeywordChange={handleKeywordChange}
        onSearch={handleSearch}
        nhomHang={nhomHang}
        onNhomHangChange={handleNhomHangChange}
        nhomHangOptions={NHOM_HANG}
        onAdd={openAdd}
      />

      <Card
        bodyStyle={{ padding: 0 }}
        style={{
          borderRadius: "8px",
          overflow: "hidden",
          border: "1px solid #f0f0f0",
        }}
      >
        <Table
          columns={columns}
          dataSource={table.data}
          loading={table.loading}
          rowKey="id"
          scroll={{ x: 1300 }}
          pagination={{
            ...table.pagination,
            showTotal: (total, range) =>
              `Hiển thị ${range[0]} - ${range[1]} trên tổng số ${total} nhà cung cấp`,
          }}
          onChange={table.handleTableChange}
          locale={{ emptyText: "Chưa có nhà cung cấp nào" }}
        />
      </Card>

      {detailId && (
        <SupplierDetailModal
          id={detailId}
          onClose={() => setDetailId(null)}
          onEdit={(record) => {
            setDetailId(null);
            openEdit(record);
          }}
        />
      )}

      {modal.open && (
        <SupplierModal
          mode={modal.mode}
          record={modal.record}
          nhomHangOptions={NHOM_HANG}
          onCancel={closeModal}
          onSubmit={handleSave}
        />
      )}
    </div>
  );
}
