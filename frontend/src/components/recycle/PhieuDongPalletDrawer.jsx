import { useEffect, useState } from "react";
import { Descriptions, Drawer, Spin, Table, Tag } from "antd";
import { dongPalletApi } from "../../api/recycleApi";
import { fmtDateTime, fmtNumber } from "../../utils/format";
import { notifyError } from "../../utils/notify";
import { PHIEU_TAI_CHE_TRANG_THAI, resolveStatus } from "../../utils/statusTag";
import { fmtDate } from "./helpers";

/** Chi tiết phiếu đóng pallet: số vật tư theo định mức so với thực tế, dòng lệch tô đỏ kèm lý do. */
export default function PhieuDongPalletDrawer({ id, onClose }) {
  const [data, setData] = useState(null);

  useEffect(() => {
    let ignore = false;
    dongPalletApi
      .chiTiet(id)
      .then((d) => !ignore && setData(d))
      .catch((err) => {
        if (!ignore) {
          notifyError(err);
          onClose();
        }
      });
    return () => {
      ignore = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const st = resolveStatus(PHIEU_TAI_CHE_TRANG_THAI, data?.trangThai);

  return (
    <Drawer open width={720} title={data ? `Phiếu đóng pallet ${data.maPhieu}` : "Chi tiết phiếu"} onClose={onClose}>
      {!data ? (
        <div style={{ textAlign: "center", padding: 48 }}>
          <Spin />
        </div>
      ) : (
        <>
          <Descriptions bordered size="small" column={2} styles={{ label: { fontWeight: 600, width: 150 } }}>
            <Descriptions.Item label="Trạng thái">
              <Tag color={st.color}>{st.label}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Kho">{data.tenKho || "—"}</Descriptions.Item>
            <Descriptions.Item label="Định mức" span={2}>
              {data.tenDinhMuc}
            </Descriptions.Item>
            <Descriptions.Item label="Thành phẩm">{data.tenThanhPham}</Descriptions.Item>
            <Descriptions.Item label="Số pallet sản xuất">
              <b>{fmtNumber(data.soLuongSanXuat)}</b>
            </Descriptions.Item>
            <Descriptions.Item label="Ngày thực hiện">{fmtDate(data.ngayThucHien)}</Descriptions.Item>
            <Descriptions.Item label="Người lập">{data.tenNguoiThucHien || "—"}</Descriptions.Item>
            <Descriptions.Item label="Người duyệt">{data.tenNguoiDuyet || "—"}</Descriptions.Item>
            <Descriptions.Item label="Thời gian duyệt">{fmtDateTime(data.thoiGianDuyet)}</Descriptions.Item>
            {data.ghiChu && (
              <Descriptions.Item label="Ghi chú" span={2}>
                {data.ghiChu}
              </Descriptions.Item>
            )}
            {data.lyDoHuy && (
              <Descriptions.Item label="Lý do huỷ" span={2}>
                {data.lyDoHuy}
              </Descriptions.Item>
            )}
          </Descriptions>

          <div style={{ fontWeight: 600, margin: "20px 0 8px" }}>
            Vật tư sử dụng {data.soDongChenhLech > 0 && <Tag color="red">{data.soDongChenhLech} dòng lệch</Tag>}
          </div>
          <Table
            size="small"
            pagination={false}
            rowKey="matHangId"
            dataSource={data.chiTiet ?? []}
            columns={[
              { title: "Vật tư", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
              { title: "Định mức", dataIndex: "soLuongDinhMuc", align: "right", render: fmtNumber },
              { title: "Thực tế", dataIndex: "soLuongThucTe", align: "right", render: fmtNumber },
              {
                title: "Chênh lệch",
                dataIndex: "chenhLech",
                align: "right",
                render: (v) =>
                  v === 0 ? (
                    "0"
                  ) : (
                    <b style={{ color: v > 0 ? "#cf1322" : "#389e0d" }}>{v > 0 ? `+${v}` : v}</b>
                  ),
              },
              { title: "Lý do", dataIndex: "lyDoChenhLech", render: (v) => v || "—" },
            ]}
          />
        </>
      )}
    </Drawer>
  );
}
