import { useEffect, useState } from "react";
import { Col, Form, Input, InputNumber, Modal, Row, Select } from "antd";
import { thaoDoApi } from "../../api/recycleApi";
import { notifyError, notifySuccess } from "../../utils/notify";

const NUM = { min: 0, precision: 0, style: { width: "100%" } };

/**
 * Tạo / sửa 1 đợt tháo dỡ. Khớp TaoPhieuThaoDoRequest:
 * phieuTaiCheId, soPalletThaoDo, soThanhNguyen, soCucVuong, soThanhCanXuLy, soCucCanXuLy, kgPheLieu, ghiChu.
 * Phải có ít nhất 1 loại kết quả (BE kiểm tra thêm); số pallet không vượt phần còn có thể tháo.
 */
export default function ThaoDoFormModal({ record, onCancel, onSaved }) {
  const isEdit = Boolean(record);
  const [form] = Form.useForm();
  const [dm, setDm] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let ignore = false;
    thaoDoApi
      .danhMuc()
      .then((d) => !ignore && setDm(d))
      .catch((err) => !ignore && notifyError(err));
    return () => {
      ignore = true;
    };
  }, []);

  const phieuId = Form.useWatch("phieuTaiCheId", form);
  const dsPhieu = dm?.phieuTaiChe ?? [];
  const phieu = dsPhieu.find((p) => p.id === phieuId);
  const khaDung = phieu
    ? phieu.soKhaDung + (isEdit && record.phieuTaiCheId === phieuId ? record.soPalletThaoDo : 0)
    : null;

  const options = dsPhieu.map((p) => ({
    value: p.id,
    label: `${p.maPhieu} - lô ${p.maLo} (còn ${p.soKhaDung}/${p.soCanThaoDo} pallet)`,
  }));
  if (isEdit && !dsPhieu.some((p) => p.id === record.phieuTaiCheId)) {
    options.push({ value: record.phieuTaiCheId, label: `${record.maPhieuTaiChe} - lô ${record.maLo}` });
  }

  const handleFinish = async (v) => {
    const payload = {
      phieuTaiCheId: v.phieuTaiCheId,
      soPalletThaoDo: v.soPalletThaoDo,
      soThanhNguyen: v.soThanhNguyen ?? 0,
      soCucVuong: v.soCucVuong ?? 0,
      soThanhCanXuLy: v.soThanhCanXuLy ?? 0,
      soCucCanXuLy: v.soCucCanXuLy ?? 0,
      kgPheLieu: v.kgPheLieu ?? 0,
      ghiChu: v.ghiChu?.trim() || null,
    };
    const coKetQua =
      payload.soThanhNguyen + payload.soCucVuong + payload.soThanhCanXuLy +
        payload.soCucCanXuLy + payload.kgPheLieu > 0;
    if (!coKetQua) {
      notifyError("Cần nhập ít nhất một loại kết quả tháo dỡ");
      return;
    }
    setSaving(true);
    try {
      if (isEdit) {
        await thaoDoApi.capNhat(record.id, payload);
        notifySuccess("Cập nhật đợt tháo dỡ thành công");
      } else {
        await thaoDoApi.tao(payload);
        notifySuccess("Tạo đợt tháo dỡ thành công");
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
        phieuTaiCheId: record.phieuTaiCheId,
        soPalletThaoDo: record.soPalletThaoDo,
        soThanhNguyen: record.soThanhNguyen,
        soCucVuong: record.soCucVuong,
        soThanhCanXuLy: record.soThanhCanXuLy,
        soCucCanXuLy: record.soCucCanXuLy,
        kgPheLieu: record.kgPheLieu,
        ghiChu: record.ghiChu ?? undefined,
      }
    : { soThanhNguyen: 0, soCucVuong: 0, soThanhCanXuLy: 0, soCucCanXuLy: 0, kgPheLieu: 0 };

  return (
    <Modal
      open
      centered
      width={680}
      title={isEdit ? `Cập nhật đợt tháo dỡ ${record.maPhieu}` : "Tạo đợt tháo dỡ pallet"}
      okText={isEdit ? "Lưu thay đổi" : "Tạo đợt tháo dỡ"}
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
          label="Phiếu phân loại (nhóm tháo dỡ)"
          name="phieuTaiCheId"
          rules={[{ required: true, message: "Chọn phiếu phân loại" }]}
        >
          <Select
            showSearch
            optionFilterProp="label"
            placeholder="Chọn phiếu đã duyệt còn pallet cần tháo"
            options={options}
            loading={!dm}
            disabled={isEdit}
          />
        </Form.Item>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              label={`Số pallet tháo dỡ${khaDung != null ? ` (tối đa ${khaDung})` : ""}`}
              name="soPalletThaoDo"
              rules={[{ required: true, message: "Nhập số pallet" }]}
            >
              <InputNumber {...NUM} min={1} max={khaDung ?? undefined} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Phế liệu thu được (kg)" name="kgPheLieu">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Thanh nguyên (đạt)" name="soThanhNguyen">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Cục vuông (đạt)" name="soCucVuong">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Thanh cần xử lý thêm" name="soThanhCanXuLy">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Cục cần xử lý thêm" name="soCucCanXuLy">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item label="Ghi chú" name="ghiChu" rules={[{ max: 255, message: "Tối đa 255 ký tự" }]}>
          <Input.TextArea rows={2} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
