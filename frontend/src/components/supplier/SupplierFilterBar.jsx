import React from "react";
import { Button } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import SearchInput from "../common/SearchInput";
import OtherTypeFilter from "../common/OtherTypeFilter";

const DEFAULT_FILTER = { nhomHang: null };

/** Thanh tìm kiếm + lọc nhóm hàng + nút thêm, dùng SearchInput / OtherTypeFilter chung như các trang khác. */
export default function SupplierFilterBar({
  keyword,
  onKeywordChange,
  nhomHang,
  onNhomHangChange,
  nhomHangOptions = [],
  onAdd,
}) {
  const groups = [
    {
      key: "nhomHang",
      label: "Nhóm hàng",
      type: "radio",
      options: nhomHangOptions.map((v) => ({ value: v, label: v })),
    },
  ];

  return (
    <div className="page-toolbar">
      <div className="page-toolbar-left">
        <SearchInput
          value={keyword}
          onChange={onKeywordChange}
          placeholder="Tìm theo mã, tên nhà cung cấp, SĐT..."
        />
        <OtherTypeFilter
          label="Lọc nhóm hàng"
          groups={groups}
          value={{ nhomHang: nhomHang ?? null }}
          defaultValue={DEFAULT_FILTER}
          onApply={(val) => onNhomHangChange(val.nhomHang)}
        />
      </div>

      <div className="page-toolbar-actions">
        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={onAdd}
        >
          Thêm nhà cung cấp
        </Button>
      </div>
    </div>
  );
}
