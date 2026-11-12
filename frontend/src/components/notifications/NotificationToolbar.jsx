import React from "react";
import { Flex, Card, Button, Typography, Space, Tooltip } from "antd";
import { Check, RotateCw } from "lucide-react";
import { TimeFilterPreset } from "./NotificationTimeFilter";

const { Text } = Typography;

const FILTERS = [
  { key: "all", label: "Tất cả" },
  { key: "unread", label: "Chưa đọc" },
  { key: "read", label: "Đã đọc" },
];

export default function NotificationToolbar({
  statusFilter,
  setStatusFilter,
  totalCount,
  unreadCount,
  readCount,
  timePreset,
  setTimePreset,
  onRefresh,
  loading,
  onMarkAllRead,
}) {
  const counts = { all: totalCount, unread: unreadCount, read: readCount };

  return (
    <Card className="notif-toolbar" styles={{ body: { padding: "12px 16px" } }}>
      <Flex justify="space-between" align="center" wrap="wrap" gap="middle">
        {/* Bộ lọc tab trạng thái: Tất cả / Chưa đọc / Đã đọc */}
        <div className="notif-filter">
          {FILTERS.map((f) => (
            <Button
              key={f.key}
              type="text"
              className={
                "notif-filter-btn" +
                (statusFilter === f.key ? " is-active" : "")
              }
              onClick={() => setStatusFilter(f.key)}
            >
              {f.label}
              <span
                className={
                  "notif-count" +
                  (f.key === "unread" ? " notif-count-danger" : "") +
                  (statusFilter === f.key ? " is-active" : "")
                }
              >
                {counts[f.key]}
              </span>
            </Button>
          ))}
        </div>

        {/* Nút hành động và lọc thời gian */}
        <Flex align="center" gap={12} wrap="wrap">
          <Button
            icon={<Check size={16} />}
            onClick={onMarkAllRead}
            className="notif-read-all"
          >
            Đánh dấu tất cả đã đọc
          </Button>

          <Space size={8} align="center">
            <Text type="secondary" className="notif-time-label">
              Thời gian:
            </Text>
            <TimeFilterPreset value={timePreset} onChange={setTimePreset} />

            <Tooltip title="Làm mới">
              <Button
                className="notif-icon-btn"
                aria-label="Làm mới thông báo"
                icon={<RotateCw size={15} className={loading ? "animate-spin" : ""} />}
                onClick={() => onRefresh()}
              />
            </Tooltip>
          </Space>
        </Flex>
      </Flex>
    </Card>
  );
}
