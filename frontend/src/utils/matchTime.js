/**
 * Kiểm tra 1 ngày có khớp bộ lọc thời gian không.
 * dateInput: chuỗi ngày (vd "2026-09-05") hoặc đối tượng Date
 * time: { preset: "year"|"quarter"|"month"|"custom", from, to }
 */
export function matchTime(dateInput, time) {
  const date = new Date(dateInput);
  const now = new Date();

  if (time.preset === "year") {
    return date.getFullYear() === now.getFullYear();
  }

  if (time.preset === "quarter") {
    const quarterOf = (d) => Math.floor(d.getMonth() / 3);
    return date.getFullYear() === now.getFullYear() && quarterOf(date) === quarterOf(now);
  }

  if (time.preset === "month") {
    return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth();
  }

  if (time.preset === "custom") {
    if (!time.from || !time.to) return true; // chưa chọn đủ 2 đầu ngày -> chưa lọc gì
    const from = new Date(time.from);
    from.setHours(0, 0, 0, 0);
    const to = new Date(time.to);
    to.setHours(23, 59, 59, 999);
    return date >= from && date <= to;
  }

  return true;
}

const pad = (n) => String(n).padStart(2, "0");

/** Date -> "yyyy-MM-dd" theo giờ địa phương (gửi cho backend). */
export const toIsoDate = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

/** Bộ lọc thời gian -> { from, to } (Date). Trả null nếu "tùy chọn" chưa chọn đủ 2 đầu. */
export function getTimeRange(time, now = new Date()) {
  const y = now.getFullYear();
  const m = now.getMonth();
  switch (time.preset) {
    case "year":
      return { from: new Date(y, 0, 1), to: new Date(y, 11, 31) };
    case "quarter": {
      const q = Math.floor(m / 3) * 3;
      return { from: new Date(y, q, 1), to: new Date(y, q + 3, 0) };
    }
    case "month":
      return { from: new Date(y, m, 1), to: new Date(y, m + 1, 0) };
    case "custom":
      return time.from && time.to ? { from: new Date(time.from), to: new Date(time.to) } : null;
    default:
      return null;
  }
}

/** Nhãn kỳ để hiển thị: "T10/2026", "Q4/2026", "Năm 2026" hoặc "dd/MM/yyyy - dd/MM/yyyy". */
export function nhanKy(time, range) {
  if (!range) return "";
  const y = range.from.getFullYear();
  if (time.preset === "month") return `T${range.from.getMonth() + 1}/${y}`;
  if (time.preset === "quarter") return `Q${Math.floor(range.from.getMonth() / 3) + 1}/${y}`;
  if (time.preset === "year") return `Năm ${y}`;
  const f = (d) => d.toLocaleDateString("vi-VN");
  return `${f(range.from)} - ${f(range.to)}`;
}