import React from "react";
import { Empty } from "antd";
import { BellOff } from "lucide-react";

export default function NotificationEmptyState() {
  return (
    <div className="notif-empty">
      <Empty
        image={<BellOff size={36} color="var(--text-faint)" />}
        description={
          <div>
            <div className="notif-empty-title">Không có thông báo nào</div>
            <div className="notif-empty-sub">
              Bạn đã xử lý hết tất cả các thông báo trong mục này.
            </div>
          </div>
        }
      />
    </div>
  );
}
