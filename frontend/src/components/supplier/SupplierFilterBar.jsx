import React from "react";
import { Space, Input, Select, Button } from "antd";
import { SearchOutlined, PlusOutlined } from "@ant-design/icons";

export default function SupplierFilterBar({
  keyword,
  onKeywordChange,
  onSearch,
  nhomHang,
  onNhomHangChange,
  nhomHangOptions = [],
  onAdd,
}) {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        marginBottom: "16px",
        alignItems: "center",
      }}
    >
      <Space size="middle">
        <Input
          placeholder="Tìm theo mã, tên nhà cung cấp, SĐT... (nhấn Enter)"
          prefix={<SearchOutlined style={{ color: "#bfbfbf" }} />}
          style={{ width: 360, borderRadius: "6px" }}
          size="large"
          allowClear
          value={keyword}
          onChange={(e) => onKeywordChange(e.target.value)}
          onPressEnter={onSearch}
        />
        <Select
          placeholder="Tất cả nhóm hàng"
          size="large"
          style={{ width: 260 }}
          allowClear
          value={nhomHang}
          onChange={onNhomHangChange}
          options={nhomHangOptions.map((v) => ({ value: v, label: v }))}
        />
      </Space>

      <Button
        type="primary"
        icon={<PlusOutlined />}
        size="large"
        style={{
          backgroundColor: "#d46b08",
          borderColor: "#d46b08",
          borderRadius: "6px",
          fontWeight: 500,
        }}
        onClick={onAdd}
      >
        Thêm nhà cung cấp
      </Button>
    </div>
  );
}
