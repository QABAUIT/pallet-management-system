import { useState } from "react";
import { Button, Space, Tag, Tooltip } from "antd";
import {
  CheckOutlined,
  CloseCircleOutlined,
  EditOutlined,
  EyeOutlined,
  InboxOutlined,
} from "@ant-design/icons";
import { AlarmClock, ClipboardList, Truck, Wallet } from "lucide-react";

import InventoryPage from "../../components/inventory/InventoryPage";
import ListToolbar from "../../components/recycle/ListToolbar";
import DonMuaFormModal from "../../components/recycle/DonMuaFormModal";
import NhanHangModal from "../../components/recycle/NhanHangModal";
import DonMuaDrawer from "../../components/recycle/DonMuaDrawer";
import { fmtDate } from "../../components/recycle/helpers";
import StatCard from "../../components/common/StatCard";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import { donMuaApi } from "../../api/recycleApi";
import { useLocalList } from "../../hooks/useLocalList";
import { usePhanQuyen } from "../../hooks/usePhanQuyen";
import { askReason } from "../../utils/askReason";
import { fmtNumber, fmtVnd } from "../../utils/format";
import { notifyError, notifySuccess } from "../../utils/notify";
import { DON_MUA_TRANG_THAI, resolveStatus } from "../../utils/statusTag";

import "../../components/layout/style.css";

const SEARCH_FIELDS = ["maDonMua", "tenNhaCungCap", "tenKho", "tenNhanVienLap"];

// Đơn đang chờ nhận hàng: đã xác nhận, chưa nhận đủ
const dangCho = (r) =>
  r.trangThai !== "nhap" && r.trangThai !== "da_huy" && r.tongSoLuongDaNhan < r.tongSoLuongDat;

