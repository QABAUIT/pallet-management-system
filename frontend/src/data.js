import { TrendingUp, TrendingDown, Wallet } from "lucide-react";

// Dữ liệu giả để test giao diện, sau này thay bằng gọi API thật
export const receipts = [
    { id: 1, code: "HD-0001", customer: "Công ty CP Vận tải ABC", date: "2026-09-05", amount: 12000000, status: "debt" },
    { id: 2, code: "HD-0002", customer: "Công ty TNHH XYZ", date: "2026-08-20", amount: 8500000, status: "settled" },
    { id: 3, code: "HD-0003", customer: "Công ty CP DEF Logistics", date: "2026-07-15", amount: 21000000, status: "overdue" },
    { id: 4, code: "HD-0004", customer: "Công ty TNHH GHI", date: "2026-09-28", amount: 5300000, status: "settled" },
    { id: 5, code: "HD-0005", customer: "Công ty CP JKL", date: "2026-06-10", amount: 17500000, status: "debt" },
    { id: 6, code: "HD-0006", customer: "Công ty TNHH MNO", date: "2026-09-12", amount: 9900000, status: "settled" },
    { id: 7, code: "HD-0007", customer: "Công ty CP PQR", date: "2026-05-02", amount: 14200000, status: "overdue" },
    { id: 8, code: "HD-0008", customer: "Công ty TNHH STU", date: "2026-09-20", amount: 6700000, status: "debt" },
    { id: 9,  code: "HD-0009", customer: "Công ty CP Kho vận VWX",     date: "2026-09-02", amount: 11300000, status: "settled" },
    { id: 10, code: "HD-0010", customer: "Công ty TNHH Thương mại YZA", date: "2026-08-11", amount: 4200000,  status: "debt" },
    { id: 11, code: "HD-0011", customer: "Công ty CP BCD Group",       date: "2026-04-18", amount: 25600000, status: "overdue" },
    { id: 12, code: "HD-0012", customer: "Công ty TNHH EFG",           date: "2026-09-25", amount: 7800000,  status: "settled" },
    { id: 13, code: "HD-0013", customer: "Công ty CP HIJ Logistics",   date: "2026-07-30", amount: 16400000, status: "debt" },
    { id: 14, code: "HD-0014", customer: "Công ty TNHH KLM",           date: "2026-09-08", amount: 9200000,  status: "settled" },
    { id: 15, code: "HD-0015", customer: "Công ty CP NOP Supply",      date: "2026-03-22", amount: 19800000, status: "overdue" },
    { id: 16, code: "HD-0016", customer: "Công ty TNHH QRS",           date: "2026-09-18", amount: 5600000,  status: "debt" },
    { id: 17, code: "HD-0017", customer: "Công ty CP TUV Trading",     date: "2026-06-27", amount: 13100000, status: "settled" },
    { id: 18, code: "HD-0018", customer: "Công ty TNHH WXY",           date: "2026-09-29", amount: 8900000,  status: "debt" },
    { id: 19, code: "HD-0019", customer: "Công ty CP ZAB Industries",  date: "2026-02-14", amount: 22300000, status: "overdue" },
    { id: 20, code: "HD-0020", customer: "Công ty TNHH CDE",           date: "2026-09-01", amount: 6100000,  status: "settled" },
];

export const STATS = [
  {
    label: "Tổng thu (kỳ này)",
    value: "1.250.800.000 đ",
    icon: TrendingUp,
    tone: "green",
    trend: { direction: "up", text: "14.2%" },
    caption: "so với kỳ trước",
  },
  {
    label: "Tổng chi (kỳ này)",
    value: "412.350.000 đ",
    icon: TrendingDown,
    tone: "red",
    trend: { direction: "down", text: "3.5%" },
    caption: "chi phí vận hành kho",
  },
  {
    label: "Số dư ròng đối soát",
    value: "+838.450.000 đ",
    icon: Wallet,
    tone: "blue",
    caption: "Biên độ 67% · Tỷ suất lợi nhuận ròng",
  },
];

