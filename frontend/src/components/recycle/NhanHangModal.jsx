import { useEffect, useState } from "react";
import { Alert, Input, InputNumber, Modal, Spin, Table } from "antd";
import { donMuaApi } from "../../api/recycleApi";
import { fmtNumber } from "../../utils/format";
import { notifyError, notifySuccess } from "../../utils/notify";

/**
 * Nhận hàng cho đơn mua (nhận từng phần được). Khớp NhanHangDonMuaRequest:
 * bienSoXe, ghiChu, chiTiet[{chiTietId, soLuongNhan, ghiChu}].
 * Mỗi dòng mặc định nhận hết phần còn lại; để 0 nếu lần này chưa nhận dòng đó.
 */
export default function NhanHangModal({ id, onCancel, onSaved }) {
  const [don, setDon] = useState(null);
  const [qty, setQty] = useState({}); // { [chiTietId]: số lượng nhận lần này }
  const [bienSoXe, setBienSoXe] = useState("");
  const [ghiChu, setGhiChu] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let ignore = false;
    donMuaApi
      .chiTiet(id)
      .then((d) => {
        if (ignore) return;
        setDon(d);
        setQty(Object.fromEntries(d.chiTiet.map((r) => [r.id, r.soLuongConLai])));
      })
      .catch((err) => !ignore && notifyError(err));
    return () => {
      ignore = true;
    };
  }, [id]);

  const handleOk = async () => {
    const chiTiet = Object.entries(qty)
      .filter(([, v]) => Number(v) > 0)
      .map(([chiTietId, v]) => ({ chiTietId: Number(chiTietId), soLuongNhan: Number(v) }));
    if (chiTiet.length === 0) {
      notifyError("Chọn ít nhất một dòng có số lượng nhận lớn hơn 0");
      return;
    }
    setSaving(true);
    try {
      await donMuaApi.nhanHang(id, {
        bienSoXe: bienSoXe.trim() || null,
        ghiChu: ghiChu.trim() || null,
        chiTiet,
      });
      notifySuccess("Nhận hàng thành công, tồn kho đã được cập nhật");
      onSaved();
    } catch (err) {
      notifyError(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open
      centered
      width={780}
      title={don ? `Nhận hàng - đơn ${don.maDonMua}` : "Nhận hàng"}
      okText="Xác nhận nhận hàng"
      cancelText="Hủy"
      maskClosable={false}
      confirmLoading={saving}
      onCancel={onCancel}
      onOk={handleOk}
    >
      {!don ? (
        <div style={{ textAlign: "center", padding: 48 }}>
          <Spin />
        </div>
      ) : (
        <>
          <Alert
            style={{ margin: "12px 0" }}
            type="info"
            showIcon
            message="Hàng nhận sẽ được nhập kho ngay theo đúng số lượng nhập bên dưới."
          />
          <Table
            size="small"
            pagination={false}
            rowKey="id"
            dataSource={don.chiTiet}
            columns={[
              { title: "Mặt hàng", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
              { title: "Đặt", dataIndex: "soLuongDat", align: "right", render: fmtNumber },
              { title: "Đã nhận", dataIndex: "soLuongDaNhan", align: "right", render: fmtNumber },
              { title: "Còn lại", dataIndex: "soLuongConLai", align: "right", render: fmtNumber },
              {
                title: "Nhận lần này",
                align: "right",
                width: 150,
                render: (_, r) => (
                  <InputNumber
                    min={0}
                    max={r.soLuongConLai}
                    precision={0}
                    disabled={r.soLuongConLai === 0}
                    value={qty[r.id]}
                    onChange={(v) => setQty((q) => ({ ...q, [r.id]: v ?? 0 }))}
                  />
                ),
              },
            ]}
          />
          <div style={{ display: "grid", gridTemplateColumns: "1fr 2fr", gap: 12, marginTop: 16 }}>
            <Input
              placeholder="Biển số xe giao"
              maxLength={20}
              value={bienSoXe}
              onChange={(e) => setBienSoXe(e.target.value)}
            />
            <Input placeholder="Ghi chú" value={ghiChu} onChange={(e) => setGhiChu(e.target.value)} />
          </div>
        </>
      )}
    </Modal>
  );
}
