import { useEffect, useState } from "react";
import { Button, Col, Form, Input, InputNumber, Modal, Row, Select, Spin } from "antd";
import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import { baoGiaApi } from "../../api/banHangApi";
import { notifyError, notifySuccess } from "../../utils/notify";
import { formatMoney } from "../../utils/receiptFormat";
import { DEFAULT_HIEU_LUC, DEFAULT_VAT } from "./baoGia";
import "../layout/style.css";

const LINE = {
  display: "grid",
  gridTemplateColumns: "minmax(0, 2fr) 90px 130px 90px 110px 32px",
  gap: 8,
  alignItems: "start",
};
const money = {
  formatter: (v) => `${v ?? ""}`.replace(/\B(?=(\d{3})+(?!\d))/g, "."),
  parser: (v) => v?.replace(/\./g, ""),
};

const num = (v) => Number(v) || 0;
const lineTotal = (r) => {
  const gross = num(r?.soLuong) * num(r?.donGia);
  return gross - Math.round((gross * num(r?.tyLeChietKhau)) / 100);
};

/**
 * Tạo / sửa báo giá (chỉ báo giá nháp). Khớp TaoBaoGiaRequest:
 * khachHangId, tyLeVat, tyLeGiamGiaTong, soNgayHieuLuc, ghiChu, chiTiet[{matHangId, soLuong, donGia, tyLeChietKhau}].
 * Báo giá không giữ tồn kho; giá chốt theo số đã nhập ở đây.
 */
