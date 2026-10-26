import { useEffect, useState } from "react";
import {
  Button, Col, DatePicker, Form, Input, InputNumber, Modal, Row, Select, Spin,
} from "antd";
import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import { donMuaApi } from "../../api/recycleApi";
import { fmtVnd } from "../../utils/format";
import { notifyError, notifySuccess } from "../../utils/notify";

const LINE = {
  display: "grid",
  gridTemplateColumns: "minmax(0, 2fr) 100px 130px 120px 32px",
  gap: 8,
  alignItems: "start",
};

/**
 * Tạo / sửa đơn mua (chỉ đơn ở trạng thái nháp). Khớp TaoDonMuaRequest:
 * nccId, khoId, ngayDuKienNhan, ghiChu, chiTiet[{matHangId, soLuong, donGia}].
 * Khi sửa, nhận `id` để tự tải chi tiết đơn rồi mới dựng form.
 */
export default function DonMuaFormModal({ id, onCancel, onSaved }) {
  const isEdit = Boolean(id);
  const [form] = Form.useForm();
  const [dm, setDm] = useState(null);
  const [detail, setDetail] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let ignore = false;
    donMuaApi
      .danhMuc()
      .then((d) => !ignore && setDm(d))
      .catch((err) => !ignore && notifyError(err));
    if (id) {
      donMuaApi
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
  const tongTien = lines.reduce(
    (a, r) => a + (Number(r?.soLuong) || 0) * (Number(r?.donGia) || 0),
    0,
  );

  const handleFinish = async (v) => {
    const payload = {
      nccId: v.nccId,
      khoId: v.khoId,
      ngayDuKienNhan: v.ngayDuKienNhan ? v.ngayDuKienNhan.format("YYYY-MM-DD") : null,
      ghiChu: v.ghiChu?.trim() || null,
      chiTiet: v.chiTiet.map((r) => ({
        matHangId: r.matHangId,
        soLuong: r.soLuong,
        donGia: r.donGia,
      })),
    };
    setSaving(true);
    try {
      if (isEdit) {
        await donMuaApi.capNhat(id, payload);
        notifySuccess("Cập nhật đơn mua thành công");
      } else {
        await donMuaApi.tao(payload);
        notifySuccess("Tạo đơn mua thành công");
      }
      onSaved();
    } catch (err) {
      notifyError(err);
    } finally {
      setSaving(false);
    }
  };

  const matHangOptions = (dm?.matHang ?? []).map((m) => ({
    value: m.id,
    label: `${m.maMatHang} - ${m.tenMatHang}${m.donViTinh ? ` (${m.donViTinh})` : ""}`,
  }));

  const initialValues = isEdit
    ? {
        nccId: detail?.nhaCungCap?.id,
        khoId: detail?.khoId,
        ngayDuKienNhan: detail?.ngayDuKienNhan ? dayjs(detail.ngayDuKienNhan) : undefined,
        ghiChu: detail?.ghiChu ?? undefined,
        chiTiet: (detail?.chiTiet ?? []).map((d) => ({
          matHangId: d.matHangId,
          soLuong: d.soLuongDat,
          donGia: d.donGia,
        })),
      }
    : { khoId: dm?.khoMacDinhId, chiTiet: [{}] };

  return (
    <Modal
      open
      centered
      width={820}
      title={isEdit ? `Cập nhật đơn mua ${detail?.maDonMua ?? ""}` : "Tạo đơn mua"}
      okText={isEdit ? "Lưu thay đổi" : "Tạo đơn mua"}
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
              <Form.Item
                label="Nhà cung cấp"
                name="nccId"
                rules={[{ required: true, message: "Chọn nhà cung cấp" }]}
              >
                <Select
                  showSearch
                  optionFilterProp="label"
                  placeholder="Chọn nhà cung cấp"
                  options={dm.nhaCungCap.map((n) => ({
                    value: n.id,
                    label: `${n.maNcc} - ${n.tenNcc}`,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Kho nhận hàng" name="khoId" rules={[{ required: true, message: "Chọn kho" }]}>
                <Select
                  options={dm.kho.map((k) => ({ value: k.id, label: `${k.maKho} - ${k.tenKho}` }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Ngày dự kiến nhận" name="ngayDuKienNhan">
                <DatePicker format="DD/MM/YYYY" style={{ width: "100%" }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Ghi chú" name="ghiChu" rules={[{ max: 255, message: "Tối đa 255 ký tự" }]}>
                <Input placeholder="Ghi chú thêm (nếu có)" />
              </Form.Item>
            </Col>
          </Row>

          <div style={{ fontWeight: 600, margin: "4px 0 8px" }}>Dòng hàng</div>
          <Form.List
            name="chiTiet"
            rules={[
              {
                validator: async (_, value) => {
                  if (!value || value.length === 0) throw new Error("Cần ít nhất 1 dòng hàng");
                },
              },
            ]}
          >
            {(fields, { add, remove }, { errors }) => (
              <>
                {fields.map(({ key, name, ...rest }) => {
                  const r = lines[name];
                  const thanhTien = (Number(r?.soLuong) || 0) * (Number(r?.donGia) || 0);
                  return (
                    <div key={key} style={LINE}>
                      <Form.Item
                        {...rest}
                        name={[name, "matHangId"]}
                        rules={[{ required: true, message: "Chọn mặt hàng" }]}
                        style={{ marginBottom: 12 }}
                      >
                        <Select showSearch optionFilterProp="label" placeholder="Mặt hàng" options={matHangOptions} />
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
                        <InputNumber
                          min={0}
                          precision={0}
                          placeholder="Đơn giá"
                          style={{ width: "100%" }}
                          formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".")}
                          parser={(v) => v?.replace(/\./g, "")}
                        />
                      </Form.Item>
                      <div style={{ paddingTop: 5, textAlign: "right", fontWeight: 600 }}>
                        {fmtVnd(thanhTien)}
                      </div>
                      <Button
                        type="text"
                        danger
                        icon={<DeleteOutlined />}
                        disabled={fields.length === 1}
                        onClick={() => remove(name)}
                      />
                    </div>
                  );
                })}
                <Button type="dashed" icon={<PlusOutlined />} onClick={() => add()} block>
                  Thêm dòng hàng
                </Button>
                <Form.ErrorList errors={errors} />
              </>
            )}
          </Form.List>

          <div style={{ textAlign: "right", marginTop: 16, fontSize: 15 }}>
            Tổng tiền: <b style={{ color: "#d46b08", fontSize: 18 }}>{fmtVnd(tongTien)}</b>
          </div>
        </Form>
      )}
    </Modal>
  );
}
