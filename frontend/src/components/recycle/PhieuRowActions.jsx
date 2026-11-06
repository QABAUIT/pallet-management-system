import { Button, Popconfirm, Space, Tooltip } from "antd";
import {
  CheckOutlined,
  CloseCircleOutlined,
  EditOutlined,
  EyeOutlined,
} from "@ant-design/icons";
import { usePhanQuyen } from "../../hooks/usePhanQuyen";
import { askReason } from "../../utils/askReason";
import { notifyError, notifySuccess } from "../../utils/notify";

/**
 * Cột "Thao tác" chung cho phiếu tái chế / tháo dỡ / xử lý thêm.
 * - Xem: khi có onView. Sửa + Huỷ: chỉ phiếu "nhap". Duyệt: chỉ GD/PGD và phiếu "nhap".
 */
export default function PhieuRowActions({ r, api, onChanged, onView, onEdit }) {
  const { laQuanLy } = usePhanQuyen();
  const laNhap = r.trangThai === "nhap";

  const run = async (fn, okMsg) => {
    try {
      await fn();
      notifySuccess(okMsg);
      onChanged();
    } catch (err) {
      notifyError(err);
    }
  };

  return (
    <Space size="small">
      {onView && (
        <Tooltip title="Xem chi tiết">
          <Button
            type="text"
            icon={<EyeOutlined />}
            style={{ color: "var(--text-muted)" }}
            onClick={() => onView(r)}
          />
        </Tooltip>
      )}

      <Tooltip title={laNhap ? "Chỉnh sửa" : "Chỉ sửa được phiếu đang chờ duyệt"}>
        <Button
          type="text"
          icon={<EditOutlined />}
          style={{ color: "var(--text-muted)" }}
          disabled={!laNhap}
          onClick={() => onEdit(r)}
        />
      </Tooltip>

      {laQuanLy && laNhap && (
        <Popconfirm
          title="Duyệt phiếu này?"
          description="Tồn kho sẽ được cập nhật ngay, không thể hoàn tác."
          okText="Duyệt"
          cancelText="Đóng"
          onConfirm={() => run(() => api.duyet(r.id), "Đã duyệt phiếu")}
        >
          <Tooltip title="Duyệt phiếu">
            <Button type="text" icon={<CheckOutlined />} style={{ color: "var(--color-green)" }} />
          </Tooltip>
        </Popconfirm>
      )}

      {laNhap && (
        <Tooltip title="Huỷ phiếu">
          <Button
            type="text"
            danger
            icon={<CloseCircleOutlined />}
            onClick={() =>
              askReason({
                title: `Huỷ phiếu ${r.maPhieu}?`,
                onOk: (lyDo) => run(() => api.huy(r.id, lyDo), "Đã huỷ phiếu"),
              })
            }
          />
        </Tooltip>
      )}
    </Space>
  );
}
