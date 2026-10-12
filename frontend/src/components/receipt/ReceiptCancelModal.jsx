import { useEffect, useState } from "react";
import { Input, Modal } from "antd";
import "../layout/style.css";

export default function ReceiptCancelModal({ open, ma, loading, onOk, onCancel }) {
  const [lyDo, setLyDo] = useState("");

  useEffect(() => {
    if (open) setLyDo("");
  }, [open]);

  return (
    <Modal
      title={`Xóa hóa đơn ${ma}`}
      open={open}
      onCancel={onCancel}
      confirmLoading={loading}
      okText="Xác nhận xóa"
      cancelText="Đóng"
      okButtonProps={{ danger: true, disabled: !lyDo.trim() }}
      onOk={() => onOk(lyDo.trim())}
    >
      <p className="muted-text">
        Hóa đơn chuyển sang "Đã huỷ" (xóa mềm): vẫn lưu lịch sử, tồn kho đã giữ chỗ được trả lại.
        Hóa đơn đã thanh toán không xóa được.
      </p>
      <span className="rc-label">Lý do xóa</span>
      <Input.TextArea rows={3} value={lyDo} onChange={(e) => setLyDo(e.target.value)} maxLength={255} />
    </Modal>
  );
}