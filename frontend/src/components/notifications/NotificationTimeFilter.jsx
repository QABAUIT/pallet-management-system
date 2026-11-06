import React from "react";
import { Segmented } from "antd";

const PRESET_OPTIONS = [
  { label: "Hôm nay", value: "today" },
  { label: "7 ngày qua", value: "7days" },
  { label: "Tháng này", value: "month" },
];

export function TimeFilterPreset({ value, onChange }) {
  return (
    <Segmented
      options={PRESET_OPTIONS}
      value={value}
      onChange={onChange}
      className="notif-time-filter"
    />
  );
}
