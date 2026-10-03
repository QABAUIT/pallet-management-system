import { Search } from "lucide-react";
import "../layout/style.css";

/**
 * SearchInput - ô tìm kiếm dùng chung, đặt trên đầu các trang có bảng dữ liệu.
 * Không tự lọc dữ liệu - chỉ báo chữ gõ ra qua onChange, trang cha tự
 * quyết định lọc theo field nào.
 *
 * Props:
 * - value: chuỗi đang gõ
 * - onChange(nextValue): gọi mỗi khi gõ
 * - placeholder: chữ mờ, đổi theo từng trang (vd "Tìm mã HD, đối tác, SĐT...")
 */
export default function SearchInput({ value, onChange, placeholder = "Tìm kiếm..." }) {
  return (
    <div className="search-box">
      <Search size={16} className="search-icon" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="search-input"
      />
    </div>
  );
}