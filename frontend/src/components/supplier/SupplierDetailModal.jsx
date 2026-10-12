import { useEffect, useState } from "react";
import { Button, Descriptions, Modal, Spin } from "antd";
import dayjs from "dayjs";
import { nhaCungCapApi } from "../../api/nhaCungCapApi";
import { notifyError } from "../../utils/notify";

const TRANG_THAI_LABEL = {
  dang_hop_tac: "Đang hợp tác",
  ngung_hop_tac: "Ngừng hợp tác",
};

/**
 * Modal xem chi tiết 1 nhà cung cấp (chỉ đọc).
 * Props:
 * - id: id nhà cung cấp cần xem
 * - onClose(): đóng modal
 * - onEdit(record): bấm "Chỉnh sửa" -> trang cha đóng modal này và mở form sửa
 */
export default function SupplierDetailModal({ id, onClose, onEdit }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    nhaCungCapApi
      .getById(id)
      .then((res) => {
        if (!ignore) setData(res.data); // res = ApiResponse, res.data = NhaCungCapResponse
      })
      .catch((err) => {
        if (!ignore) {
          notifyError(err);
          onClose();
        }
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const show = (v) => (v ? v : "—");

  return (
    <Modal
      open
      title="Chi tiết nhà cung cấp"
      width={680}
      onCancel={onClose}
      footer={[
        <Button key="close" onClick={onClose}>
          Đóng
        </Button>,
        <Button
          key="edit"
          type="primary"
          disabled={!data}
          onClick={() => onEdit(data)}
        >
          Chỉnh sửa
        </Button>,
      ]}
    >
      {loading || !data ? (
        <div style={{ textAlign: "center", padding: 32 }}>
          <Spin />
        </div>
      ) : (
        <Descriptions
          bordered
          size="small"
          column={2}
          style={{ marginTop: 16 }}
          labelStyle={{ width: 130 }}
        >
          <Descriptions.Item label="Mã NCC">
            {show(data.maNcc)}
          </Descriptions.Item>
          <Descriptions.Item label="Trạng thái">
            {TRANG_THAI_LABEL[data.trangThai] || show(data.trangThai)}
          </Descriptions.Item>
          <Descriptions.Item label="Tên NCC" span={2}>
            {show(data.tenNcc)}
          </Descriptions.Item>
          <Descriptions.Item label="Mã số thuế">
            {show(data.mst)}
          </Descriptions.Item>
          <Descriptions.Item label="Nhóm hàng">
            {show(data.nhomHang)}
          </Descriptions.Item>
          <Descriptions.Item label="Số điện thoại">
            {show(data.sdt)}
          </Descriptions.Item>
          <Descriptions.Item label="Email">
            {show(data.email)}
          </Descriptions.Item>
          <Descriptions.Item label="Người liên hệ">
            {show(data.nguoiLienHe)}
          </Descriptions.Item>
          <Descriptions.Item label="Ngày tạo">
            {data.createdAt
              ? dayjs(data.createdAt).format("DD/MM/YYYY HH:mm")
              : "—"}
          </Descriptions.Item>
          <Descriptions.Item label="Địa chỉ" span={2}>
            {show(data.diaChi)}
          </Descriptions.Item>
          <Descriptions.Item label="Ghi chú" span={2}>
            {show(data.ghiChu)}
          </Descriptions.Item>
        </Descriptions>
      )}
    </Modal>
  );
}
