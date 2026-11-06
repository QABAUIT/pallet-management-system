import { useEffect, useState } from "react";
import { Alert, Button, Form, Input, InputNumber, Modal, Select, Spin, Switch, Table, Tag } from "antd";
import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { dongPalletApi } from "../../api/recycleApi";
import { KHO_ID } from "../../config/kho";
import { fmtNumber } from "../../utils/format";
import { notifyError, notifySuccess } from "../../utils/notify";

const LINE = {
  display: "grid",
  gridTemplateColumns: "minmax(0, 2fr) 110px 80px minmax(0, 1.6fr) 32px",
  gap: 8,
  alignItems: "start",
};

/**
 * Tạo / sửa phiếu đóng pallet. Khớp TaoPhieuDongPalletRequest:
 * dinhMucId, khoId, soLuongSanXuat, ghiChu, chiTiet[{matHangId, soLuongThucTe, lyDoChenhLech}] (tuỳ chọn).
 * - Bỏ trống chi tiết = dùng đúng định mức. Bật "Điều chỉnh số thực tế" để ghi số vật tư dùng thật.
 * - Dòng lệch định mức bắt buộc có lý do (TC12).
 * - Phần "Dự kiến vật tư" gọi /dong-pallet/du-kien để biết đủ hay thiếu trước khi lập phiếu.
 */
