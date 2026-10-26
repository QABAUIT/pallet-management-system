import { useEffect, useState } from "react";
import { Alert, Col, Form, Input, InputNumber, Modal, Row, Select } from "antd";
import { taiCheApi } from "../../api/recycleApi";
import { notifyError, notifySuccess } from "../../utils/notify";
import { sum } from "./helpers";

const NUM = { min: 0, precision: 0, style: { width: "100%" } };

/**
 * Tạo / sửa phiếu phân loại lô cũ. Khớp TaoPhieuTaiCheRequest:
 * loHangId, soLuongXuLy, soBanLai, soThaoDo, soLoaiBo, kgPheLieuLoaiBo, ghiChu.
 * Quy tắc: bán lại + tháo dỡ + loại bỏ = số pallet xử lý, và không vượt số khả dụng của lô.
 */
export default function PhanLoaiFormModal({ record, onCancel, onSaved }) {
  const isEdit = Boolean(record);
  const [form] = Form.useForm();
  const [dm, setDm] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let ignore = false;
    taiCheApi
      .danhMuc()
      .then((d) => !ignore && setDm(d))
      .catch((err) => !ignore && notifyError(err));
    return () => {
      ignore = true;
    };
  }, []);

  const loId = Form.useWatch("loHangId", form);
  const xuLy = Form.useWatch("soLuongXuLy", form);
  const banLai = Form.useWatch("soBanLai", form);
  const thaoDo = Form.useWatch("soThaoDo", form);
  const loaiBo = Form.useWatch("soLoaiBo", form);

  const dsLo = dm?.loHang ?? [];
  const lo = dsLo.find((l) => l.id === loId);
  // Khi sửa: số pallet của chính phiếu này đang được tính vào "đang chờ" nên cộng lại
  const khaDung = lo
    ? lo.soLuongKhaDung + (isEdit && record.loHangId === loId ? record.soLuongXuLy : 0)
    : null;
  const tong = sum(banLai, thaoDo, loaiBo);

  const loOptions = dsLo.map((l) => ({
    value: l.id,
    label: `${l.maLo} - ${l.tenMatHang} (khả dụng ${l.soLuongKhaDung})`,
  }));
  if (isEdit && !dsLo.some((l) => l.id === record.loHangId)) {
    loOptions.push({ value: record.loHangId, label: `${record.maLo} - ${record.tenMatHang}` });
  }

  const handleFinish = async (v) => {
    const soXuLy = Number(v.soLuongXuLy) || 0;
    if (sum(v.soBanLai, v.soThaoDo, v.soLoaiBo) !== soXuLy) {
      notifyError("Bán lại + tháo dỡ + loại bỏ phải đúng bằng số pallet xử lý");
      return;
    }
    if (khaDung != null && soXuLy > khaDung) {
      notifyError(`Lô này chỉ còn ${khaDung} pallet khả dụng`);
      return;
    }
    const payload = {
      loHangId: v.loHangId,
      soLuongXuLy: soXuLy,
      soBanLai: v.soBanLai ?? 0,
      soThaoDo: v.soThaoDo ?? 0,
      soLoaiBo: v.soLoaiBo ?? 0,
      kgPheLieuLoaiBo: v.kgPheLieuLoaiBo ?? 0,
      ghiChu: v.ghiChu?.trim() || null,
    };
    setSaving(true);
    try {
      if (isEdit) {
        await taiCheApi.capNhat(record.id, payload);
        notifySuccess("Cập nhật phiếu phân loại thành công");
      } else {
        await taiCheApi.tao(payload);
        notifySuccess("Tạo phiếu phân loại thành công");
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
        loHangId: record.loHangId,
        soLuongXuLy: record.soLuongXuLy,
        soBanLai: record.soBanLai,
        soThaoDo: record.soThaoDo,
        soLoaiBo: record.soLoaiBo,
        kgPheLieuLoaiBo: record.kgPheLieuLoaiBo,
        ghiChu: record.ghiChu ?? undefined,
      }
    : { soBanLai: 0, soThaoDo: 0, soLoaiBo: 0, kgPheLieuLoaiBo: 0 };

  const lech = xuLy != null && tong !== Number(xuLy);

  return (
    <Modal
      open
      centered
      width={680}
      title={isEdit ? `Cập nhật phiếu ${record.maPhieu}` : "Phân loại lô pallet cũ"}
      okText={isEdit ? "Lưu thay đổi" : "Tạo phiếu phân loại"}
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
          label="Lô hàng"
          name="loHangId"
          rules={[{ required: true, message: "Chọn lô hàng" }]}
        >
          <Select
            showSearch
            optionFilterProp="label"
            placeholder="Chọn lô pallet cũ cần phân loại"
            options={loOptions}
            loading={!dm}
          />
        </Form.Item>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              label={`Số pallet xử lý${khaDung != null ? ` (tối đa ${khaDung})` : ""}`}
              name="soLuongXuLy"
              rules={[{ required: true, message: "Nhập số pallet" }]}
            >
              <InputNumber {...NUM} min={1} max={khaDung ?? undefined} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Bán lại (đạt chuẩn)" name="soBanLai" rules={[{ required: true }]}>
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Chuyển tháo dỡ" name="soThaoDo" rules={[{ required: true }]}>
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Loại bỏ (phế liệu)" name="soLoaiBo" rules={[{ required: true }]}>
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="Khối lượng phế liệu loại bỏ (kg)" name="kgPheLieuLoaiBo">
              <InputNumber {...NUM} />
            </Form.Item>
          </Col>
        </Row>

        {xuLy != null && (
          <Alert
            style={{ marginBottom: 16 }}
            type={lech ? "warning" : "success"}
            showIcon
            message={`Đã phân ${tong} / ${xuLy} pallet${lech ? " - chưa khớp" : " - khớp"}`}
          />
        )}

        <Form.Item label="Ghi chú" name="ghiChu" rules={[{ max: 255, message: "Tối đa 255 ký tự" }]}>
          <Input.TextArea rows={2} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
