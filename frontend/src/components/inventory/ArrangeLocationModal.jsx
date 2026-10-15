import { useState, useEffect } from "react";
import { Modal, Form, Select, Table, message } from "antd";
import { phieuNhapApi } from "../../api/phieuNhapApi";
import { tonKhoApi } from "../../api/tonKhoApi";

export default function ArrangeLocationModal({ record, onCancel, onSuccess }) {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [items, setItems] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [locationsByKho, setLocationsByKho] = useState({});

  useEffect(() => {
    if (record) {
      setLoading(true);
      Promise.all([phieuNhapApi.chiTiet(record.id), tonKhoApi.layDanhSachKho()])
        .then(([resChiTiet, resKho]) => {
          setItems(resChiTiet.chiTiet || []);
          setWarehouses(resKho || []);

          // Mặc định thiết lập sẵn dòng "Kho" vào Form bằng Kho đầu tiên (nếu có)
          const initialValues = {};
          const defaultKhoId = resKho.length > 0 ? resKho[0].id : null;

          (resChiTiet.chiTiet || []).forEach((it) => {
            initialValues[`khoId_${it.matHangId}`] = defaultKhoId;
          });
          form.setFieldsValue(initialValues);

          if (defaultKhoId) {
            fetchLocations(defaultKhoId);
          }
        })
        .catch(() =>
          message.error("Lỗi khi tải dữ liệu phiếu hoặc danh sách kho"),
        )
        .finally(() => setLoading(false));
    }
  }, [record, form]);

  const fetchLocations = async (khoId) => {
    if (!locationsByKho[khoId]) {
      try {
        const locs = await tonKhoApi.layDanhSachViTri(khoId);
        setLocationsByKho((prev) => ({ ...prev, [khoId]: locs }));
      } catch (e) {
        message.error("Lỗi lấy vị trí lưu trữ");
      }
    }
  };

  const handleKhoChange = (khoId, matHangId) => {
    fetchLocations(khoId);
    // Reset lựa chọn vị trí vì khác Kho thì Vị trí lưu trữ cũng khác nhau
    form.setFieldValue(`viTriId_${matHangId}`, undefined);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      const payloadItems = items
        .map((it) => ({
          matHangId: it.matHangId,
          khoId: values[`khoId_${it.matHangId}`],
          viTriId: values[`viTriId_${it.matHangId}`],
        }))
        .filter((it) => it.khoId && it.viTriId);

      if (payloadItems.length === 0) {
        return message.warning(
          "Vui lòng chọn đầy đủ Kho và Vị trí lưu trữ để xếp hàng!",
        );
      }

      setLoading(true);
      await phieuNhapApi.xepViTri(record.id, { items: payloadItems });
      message.success("Xếp vị trí lưu trữ thành công");
      onSuccess();
    } catch (error) {
      if (error.errorFields) return;
      message.error(error.message || "Xếp vị trí thất bại");
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      title: "Mặt hàng",
      dataIndex: "tenMatHang",
      key: "tenMatHang",
      render: (text, r) => (
        <b>
          {r.maMatHang} - {text}
        </b>
      ),
    },
    {
      title: "SL trong phiếu",
      dataIndex: "soLuong",
      key: "soLuong",
      align: "center",
      width: 140,
    },
    {
      title: "Chọn Kho",
      key: "kho",
      width: 230,
      render: (_, r) => (
        <Form.Item
          name={`khoId_${r.matHangId}`}
          rules={[{ required: true, message: "Chọn kho" }]}
          style={{ margin: 0 }}
        >
          <Select
            placeholder="Chọn kho"
            showSearch
            optionFilterProp="children"
            options={warehouses.map((k) => ({ value: k.id, label: k.tenKho }))}
            onChange={(val) => handleKhoChange(val, r.matHangId)}
          />
        </Form.Item>
      ),
    },
    {
      title: "Vị trí lưu trữ (Lên kệ)",
      key: "viTri",
      width: 280,
      // Fix lỗi Hook bằng cách dùng dependencies của Form.Item
      render: (_, r) => (
        <Form.Item noStyle dependencies={[`khoId_${r.matHangId}`]}>
          {() => {
            const selectedKhoId = form.getFieldValue(`khoId_${r.matHangId}`);
            const locs = selectedKhoId
              ? locationsByKho[selectedKhoId] || []
              : [];
            return (
              <Form.Item
                name={`viTriId_${r.matHangId}`}
                style={{ margin: 0 }}
                rules={[{ required: true, message: "Chọn vị trí" }]}
              >
                <Select
                  placeholder="Chọn vị trí lưu trữ"
                  showSearch
                  optionFilterProp="children"
                  options={locs.map((v) => ({
                    value: v.id,
                    label: v.tenViTri,
                  }))}
                  disabled={!selectedKhoId}
                />
              </Form.Item>
            );
          }}
        </Form.Item>
      ),
    },
  ];

  return (
    <Modal
      title={`Xếp vị trí cho Phiếu Nhập: ${record?.maPhieu}`}
      open={!!record}
      onCancel={onCancel}
      onOk={handleSubmit}
      confirmLoading={loading}
      width={1000}
      okText="Xác nhận xếp vị trí"
      cancelText="Đóng"
    >
      <Form form={form} layout="vertical">
        <Table
          dataSource={items}
          columns={columns}
          rowKey="matHangId"
          pagination={false}
          loading={loading}
          bordered
          size="middle"
        />
      </Form>
    </Modal>
  );
}
