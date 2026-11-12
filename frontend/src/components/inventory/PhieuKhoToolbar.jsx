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
    <div className="page-toolbar">
      <div className="page-toolbar-left">
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

      <div className="page-toolbar-actions">
        <Button
          size="large"
          icon={<DownloadOutlined />}
          loading={exporting}
          onClick={onExport}
        >
          Xuất file Excel
        </Button>
        <Button
          type="primary"
          size="large"
          icon={<PlusOutlined />}
          onClick={onCreate}
        >
          {createLabel}
        </Button>
      </div>
    </div>
  );
}
