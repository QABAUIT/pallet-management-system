import { useEffect, useMemo, useState } from "react";
import dayjs from "dayjs";
import { useServerTable } from "./useServerTable";
import { notifyError } from "../utils/notify";
import { downloadBlob } from "../utils/downloadFile";
import { toApiDateTime } from "../utils/format";

const PAGE_SIZE = 8; // khớp <Pagination> dùng chung (mặc định 8 dòng/trang)
const EMPTY_TIME = { preset: null, from: null, to: null }; // preset null = không lọc ngày

// Đổi giá trị của <TimeFilter> thành [từ, đến] (dayjs)
function timeToRange(t) {
  const now = dayjs();
  switch (t?.preset) {
    case "year":
      return [now.startOf("year"), now.endOf("year")];
    case "month":
      return [now.startOf("month"), now.endOf("month")];
    case "quarter": {
      const start = now.startOf("month").month(Math.floor(now.month() / 3) * 3);
      return [start, start.add(2, "month").endOf("month")];
    }
    case "custom":
      return t.from && t.to
        ? [dayjs(t.from).startOf("day"), dayjs(t.to).endOf("day")]
        : [null, null];
    default:
      return [null, null];
  }
}

/**
 * Logic chung cho 2 trang Lịch sử nhập / xuất kho: tìm kiếm (tự gửi sau khi ngừng gõ 0,4s),
 * lọc trạng thái (OtherTypeFilter), lọc thời gian (TimeFilter), phân trang phía server
 * (nối vào <Pagination> dùng chung), xuất Excel theo đúng bộ lọc đang chọn.
 */
export function usePhieuKhoList({ fetchList, exportExcel, fileName }) {
  const [keyword, setKeyword] = useState(""); // chữ đang gõ
  const [searchTerm, setSearchTerm] = useState(""); // chữ đã gửi lên BE
  const [trangThai, setTrangThai] = useState(null);
  const [time, setTime] = useState(EMPTY_TIME);
  const [exporting, setExporting] = useState(false);

  const extraParams = useMemo(() => {
    const [from, to] = timeToRange(time);
    return {
      keyword: searchTerm || undefined,
      trangThai: trangThai || undefined,
      tuNgay: from ? toApiDateTime(from) : undefined,
      denNgay: to ? toApiDateTime(to) : undefined,
    };
  }, [searchTerm, trangThai, time]);

  const table = useServerTable(fetchList, extraParams, PAGE_SIZE);
  const goToPage = (page) =>
    table.handleTableChange({ current: page, pageSize: PAGE_SIZE });

  // Debounce ô tìm kiếm (SearchInput chỉ có onChange, không có Enter)
  useEffect(() => {
    const t = setTimeout(() => {
      const next = keyword.trim();
      if (next !== searchTerm) {
        setSearchTerm(next);
        goToPage(1);
      }
    }, 400);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword]);

  const handleTrangThaiChange = (value) => {
    setTrangThai(value || null);
    goToPage(1);
  };

  // Bấm lại đúng preset đang chọn (Năm nay / Quý này / Tháng này) thì bỏ lọc ngày
  const handleTimeChange = (next) => {
    const isToggleOff = next.preset !== "custom" && next.preset === time.preset;
    setTime(isToggleOff ? EMPTY_TIME : next);
    goToPage(1);
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      const blob = await exportExcel(extraParams);
      downloadBlob(blob, fileName);
    } catch (err) {
      notifyError(err);
    } finally {
      setExporting(false);
    }
  };

  return {
    table,
    keyword,
    setKeyword,
    trangThai,
    time,
    exporting,
    goToPage,
    handleTrangThaiChange,
    handleTimeChange,
    handleExport,
  };
}
