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

  const dotColor = isReadSection ? "#f59e0b" : "#dc2626";
  const titleColor = isReadSection ? "#d97706" : "#dc2626";

  return (
    <Flex vertical gap={12} style={{ marginBottom: 20 }}>
      {/* Section Header */}
      <Flex justify="space-between" align="center" style={{ padding: "0 4px" }}>
        <Flex align="center" gap={8}>
          <span
            style={{
              width: 8,
              height: 8,
              borderRadius: "50%",
              background: dotColor,
              display: "inline-block",
              boxShadow: isReadSection ? "none" : "0 0 0 3px rgba(220, 38, 38, 0.2)",
            }}
          />
          <Title
            level={5}
            style={{
              margin: 0,
              fontSize: 13,
              fontWeight: 700,
              letterSpacing: "0.04em",
              color: titleColor,
              textTransform: "uppercase",
            }}
          >
            {title} ({count})
          </Title>
        </Flex>

        {subtitle && (
          <Text type="secondary" style={{ fontSize: 12, fontStyle: "italic" }}>
            {subtitle}
          </Text>
        )}
      </Flex>

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
