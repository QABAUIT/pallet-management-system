import { useEffect, useState } from "react";
import { Button, Form, Input, InputNumber, Modal, Select, Spin } from "antd";
import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { dinhMucApi } from "../../api/recycleApi";
import { useMatHangAll } from "../../hooks/useMatHangAll";
import { notifyError, notifySuccess } from "../../utils/notify";

const LINE = {
  display: "grid",
  gridTemplateColumns: "minmax(0, 2fr) 130px 32px",
  gap: 8,
  alignItems: "start",
};

const opt = (m) => ({ value: m.id, label: `${m.maMatHang} - ${m.tenMatHang}` });

/**
 * Tạo / sửa định mức vật tư cho 1 thành phẩm. Khớp TaoDinhMucRequest:
 * tenDinhMuc, thanhPhamId, ghiChu, chiTiet[{matHangId, soLuong}]  (soLuong = vật tư cho 1 thành phẩm).
 * Khi sửa, nhận `id` để tự tải chi tiết.
 */
export default function DinhMucFormModal({ id, onCancel, onSaved }) {
  const isEdit = Boolean(id);
  const [form] = Form.useForm();
  const [detail, setDetail] = useState(null);
  const [saving, setSaving] = useState(false);
  const { list: matHang, loading } = useMatHangAll();

  useEffect(() => {
    if (!id) return undefined;
    let ignore = false;
    dinhMucApi
      .chiTiet(id)
      .then((d) => !ignore && setDetail(d))
      .catch((err) => !ignore && notifyError(err));
    return () => {
      ignore = true;
    };
  }, [id]);

  const ready = !loading && (!isEdit || detail);
  const thanhPhamId = Form.useWatch("thanhPhamId", form);

  const thanhPhamOptions = matHang.filter((m) => m.loaiMatHang === "pallet").map(opt);
  const vatTuOptions = matHang
    .filter((m) => m.loaiMatHang === "linh_kien" && m.id !== thanhPhamId)
    .map(opt);

  const handleFinish = async (v) => {
    const ids = v.chiTiet.map((r) => r.matHangId);
    if (new Set(ids).size !== ids.length) {
      notifyError("Mỗi vật tư chỉ được xuất hiện một lần trong định mức");
      return;
    }
    const payload = {
      tenDinhMuc: v.tenDinhMuc.trim(),
      thanhPhamId: v.thanhPhamId,
      ghiChu: v.ghiChu?.trim() || null,
      chiTiet: v.chiTiet.map((r) => ({ matHangId: r.matHangId, soLuong: r.soLuong })),
    };
    setSaving(true);
    try {
      if (isEdit) {
        await dinhMucApi.capNhat(id, payload);
        notifySuccess("Cập nhật định mức thành công");
      } else {
        await dinhMucApi.tao(payload);
        notifySuccess("Tạo định mức thành công");
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
        tenDinhMuc: detail?.tenDinhMuc,
        thanhPhamId: detail?.thanhPhamId,
        ghiChu: detail?.ghiChu ?? undefined,
        chiTiet: (detail?.chiTiet ?? []).map((d) => ({ matHangId: d.matHangId, soLuong: d.soLuong })),
      }
    : { chiTiet: [{}] };

  return (
    <Modal
      open
      centered
      width={640}
      title={isEdit ? `Cập nhật định mức ${detail?.maDinhMuc ?? ""}` : "Tạo định mức vật tư"}
      okText={isEdit ? "Lưu thay đổi" : "Tạo định mức"}
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
          <Form.Item
            label="Tên định mức"
            name="tenDinhMuc"
            rules={[
              { required: true, whitespace: true, message: "Nhập tên định mức" },
              { max: 150, message: "Tối đa 150 ký tự" },
            ]}
          >
            <Input placeholder="Ví dụ: Pallet tái chế 1200x1000 mặc định" />
          </Form.Item>
          <Form.Item
            label="Thành phẩm"
            name="thanhPhamId"
            rules={[{ required: true, message: "Chọn thành phẩm" }]}
          >
            <Select
              showSearch
              optionFilterProp="label"
              placeholder="Pallet thành phẩm làm ra"
              options={thanhPhamOptions}
            />
          </Form.Item>

          <div style={{ fontWeight: 600, margin: "4px 0 8px" }}>Vật tư cho 1 thành phẩm</div>
          <Form.List
            name="chiTiet"
            rules={[
              {
                validator: async (_, value) => {
                  if (!value || value.length === 0) throw new Error("Cần ít nhất 1 vật tư");
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
                      rules={[{ required: true, message: "Chọn vật tư" }]}
                      style={{ marginBottom: 12 }}
                    >
                      <Select showSearch optionFilterProp="label" placeholder="Vật tư" options={vatTuOptions} />
                    </Form.Item>
                    <Form.Item
                      {...rest}
                      name={[name, "soLuong"]}
                      rules={[{ required: true, message: "Nhập SL" }]}
                      style={{ marginBottom: 12 }}
                    >
                      <InputNumber min={1} precision={0} placeholder="Số lượng" style={{ width: "100%" }} />
                    </Form.Item>
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
                  Thêm vật tư
                </Button>
                <Form.ErrorList errors={errors} />
              </>
            )}
          </Form.List>

          <Form.Item
            label="Ghi chú"
            name="ghiChu"
            style={{ marginTop: 16 }}
            rules={[{ max: 255, message: "Tối đa 255 ký tự" }]}
          >
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      )}
    </Modal>
  );
}
