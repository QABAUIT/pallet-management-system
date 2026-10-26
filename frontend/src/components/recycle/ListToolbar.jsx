import { useMemo } from "react";
import { Button } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import SearchInput from "../common/SearchInput";
import OtherTypeFilter from "../common/OtherTypeFilter";

const DEFAULT_FILTER = { status: null };

/** Thanh tìm kiếm + lọc trạng thái + nút tạo, dùng cho các danh sách tải 1 lần (không phân trang BE). */
export default function ListToolbar({
  placeholder,
  keyword,
  onKeywordChange,
  statusMap,
  trangThai,
  onTrangThaiChange,
  onCreate,
  createLabel,
  extra,
}) {
  const groups = useMemo(
    () => [
      {
        key: "status",
        label: "Trạng thái",
        type: "radio",
        options: Object.entries(statusMap ?? {}).map(([value, { label }]) => ({
          value,
          label,
        })),
      },
    ],
    [statusMap],
  );

  return (
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
        style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: 16 }}
      >
        <SearchInput
          value={keyword}
          onChange={onKeywordChange}
          placeholder={placeholder}
        />
        {statusMap && (
          <OtherTypeFilter
            label="Lọc trạng thái"
            groups={groups}
            value={{ status: trangThai ?? null }}
            defaultValue={DEFAULT_FILTER}
            onApply={(val) => onTrangThaiChange(val.status ?? null)}
          />
        )}
      </div>

      <div style={{ display: "flex", gap: 12 }}>
      {extra}
      {onCreate && (
        <Button
          type="primary"
          size="large"
          icon={<PlusOutlined />}
          onClick={onCreate}
          style={{
            backgroundColor: "#d46b08",
            borderColor: "#d46b08",
            borderRadius: 6,
            fontWeight: 500,
          }}
        >
          {createLabel}
        </Button>
      )}
      </div>
    </div>
  );
}
