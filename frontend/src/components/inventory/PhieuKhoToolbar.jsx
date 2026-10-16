import { Button } from "antd";
import { DownloadOutlined, PlusOutlined } from "@ant-design/icons";
import SearchInput from "../common/SearchInput";
import OtherTypeFilter from "../common/OtherTypeFilter";
import TimeFilter from "../common/TimeFilter";
import { PHIEU_KHO_TRANG_THAI } from "../../utils/statusTag";

const STATUS_GROUPS = [
  {
    key: "status",
    label: "Trạng thái",
    type: "radio",
    options: Object.entries(PHIEU_KHO_TRANG_THAI).map(([value, { label }]) => ({
      value,
      label,
    })),
  },
];
const DEFAULT_FILTER = { status: null };

/** Thanh tìm kiếm / lọc / xuất Excel / tạo phiếu dùng chung cho Lịch sử nhập & xuất kho. */
export default function PhieuKhoToolbar({
  placeholder,
  keyword,
  onKeywordChange,
  trangThai,
  onTrangThaiChange,
  time,
  onTimeChange,
  onExport,
  exporting,
  onCreate,
  createLabel,
}) {
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
        style={{
          display: "flex",
          alignItems: "center",
          flexWrap: "wrap",
          gap: 16,
        }}
      >
        <SearchInput
          value={keyword}
          onChange={onKeywordChange}
          placeholder={placeholder}
        />
        <OtherTypeFilter
          label="Lọc trạng thái"
          groups={STATUS_GROUPS}
          value={{ status: trangThai ?? null }}
          defaultValue={DEFAULT_FILTER}
          onApply={(val) => onTrangThaiChange(val.status)}
        />
        <TimeFilter value={time} onChange={onTimeChange} />
      </div>

      <div style={{ display: "flex", gap: 12 }}>
        <Button
          size="large"
          icon={<DownloadOutlined />}
          loading={exporting}
          onClick={onExport}
          style={{ borderRadius: 6 }}
        >
          Xuất file Excel
        </Button>
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
      </div>
    </div>
  );
}
