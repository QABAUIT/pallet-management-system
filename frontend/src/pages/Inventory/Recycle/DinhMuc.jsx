import { useEffect, useState } from "react";
import { Button, Descriptions, Drawer, Popconfirm, Space, Spin, Table, Tag, Tooltip } from "antd";
import { CheckOutlined, EditOutlined, EyeOutlined, StopOutlined } from "@ant-design/icons";
import { ListChecks, PauseCircle, PlayCircle } from "lucide-react";

import InventoryPage from "../../../components/inventory/InventoryPage";
import RecycleTabs from "../../../components/recycle/RecycleTabs";
import ListToolbar from "../../../components/recycle/ListToolbar";
import DinhMucFormModal from "../../../components/recycle/DinhMucFormModal";
import StatCard from "../../../components/common/StatCard";
import AppTable from "../../../components/common/AppTable";
import Pagination from "../../../components/common/Pagination";
import { dinhMucApi } from "../../../api/recycleApi";
import { useLocalList } from "../../../hooks/useLocalList";
import { usePhanQuyen } from "../../../hooks/usePhanQuyen";
import { fmtNumber } from "../../../utils/format";
import { notifyError, notifySuccess } from "../../../utils/notify";

import "../../../components/layout/style.css";

const SEARCH_FIELDS = ["maDinhMuc", "tenDinhMuc", "maThanhPham", "tenThanhPham"];

// Gộp danh sách đầy đủ với danh sách "chỉ hoạt động" để biết định mức nào đang dùng
// (không phụ thuộc cách đặt tên trạng thái ở DB).
const fetchDinhMuc = () =>
  Promise.all([dinhMucApi.danhSach(false), dinhMucApi.danhSach(true)]).then(([all, act]) => {
    const on = new Set((act ?? []).map((x) => x.id));
    return (all ?? []).map((r) => ({ ...r, dangDung: on.has(r.id) }));
  });

