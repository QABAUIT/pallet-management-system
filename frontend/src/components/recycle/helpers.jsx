import dayjs from "dayjs";
import { Tag } from "antd";
import { resolveStatus } from "../../utils/statusTag";

export const fmtDate = (v) => (v ? dayjs(v).format("DD/MM/YYYY") : "—");

export const LOAI_NGUON = {
  thanh: "Thanh cần xử lý thêm",
  cuc: "Cục cần xử lý thêm",
};

/** Cột "Trạng thái" dùng chung cho các bảng tái chế. */
export const statusColumn = (map) => ({
  key: "trangThai",
  header: "TRẠNG THÁI",
  render: (r) => {
    const st = resolveStatus(map, r.trangThai);
    return <Tag color={st.color}>{st.label}</Tag>;
  },
});

/** Tổng các số trong 1 mảng giá trị (bỏ qua rỗng). */
export const sum = (...vals) => vals.reduce((a, v) => a + (Number(v) || 0), 0);
