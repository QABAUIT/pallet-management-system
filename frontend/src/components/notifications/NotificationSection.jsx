import React from "react";
import { Flex, Typography } from "antd";
import NotificationCard from "./NotificationCard";

const { Title, Text } = Typography;

export default function NotificationSection({
  title,
  count,
  subtitle,
  items,
  isReadSection = false,
  onToggleRead,
  onDelete,
}) {
  if (!items || items.length === 0) return null;

  return (
    <Flex vertical gap={12} className="notif-section">
      {/* Section Header */}
      <div className="notif-section-head">
        <div className="notif-section-title-wrap">
          <span
            className={
              "notif-section-dot" + (isReadSection ? " is-read" : " is-unread")
            }
          />
          <Title
            level={5}
            className={
              "notif-section-title" + (isReadSection ? " is-read" : " is-unread")
            }
          >
            {title} ({count})
          </Title>
        </div>

        {subtitle && (
          <Text type="secondary" className="notif-section-sub">
            {subtitle}
          </Text>
        )}
      </div>

      {/* Cards List */}
      <Flex vertical gap={12}>
        {items.map((item) => (
          <NotificationCard
            key={item.id}
            item={item}
            isRead={isReadSection}
            onToggleRead={onToggleRead}
            onDelete={onDelete}
          />
        ))}
      </Flex>
    </Flex>
  );
}
