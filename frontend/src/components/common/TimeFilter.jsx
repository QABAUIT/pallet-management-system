import { useEffect, useRef, useState } from "react";
import { ChevronDown } from "lucide-react";
import { DayPicker } from "react-day-picker";
import { vi } from "date-fns/locale";
import "react-day-picker/dist/style.css"; // v9 thì đổi thành "react-day-picker/style.css", xem ghi chú bên dưới
import "../layout/style.css";

const PRESETS = [
  { key: "year", label: "Năm nay" },
  { key: "quarter", label: "Quý này" },
  { key: "month", label: "Tháng này" },
];

const fmt = (d) =>
  d
    ? new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }).format(d)
    : "";

/**
 * TimeFilter - Năm nay | Quý này | Tháng này | Tùy chọn ngày.
 * Chọn preset -> áp dụng ngay. Chọn "Tùy chọn ngày" -> mở lịch, chọn xong
 * cả 2 đầu (từ/đến) thì tự áp dụng và đóng lịch lại.
 *
 * Props:
 * - value: { preset: "year"|"quarter"|"month"|"custom", from: Date|null, to: Date|null }
 * - onChange(next): gọi khi chọn preset, hoặc khi đã chọn đủ from/to cho custom
 * - counts: { year, quarter, month, custom } - số dòng khớp mỗi loại, không truyền thì ẩn số
 */
export default function TimeFilter({ value, onChange, counts = {} }) {
  const [openCalendar, setOpenCalendar] = useState(false);
  const [range, setRange] = useState({
    from: value.from ?? undefined,
    to: value.to ?? undefined,
  });
  const rootRef = useRef(null);

  const isCustom = value.preset === "custom";

  useEffect(() => {
    if (!openCalendar) return;
    const onDown = (e) => {
      if (rootRef.current && !rootRef.current.contains(e.target)) setOpenCalendar(false);
    };
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, [openCalendar]);

  const selectPreset = (key) => {
    setOpenCalendar(false);
    onChange({ preset: key, from: null, to: null });
  };

  const toggleCalendar = () => {
    setRange({ from: value.from ?? undefined, to: value.to ?? undefined });
    setOpenCalendar((o) => !o);
  };

  const handleRangeSelect = (next) => {
  setRange(next ?? { from: undefined, to: undefined });

  // Chỉ tự áp dụng khi đã chọn ĐỦ 2 ngày KHÁC NHAU (from < to thực sự)
  const hasTwoDistinctDates =
    next?.from && next?.to && next.from.getTime() !== next.to.getTime();

  if (hasTwoDistinctDates) {
    onChange({ preset: "custom", from: next.from, to: next.to });
    setOpenCalendar(false);
  }
};

  return (
    <div className="tf-row" ref={rootRef}>
      {PRESETS.map((p) => (
        <button
          key={p.key}
          type="button"
          className={"tf-tab" + (value.preset === p.key ? " active" : "")}
          onClick={() => selectPreset(p.key)}
        >
          <span>{p.label}</span>
          {counts[p.key] !== undefined && (
            <span className="tf-count">{counts[p.key].toLocaleString("vi-VN")}</span>
          )}
        </button>
      ))}

      <div className="tf-cal-wrap">
        <button
          type="button"
          className={"tf-tab" + (isCustom ? " active" : "")}
          onClick={toggleCalendar}
          aria-expanded={openCalendar}
        >
          <span>
            {isCustom && value.from && value.to
              ? `${fmt(value.from)} - ${fmt(value.to)}`
              : "Tùy chọn ngày"}
          </span>
          {counts.custom !== undefined && isCustom && (
            <span className="tf-count">{counts.custom.toLocaleString("vi-VN")}</span>
          )}
          <ChevronDown size={14} className={"tf-chevron" + (openCalendar ? " up" : "")} />
        </button>

        {openCalendar && (
          <div className="tf-popover">
            <div className="tf-date-inputs">
              <div className="tf-date-box">
                <span className="tf-date-label">Từ ngày</span>
                <span className="tf-date-value">{fmt(range.from) || "--/--/----"}</span>
              </div>
              <div className="tf-date-box">
                <span className="tf-date-label">Đến ngày</span>
                <span className="tf-date-value">{fmt(range.to) || "--/--/----"}</span>
              </div>
            </div>

            <DayPicker
                locale={vi}
                mode="range"
                selected={range}
                onSelect={handleRangeSelect}
                numberOfMonths={1}
            />
          </div>
        )}
      </div>
    </div>
  );
}