export default function DongPalletFormModal({ record, onCancel, onSaved }) {
  const isEdit = Boolean(record);
  const khoId = isEdit ? record.khoId : KHO_ID;
  const [form] = Form.useForm();
  const [dm, setDm] = useState(null);
  const [detail, setDetail] = useState(null);
  const [duKien, setDuKien] = useState(null);
  const [dieuChinh, setDieuChinh] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let ignore = false;
    dongPalletApi
      .danhMuc()
      .then((d) => !ignore && setDm(d))
      .catch((err) => !ignore && notifyError(err));
    if (isEdit) {
      dongPalletApi
        .chiTiet(record.id)
        .then((d) => {
          if (ignore) return;
          setDetail(d);
          setDieuChinh((d.chiTiet ?? []).some((c) => c.chenhLech !== 0));
        })
        .catch((err) => !ignore && notifyError(err));
    }
    return () => {
      ignore = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const ready = dm && (!isEdit || detail);
  const dinhMucId = Form.useWatch("dinhMucId", form);
  const soLuong = Form.useWatch("soLuongSanXuat", form);
  const dinhMuc = (dm?.dinhMuc ?? []).find((d) => d.id === dinhMucId);

  // Định mức cho từng vật tư với số lượng sản xuất hiện tại: { matHangId: số cần }
  const dinhMucQty = {};
  (dinhMuc?.chiTiet ?? []).forEach((c) => {
    dinhMucQty[c.matHangId] = c.soLuong * (Number(soLuong) || 0);
  });

  // Gọi dự kiến vật tư sau khi ngừng nhập 0,4s
  useEffect(() => {
    if (!dinhMucId || !soLuong || soLuong < 1) {
      setDuKien(null);
      return undefined;
    }
    let ignore = false;
    const t = setTimeout(() => {
      dongPalletApi
        .duKien({ dinhMucId, khoId, soLuong })
        .then((d) => !ignore && setDuKien(d))
        .catch(() => !ignore && setDuKien(null));
    }, 400);
    return () => {
      ignore = true;
      clearTimeout(t);
    };
  }, [dinhMucId, soLuong, khoId]);

  const buildRows = (dmx, sl) =>
    (dmx?.chiTiet ?? []).map((c) => ({
      matHangId: c.matHangId,
      soLuongThucTe: c.soLuong * (Number(sl) || 0),
    }));

  const toggleDieuChinh = (on) => {
    setDieuChinh(on);
    if (on && !(form.getFieldValue("chiTiet") ?? []).length) {
      form.setFieldValue("chiTiet", buildRows(dinhMuc, soLuong));
    }
  };

  // Đổi định mức / số lượng khi đang điều chỉnh: dựng lại bảng theo định mức mới
  const handleValuesChange = (changed, all) => {
    if (dieuChinh && ("dinhMucId" in changed || "soLuongSanXuat" in changed)) {
      const dmx = (dm?.dinhMuc ?? []).find((d) => d.id === all.dinhMucId);
      form.setFieldValue("chiTiet", buildRows(dmx, all.soLuongSanXuat));
    }
  };

  const vatTuOptions = (() => {
    const map = new Map();
    (dm?.vatTu ?? []).forEach((v) => map.set(v.id, `${v.maMatHang} - ${v.tenMatHang}`));
    (dinhMuc?.chiTiet ?? []).forEach((c) => map.set(c.matHangId, `${c.maMatHang} - ${c.tenMatHang}`));
    (detail?.chiTiet ?? []).forEach((c) => map.set(c.matHangId, `${c.maMatHang} - ${c.tenMatHang}`));
    return [...map].map(([value, label]) => ({ value, label }));
  })();

  const handleFinish = async (v) => {
    const payload = {
      dinhMucId: v.dinhMucId,
      khoId,
      soLuongSanXuat: v.soLuongSanXuat,
      ghiChu: v.ghiChu?.trim() || null,
    };
    if (dieuChinh) {
      const ids = (v.chiTiet ?? []).map((r) => r.matHangId);
      if (new Set(ids).size !== ids.length) {
        notifyError("Mỗi vật tư chỉ được xuất hiện một lần");
        return;
      }
      payload.chiTiet = (v.chiTiet ?? []).map((r) => ({
        matHangId: r.matHangId,
        soLuongThucTe: r.soLuongThucTe,
        lyDoChenhLech: r.lyDoChenhLech?.trim() || null,
      }));
    }
    setSaving(true);
    try {
      if (isEdit) {
        await dongPalletApi.capNhat(record.id, payload);
        notifySuccess("Cập nhật phiếu đóng pallet thành công");
      } else {
        await dongPalletApi.tao(payload);
        notifySuccess("Tạo phiếu đóng pallet thành công");
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
        dinhMucId: detail?.dinhMucId,
        soLuongSanXuat: detail?.soLuongSanXuat,
        ghiChu: detail?.ghiChu ?? undefined,
        chiTiet: (detail?.chiTiet ?? []).map((c) => ({
          matHangId: c.matHangId,
          soLuongThucTe: c.soLuongThucTe,
          lyDoChenhLech: c.lyDoChenhLech ?? undefined,
        })),
      }
    : {};

  const dmOptions = (dm?.dinhMuc ?? []).map((d) => ({
    value: d.id,
    label: `${d.maDinhMuc} - ${d.tenDinhMuc} → ${d.tenThanhPham}`,
  }));
  if (isEdit && detail && !(dm?.dinhMuc ?? []).some((d) => d.id === detail.dinhMucId)) {
    dmOptions.push({ value: detail.dinhMucId, label: detail.tenDinhMuc });
  }

  return (
    <Modal
      open
      centered
      width={840}
      title={isEdit ? `Cập nhật phiếu ${record.maPhieu}` : "Lập phiếu đóng pallet"}
      okText={isEdit ? "Lưu thay đổi" : "Tạo phiếu đóng pallet"}
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
          onValuesChange={handleValuesChange}
          style={{ marginTop: 16 }}
        >
          <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: 16 }}>
            <Form.Item
              label="Định mức"
              name="dinhMucId"
              rules={[{ required: true, message: "Chọn định mức" }]}
            >
              <Select showSearch optionFilterProp="label" placeholder="Chọn định mức đang dùng" options={dmOptions} />
            </Form.Item>
            <Form.Item
              label="Số pallet sản xuất"
              name="soLuongSanXuat"
              rules={[{ required: true, message: "Nhập số lượng" }]}
            >
              <InputNumber min={1} precision={0} style={{ width: "100%" }} />
            </Form.Item>
          </div>

          {duKien && (
            <div style={{ marginBottom: 16 }}>
              <Alert
                style={{ marginBottom: 8 }}
                type={duKien.duVatTu ? "success" : "warning"}
                showIcon
                message={
                  duKien.duVatTu
                    ? "Đủ vật tư trong kho để đóng số pallet này"
                    : "Thiếu vật tư: cần mua thêm hoặc tháo dỡ thêm trước khi duyệt phiếu"
                }
              />
              <Table
                size="small"
                pagination={false}
                rowKey="matHangId"
                dataSource={duKien.chiTiet ?? []}
                columns={[
                  { title: "Vật tư", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
                  { title: "Cần", dataIndex: "nhuCau", align: "right", render: fmtNumber },
                  { title: "Khả dụng", dataIndex: "khaDung", align: "right", render: fmtNumber },
                  {
                    title: "Thiếu",
                    dataIndex: "thieu",
                    align: "right",
                    render: (v) => (v > 0 ? <Tag color="red">{fmtNumber(v)}</Tag> : "—"),
                  },
                ]}
              />
            </div>
          )}

          <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
            <Switch checked={dieuChinh} onChange={toggleDieuChinh} disabled={!dinhMucId} />
            <span style={{ fontWeight: 600 }}>Điều chỉnh số vật tư thực tế</span>
            <span style={{ color: "var(--text-muted)", fontSize: 12 }}>
              Tắt = dùng đúng định mức. Dòng lệch phải ghi lý do.
            </span>
          </div>

          {dieuChinh && (
            <Form.List name="chiTiet">
              {(fields, { add, remove }) => (
                <>
                  {fields.map(({ key, name, ...rest }) => {
                    const row = (form.getFieldValue("chiTiet") ?? [])[name] ?? {};
                    const base = dinhMucQty[row.matHangId] ?? 0;
                    const lech = Number(row.soLuongThucTe) !== base;
                    return (
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
                          name={[name, "soLuongThucTe"]}
                          rules={[{ required: true, message: "Nhập số thực tế" }]}
                          style={{ marginBottom: 12 }}
                        >
                          <InputNumber min={0} precision={0} placeholder="Thực tế" style={{ width: "100%" }} />
                        </Form.Item>
                        <div style={{ paddingTop: 5, fontSize: 12, color: lech ? "var(--color-red)" : "var(--text-muted)" }}>
                          ĐM: {fmtNumber(base)}
                        </div>
                        <Form.Item
                          {...rest}
                          name={[name, "lyDoChenhLech"]}
                          dependencies={[["chiTiet", name, "soLuongThucTe"], ["chiTiet", name, "matHangId"]]}
                          rules={[
                            ({ getFieldValue }) => ({
                              validator: (_, val) => {
                                const r = getFieldValue(["chiTiet", name]) ?? {};
                                const dmQty = dinhMucQty[r.matHangId] ?? 0;
                                if (Number(r.soLuongThucTe) !== dmQty && !val?.trim()) {
                                  return Promise.reject(new Error("Bắt buộc ghi lý do lệch"));
                                }
                                return Promise.resolve();
                              },
                            }),
                          ]}
                          style={{ marginBottom: 12 }}
                        >
                          <Input placeholder={lech ? "Lý do lệch (bắt buộc)" : "Lý do"} maxLength={255} />
                        </Form.Item>
                        <Button type="text" danger icon={<DeleteOutlined />} onClick={() => remove(name)} />
                      </div>
                    );
                  })}
                  <Button type="dashed" icon={<PlusOutlined />} onClick={() => add()} block>
                    Thêm vật tư ngoài định mức
                  </Button>
                </>
              )}
            </Form.List>
          )}

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
