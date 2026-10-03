import { useState } from "react";
import TimeFilter from "../../components/common/TimeFilter";
import AppTable from "../../components/common/AppTable";
import Pagination from "../../components/common/Pagination";
import { matchTime } from "../../utils/matchTime";
import { receipts } from "../../data";
import SearchInput from "../../components/common/SearchInput";


const STATUS_LABEL = { settled: "Tất toán", debt: "Đang nợ", overdue: "Quá hạn" };
const STATUS_TONE = { settled: "green", debt: "amber", overdue: "red" };

const columns = [
  { key: "code", header: "Mã HĐ", width: "110px" },
  { key: "customer", header: "Khách hàng" },
  {
    key: "date",
    header: "Ngày",
    width: "110px",
    render: (row) => new Date(row.date).toLocaleDateString("vi-VN"),
  },
  {
    key: "amount",
    header: "Số tiền",
    align: "right",
    width: "150px",
    render: (row) => row.amount.toLocaleString("vi-VN") + " đ",
  },
  {
    key: "status",
    header: "Trạng thái",
    align: "center",
    width: "120px",
    render: (row) => (
      <span className={`stat-badge tone-${STATUS_TONE[row.status]}`}>
        {STATUS_LABEL[row.status]}
      </span>
    ),
  },
];

export default function TransactionHistory() {
  const [search, setSearch] = useState("");
  const [time, setTime] = useState({ preset: "month", from: null, to: null });
  const [page, setPage] = useState(1);
  const perPage = 8;

  const filteredRows = receipts
    .filter((row) => matchTime(row.date, time))
    .filter((row) => {
      if (!search.trim()) return true;
      const q = search.toLowerCase();
      // tìm theo mã HD hoặc tên khách hàng - đổi field tuỳ trang
      return row.code.toLowerCase().includes(q) || row.customer.toLowerCase().includes(q);
    });

  const pagedRows = filteredRows.slice((page - 1) * perPage, page * perPage);

  return (
    <div>
      <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "center" }}>
        <SearchInput
            value={search}
            onChange={(v) => {
              setSearch(v);
              setPage(1); // gõ tìm kiếm cũng nên reset về trang 1, giống đổi filter
            }}
            placeholder="Tìm mã HD, đối tác, SĐT..."
          />
        <TimeFilter
          value={time}
          onChange={(next) => {
            setTime(next);
            setPage(1);
          }}
        />
      </div>
      <div style={{ marginTop: 16 }}>
        <AppTable columns={columns} data={pagedRows} />
      </div>

      <div style={{ marginTop: 16 }}>
        <Pagination
          currentPage={page}
          totalItems={filteredRows.length}
          itemsPerPage={perPage}
          itemLabel="giao dịch"
          onPageChange={setPage}
        />
      </div>
    </div>
  );
}