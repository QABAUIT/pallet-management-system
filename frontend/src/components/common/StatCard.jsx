import "../layout/style.css";

/**
 * StatCard - 1 thẻ thống kê. Dùng nhiều lần bằng cách .map() từ mảng dữ liệu
 * ở trang cha, KHÔNG sửa file này để thêm/bớt thẻ.
 *
 * Props:
 * - label: nhãn nhỏ phía trên (vd "TỔNG THU (KỲ NÀY)")
 * - value: số liệu chính, đã format sẵn (vd "1.250.800.000 đ")
 * - icon: component icon (từ lucide-react), vd icon={TrendingUp}
 * - tone: "blue" | "green" | "red" | "amber" - quyết định màu icon + badge
 * - trend: { direction: "up" | "down", text: "+14.2%" } - optional
 * - caption: chữ nhỏ bên phải badge (vd "so với kỳ trước")
 */
export default function StatCard({
  label,
  value,
  icon: Icon,
  tone = "blue",
  trend,
  badge,   // MỚI: pill chữ thường, dùng khi không có tăng/giảm (không mũi tên)
  caption,
}) {
  return (
    <div className={`stat-card tone-${tone}`}>
      <div className="stat-head">
        <span className="stat-label">{label}</span>
        {Icon && (
          <span className={`stat-icon tone-${tone}`}>
            <Icon size={16} />
          </span>
        )}
      </div>

      <p className="stat-value">{value}</p>

      {(trend || badge || caption) && (
        <div className="stat-foot">
          {trend && (
            <span className={`stat-badge tone-${trend.direction === "up" ? "green" : "red"}`}>
              {trend.direction === "up" ? "↑" : "↓"} {trend.text}
            </span>
          )}
          {!trend && badge && <span className={`stat-badge tone-${tone}`}>{badge}</span>}
          {caption && <span className="stat-caption">{caption}</span>}
        </div>
      )}
    </div>
  );
}