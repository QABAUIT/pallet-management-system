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
    <div className="page-toolbar">
      <div className="page-toolbar-left">
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

      <div className="page-toolbar-actions">
      {extra}
      {onCreate && (
        <Button
          type="primary"
          size="large"
          icon={<PlusOutlined />}
          onClick={onCreate}
        >
          {createLabel}
        </Button>
      )}
      </div>
    </div>
  );
}
