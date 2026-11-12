import { useEffect, useState } from "react";
import { Descriptions, Drawer, Space, Spin, Table, Tag } from "antd";
import { taiCheApi } from "../../api/recycleApi";
import { fmtDateTime, fmtNumber } from "../../utils/format";
import { notifyError } from "../../utils/notify";
import { PHIEU_TAI_CHE_TRANG_THAI, resolveStatus } from "../../utils/statusTag";
import { fmtDate } from "./helpers";

/** Chi tiết phiếu phân loại: thông tin, kết quả nhập vào kho và các phiếu kho sinh ra. */
export default function PhieuTaiCheDrawer({ id, onClose }) {
  const [data, setData] = useState(null);

  useEffect(() => {
    let ignore = false;
    taiCheApi
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
    <Drawer open width={680} title={data ? `Phiếu phân loại ${data.maPhieu}` : "Chi tiết phiếu"} onClose={onClose}>
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
            <Descriptions.Item label="Lô hàng" span={2}>
              {data.maLo} - {data.tenMatHang}
            </Descriptions.Item>
            <Descriptions.Item label="Số pallet xử lý">{fmtNumber(data.soLuongXuLy)}</Descriptions.Item>
            <Descriptions.Item label="Ngày thực hiện">{fmtDate(data.ngayThucHien)}</Descriptions.Item>
            <Descriptions.Item label="Bán lại">{fmtNumber(data.soBanLai)}</Descriptions.Item>
            <Descriptions.Item label="Tháo dỡ">{fmtNumber(data.soThaoDo)}</Descriptions.Item>
            <Descriptions.Item label="Loại bỏ">
              {fmtNumber(data.soLoaiBo)} ({fmtNumber(data.kgPheLieuLoaiBo)} kg)
            </Descriptions.Item>
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

          <div style={{ fontWeight: 600, margin: "20px 0 8px" }}>Hàng đã nhập kho theo phân loại</div>
          <Table
            size="small"
            pagination={false}
            rowKey={(r) => `${r.matHangId}`}
            dataSource={data.ketQua ?? []}
            locale={{ emptyText: "Chưa có (phiếu chưa được duyệt)" }}
            columns={[
              { title: "Mặt hàng", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
              {
                title: "Số lượng",
                align: "right",
                render: (_, r) => `${fmtNumber(r.soLuong)} ${r.donViTinh ?? ""}`,
              },
              { title: "Ghi chú", dataIndex: "ghiChu" },
            ]}
          />

          {(data.phieuKho ?? []).length > 0 && (
            <>
              <div style={{ fontWeight: 600, margin: "20px 0 8px" }}>Phiếu kho liên quan</div>
              <Space wrap>
                {data.phieuKho.map((p) => (
                  <Tag key={p.id}>
                    {p.maPhieu} · {p.loaiPhieu}
                  </Tag>
                ))}
              </Space>
            </>
          )}
        </>
      )}
    </Drawer>
  );
}