function DinhMucDrawer({ id, onClose }) {
  const [data, setData] = useState(null);
  useEffect(() => {
    let ignore = false;
    dinhMucApi
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

  return (
    <Drawer open width={560} title={data ? `Định mức ${data.maDinhMuc}` : "Chi tiết định mức"} onClose={onClose}>
      {!data ? (
        <div style={{ textAlign: "center", padding: 48 }}>
          <Spin />
        </div>
      ) : (
        <>
          <Descriptions bordered size="small" column={1} styles={{ label: { fontWeight: 600, width: 140 } }}>
            <Descriptions.Item label="Tên định mức">{data.tenDinhMuc}</Descriptions.Item>
            <Descriptions.Item label="Thành phẩm">
              {data.maThanhPham} - {data.tenThanhPham}
            </Descriptions.Item>
            {data.ghiChu && <Descriptions.Item label="Ghi chú">{data.ghiChu}</Descriptions.Item>}
          </Descriptions>
          <div style={{ fontWeight: 600, margin: "20px 0 8px" }}>Vật tư cho 1 thành phẩm</div>
          <Table
            size="small"
            pagination={false}
            rowKey="matHangId"
            dataSource={data.chiTiet ?? []}
            columns={[
              { title: "Vật tư", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
              { title: "Số lượng", dataIndex: "soLuong", align: "right", render: fmtNumber },
            ]}
          />
        </>
      )}
    </Drawer>
  );
}

// Định mức vật tư để đóng pallet (route /kho/tai-che/dinh-muc). Chỉ GD/PGD tạo, sửa, ngừng, kích hoạt.
export default function DinhMuc() {
  const list = useLocalList(fetchDinhMuc, { searchFields: SEARCH_FIELDS });
  const { laQuanLy } = usePhanQuyen();
  const [form, setForm] = useState(null); // { id } | {}
  const [viewId, setViewId] = useState(null);

  const doiTrangThai = async (r) => {
    try {
      if (r.dangDung) await dinhMucApi.ngung(r.id);
      else await dinhMucApi.kichHoat(r.id);
      notifySuccess(r.dangDung ? "Đã ngừng định mức" : "Đã kích hoạt định mức");
      list.reload();
    } catch (err) {
      notifyError(err);
    }
  };

  const columns = [
    { key: "maDinhMuc", header: "MÃ ĐỊNH MỨC", render: (r) => <b>{r.maDinhMuc}</b> },
    { key: "tenDinhMuc", header: "TÊN ĐỊNH MỨC" },
    {
      key: "thanhPham",
      header: "THÀNH PHẨM",
      render: (r) => `${r.maThanhPham ?? ""} - ${r.tenThanhPham ?? ""}`,
    },
    {
      key: "soVatTu",
      header: "SỐ VẬT TƯ",
      align: "right",
      render: (r) => (r.chiTiet ? fmtNumber(r.chiTiet.length) : "—"),
    },
    {
      key: "trangThai",
      header: "TRẠNG THÁI",
      render: (r) => (
        <Tag color={r.dangDung ? "green" : "default"}>{r.dangDung ? "Đang dùng" : "Đã ngừng"}</Tag>
      ),
    },
    {
      key: "action",
      header: "THAO TÁC",
      align: "center",
      render: (r) => (
        <Space size="small">
          <Tooltip title="Xem chi tiết">
            <Button type="text" icon={<EyeOutlined />} style={{ color: "var(--text-muted)" }} onClick={() => setViewId(r.id)} />
          </Tooltip>
          {laQuanLy && (
            <>
              <Tooltip title="Chỉnh sửa">
                <Button
                  type="text"
                  icon={<EditOutlined />}
                  style={{ color: "var(--text-muted)" }}
                  onClick={() => setForm({ id: r.id })}
                />
              </Tooltip>
              <Popconfirm
                title={r.dangDung ? "Ngừng định mức này?" : "Kích hoạt lại định mức này?"}
                okText="Đồng ý"
                cancelText="Đóng"
                onConfirm={() => doiTrangThai(r)}
              >
                <Tooltip title={r.dangDung ? "Ngừng định mức" : "Kích hoạt"}>
                  <Button
                    type="text"
                    danger={r.dangDung}
                    icon={r.dangDung ? <StopOutlined /> : <CheckOutlined />}
                    style={r.dangDung ? undefined : { color: "var(--color-green)" }}
                  />
                </Tooltip>
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ];

  const show = (v) => (list.loading ? "…" : v);
  const dang = list.rows.filter((r) => r.dangDung).length;

  return (
    <InventoryPage>
      <RecycleTabs />

      <div className="stat-row">
        <StatCard label="TỔNG ĐỊNH MỨC" value={show(fmtNumber(list.rows.length))} icon={ListChecks} tone="blue" badge="định mức" />
        <StatCard label="ĐANG DÙNG" value={show(fmtNumber(dang))} icon={PlayCircle} tone="green" badge="định mức" caption="Chọn được khi đóng pallet" />
        <StatCard label="ĐÃ NGỪNG" value={show(fmtNumber(list.rows.length - dang))} icon={PauseCircle} tone="amber" badge="định mức" />
      </div>

      <ListToolbar
        placeholder="Tìm theo mã, tên định mức, thành phẩm..."
        keyword={list.keyword}
        onKeywordChange={list.setKeyword}
        onCreate={laQuanLy ? () => setForm({}) : undefined}
        createLabel="Tạo định mức"
      />

      <AppTable
        columns={columns}
        data={list.paged}
        rowKey={(r) => r.id}
        emptyText={list.loading ? "Đang tải dữ liệu..." : "Chưa có định mức nào"}
      />

      <Pagination
        currentPage={list.page}
        totalItems={list.filtered.length}
        itemsPerPage={list.pageSize}
        itemLabel="định mức"
        onPageChange={list.setPage}
      />

      {form && (
        <DinhMucFormModal
          key={form.id ?? "new"}
          id={form.id}
          onCancel={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            list.reload();
          }}
        />
      )}
      {viewId && <DinhMucDrawer id={viewId} onClose={() => setViewId(null)} />}
    </InventoryPage>
  );
}
