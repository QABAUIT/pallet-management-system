import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Descriptions, Drawer, Popconfirm, Spin, Table, Tag } from "antd";
import { Check, CheckCircle2, Clock, Eye, FilePlus2, Pencil, Plus, Send, Wallet, X } from "lucide-react";

import ReceiptTabs from "../../components/receipt/ReceiptTabs";
import BaoGiaFormModal from "../../components/receipt/BaoGiaFormModal";
import { BAO_GIA_TRANG_THAI } from "../../components/receipt/baoGia";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import SearchInput from "../../components/common/SearchInput";
import StatCard from "../../components/common/StatCard";
import OtherTypeFilter from "../../components/common/OtherTypeFilter";
import { baoGiaApi } from "../../api/banHangApi";
import { useLocalList } from "../../hooks/useLocalList";
import { askReason } from "../../utils/askReason";
import { notifyError, notifySuccess } from "../../utils/notify";
import { formatDate, formatMoney } from "../../utils/receiptFormat";
import { resolveStatus } from "../../utils/statusTag";

const SEARCH_FIELDS = ["maBaoGia", "tenKhachHang", "maHoaDon"];
const DEFAULT_FILTER = { status: null };
const FILTER_GROUPS = [
  {
    key: "status",
    type: "radio",
    options: Object.entries(BAO_GIA_TRANG_THAI).map(([value, { label }]) => ({ value, label })),
  },
];

