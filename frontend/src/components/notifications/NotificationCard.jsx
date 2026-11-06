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

  return (
    <Card
      className={"notif-item-card" + (isRead ? " is-read" : " is-unread")}
      styles={{ body: { padding: 16 } }}
    >
      <Flex gap={16} align="flex-start">
        {/* Cột Icon bên trái */}
        <div className="notif-icon-wrap">
          <div className={"notif-icon" + (isRead ? " is-read" : " is-unread")}>
            {renderIcon(item.iconType)}
          </div>
          {!isRead && <span className="notif-unread-dot" />}
        </div>

        {/* Nội dung thông báo */}
        <Flex vertical gap={8} style={{ flex: 1, minWidth: 0 }}>
          {/* Header thông báo: Type pill, mã code, thời gian */}
          <Flex justify="space-between" align="center" wrap="wrap" gap={8}>
            <Flex align="center" gap={8} wrap="wrap">
              <Tag
                className={
                  "status-tag " + (isRead ? "status-amber" : "status-red")
                }
              >
                {item.type}
              </Tag>
              <Text strong className="notif-code">
                {item.code}
              </Text>
              <Text type="secondary" className="notif-subtext">
                · {item.subtext}
              </Text>
            </Flex>

            <Space
              size={4}
              className={"notif-time" + (isRead ? "" : " is-unread")}
            >
              <Clock size={13} />
              <span>{item.time}</span>
            </Space>
          </Flex>

          {/* Mô tả chi tiết */}
          <div className="notif-description">{item.description}</div>

          {/* Footer hàng dưới: Chips metadata + Action buttons */}
          <Flex
            justify="space-between"
            align="center"
            wrap="wrap"
            gap={12}
            style={{ marginTop: 4 }}
          >
            <Flex align="center" gap={16} wrap="wrap">
              {item.meta?.map((m, idx) => (
                <Space key={idx} size={4} className="notif-meta">
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
                  className="notif-open-btn"
                  onClick={() => navigate(item.primaryAction.link)}
                >
                  {item.primaryAction.label}
                </Button>
              )}

              <Tooltip title={isRead ? "Đánh dấu chưa đọc" : "Đánh dấu đã đọc"}>
                <Button
                  type="text"
                  size="small"
                  className="notif-icon-btn"
                  aria-label={isRead ? "Đánh dấu chưa đọc" : "Đánh dấu đã đọc"}
                  icon={isRead ? <Mail size={16} /> : <Check size={16} />}
                  onClick={() => onToggleRead(item.id)}
                />
              </Tooltip>

              <Tooltip title="Xóa thông báo">
                <Button
                  type="text"
                  danger
                  size="small"
                  className="notif-icon-btn"
                  aria-label="Xóa thông báo"
                  icon={<Trash2 size={16} />}
                  onClick={() => onDelete(item.id)}
                />
              </Tooltip>
            </Flex>
          </Flex>
        </Flex>
      </Flex>
    </Card>
  );
}
