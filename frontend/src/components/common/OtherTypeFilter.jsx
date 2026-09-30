import { useEffect, useId, useRef, useState } from "react";
import { ListFilter, X } from "lucide-react";
import "../layout/style.css";

// So sánh 2 giá trị lọc (chuỗi hoặc mảng, không phụ thuộc thứ tự)
const isSame = (a, b) => {
  const norm = (v) => (Array.isArray(v) ? [...v].sort() : v);
  return JSON.stringify(norm(a)) === JSON.stringify(norm(b));
};

/**
 * Nút lọc xổ bảng: chọn xong bấm "Áp dụng" mới lọc, "Bỏ chọn tất cả" thì về mặc định.
 *
 * Props:
 * - label: chữ trên nút (vd "Lọc tình trạng")
 * - title: tiêu đề trong bảng (mặc định = label)
 * - groups: [{ key, label?, type: "radio" | "checkbox", options: [{ value, label }] }]
 *     radio    -> value[key] là 1 chuỗi
 *     checkbox -> value[key] là 1 mảng
 * - sortOptions: [{ value, label }] - có thì hiện ô "Sắp xếp theo" (lưu ở value.sort)
 * - value: giá trị đang áp dụng, vd { status: "debt", sort: "newest" }
 * - defaultValue: giá trị khi chưa lọc (dùng cho "Bỏ chọn tất cả" và đếm badge)
 * - onApply(next): gọi khi bấm Áp dụng hoặc Bỏ chọn tất cả
 * - align: "left" | "right" - bảng xổ ra lệch về phía nào của nút
 */
export default function OtherTypeFilter({
  label = "Bộ lọc",
  title,
  groups = [],
  sortOptions = [],
  value,
  defaultValue,
  onApply,
  align = "left",
}) {
  const [open, setOpen] = useState(false);
  const [draft, setDraft] = useState(value); // bản nháp, chỉ ghi ra ngoài khi bấm Áp dụng
  const rootRef = useRef(null);
  const triggerRef = useRef(null);
  const uid = useId();

  // Số nhóm đang khác mặc định -> hiện badge trên nút
  const activeCount = Object.keys(defaultValue).filter(
    (k) => !isSame(value[k], defaultValue[k])
  ).length;

  // Bấm ra ngoài hoặc Esc thì đóng (bản nháp bị bỏ)
  useEffect(() => {
    if (!open) return;
    const onDown = (e) => {
      if (rootRef.current && !rootRef.current.contains(e.target)) setOpen(false);
    };
    const onKey = (e) => {
      if (e.key !== "Escape") return;
      setOpen(false);
      triggerRef.current?.focus();
    };
    document.addEventListener("mousedown", onDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onDown);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const toggleOpen = () => {
    if (!open) setDraft(value); // mở bảng thì nạp lại bản nháp từ giá trị đang áp dụng
    setOpen((o) => !o);
  };

  const setGroup = (key, v) => setDraft((d) => ({ ...d, [key]: v }));

  const toggleCheckbox = (key, v) => {
    const cur = draft[key] ?? [];
    setGroup(key, cur.includes(v) ? cur.filter((x) => x !== v) : [...cur, v]);
  };

  const apply = () => {
    onApply(draft);
    setOpen(false);
  };

  const clearAll = () => {
  const empty = { ...draft };
  groups.forEach((g) => {
    empty[g.key] = g.type === "checkbox" ? [] : null; // không mục nào khớp -> không thẻ nào sáng
  });
  setDraft(empty);
  };

 // Tìm nhãn để hiện lên nút, dựa trên value ĐÃ áp dụng (không phải draft)
  const activeGroups = groups.filter((g) => {
    const v = value[g.key];
    return g.type === "checkbox" ? (v?.length ?? 0) > 0 : v != null;
  });

  const buttonLabel = (() => {
    if (activeGroups.length === 0) return label; // chưa chọn gì -> giữ chữ gốc "Lọc tình trạng"

    const g = activeGroups[0];
    const v = value[g.key];

    if (g.type === "checkbox") {
      if (v.length === 1) {
        return g.options.find((o) => o.value === v[0])?.label ?? label;
      }
      return `${v.length} lựa chọn`; // chọn nhiều checkbox thì hiện số lượng
    }

    return g.options.find((o) => o.value === v)?.label ?? label; // radio: hiện đúng nhãn đã chọn
    })();

  return (
    <div className="ft" ref={rootRef}>
      <button
        ref={triggerRef}
        type="button"
        className={"ft-btn" + (open || activeCount > 0 ? " on" : "")}
        onClick={toggleOpen}
        aria-expanded={open}
        aria-haspopup="true"
      >
        <ListFilter size={16} aria-hidden />
        <span>{buttonLabel}</span>
      </button>

      {open && (
        <div className={"ft-panel " + align} role="group" aria-label={title || label}>
          <div className="ft-head">
            <p className="ft-title">{title || label}</p>
            <button type="button" className="ft-close" onClick={() => setOpen(false)} aria-label="Đóng">
              <X size={16} />
            </button>
          </div>

          <div className="ft-body">
            {groups.map((g) => (
              <div className="ft-group" key={g.key}>
                {g.label && <p className="ft-label">{g.label}</p>}
                {g.options.map((o) => {
                  const isCheckbox = g.type === "checkbox";
                  const checked = isCheckbox
                    ? (draft[g.key] ?? []).includes(o.value)
                    : draft[g.key] === o.value;
                  return (
                    <label key={o.value} className={"ft-opt" + (checked ? " on" : "")}>
                      <input
                        type={isCheckbox ? "checkbox" : "radio"}
                        name={uid + g.key}
                        checked={checked}
                        onChange={() =>
                          isCheckbox ? toggleCheckbox(g.key, o.value) : setGroup(g.key, o.value)
                        }
                      />
                      <span>{o.label}</span>
                    </label>
                  );
                })}
              </div>
            ))}

            {sortOptions.length > 0 && (
              <div className="ft-group sort">
                <p className="ft-label">Sắp xếp theo</p>
                <select
                  className="ft-select"
                  value={draft.sort}
                  onChange={(e) => setGroup("sort", e.target.value)}
                >
                  {sortOptions.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              </div>
            )}
          </div>

          <div className="ft-foot">
            <button type="button" className="ft-apply" onClick={apply}>
              Áp dụng
            </button>
            <button type="button" className="ft-clear" onClick={clearAll}>
              Bỏ chọn tất cả
            </button>
          </div>
        </div>
      )}
    </div>
  );
}