function BaoGiaDrawer({ id, onClose }) {
  const [data, setData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    let ignore = false;
    baoGiaApi
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

  const st = resolveStatus(BAO_GIA_TRANG_THAI, data?.trangThai);

  return (
    <Drawer open width={720} title={data ? `Báo giá ${data.maBaoGia}` : "Chi tiết báo giá"} onClose={onClose}>
      {!data ? (
        <div style={{ textAlign: "center", padding: 48 }}>
          <Spin />
        </div>
      ) : (
        <>
          <Descriptions bordered size="small" column={2} styles={{ label: { fontWeight: 600, width: 140 } }}>
            <Descriptions.Item label="Trạng thái">
              <Tag color={st.color}>{st.label}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Người tạo">{data.tenNguoiTao || "—"}</Descriptions.Item>
            <Descriptions.Item label="Khách hàng" span={2}>
              {data.tenKhachHang}
            </Descriptions.Item>
            <Descriptions.Item label="Ngày tạo">{formatDate(data.ngayTao)}</Descriptions.Item>
            <Descriptions.Item label="Hiệu lực đến">{formatDate(data.hanHieuLuc)}</Descriptions.Item>
            <Descriptions.Item label="Hóa đơn" span={2}>
              {data.hoaDonId ? (
                <a onClick={() => navigate(`/hoa-don/${data.hoaDonId}`)}>{data.maHoaDon}</a>
              ) : (
                "Chưa lập"
              )}
            </Descriptions.Item>
            {data.ghiChu && (
              <Descriptions.Item label="Ghi chú" span={2}>
                {data.ghiChu}
              </Descriptions.Item>
            )}
          </Descriptions>

          <div style={{ fontWeight: 600, margin: "20px 0 8px" }}>Mặt hàng</div>
          <Table
            size="small"
            pagination={false}
            rowKey="matHangId"
            dataSource={data.chiTiet ?? []}
            columns={[
              { title: "Mặt hàng", render: (_, r) => `${r.maMatHang} - ${r.tenMatHang}` },
              { title: "SL", dataIndex: "soLuong", align: "right" },
              { title: "Đơn giá", dataIndex: "donGia", align: "right", render: formatMoney },
              { title: "CK %", dataIndex: "tyLeChietKhau", align: "right", render: (v) => Number(v ?? 0) },
              { title: "Thành tiền", dataIndex: "thanhTien", align: "right", render: (v) => <b>{formatMoney(v)}</b> },
            ]}
          />

          <div className="rc-ledger" style={{ marginTop: 16 }}>
            <div className="rc-ledger-row"><span>Tiền hàng</span><b>{formatMoney(data.tongTienHang)} đ</b></div>
            {Number(data.tienGiamGiaTong) > 0 && (
              <div className="rc-ledger-row"><span>Giảm giá</span><b>-{formatMoney(data.tienGiamGiaTong)} đ</b></div>
            )}
            <div className="rc-ledger-row"><span>VAT ({Number(data.tyLeVat)}%)</span><b>{formatMoney(data.tienVat)} đ</b></div>
            <div className="rc-ledger-total"><strong>Tổng cộng</strong><span>{formatMoney(data.tongTien)} đ</span></div>
          </div>
        </>
      )}
    </Drawer>
  );
}

// Báo giá bán hàng (route /hoa-don/bao-gia): lập, gửi khách, ghi nhận kết quả, chuyển thành hóa đơn
export default function BaoGia() {
  const navigate = useNavigate();
  const list = useLocalList(baoGiaApi.danhSach, { searchFields: SEARCH_FIELDS });
  const [form, setForm] = useState(null); // { id } | {}
  const [viewId, setViewId] = useState(null);

  const run = async (fn, okMsg) => {
    try {
      const res = await fn();
      notifySuccess(okMsg);
      list.reload();
      return res;
    } catch (err) {
      notifyError(err);
      return null;
    }
  };

  const chuyen = async (r) => {
    const hd = await run(() => baoGiaApi.chuyenHoaDon(r.id), "Đã lập hóa đơn từ báo giá");
    if (hd?.id) navigate(`/hoa-don/${hd.id}`);
  };

  const columns = [
    { key: "ma", header: "Mã báo giá", width: "140px", render: (r) => <span className="rc-name">{r.maBaoGia}</span> },
    {
      key: "kh",
      header: "Khách hàng",
      render: (r) => (
        <div>
          <div className="rc-name">{r.tenKhachHang}</div>
          <div className="muted-text">Tạo {formatDate(r.ngayTao)} · hiệu lực đến {formatDate(r.hanHieuLuc)}</div>
        </div>
      ),
    },
    { key: "tong", header: "Tổng tiền", align: "right", render: (r) => <b>{formatMoney(r.tongTien)} đ</b> },
    {
      key: "tt",
      header: "Trạng thái",
      align: "center",
      render: (r) => {
        const s = resolveStatus(BAO_GIA_TRANG_THAI, r.trangThai);
        return <Tag color={s.color}>{s.label}</Tag>;
      },
    },
    {
      key: "hd",
      header: "Hóa đơn",
      render: (r) =>
        r.hoaDonId ? (
          <a onClick={() => navigate(`/hoa-don/${r.hoaDonId}`)}>{r.maHoaDon}</a>
        ) : (
          <span className="muted-text">Chưa lập</span>
        ),
    },
    {
      key: "act",
      header: "Thao tác",
      align: "center",
      width: "220px",
      render: (r) => {
        const t = r.trangThai;
        return (
          <div style={{ display: "flex", justifyContent: "center", gap: 2, flexWrap: "wrap" }}>
            <button className="icon-btn" title="Xem chi tiết" onClick={() => setViewId(r.id)}>
              <Eye size={16} />
            </button>
            {t === "nhap" && (
              <>
                <button className="icon-btn" title="Chỉnh sửa" onClick={() => setForm({ id: r.id })}>
                  <Pencil size={16} />
                </button>
                <Popconfirm
                  title="Gửi báo giá cho khách?"
                  description="Sau khi gửi sẽ không sửa được nữa."
                  okText="Gửi"
                  cancelText="Đóng"
                  onConfirm={() => run(() => baoGiaApi.gui(r.id), "Đã gửi báo giá cho khách")}
                >
                  <button className="icon-btn" title="Gửi khách">
                    <Send size={16} color="#1d4ed8" />
                  </button>
                </Popconfirm>
              </>
            )}
            {t === "da_gui" && (
              <>
                <Popconfirm
                  title="Khách đã chấp nhận báo giá?"
                  okText="Chấp nhận"
                  cancelText="Đóng"
                  onConfirm={() => run(() => baoGiaApi.chapNhan(r.id), "Khách đã chấp nhận báo giá")}
                >
                  <button className="icon-btn" title="Khách chấp nhận">
                    <Check size={16} color="#16a34a" />
                  </button>
                </Popconfirm>
                <button
                  className="icon-btn"
                  title="Khách từ chối"
                  onClick={() =>
                    askReason({
                      title: `Khách từ chối báo giá ${r.maBaoGia}?`,
                      okText: "Ghi nhận từ chối",
                      onOk: (lyDo) => run(() => baoGiaApi.tuChoi(r.id, lyDo), "Đã ghi nhận khách từ chối"),
                    })
                  }
                >
                  <X size={16} color="#dc2626" />
                </button>
              </>
            )}
            {t === "da_chap_nhan" && !r.hoaDonId && (
              <Popconfirm
                title="Lập hóa đơn từ báo giá này?"
                description="Hóa đơn lập theo đúng giá và số lượng đã báo, ở trạng thái chờ thanh toán."
                okText="Lập hóa đơn"
                cancelText="Đóng"
                onConfirm={() => chuyen(r)}
              >
                <button className="icon-btn" title="Chuyển thành hóa đơn">
                  <FilePlus2 size={16} color="#d46b08" />
                </button>
              </Popconfirm>
            )}
          </div>
        );
      },
    },
  ];

  const rows = list.rows;
  const cho = rows.filter((r) => r.trangThai === "da_gui");

  return (
    <div>
      <ReceiptTabs />

      <div className="stat-row">
        <StatCard
          label="BÁO GIÁ NHÁP"
          value={rows.filter((r) => r.trangThai === "nhap").length}
          icon={Clock}
          tone="amber"
          badge="báo giá"
          caption="Chưa gửi khách"
        />
        <StatCard
          label="ĐANG CHỜ KHÁCH TRẢ LỜI"
          value={cho.length}
          icon={Send}
          tone="blue"
          badge="báo giá"
          caption={`Giá trị ${formatMoney(cho.reduce((s, r) => s + Number(r.tongTien), 0))} đ`}
        />
        <StatCard
          label="CHƯA LẬP HÓA ĐƠN"
          value={rows.filter((r) => r.trangThai === "da_chap_nhan" && !r.hoaDonId).length}
          icon={Wallet}
          tone="red"
          badge="báo giá"
          caption="Khách đã chấp nhận"
        />
        <StatCard
          label="ĐÃ LẬP HÓA ĐƠN"
          value={rows.filter((r) => r.hoaDonId).length}
          icon={CheckCircle2}
          tone="green"
          badge="báo giá"
        />
      </div>

      <div className="rc-toolbar">
        <SearchInput value={list.keyword} onChange={list.setKeyword} placeholder="Tìm mã báo giá, khách hàng..." />
        <OtherTypeFilter
          label="Lọc trạng thái"
          groups={FILTER_GROUPS}
          value={{ status: list.trangThai }}
          defaultValue={DEFAULT_FILTER}
          onApply={(v) => list.setTrangThai(v.status ?? null)}
        />
        <div style={{ marginLeft: "auto" }}>
          <button className="btn-solid" onClick={() => setForm({})}>
            <Plus size={16} /> Tạo báo giá
          </button>
        </div>
      </div>

      {list.loading ? (
        <p className="muted-text">Đang tải dữ liệu...</p>
      ) : (
        <AppTable columns={columns} data={list.paged} emptyText="Chưa có báo giá nào" />
      )}

      <div style={{ marginTop: 16 }}>
        <Pagination
          currentPage={list.page}
          totalItems={list.filtered.length}
          itemsPerPage={list.pageSize}
          itemLabel="báo giá"
          onPageChange={list.setPage}
        />
      </div>

      {form && (
        <BaoGiaFormModal
          key={form.id ?? "new"}
          id={form.id}
          onCancel={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            list.reload();
          }}
        />
      )}
      {viewId && <BaoGiaDrawer id={viewId} onClose={() => setViewId(null)} />}
    </div>
  );
}
