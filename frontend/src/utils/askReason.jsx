import { Input, Modal, message } from "antd";

/**
 * Hộp xác nhận có ô nhập lý do. onOk(lyDo) có thể trả Promise.
 * required (mặc định true): bắt buộc nhập lý do, chưa nhập thì giữ nguyên hộp thoại.
 * BE đòi lý do khi huỷ phiếu đóng pallet, từ chối báo giá... nên mặc định là bắt buộc.
 */
export function askReason({
  title,
  okText = "Xác nhận huỷ",
  placeholder = "Lý do huỷ (bắt buộc)",
  required = true,
  onOk,
}) {
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
        placeholder={placeholder}
        style={{ marginTop: 8 }}
        onChange={(e) => {
          value = e.target.value;
        }}
      />
    ),
    onOk: () => {
      const lyDo = value.trim();
      if (required && !lyDo) {
        message.warning("Vui lòng nhập lý do");
        return Promise.reject(new Error("Thiếu lý do"));
      }
      return onOk(lyDo);
    },
  });
}
