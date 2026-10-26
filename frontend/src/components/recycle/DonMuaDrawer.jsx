import { useEffect, useState } from "react";
import { Descriptions, Drawer, Spin, Table, Tag } from "antd";
import { donMuaApi } from "../../api/recycleApi";
import { fmtDateTime, fmtNumber, fmtVnd } from "../../utils/format";
import { notifyError } from "../../utils/notify";
import { DON_MUA_TRANG_THAI, PHIEU_KHO_TRANG_THAI, resolveStatus } from "../../utils/statusTag";
import { fmtDate } from "./helpers";

/** Chi tiết đơn mua: thông tin, dòng hàng (đặt / đã nhận) và các phiếu nhập kho từ đơn. */
export default function DonMuaDrawer({ id, onClose }) {
  const [data, setData] = useState(null);

  useEffect(() => {
    let ignore = false;
    donMuaApi
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

  const st = resolveStatus(DON_MUA_TRANG_THAI, data?.trangThai);
  const ncc = data?.nhaCungCap;

  return (
    <Drawer open width={760} title={data ? `Đơn mua ${data.maDonMua}` : "Chi tiết đơn mua"} onClose={onClose}>
      {!data ? (
        <div style={{ textAlign: "center", padding: 48 }}>
          <Spin />
        </div>
      ) : (
        <>
          <Descriptions bordered size="small" column={2} styles={{ label: { fontWeight: 600, width: 150 } }}>
            <Descriptions.Item label="Trạng thái">
              <Tag color={st.color}>{st.label}</Tag>
              {data.treHan && <Tag color="red">Trễ hạn</Tag>}
            </Descriptions.Item>
            <Descriptions.Item label="Kho nhận">{data.tenKho || "—"}</Descriptions.Item>
            <Descriptions.Item label="Nhà cung cấp" span={2}>
              {ncc ? `${ncc.ma} - ${ncc.ten}` : "—"}
              {ncc?.sdt ? ` · ${ncc.sdt}` : ""}
            </Descriptions.Item>
            <Descriptions.Item label="Ngày đặt">{fmtDate(data.ngayDat)}</Descriptions.Item>
            <Descriptions.Item label="Dự kiến nhận">{fmtDate(data.ngayDuKienNhan)}</Descriptions.Item>
            <Descriptions.Item label="Người lập">{data.tenNhanVienLap || "—"}</Descriptions.Item>
            <Descriptions.Item label="Người xác nhận">{data.tenNguoiXacNhan || "—"}</Descriptions.Item>
            <Descriptions.Item label="Tổng tiền" span={2}>
              <b>{fmtVnd(data.tongTien)}</b>
            </Descriptions.Item>
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

          <div style={{ fontWeight: 600, margin: "20px 0 8px" }}>Dòng hàng</div>
          <Table
            size="small"
            pagination={false}
            rowKey="id"
            dataSource={data.chiTiet ?? []}
            columns={[
              { title: "Mặt hàng", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
              { title: "Đặt", dataIndex: "soLuongDat", align: "right", render: fmtNumber },
              { title: "Đã nhận", dataIndex: "soLuongDaNhan", align: "right", render: fmtNumber },
              { title: "Đơn giá", dataIndex: "donGia", align: "right", render: fmtVnd },
              { title: "Thành tiền", dataIndex: "thanhTien", align: "right", render: fmtVnd },
            ]}
          />

          <div style={{ fontWeight: 600, margin: "20px 0 8px" }}>Phiếu nhập kho từ đơn này</div>
          <Table
            size="small"
            pagination={false}
            rowKey="id"
            dataSource={data.phieuNhap ?? []}
            locale={{ emptyText: "Chưa nhận hàng lần nào" }}
            columns={[
              { title: "Mã phiếu", dataIndex: "maPhieu" },
              { title: "Thời gian", dataIndex: "ngayGio", render: fmtDateTime },
              { title: "Số lượng", dataIndex: "tongSoLuong", align: "right", render: fmtNumber },
              {
                title: "Trạng thái",
                dataIndex: "trangThai",
                render: (v) => {
                  const s = resolveStatus(PHIEU_KHO_TRANG_THAI, v);
                  return <Tag color={s.color}>{s.label}</Tag>;
                },
              },
            ]}
          />
        </>
      )}
    </Drawer>
  );
}
