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
      style={{
        background: "#f8fafc",
        border: "1px solid #e2e8f0",
        padding: 2,
        borderRadius: 8,
      }}
    />
  );
}
