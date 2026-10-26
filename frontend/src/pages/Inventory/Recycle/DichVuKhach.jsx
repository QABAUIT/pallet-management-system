import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button, Descriptions, Drawer, Modal, Popconfirm, Space, Table, Tag, Tooltip } from "antd";
import {
  CarOutlined,
  CheckOutlined,
  CloseCircleOutlined,
  DollarOutlined,
  EyeOutlined,
  FileTextOutlined,
  InboxOutlined,
  ToolOutlined,
} from "@ant-design/icons";
import { ClipboardList, FileWarning, Hammer, Truck } from "lucide-react";

import InventoryPage from "../../../components/inventory/InventoryPage";
import RecycleTabs from "../../../components/recycle/RecycleTabs";
import ListToolbar from "../../../components/recycle/ListToolbar";
import {
  KetQuaModal,
  LapHoaDonModal,
  MuaLaiModal,
  TiepNhanModal,
} from "../../../components/recycle/DichVuModals";
import { DICH_VU_GIAI_DOAN, gomGiaiDoan } from "../../../components/recycle/dichVu";
import { fmtDate } from "../../../components/recycle/helpers";
import StatCard from "../../../components/common/StatCard";
import AppTable from "../../../components/common/AppTable";
import Pagination from "../../../components/common/Pagination";
import { dichVuApi } from "../../../api/recycleApi";
import { useLocalList } from "../../../hooks/useLocalList";
import { usePhanQuyen } from "../../../hooks/usePhanQuyen";
import { askReason } from "../../../utils/askReason";
import { fmtDateTime, fmtNumber, fmtVnd } from "../../../utils/format";
import { notifyError, notifySuccess } from "../../../utils/notify";
import { resolveStatus } from "../../../utils/statusTag";

import "../../../components/layout/style.css";

const SEARCH_FIELDS = ["maPhieu", "tenKhachHang", "maHoaDon", "tenKho"];

// Gộp giai đoạn vào trường trangThai để dùng chung bộ lọc (hàm ổn định, đặt ngoài component)
const fetchDichVu = () => dichVuApi.danhSach().then(gomGiaiDoan);

