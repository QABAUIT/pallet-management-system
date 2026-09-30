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