// Quy trình 3: đơn mua (route /kho/don-mua)
export default function DonMua() {
  const list = useLocalList(donMuaApi.danhSach, { searchFields: SEARCH_FIELDS });
  const { laQuanLy, duocThaoTacKho } = usePhanQuyen();
  const [form, setForm] = useState(null); // { id } | {} (tạo mới)
  const [nhanId, setNhanId] = useState(null);
  const [viewId, setViewId] = useState(null);

  const run = async (fn, okMsg) => {
    try {
      await fn();
      notifySuccess(okMsg);
      list.reload();
    } catch (err) {
      notifyError(err);
    }
  };

  const columns = [
    {
      key: "maDonMua",
      header: "MÃ ĐƠN MUA",
      render: (r) => (
        <span>
          <b>{r.maDonMua}</b> {r.treHan && <Tag color="red">Trễ hạn</Tag>}
        </span>
      ),
    },
    { key: "tenNhaCungCap", header: "NHÀ CUNG CẤP" },
    { key: "tenKho", header: "KHO" },
    { key: "ngayDat", header: "NGÀY ĐẶT", render: (r) => fmtDate(r.ngayDat) },
    { key: "ngayDuKienNhan", header: "DỰ KIẾN NHẬN", render: (r) => fmtDate(r.ngayDuKienNhan) },
    {
      key: "soLuong",
      header: "ĐÃ NHẬN / ĐẶT",
      align: "right",
      render: (r) => `${fmtNumber(r.tongSoLuongDaNhan)} / ${fmtNumber(r.tongSoLuongDat)}`,
    },
    { key: "tongTien", header: "TỔNG TIỀN", align: "right", render: (r) => fmtVnd(r.tongTien) },
    {
      key: "trangThai",
      header: "TRẠNG THÁI",
      render: (r) => {
        const st = resolveStatus(DON_MUA_TRANG_THAI, r.trangThai);
        return <Tag color={st.color}>{st.label}</Tag>;
      },
    },
    {
      key: "action",
      header: "THAO TÁC",
      align: "center",
      render: (r) => {
        const laNhap = r.trangThai === "nhap";
        return (
          <Space size="small">
            <Tooltip title="Xem chi tiết">
              <Button type="text" icon={<EyeOutlined />} style={{ color: "#8c8c8c" }} onClick={() => setViewId(r.id)} />
            </Tooltip>
            <Tooltip title={laNhap ? "Chỉnh sửa" : "Chỉ sửa được đơn nháp"}>
              <Button
                type="text"
                icon={<EditOutlined />}
                style={{ color: "#8c8c8c" }}
                disabled={!laNhap}
                onClick={() => setForm({ id: r.id })}
              />
            </Tooltip>
            {laQuanLy && laNhap && (
              <Tooltip title="Xác nhận đơn mua">
                <Button
                  type="text"
                  icon={<CheckOutlined />}
                  style={{ color: "#389e0d" }}
                  onClick={() => run(() => donMuaApi.xacNhan(r.id), "Đã xác nhận đơn mua")}
                />
              </Tooltip>
            )}
            {dangCho(r) && (
              <Tooltip title="Nhận hàng">
                <Button
                  type="text"
                  icon={<InboxOutlined />}
                  style={{ color: "#1677ff" }}
                  onClick={() => setNhanId(r.id)}
                />
              </Tooltip>
            )}
            {laQuanLy && r.trangThai !== "da_huy" && r.tongSoLuongDaNhan === 0 && (
              <Tooltip title="Huỷ đơn">
                <Button
                  type="text"
                  danger
                  icon={<CloseCircleOutlined />}
                  onClick={() =>
                    askReason({
                      title: `Huỷ đơn mua ${r.maDonMua}?`,
                      onOk: (lyDo) => run(() => donMuaApi.huy(r.id, lyDo), "Đã huỷ đơn mua"),
                    })
                  }
                />
              </Tooltip>
            )}
          </Space>
        );
      },
    },
  ];

  const show = (v) => (list.loading ? "…" : v);
  const rows = list.rows;
  const cho = rows.filter(dangCho);

  return (
    <InventoryPage>
      <div className="stat-row">
        <StatCard
          label="ĐƠN CHỜ XÁC NHẬN"
          value={show(fmtNumber(rows.filter((r) => r.trangThai === "nhap").length))}
          icon={ClipboardList}
          tone="amber"
          badge="đơn"
          caption="Đơn nháp, chờ GD/PGD"
        />
        <StatCard
          label="ĐANG CHỜ NHẬN HÀNG"
          value={show(fmtNumber(cho.length))}
          icon={Truck}
          tone="blue"
          badge="đơn"
          caption="Đã xác nhận, chưa nhận đủ"
        />
        <StatCard
          label="ĐƠN TRỄ HẠN"
          value={show(fmtNumber(rows.filter((r) => r.treHan).length))}
          icon={AlarmClock}
          tone="red"
          badge="đơn"
          caption="Quá ngày dự kiến"
        />
        <StatCard
          label="GIÁ TRỊ ĐƠN ĐANG MỞ"
          value={show(fmtVnd(cho.reduce((a, r) => a + Number(r.tongTien || 0), 0)))}
          icon={Wallet}
          tone="green"
          caption="Các đơn đang chờ nhận"
        />
      </div>

      <ListToolbar
        placeholder="Tìm theo mã đơn, nhà cung cấp..."
        keyword={list.keyword}
        onKeywordChange={list.setKeyword}
        statusMap={DON_MUA_TRANG_THAI}
        trangThai={list.trangThai}
        onTrangThaiChange={list.setTrangThai}
        onCreate={duocThaoTacKho ? () => setForm({}) : undefined}
        createLabel="Tạo đơn mua"
      />

      <AppTable
        columns={columns}
        data={list.paged}
        rowKey={(r) => r.id}
        emptyText={list.loading ? "Đang tải dữ liệu..." : "Chưa có đơn mua nào"}
      />

      <Pagination
        currentPage={list.page}
        totalItems={list.filtered.length}
        itemsPerPage={list.pageSize}
        itemLabel="đơn mua"
        onPageChange={list.setPage}
      />

      {form && (
        <DonMuaFormModal
          key={form.id ?? "new"}
          id={form.id}
          onCancel={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            list.reload();
          }}
        />
      )}
      {nhanId && (
        <NhanHangModal
          id={nhanId}
          onCancel={() => setNhanId(null)}
          onSaved={() => {
            setNhanId(null);
            list.reload();
          }}
        />
      )}
      {viewId && <DonMuaDrawer id={viewId} onClose={() => setViewId(null)} />}
    </InventoryPage>
  );
}
