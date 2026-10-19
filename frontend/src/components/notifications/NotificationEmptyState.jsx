import React from "react";
import { Empty } from "antd";
import { BellOff } from "lucide-react";

export default function NotificationEmptyState() {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        padding: "48px 24px",
        background: "#ffffff",
        border: "1px dashed #cbd5e1",
        borderRadius: 12,
        margin: "16px 0",
      }}
    >
      <Empty
        image={<BellOff size={36} color="#94a3b8" />}
        description={
          <div>
            <div style={{ fontWeight: 600, color: "#334155", fontSize: 15, marginBottom: 4 }}>
              Không có thông báo nào
            </div>
            <div style={{ color: "#64748b", fontSize: 13 }}>
              Bạn đã xử lý hết tất cả các thông báo trong mục này.
            </div>
          </div>
        }
      />
    </div>
  );
}