/** Hàng của khách đang giữ trong kho (sổ riêng, không tính vào tồn kho công ty). */
function TonKhachModal({ onClose }) {
  const [rows, setRows] = useState(null);
  useEffect(() => {
    let ignore = false;
    dichVuApi
      .tonKhach()
      .then((d) => !ignore && setRows(d?.dong ?? []))
      .catch((err) => !ignore && notifyError(err));
    return () => {
      ignore = true;
    };
  }, []);

  return (
    <Modal open centered width={760} title="Hàng của khách đang giữ" footer={null} onCancel={onClose}>
      <Table
        size="small"
        loading={rows === null}
        pagination={false}
        rowKey={(r) => `${r.khachHangId}-${r.khoId}-${r.matHangId}`}
        dataSource={rows ?? []}
        locale={{ emptyText: "Hiện không giữ hàng nào của khách" }}
        columns={[
          { title: "Khách hàng", dataIndex: "tenKhachHang" },
          { title: "Kho", dataIndex: "tenKho" },
          { title: "Mặt hàng", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
          {
            title: "Số lượng",
            align: "right",
            render: (_, r) => `${fmtNumber(r.soLuong)} ${r.donViTinh ?? ""}`,
          },
        ]}
      />
    </Modal>
  );
}

function ChiTietDrawer({ r, onClose }) {
  const navigate = useNavigate();
  const st = resolveStatus(DICH_VU_GIAI_DOAN, r.trangThai);
  return (
    <Drawer open width={640} title={`Dịch vụ tháo dỡ ${r.maPhieu}`} onClose={onClose}>
      <Descriptions bordered size="small" column={2} styles={{ label: { fontWeight: 600, width: 150 } }}>
        <Descriptions.Item label="Giai đoạn">
          <Tag color={st.color}>{st.label}</Tag>
        </Descriptions.Item>
        <Descriptions.Item label="Kho">{r.tenKho || "—"}</Descriptions.Item>
        <Descriptions.Item label="Khách hàng" span={2}>
          {r.tenKhachHang}
        </Descriptions.Item>
        <Descriptions.Item label="Pallet nhận">{fmtNumber(r.soPalletNhan)}</Descriptions.Item>
        <Descriptions.Item label="Đơn giá dịch vụ">{fmtVnd(r.donGiaDichVu)}</Descriptions.Item>
        <Descriptions.Item label="Pallet đã tháo dỡ">{fmtNumber(r.soPalletThaoDo)}</Descriptions.Item>
        <Descriptions.Item label="Pallet trả nguyên">{fmtNumber(r.soPalletTraNguyen)}</Descriptions.Item>
        <Descriptions.Item label="Thanh nguyên">{fmtNumber(r.soThanhNguyen)}</Descriptions.Item>
        <Descriptions.Item label="Cục vuông">{fmtNumber(r.soCucVuong)}</Descriptions.Item>
        <Descriptions.Item label="Thanh cần xử lý">{fmtNumber(r.soThanhCanXuLy)}</Descriptions.Item>
        <Descriptions.Item label="Cục cần xử lý">{fmtNumber(r.soCucCanXuLy)}</Descriptions.Item>
        <Descriptions.Item label="Phế liệu (kg)">{fmtNumber(r.kgPheLieu)}</Descriptions.Item>
        <Descriptions.Item label="Đã mua lại / còn của khách (kg)">
          {fmtNumber(r.kgPheLieuDaMua)} / {fmtNumber(r.kgPheLieuConLai)}
        </Descriptions.Item>
        <Descriptions.Item label="Phí dịch vụ" span={2}>
          <b>{fmtVnd(r.phiDichVu)}</b>
        </Descriptions.Item>
        <Descriptions.Item label="Hóa đơn" span={2}>
          {r.hoaDonId ? (
            <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/hoa-don/${r.hoaDonId}`)}>
              {r.maHoaDon}
            </Button>
          ) : (
            "Chưa lập"
          )}
        </Descriptions.Item>
        <Descriptions.Item label="Người tiếp nhận">{r.tenNguoiTiepNhan || "—"}</Descriptions.Item>
        <Descriptions.Item label="Ngày tiếp nhận">{fmtDate(r.ngayTiepNhan)}</Descriptions.Item>
        <Descriptions.Item label="Người duyệt">{r.tenNguoiDuyet || "—"}</Descriptions.Item>
        <Descriptions.Item label="Thời gian duyệt">{fmtDateTime(r.thoiGianDuyet)}</Descriptions.Item>
        <Descriptions.Item label="Thời gian trả hàng" span={2}>
          {fmtDateTime(r.thoiGianTraHang)}
        </Descriptions.Item>
        {r.ghiChu && (
          <Descriptions.Item label="Ghi chú" span={2}>
            {r.ghiChu}
          </Descriptions.Item>
        )}
        {r.lyDoHuy && (
          <Descriptions.Item label="Lý do huỷ" span={2}>
            {r.lyDoHuy}
          </Descriptions.Item>
        )}
      </Descriptions>
    </Drawer>
  );
}

// Quy trình 11: dịch vụ tháo dỡ pallet cho khách (route /kho/tai-che/dich-vu-khach)
export default function DichVuKhach() {
  const list = useLocalList(fetchDichVu, { searchFields: SEARCH_FIELDS });
  const { laQuanLy, duocThaoTacKho } = usePhanQuyen();
  const [modal, setModal] = useState(null); // { loai, record? }
  const [viewId, setViewId] = useState(null);
  const [tonOpen, setTonOpen] = useState(false);

  const close = useCallback(() => setModal(null), []);
  const done = () => {
    setModal(null);
    list.reload();
  };

  const run = async (fn, okMsg) => {
    try {
      await fn();
      notifySuccess(okMsg);
      list.reload();
    } catch (err) {
      notifyError(err);
    }
  };

  const btn = (title, icon, onClick, props = {}) => (
    <Tooltip title={title}>
      <Button type="text" icon={icon} onClick={onClick} {...props} />
    </Tooltip>
  );

  const columns = [
    { key: "maPhieu", header: "MÃ PHIẾU", render: (r) => <b>{r.maPhieu}</b> },
    { key: "tenKhachHang", header: "KHÁCH HÀNG" },
    {
      key: "pallet",
      header: "PALLET NHẬN / THÁO",
      align: "right",
      render: (r) => `${fmtNumber(r.soPalletNhan)} / ${fmtNumber(r.soPalletThaoDo)}`,
    },
    { key: "phi", header: "PHÍ DỊCH VỤ", align: "right", render: (r) => fmtVnd(r.phiDichVu) },
    {
      key: "kg",
      header: "PHẾ LIỆU CÒN (KG)",
      align: "right",
      render: (r) => fmtNumber(r.kgPheLieuConLai),
    },
    {
      key: "hoaDon",
      header: "HÓA ĐƠN",
      render: (r) => r.maHoaDon || <span style={{ color: "#bfbfbf" }}>Chưa lập</span>,
    },
    { key: "ngayTiepNhan", header: "NGÀY NHẬN", render: (r) => fmtDate(r.ngayTiepNhan) },
    {
      key: "trangThai",
      header: "GIAI ĐOẠN",
      render: (r) => {
        const st = resolveStatus(DICH_VU_GIAI_DOAN, r.trangThai);
        return <Tag color={st.color}>{st.label}</Tag>;
      },
    },
    {
      key: "action",
      header: "THAO TÁC",
      align: "center",
      render: (r) => {
        const gd = r.trangThai;
        const dangMo = gd === "da_tiep_nhan" || gd === "cho_duyet";
        const daDuyet = gd === "da_duyet" || gd === "da_tra";
        return (
          <Space size={0} wrap>
            {btn("Xem chi tiết", <EyeOutlined />, () => setViewId(r.id), { style: { color: "#8c8c8c" } })}
            {duocThaoTacKho &&
              dangMo &&
              btn(
                gd === "da_tiep_nhan" ? "Ghi kết quả tháo dỡ" : "Sửa kết quả tháo dỡ",
                <ToolOutlined />,
                () => setModal({ loai: "ketQua", record: r }),
                { style: { color: "#1677ff" } },
              )}
            {laQuanLy && gd === "cho_duyet" && (
              <Popconfirm
                title="Duyệt kết quả tháo dỡ?"
                description="Hàng thu được sẽ ghi vào sổ hàng của khách."
                okText="Duyệt"
                cancelText="Đóng"
                onConfirm={() => run(() => dichVuApi.duyet(r.id), "Đã duyệt kết quả tháo dỡ")}
              >
                {btn("Duyệt kết quả", <CheckOutlined />, undefined, { style: { color: "#389e0d" } })}
              </Popconfirm>
            )}
            {laQuanLy &&
              daDuyet &&
              !r.hoaDonId &&
              btn("Lập hóa đơn dịch vụ", <FileTextOutlined />, () => setModal({ loai: "hoaDon", record: r }), {
                style: { color: "#d46b08" },
              })}
            {laQuanLy &&
              daDuyet &&
              r.kgPheLieuConLai > 0 &&
              btn("Mua lại phế liệu", <DollarOutlined />, () => setModal({ loai: "muaLai", record: r }), {
                style: { color: "#531dab" },
              })}
            {duocThaoTacKho && gd === "da_duyet" && (
              <Popconfirm
                title="Trả hàng cho khách?"
                description="Pallet nguyên, thanh/cục và phế liệu còn lại sẽ được trả, không thể hoàn tác."
                okText="Trả hàng"
                cancelText="Đóng"
                onConfirm={() => run(() => dichVuApi.traHang(r.id), "Đã trả hàng cho khách")}
              >
                {btn("Trả hàng cho khách", <CarOutlined />, undefined, { style: { color: "#08979c" } })}
              </Popconfirm>
            )}
            {duocThaoTacKho &&
              dangMo &&
              btn(
                "Huỷ phiếu",
                <CloseCircleOutlined />,
                () =>
                  askReason({
                    title: `Huỷ phiếu ${r.maPhieu}?`,
                    onOk: (lyDo) => run(() => dichVuApi.huy(r.id, lyDo), "Đã huỷ phiếu dịch vụ"),
                  }),
                { danger: true },
              )}
          </Space>
        );
      },
    },
  ];

  const show = (v) => (list.loading ? "…" : v);
  const rows = list.rows;
  const viewRow = rows.find((r) => r.id === viewId);

  return (
    <InventoryPage>
      <RecycleTabs />

      <div className="stat-row">
        <StatCard
          label="CHỜ THÁO DỠ"
          value={show(fmtNumber(rows.filter((r) => r.trangThai === "da_tiep_nhan").length))}
          icon={Hammer}
          tone="blue"
          badge="phiếu"
          caption="Đã nhận pallet, chưa ghi kết quả"
        />
        <StatCard
          label="CHỜ DUYỆT KẾT QUẢ"
          value={show(fmtNumber(rows.filter((r) => r.trangThai === "cho_duyet").length))}
          icon={ClipboardList}
          tone="amber"
          badge="phiếu"
          caption="Chờ GD/PGD duyệt"
        />
        <StatCard
          label="CHỜ TRẢ HÀNG"
          value={show(fmtNumber(rows.filter((r) => r.trangThai === "da_duyet").length))}
          icon={Truck}
          tone="purple"
          badge="phiếu"
          caption="Đã duyệt, hàng còn ở kho"
        />
        <StatCard
          label="CHƯA LẬP HÓA ĐƠN"
          value={show(
            fmtNumber(
              rows.filter((r) => (r.trangThai === "da_duyet" || r.trangThai === "da_tra") && !r.hoaDonId).length,
            ),
          )}
          icon={FileWarning}
          tone="red"
          badge="phiếu"
          caption="Đã tháo dỡ nhưng chưa thu phí"
        />
      </div>

      <ListToolbar
        placeholder="Tìm theo mã phiếu, khách hàng, hóa đơn..."
        keyword={list.keyword}
        onKeywordChange={list.setKeyword}
        statusMap={DICH_VU_GIAI_DOAN}
        trangThai={list.trangThai}
        onTrangThaiChange={list.setTrangThai}
        onCreate={duocThaoTacKho ? () => setModal({ loai: "tiepNhan" }) : undefined}
        createLabel="Tiếp nhận pallet của khách"
        extra={
          <Button size="large" icon={<InboxOutlined />} style={{ borderRadius: 6 }} onClick={() => setTonOpen(true)}>
            Hàng của khách đang giữ
          </Button>
        }
      />

      <AppTable
        columns={columns}
        data={list.paged}
        rowKey={(r) => r.id}
        emptyText={list.loading ? "Đang tải dữ liệu..." : "Chưa có phiếu dịch vụ nào"}
      />

      <Pagination
        currentPage={list.page}
        totalItems={list.filtered.length}
        itemsPerPage={list.pageSize}
        itemLabel="phiếu dịch vụ"
        onPageChange={list.setPage}
      />

      {modal?.loai === "tiepNhan" && <TiepNhanModal onCancel={close} onSaved={done} />}
      {modal?.loai === "ketQua" && <KetQuaModal record={modal.record} onCancel={close} onSaved={done} />}
      {modal?.loai === "hoaDon" && <LapHoaDonModal record={modal.record} onCancel={close} onSaved={done} />}
      {modal?.loai === "muaLai" && <MuaLaiModal record={modal.record} onCancel={close} onSaved={done} />}
      {tonOpen && <TonKhachModal onClose={() => setTonOpen(false)} />}
      {viewRow && <ChiTietDrawer r={viewRow} onClose={() => setViewId(null)} />}
    </InventoryPage>
  );
}
