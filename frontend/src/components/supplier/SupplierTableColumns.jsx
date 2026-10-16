import React from "react";
import { Space, Typography, Tag, Button } from "antd";
import { EyeOutlined, EditOutlined } from "@ant-design/icons";
import ConfirmDeleteButton from "../common/ConfirmDeleteButton";

const { Text } = Typography;

/**
 * Cột cho <AppTable> (dạng { key, header, align?, width?, render?(row) }).
 * AppTable không đưa số thứ tự vào render nên trang cha thêm sẵn field `stt` vào từng dòng.
 */
export const getSupplierColumns = ({ onView, onEdit, onDelete }) => [
  {
    key: "stt",
    header: "STT",
    align: "center",
    width: "60px",
    render: (row) => String(row.stt).padStart(2, "0"),
  },
  {
    key: "maNcc",
    header: "MÃ NCC",
    render: (row) => (
      <div style={{ fontWeight: 600 }}>{row.maNcc || "N/A"}</div>
    ),
  },
  {
    key: "tenNcc",
    header: "TÊN NHÀ CUNG CẤP",
    render: (row) => (
      <div>
        <div style={{ fontWeight: 600 }}>{row.tenNcc}</div>
        <Text type="secondary" style={{ fontSize: 12 }}>
          MST: {row.mst || "N/A"}
        </Text>
      </div>
    ),
  },
  {
    key: "sdt",
    header: "SỐ ĐIỆN THOẠI",
    render: (row) => <Text>{row.sdt}</Text>,
  },
  {
    key: "diaChi",
    header: "ĐỊA CHỈ",
    render: (row) => <Text>{row.diaChi}</Text>,
  },
  {
    key: "email",
    header: "EMAIL",
    render: (row) => <Text>{row.email}</Text>,
  },
  {
    key: "nhomHang",
    header: "NHÓM HÀNG",
    render: (row) =>
      row.nhomHang ? (
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
          {row.nhomHang}
        </Tag>
      ) : null,
  },
  {
    key: "trangThai",
    header: "TRẠNG THÁI",
    render: (row) => {
      let color = "#595959";
      let bg = "#fafafa";
      let borderColor = "#d9d9d9";
      let label = row.trangThai;

      if (row.trangThai === "dang_hop_tac") {
        color = "#389e0d";
        bg = "#f6ffed";
        borderColor = "#b7eb8f";
        label = "Đang hợp tác";
      } else if (row.trangThai === "ngung_hop_tac") {
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
    key: "action",
    header: "THAO TÁC",
    align: "center",
    render: (row) => (
      <Space size="middle">
        <Button
          type="text"
          icon={<EyeOutlined />}
          style={{ color: "#8c8c8c" }}
          onClick={() => onView(row)}
        />
        <Button
          type="text"
          icon={<EditOutlined />}
          style={{ color: "#8c8c8c" }}
          onClick={() => onEdit(row)}
        />
        {/* Nhà cung cấp là dữ liệu gốc nên không xóa thật, chỉ chuyển sang "Ngừng hợp tác" */}
        <ConfirmDeleteButton
          title="Chuyển nhà cung cấp này sang trạng thái Ngừng hợp tác?"
          okText="Đồng ý"
          disabled={row.trangThai === "ngung_hop_tac"}
          onConfirm={() => onDelete(row.id)}
        />
      </Space>
    ),
  },
];
