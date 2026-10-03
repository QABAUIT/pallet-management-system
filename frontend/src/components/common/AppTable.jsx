import "../layout/style.css";

/**
 * AppTable - bảng dùng chung cho mọi trang, số cột/nội dung cột hoàn toàn
 * do "columns" quyết định, KHÔNG sửa file này khi đổi cột giữa các trang.
 *
 * Props:
 * - columns: [{ key, header, align?, width?, render? }]
 *     key:    tên field trong 1 dòng data (vd "customer")
 *     header: chữ hiện ở dòng tiêu đề (vd "Khách hàng")
 *     align:  "left" | "right" | "center" (mặc định "left")
 *     width:  vd "120px" (optional, ép độ rộng cột)
 *     render: (row) => ReactNode - tự vẽ ô này (badge, nút, format số...),
 *             không truyền thì hiện thẳng row[key]
 * - data: mảng dữ liệu đã phân trang (dùng chung với Pagination)
 * - rowKey: (row) => string|number - lấy key duy nhất cho từng dòng (mặc định row.id)
 * - onRowClick: (row) => void - optional, click cả dòng (vd mở trang chi tiết)
 * - emptyText: chữ hiện khi data rỗng
 */
export default function AppTable({
  columns,
  data,
  rowKey = (row) => row.id,
  onRowClick,
  emptyText = "Không có dữ liệu",
}) {
  return (
    <div className="tbl-wrap">
      <table className="tbl">
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                style={{ width: col.width, textAlign: col.align || "left" }}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 && (
            <tr>
              <td colSpan={columns.length} className="tbl-empty">
                {emptyText}
              </td>
            </tr>
          )}

          {data.map((row) => (
            <tr
              key={rowKey(row)}
              className={onRowClick ? "clickable" : undefined}
              onClick={() => onRowClick?.(row)}
            >
              {columns.map((col) => (
                <td key={col.key} style={{ textAlign: col.align || "left" }}>
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}