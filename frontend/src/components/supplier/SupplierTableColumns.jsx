import React from "react";
import { Space, Typography, Tag, Button, Popconfirm } from "antd";
import { EyeOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";

const { Text } = Typography;

/**
 * @param offset số dòng đã đi qua ở các trang trước, dùng để đánh STT liên tục
 *               (= (trang hiện tại - 1) * số dòng/trang)
 */
export const getSupplierColumns = ({
  offset = 0,
  onView,
  onEdit,
  onDelete,
}) => [
  {
    title: "STT",
    key: "stt",
    width: 60,
    align: "center",
    render: (_, __, index) => String(offset + index + 1).padStart(2, "0"),
  },
  {
    title: "MÃ NCC",
    dataIndex: "maNcc",
    key: "maNcc",
    width: 110,
    render: (text) => <div style={{ fontWeight: 600 }}>{text || "N/A"}</div>,
  },
  {
    title: "TÊN NHÀ CUNG CẤP",
    dataIndex: "tenNcc",
    key: "tenNcc",
    width: 240,
    render: (text, record) => (
      <div>
        <div style={{ fontWeight: 600 }}>{text}</div>
        <Text type="secondary" style={{ fontSize: 12 }}>
          MST: {record.mst || "N/A"}
        </Text>
      </div>
    ),
  },
  {
    title: "SỐ ĐIỆN THOẠI",
    dataIndex: "sdt",
    key: "sdt",
    width: 130,
    render: (text) => <Text>{text}</Text>,
  },
  {
    title: "ĐỊA CHỈ",
    dataIndex: "diaChi",
    key: "diaChi",
    width: 260,
    render: (text) => <Text>{text}</Text>,
  },
  {
    title: "EMAIL",
    dataIndex: "email",
    key: "email",
    width: 200,
    render: (text) => <Text>{text}</Text>,
  },
  {
    title: "NHÓM HÀNG",
    dataIndex: "nhomHang",
    key: "nhomHang",
    width: 190,
    render: (text) =>
      text ? (
        <Tag
          style={{
            color: "#d46b08",
            background: "#fff7e6",
            borderColor: "#ffd591",
            borderRadius: 4,
            fontWeight: 500,
            whiteSpace: "normal",
          }}
        >
          {text}
        </Tag>
      ) : null,
  },
  {
    title: "TRẠNG THÁI",
    dataIndex: "trangThai",
    key: "trangThai",
    width: 140,
    render: (status) => {
      let color = "#595959";
      let bg = "#fafafa";
      let borderColor = "#d9d9d9";
      let label = status;

      if (status === "dang_hop_tac") {
        color = "#389e0d";
        bg = "#f6ffed";
        borderColor = "#b7eb8f";
        label = "Đang hợp tác";
      } else if (status === "ngung_hop_tac") {
        color = "#cf1322";
        bg = "#fff1f0";
        borderColor = "#ffa39e";
        label = "Ngừng hợp tác";
      }

      return (
        <Tag
          style={{
            color,
            background: bg,
            borderColor,
            borderRadius: 4,
            fontWeight: 500,
          }}
        >
          {label || "N/A"}
        </Tag>
      );
    },
  },
  {
    title: "THAO TÁC",
    key: "action",
    width: 140,
    render: (_, record) => (
      <Space size="small">
        <Button
          type="text"
          icon={<EyeOutlined />}
          style={{ color: "#8c8c8c" }}
          onClick={() => onView(record)}
        />
        <Button
          type="text"
          icon={<EditOutlined />}
          style={{ color: "#8c8c8c" }}
          onClick={() => onEdit(record)}
        />
        {/* Nhà cung cấp là dữ liệu gốc nên không xóa thật, chỉ chuyển sang "Ngừng hợp tác" */}
        <Popconfirm
          title="Chuyển nhà cung cấp này sang trạng thái Ngừng hợp tác?"
          okText="Đồng ý"
          cancelText="Hủy"
          onConfirm={() => onDelete(record.id)}
        >
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
            disabled={record.trangThai === "ngung_hop_tac"}
          />
        </Popconfirm>
      </Space>
    ),
  },
];
