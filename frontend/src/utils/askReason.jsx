import { Input, Modal } from "antd";

/** Hộp xác nhận có ô nhập lý do (dùng cho nút Huỷ). onOk(lyDo) có thể trả Promise. */
export function askReason({ title, okText = "Xác nhận huỷ", onOk }) {
  let value = "";
  Modal.confirm({
    title,
    centered: true,
    okText,
    cancelText: "Đóng",
    okButtonProps: { danger: true },
    content: (
      <Input.TextArea
        rows={3}
        maxLength={255}
        placeholder="Lý do huỷ (không bắt buộc)"
        style={{ marginTop: 8 }}
        onChange={(e) => {
          value = e.target.value;
        }}
      />
    ),
    onOk: () => onOk(value.trim()),
  });
}
