import { Calendar, FileDown } from "lucide-react";
import TimeFilter from "../common/TimeFilter";
import "../layout/style.css";

/** Thanh "Khung thời gian" + nút Xuất file PDF (in trang). children = nút riêng của từng trang. */
export default function FinanceToolbar({ time, onTimeChange, children }) {
  return (
    <div className="fn-toolbar rc-no-print">
      <div className="fn-toolbar-left">
        <span className="fn-toolbar-label">
          <Calendar size={16} aria-hidden /> Khung thời gian:
        </span>
        <TimeFilter value={time} onChange={onTimeChange} />
      </div>
      <div className="fn-toolbar-actions">
        {children}
        <button type="button" className="btn-accent" onClick={() => window.print()}>
          <FileDown size={16} aria-hidden /> Xuất file PDF
        </button>
      </div>
    </div>
  );
}