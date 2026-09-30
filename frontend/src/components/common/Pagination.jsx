import { ChevronLeft, ChevronRight } from "lucide-react";
import "../layout/style.css";

/**
 * Pagination - hiển thị "Hiển thị x-y trên tổng số z ..." + danh sách số trang.
 * Mỗi trang cố định 8 dòng (itemsPerPage), tối đa 4 nút số trang, còn lại rút gọn "...".
 *
 * Props:
 * - currentPage: trang hiện tại (bắt đầu từ 1)
 * - totalItems: tổng số dòng dữ liệu (chưa phân trang)
 * - itemLabel: danh từ hiển thị cuối dòng, vd "giao dịch", "khách hàng", "phiếu nhập kho"
 * - itemsPerPage: số dòng/trang (mặc định 8)
 * - onPageChange(page): gọi khi bấm 1 trang khác
 */
export default function Pagination({
  currentPage,
  totalItems,
  itemLabel = "dòng",
  itemsPerPage = 8,
  onPageChange,
}) {
  const totalPages = Math.max(1, Math.ceil(totalItems / itemsPerPage));
  const start = totalItems === 0 ? 0 : (currentPage - 1) * itemsPerPage + 1;
  const end = Math.min(currentPage * itemsPerPage, totalItems);

  const pages = getPageList(currentPage, totalPages, 4);

  const goTo = (page) => {
    if (page < 1 || page > totalPages || page === currentPage) return;
    onPageChange(page);
  };

  return (
    <div className="pg-wrap">
      <p className="pg-info">
        Hiển thị {start}-{end} trên tổng số {totalItems} {itemLabel}
      </p>

      <div className="pg-nav">
        <button
          type="button"
          className="pg-arrow"
          disabled={currentPage === 1}
          onClick={() => goTo(currentPage - 1)}
          aria-label="Trang trước"
        >
          <ChevronLeft size={16} />
        </button>

        {pages.map((p, i) =>
          p === "..." ? (
            <span key={"dots-" + i} className="pg-ellipsis">
              ...
            </span>
          ) : (
            <button
              key={p}
              type="button"
              className={"pg-btn" + (p === currentPage ? " active" : "")}
              onClick={() => goTo(p)}
            >
              {p}
            </button>
          )
        )}

        <button
          type="button"
          className="pg-arrow"
          disabled={currentPage === totalPages}
          onClick={() => goTo(currentPage + 1)}
          aria-label="Trang sau"
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}

// Trả về mảng số trang cần hiện, vd [1,2,3,"...",10]. Luôn giữ đúng
// tối đa maxButtons số thật (không tính dấu "..."), luôn có trang 1 và trang cuối.
function getPageList(current, total, maxButtons) {
  if (total <= maxButtons) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  const set = new Set([1, total]);
  let left = current;
  let right = current;

  while (set.size < maxButtons) {
    if (right < total) {
      set.add(right);
      right++;
    }
    if (set.size < maxButtons && left > 1) {
      set.add(left);
      left--;
    }
  }

  const sorted = [...set].sort((a, b) => a - b);
  const result = [];
  sorted.forEach((p, i) => {
    if (i > 0 && p - sorted[i - 1] > 1) result.push("...");
    result.push(p);
  });
  return result;
}