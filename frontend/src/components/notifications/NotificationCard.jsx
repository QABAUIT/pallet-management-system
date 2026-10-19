import React from "react";
import { Card, Flex, Tag, Typography, Button, Space, Tooltip } from "antd";
import {
  FileText,
  Truck,
  Package,
  Warehouse,
  Clock,
  Eye,
  Check,
  Mail,
  Trash2,
} from "lucide-react";
import { useNavigate } from "react-router-dom";

const { Text } = Typography;

export default function NotificationCard({
  item,
  isRead,
  onToggleRead,
  onDelete,
}) {
  const navigate = useNavigate();

  const renderIcon = (type) => {
    switch (type) {
      case "invoice":
        return <FileText size={22} />;
      case "truck":
        return <Truck size={22} />;
      case "warehouse":
        return <Warehouse size={22} />;
      default:
        return <Package size={22} />;
    }
  };

  const iconBg = isRead ? "#f59e0b" : "#ef4444";
  const borderColor = isRead ? "#fef08a" : "#fecaca";

  return (
    <Card
      styles={{ body: { padding: 16 } }}
      style={{
        borderRadius: 12,
        border: `1px solid ${borderColor}`,
        background: "#ffffff",
        transition: "all 0.2s ease",
      }}
      className="notif-item-card"
    >
      <Flex gap={16} align="flex-start">
        {/* Cột Icon bên trái */}
        <div style={{ position: "relative", flexShrink: 0 }}>
          <div
            style={{
              width: 48,
              height: 48,
              borderRadius: 10,
              background: iconBg,
              color: "#ffffff",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            {renderIcon(item.iconType)}
          </div>
          {!isRead && (
            <span
              style={{
                position: "absolute",
                top: -3,
                right: -3,
                width: 9,
                height: 9,
                background: "#ffffff",
                border: "2px solid #ef4444",
                borderRadius: "50%",
              }}
            />
          )}
        </div>

        {/* Nội dung thông báo */}
        <Flex vertical gap={8} style={{ flex: 1, minWidth: 0 }}>
          {/* Header thông báo: Type pill, mã code, thời gian */}
          <Flex justify="space-between" align="center" wrap="wrap" gap={8}>
            <Flex align="center" gap={8} wrap="wrap">
              <Tag
                color={isRead ? "warning" : "error"}
                style={{
                  fontWeight: 700,
                  fontSize: 11,
                  borderRadius: 4,
                  marginRight: 0,
                  letterSpacing: "0.03em",
                }}
              >
                {item.type}
              </Tag>
              <Text strong style={{ fontSize: 13, color: "#0f172a" }}>
                {item.code}
              </Text>
              <Text type="secondary" style={{ fontSize: 13 }}>
                · {item.subtext}
              </Text>
            </Flex>

            <Space size={4} style={{ color: isRead ? "#64748b" : "#ef4444", fontSize: 12, fontWeight: 500 }}>
              <Clock size={13} />
              <span>{item.time}</span>
            </Space>
          </Flex>

          {/* Mô tả chi tiết */}
          <div style={{ fontSize: 14, lineHeight: "1.55", color: "#334155" }}>
            {item.description}
          </div>

          {/* Footer hàng dưới: Chips metadata + Action buttons */}
          <Flex justify="space-between" align="center" wrap="wrap" gap={12} style={{ marginTop: 4 }}>
            <Flex align="center" gap={16} wrap="wrap">
              {item.meta?.map((m, idx) => (
                <Space key={idx} size={4} style={{ fontSize: 12.5, color: "#64748b" }}>
                  <m.icon size={14} />
                  <span>{m.text}</span>
                </Space>
              ))}
            </Flex>

            <Flex align="center" gap={8} style={{ marginLeft: "auto" }}>
              {item.primaryAction && (
                <Button
                  type={isRead ? "default" : "primary"}
                  size="small"
                  icon={<Eye size={14} />}
                  onClick={() => navigate(item.primaryAction.link)}
                  style={{
                    borderRadius: 6,
                    fontSize: 12.5,
                    fontWeight: 600,
                    height: 32,
                    background: isRead ? "#ffffff" : "#0f172a",
                    borderColor: isRead ? "#cbd5e1" : "#0f172a",
                  }}
                >
                  {item.primaryAction.label}
                </Button>
              )}

              <Tooltip title={isRead ? "Đánh dấu chưa đọc" : "Đánh dấu đã đọc"}>
                <Button
                  type="text"
                  size="small"
                  icon={isRead ? <Mail size={16} /> : <Check size={16} />}
                  onClick={() => onToggleRead(item.id)}
                  style={{
                    borderRadius: 6,
                    height: 32,
                    width: 32,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: "#94a3b8",
                  }}
                />
              </Tooltip>

              <Tooltip title="Xóa thông báo">
                <Button
                  type="text"
                  danger
                  size="small"
                  icon={<Trash2 size={16} />}
                  onClick={() => onDelete(item.id)}
                  style={{
                    borderRadius: 6,
                    height: 32,
                    width: 32,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                />
              </Tooltip>
            </Flex>
          </Flex>
        </Flex>
      </Flex>
    </Card>
  );
}
