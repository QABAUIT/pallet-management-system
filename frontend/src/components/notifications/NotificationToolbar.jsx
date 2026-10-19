import React from "react";
import { Flex, Card, Button, Typography, Space, Tooltip } from "antd";
import { Check, RotateCw } from "lucide-react";
import { TimeFilterPreset } from "./NotificationTimeFilter";

const { Text } = Typography;

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
  return (
    <Card
      styles={{ body: { padding: "12px 16px" } }}
      style={{
        borderRadius: 12,
        border: "1px solid #e2e8f0",
        marginBottom: 16,
        background: "#ffffff",
      }}
    >
      <Flex justify="space-between" align="center" wrap="wrap" gap="middle">
        {/* Bộ lọc tab trạng thái: Tất cả / Chưa đọc / Đã đọc */}
        <Flex
          align="center"
          gap={4}
          style={{
            background: "#f1f5f9",
            padding: 4,
            borderRadius: 8,
          }}
        >
          <Button
            type={statusFilter === "all" ? "primary" : "text"}
            size="middle"
            onClick={() => setStatusFilter("all")}
            style={{
              borderRadius: 6,
              background: statusFilter === "all" ? "#0f172a" : "transparent",
              color: statusFilter === "all" ? "#ffffff" : "#475569",
              fontWeight: 500,
            }}
          >
            Tất cả{" "}
            <span
              style={{
                marginLeft: 4,
                fontSize: 11,
                padding: "1px 6px",
                borderRadius: 10,
                background: statusFilter === "all" ? "rgba(255,255,255,0.25)" : "#cbd5e1",
                color: statusFilter === "all" ? "#ffffff" : "#334155",
              }}
            >
              {totalCount}
            </span>
          </Button>

          <Button
            type={statusFilter === "unread" ? "primary" : "text"}
            size="middle"
            onClick={() => setStatusFilter("unread")}
            style={{
              borderRadius: 6,
              background: statusFilter === "unread" ? "#0f172a" : "transparent",
              color: statusFilter === "unread" ? "#ffffff" : "#475569",
              fontWeight: 500,
            }}
          >
            Chưa đọc{" "}
            <span
              style={{
                marginLeft: 4,
                fontSize: 11,
                padding: "1px 6px",
                borderRadius: 10,
                background: "#dc2626",
                color: "#ffffff",
                fontWeight: 600,
              }}
            >
              {unreadCount}
            </span>
          </Button>

          <Button
            type={statusFilter === "read" ? "primary" : "text"}
            size="middle"
            onClick={() => setStatusFilter("read")}
            style={{
              borderRadius: 6,
              background: statusFilter === "read" ? "#0f172a" : "transparent",
              color: statusFilter === "read" ? "#ffffff" : "#475569",
              fontWeight: 500,
            }}
          >
            Đã đọc{" "}
            <span
              style={{
                marginLeft: 4,
                fontSize: 11,
                padding: "1px 6px",
                borderRadius: 10,
                background: statusFilter === "read" ? "rgba(255,255,255,0.25)" : "#e2e8f0",
                color: statusFilter === "read" ? "#ffffff" : "#475569",
              }}
            >
              {readCount}
            </span>
          </Button>
        </Flex>

        {/* Nút hành động và lọc thời gian */}
        <Flex align="center" gap={12} wrap="wrap">
          <Button
            icon={<Check size={16} />}
            onClick={onMarkAllRead}
            style={{
              borderColor: "#bbf7d0",
              background: "#f0fdf4",
              color: "#15803d",
              fontWeight: 600,
              borderRadius: 8,
              height: 36,
            }}
          >
            Đánh dấu tất cả đã đọc
          </Button>

          <Space size={8} align="center">
            <Text type="secondary" style={{ fontSize: 13 }}>
              Thời gian:
            </Text>
            <TimeFilterPreset value={timePreset} onChange={setTimePreset} />

            <Tooltip title="Làm mới">
              <Button
                icon={<RotateCw size={15} className={loading ? "animate-spin" : ""} />}
                onClick={onRefresh}
                style={{
                  borderRadius: 8,
                  height: 36,
                  width: 36,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  borderColor: "#e2e8f0",
                }}
              />
            </Tooltip>
          </Space>
        </Flex>
      </Flex>
    </Card>
  );
}
