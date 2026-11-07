import { useEffect, useState } from "react";
import { Alert, Col, Form, Input, InputNumber, Modal, Row, Select } from "antd";
import { xuLyThemApi } from "../../api/recycleApi";
import { KHO_ID } from "../../config/kho";
import { notifyError, notifySuccess } from "../../utils/notify";
import { LOAI_NGUON, sum } from "./helpers";

const NUM = { min: 0, precision: 0, style: { width: "100%" } };
const NGUON_OPTIONS = Object.entries(LOAI_NGUON).map(([value, label]) => ({ value, label }));

/**
 * Tạo / sửa phiếu xử lý hàng cần xử lý thêm. Khớp TaoPhieuXuLyThemRequest:
 * khoId, loaiNguon (thanh|cuc), soDatLai, soPheLieu, kgPheLieu, lyDo (bắt buộc).
 * Số dùng lại + số bỏ không vượt tồn của nhóm đã chọn.
 */
export default function XuLyThemFormModal({ record, onCancel, onSaved }) {
  const isEdit = Boolean(record);
  const [form] = Form.useForm();
  const [ton, setTon] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let ignore = false;
    xuLyThemApi
      .ton(record?.id)
      .then((d) => !ignore && setTon(d?.dong ?? []))
      .catch((err) => !ignore && notifyError(err));
    return () => {
      ignore = true;
    };
  }, [record?.id]);

  const khoId = isEdit ? record.khoId : KHO_ID;
  const loai = Form.useWatch("loaiNguon", form);
  const datLai = Form.useWatch("soDatLai", form);
  const pheLieu = Form.useWatch("soPheLieu", form);

  const dongTon = (ton ?? []).find((d) => d.loaiNguon === loai && d.khoId === khoId);
  // Khả dụng = tồn - hàng đã giữ - các phiếu nháp khác (BE đã trừ sẵn, trừ chính phiếu đang sửa)
  const toiDa = ton == null
    ? null
    : dongTon
      ? (dongTon.soLuongKhaDung ?? dongTon.soLuongTon ?? 0)
      : 0;
  const tong = sum(datLai, pheLieu);

  const handleFinish = async (v) => {
    const payload = {
      khoId,
      loaiNguon: v.loaiNguon,
      soDatLai: v.soDatLai ?? 0,
      soPheLieu: v.soPheLieu ?? 0,
      kgPheLieu: v.kgPheLieu ?? 0,
      lyDo: v.lyDo.trim(),
    };
    if (payload.soDatLai + payload.soPheLieu <= 0) {
      notifyError("Cần nhập số chiếc dùng lại hoặc số chiếc bỏ");
      return;
    }
    if (toiDa != null && payload.soDatLai + payload.soPheLieu > toiDa) {
      notifyError(`Nhóm này chỉ còn ${toiDa} chiếc khả dụng (đã trừ phiếu nháp khác)`);
      return;
    }
    setSaving(true);
    try {
      if (isEdit) {
        await xuLyThemApi.capNhat(record.id, payload);
        notifySuccess("Cập nhật phiếu xử lý thành công");
      } else {
        await xuLyThemApi.tao(payload);
        notifySuccess("Tạo phiếu xử lý thành công");
      }
      onSaved();
    } catch (err) {
      notifyError(err);
    } finally {
      setSaving(false);
    }
  };

  const initialValues = isEdit
    ? {
        loaiNguon: record.loaiNguon,
        soDatLai: record.soDatLai,
        soPheLieu: record.soPheLieu,
        kgPheLieu: record.kgPheLieu,
        lyDo: record.lyDo,
      }
    : { soDatLai: 0, soPheLieu: 0, kgPheLieu: 0 };

  return (
    <Modal
      open
      centered
      width={620}
      title={isEdit ? `Cập nhật phiếu ${record.maPhieu}` : "Xử lý hàng cần xử lý thêm"}
      okText={isEdit ? "Lưu thay đổi" : "Tạo phiếu xử lý"}
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
        <Form.Item
          label="Nhóm cần xử lý"
          name="loaiNguon"
          rules={[{ required: true, message: "Chọn nhóm cần xử lý" }]}
        >
          <Select options={NGUON_OPTIONS} placeholder="Thanh hay cục" disabled={isEdit} />
        </Form.Item>

        {loai && (
          <Alert
            style={{ marginBottom: 16 }}
            type={toiDa === 0 ? "warning" : "info"}
            showIcon
            message={
              ton == null
                ? "Đang tải tồn..."
                : `Khả dụng: ${toiDa ?? 0} chiếc (tồn ${dongTon?.soLuongTon ?? 0}, phiếu nháp khác giữ ${dongTon?.soLuongDangCho ?? 0}). Đã chọn xử lý: ${tong}.`
            }
          />
        )}

        <Row gutter={16}>
          <Col span={8}>
            <Form.Item label="Dùng lại được (chiếc)" name="soDatLai">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item label="Bỏ đi (chiếc)" name="soPheLieu">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item label="Phế liệu (kg)" name="kgPheLieu">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item
          label="Lý do xử lý"
          name="lyDo"
          rules={[
            { required: true, whitespace: true, message: "Bắt buộc nhập lý do" },
            { max: 255, message: "Tối đa 255 ký tự" },
          ]}
        >
          <Input.TextArea rows={3} placeholder="Ví dụ: cắt lại thanh còn dùng được, phần nứt bỏ" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
