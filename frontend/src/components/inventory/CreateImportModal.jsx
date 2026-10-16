import { useEffect, useState } from "react";
import { Col, Form, Input, Modal, Row, Select } from "antd";
import { nhaCungCapApi } from "../../api/nhaCungCapApi";
import { phieuNhapApi } from "../../api/phieuNhapApi";
import { useAuth } from "../../store/AuthContext";
import { useMatHangOptions } from "../../hooks/useMatHangOptions";
import { KHO_ID } from "../../config/kho";
import { unwrap } from "../../utils/unwrap";
import { notifyError, notifySuccess } from "../../utils/notify";
import PhieuKhoItemsField from "./PhieuKhoItemsField";

const TRANG_THAI_OPTIONS = [
  { value: "cho_xu_ly", label: "Chờ nhập (chưa cộng tồn kho)" },
  { value: "da_hoan_thanh", label: "Hoàn tất nhập (cộng tồn kho ngay)" },
];
const TRANG_THAI_HUY = { value: "da_huy", label: "Hủy phiếu" };

/**
 * Modal tạo / sửa phiếu nhập kho. Khớp TaoPhieuNhapRequest:
 * khoId, nccId, nhanVienTiepNhanId, trangThai, ghiChu, bienSoXe, chiTiet[{matHangId, soLuong, ghiChu}]
 *
 * - Không truyền `record`: chế độ TẠO MỚI.
 * - Truyền `record` (1 dòng của bảng, BE trả kèm chiTiet): chế độ SỬA (chỉ phiếu "cho_xu_ly").
 * Trang cha chỉ render khi cần mở: {open && <CreateImportModal ... />}
 */
export default function CreateImportModal({ record, onCancel, onCreated }) {
  const isEdit = Boolean(record);
  const { user } = useAuth();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const [nccOptions, setNccOptions] = useState([]);
  const [nccLoading, setNccLoading] = useState(true);
  const { options: matHangOptions, loading: matHangLoading } =
    useMatHangOptions();

  useEffect(() => {
    let ignore = false;
    nhaCungCapApi
      .getAll({ page: 0, size: 200 })
      .then(unwrap)
      .then((page) => {
        if (ignore) return;
        const list = page?.content ?? (Array.isArray(page) ? page : []);
        setNccOptions(
          list
            .filter((n) => !n.trangThai || n.trangThai === "dang_hop_tac")
            .map((n) => ({
              value: n.id,
              label: n.maNcc ? `${n.maNcc} - ${n.tenNcc}` : n.tenNcc,
            })),
        );
      })
      .catch((err) => {
        if (!ignore) notifyError(err);
      })
      .finally(() => {
        if (!ignore) setNccLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, []);

  const handleFinish = async (values) => {
    const payload = {
      khoId: KHO_ID,
      nccId: values.nccId,
      nhanVienTiepNhanId: user?.nhanVienId,
      trangThai: values.trangThai,
      ghiChu: values.ghiChu?.trim() || null,
      bienSoXe: values.bienSoXe?.trim() || null,
      chiTiet: values.chiTiet.map((r) => ({
        matHangId: r.matHangId,
        soLuong: r.soLuong,
        ghiChu: r.ghiChu?.trim() || null,
      })),
    };

    setSaving(true);
    try {
      if (isEdit) {
        await phieuNhapApi.capNhat(record.id, payload);
        notifySuccess("Cập nhật phiếu nhập kho thành công");
      } else {
        await phieuNhapApi.tao(payload);
        notifySuccess("Tạo phiếu nhập kho thành công");
      }
      onCreated();
    } catch (err) {
      notifyError(err);
    } finally {
      setSaving(false);
    }
  };

  const initialValues = isEdit
    ? {
        nccId: record.nccId ?? undefined,
        trangThai: record.trangThai,
        bienSoXe: record.bienSoXe ?? undefined,
        ghiChu: record.ghiChu ?? undefined,
        chiTiet: record.chiTiet?.length
          ? record.chiTiet.map((r) => ({
              matHangId: r.matHangId,
              soLuong: r.soLuong,
              ghiChu: r.ghiChu ?? undefined,
            }))
          : [{}],
      }
    : { trangThai: "cho_xu_ly", chiTiet: [{}] };

  return (
    <Modal
      open
      centered
      width={760}
      title={
        isEdit
          ? `Cập nhật phiếu nhập kho ${record.maPhieu}`
          : "Tạo phiếu nhập kho pallet"
      }
      okText={isEdit ? "Lưu thay đổi" : "Xác nhận tạo phiếu nhập"}
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
        onFinish={handleFinish}
        style={{ marginTop: 16 }}
      >
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item label="Mã phiếu nhập">
              <Input
                disabled
                value={record?.maPhieu}
                placeholder="Hệ thống tự tạo khi lưu"
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Nhân viên tiếp nhận">
              <Input
                disabled
                value={
                  (isEdit ? record.tenNhanVienTiepNhan : user?.hoTen) || ""
                }
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              label="Nhà cung cấp"
              name="nccId"
              rules={[
                { required: true, message: "Vui lòng chọn nhà cung cấp" },
              ]}
            >
              <Select
                showSearch
                optionFilterProp="label"
                placeholder="Chọn nhà cung cấp"
                options={nccOptions}
                loading={nccLoading}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              label="Trạng thái phiếu nhập"
              name="trangThai"
              rules={[{ required: true }]}
            >
              <Select
                options={
                  isEdit
                    ? [...TRANG_THAI_OPTIONS, TRANG_THAI_HUY]
                    : TRANG_THAI_OPTIONS
                }
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              label="Biển số xe giao"
              name="bienSoXe"
              rules={[{ max: 20, message: "Tối đa 20 ký tự" }]}
            >
              <Input placeholder="60C-892.41" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Ghi chú" name="ghiChu">
              <Input placeholder="Ghi chú thêm (nếu có)" />
            </Form.Item>
          </Col>
        </Row>

        <PhieuKhoItemsField
          matHangOptions={matHangOptions}
          loading={matHangLoading}
        />
      </Form>
    </Modal>
  );
}