export default function BaoGiaFormModal({ id, onCancel, onSaved }) {
  const isEdit = Boolean(id);
  const [form] = Form.useForm();
  const [dm, setDm] = useState(null);
  const [detail, setDetail] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let ignore = false;
    baoGiaApi
      .danhMuc()
      .then((d) => !ignore && setDm(d))
      .catch((err) => !ignore && notifyError(err));
    if (id) {
      baoGiaApi
        .chiTiet(id)
        .then((d) => !ignore && setDetail(d))
        .catch((err) => !ignore && notifyError(err));
    }
    return () => {
      ignore = true;
    };
  }, [id]);

  const ready = dm && (!isEdit || detail);
  const lines = Form.useWatch("chiTiet", form) ?? [];
  const vat = Form.useWatch("tyLeVat", form);
  const giamTong = Form.useWatch("tyLeGiamGiaTong", form);

  const tongHang = lines.reduce((s, r) => s + lineTotal(r), 0);
  const giam = Math.round((tongHang * num(giamTong)) / 100);
  const truocThue = tongHang - giam;
  const tong = truocThue + Math.round((truocThue * num(vat)) / 100);

  const matHangOptions = (dm?.matHang ?? []).map((m) => ({
    value: m.id,
    label: `${m.tenMatHang} (${m.maMatHang})`,
  }));

  const chonMatHang = (name, matHangId) => {
    const mh = dm.matHang.find((m) => m.id === matHangId);
    form.setFieldValue(["chiTiet", name, "donGia"], mh ? Number(mh.donGia) : undefined);
    if (!form.getFieldValue(["chiTiet", name, "soLuong"])) form.setFieldValue(["chiTiet", name, "soLuong"], 1);
  };

  const handleFinish = async (v) => {
    const ids = v.chiTiet.map((r) => r.matHangId);
    if (new Set(ids).size !== ids.length) {
      notifyError("Mỗi mặt hàng chỉ được xuất hiện một lần");
      return;
    }
    const payload = {
      khachHangId: v.khachHangId,
      tyLeVat: v.tyLeVat ?? null,
      tyLeGiamGiaTong: v.tyLeGiamGiaTong ?? null,
      soNgayHieuLuc: v.soNgayHieuLuc ?? null,
      ghiChu: v.ghiChu?.trim() || null,
      chiTiet: v.chiTiet.map((r) => ({
        matHangId: r.matHangId,
        soLuong: r.soLuong,
        donGia: r.donGia ?? null,
        tyLeChietKhau: r.tyLeChietKhau ?? 0,
      })),
    };
    setSaving(true);
    try {
      if (isEdit) {
        await baoGiaApi.capNhat(id, payload);
        notifySuccess("Cập nhật báo giá thành công");
      } else {
        await baoGiaApi.tao(payload);
        notifySuccess("Tạo báo giá thành công");
      }
      onSaved();
    } catch (err) {
      notifyError(err);
    } finally {
      setSaving(false);
    }
  };

  // Khi sửa: BE trả số tiền giảm và ngày, nên suy ngược tỷ lệ giảm và số ngày hiệu lực
  const initialValues = isEdit
    ? {
        khachHangId: detail?.khachHangId,
        tyLeVat: Number(detail?.tyLeVat ?? DEFAULT_VAT),
        tyLeGiamGiaTong:
          Number(detail?.tongTienHang) > 0
            ? Math.round((Number(detail.tienGiamGiaTong) / Number(detail.tongTienHang)) * 10000) / 100
            : 0,
        soNgayHieuLuc:
          detail?.hanHieuLuc && detail?.ngayTao
            ? Math.max(1, dayjs(detail.hanHieuLuc).diff(dayjs(detail.ngayTao), "day"))
            : DEFAULT_HIEU_LUC,
        ghiChu: detail?.ghiChu ?? undefined,
        chiTiet: (detail?.chiTiet ?? []).map((d) => ({
          matHangId: d.matHangId,
          soLuong: d.soLuong,
          donGia: Number(d.donGia),
          tyLeChietKhau: Number(d.tyLeChietKhau ?? 0),
        })),
      }
    : { tyLeVat: DEFAULT_VAT, tyLeGiamGiaTong: 0, soNgayHieuLuc: DEFAULT_HIEU_LUC, chiTiet: [{}] };

  return (
    <Modal
      open
      centered
      width={880}
      title={isEdit ? `Cập nhật báo giá ${detail?.maBaoGia ?? ""}` : "Tạo báo giá"}
      okText={isEdit ? "Lưu thay đổi" : "Tạo báo giá"}
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={() => ready && form.submit()}
    >
      {!ready ? (
        <div style={{ textAlign: "center", padding: 48 }}>
          <Spin />
        </div>
      ) : (
        <Form
          form={form}
          layout="vertical"
          initialValues={initialValues}
          onFinish={handleFinish}
          style={{ marginTop: 16 }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Khách hàng" name="khachHangId" rules={[{ required: true, message: "Chọn khách hàng" }]}>
                <Select
                  showSearch
                  optionFilterProp="label"
                  placeholder="Chọn khách hàng"
                  options={dm.khachHang.map((k) => ({ value: k.id, label: `${k.maKh} - ${k.tenKh}` }))}
                />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item label="VAT (%)" name="tyLeVat">
                <InputNumber min={0} max={100} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item label="Giảm giá (%)" name="tyLeGiamGiaTong">
                <InputNumber min={0} max={100} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item label="Hiệu lực (ngày)" name="soNgayHieuLuc">
                <InputNumber min={1} precision={0} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
          </Row>

          <div style={{ fontWeight: 600, margin: "4px 0 8px" }}>Mặt hàng báo giá</div>
          <Form.List
            name="chiTiet"
            rules={[
              {
                validator: async (_, value) => {
                  if (!value || value.length === 0) throw new Error("Cần ít nhất 1 mặt hàng");
                },
              },
            ]}
          >
            {(fields, { add, remove }, { errors }) => (
              <>
                {fields.map(({ key, name, ...rest }) => (
                  <div key={key} style={LINE}>
                    <Form.Item
                      {...rest}
                      name={[name, "matHangId"]}
                      rules={[{ required: true, message: "Chọn mặt hàng" }]}
                      style={{ marginBottom: 12 }}
                    >
                      <Select
                        showSearch
                        optionFilterProp="label"
                        placeholder="Mặt hàng"
                        options={matHangOptions}
                        onChange={(v) => chonMatHang(name, v)}
                      />
                    </Form.Item>
                    <Form.Item
                      {...rest}
                      name={[name, "soLuong"]}
                      rules={[{ required: true, message: "Nhập SL" }]}
                      style={{ marginBottom: 12 }}
                    >
                      <InputNumber min={1} precision={0} placeholder="SL" style={{ width: "100%" }} />
                    </Form.Item>
                    <Form.Item
                      {...rest}
                      name={[name, "donGia"]}
                      rules={[{ required: true, message: "Nhập đơn giá" }]}
                      style={{ marginBottom: 12 }}
                    >
                      <InputNumber min={0} precision={0} placeholder="Đơn giá" style={{ width: "100%" }} {...money} />
                    </Form.Item>
                    <Form.Item {...rest} name={[name, "tyLeChietKhau"]} style={{ marginBottom: 12 }}>
                      <InputNumber min={0} max={100} placeholder="CK %" style={{ width: "100%" }} />
                    </Form.Item>
                    <div style={{ paddingTop: 5, textAlign: "right", fontWeight: 600 }}>
                      {formatMoney(lineTotal(lines[name]))} đ
                    </div>
                    <Button
                      type="text"
                      danger
                      icon={<DeleteOutlined />}
                      disabled={fields.length === 1}
                      onClick={() => remove(name)}
                    />
                  </div>
                ))}
                <Button type="dashed" icon={<PlusOutlined />} onClick={() => add()} block>
                  Thêm mặt hàng
                </Button>
                <Form.ErrorList errors={errors} />
              </>
            )}
          </Form.List>

          <Form.Item label="Ghi chú" name="ghiChu" style={{ marginTop: 16 }} rules={[{ max: 255, message: "Tối đa 255 ký tự" }]}>
            <Input.TextArea rows={2} />
          </Form.Item>

          <div className="rc-ledger">
            <div className="rc-ledger-row"><span>Tiền hàng (sau chiết khấu dòng)</span><b>{formatMoney(tongHang)} đ</b></div>
            {giam > 0 && <div className="rc-ledger-row"><span>Giảm giá ({num(giamTong)}%)</span><b>-{formatMoney(giam)} đ</b></div>}
            <div className="rc-ledger-row"><span>Thuế VAT ({num(vat)}%)</span><b>+{formatMoney(tong - truocThue)} đ</b></div>
            <div className="rc-ledger-total"><strong>Tổng cộng (ước tính)</strong><span>{formatMoney(tong)} đ</span></div>
          </div>
        </Form>
      )}
    </Modal>
  );
}
