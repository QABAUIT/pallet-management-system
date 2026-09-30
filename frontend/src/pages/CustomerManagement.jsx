import { useState } from "react";
import OtherTypeFilter from "../components/common/OtherTypeFilter";

const DEFAULT_FILTERS = { status: "null", sort: "newest" };

export default function CustomerManagement() {
  const [filters, setFilters] = useState(DEFAULT_FILTERS);

  return (
    <OtherTypeFilter
      label="Lọc tình trạng"
      groups={[
        {
          key: "status",
          type: "radio", // chọn 1
          options: [
            { value: "settled", label: "Tất toán" },
            { value: "debt", label: "Đang nợ" },
            { value: "overdue", label: "Quá hạn" },
          ],
        },
        // Muốn chọn nhiều thì đổi type: "checkbox", value[key] khi đó là mảng, vd []
      ]}
      sortOptions={[
        { value: "newest", label: "Mới nhất" },
        { value: "oldest", label: "Cũ nhất" },
      ]}
      value={filters}
      defaultValue={DEFAULT_FILTERS}
      onApply={setFilters}
    />
  );
}