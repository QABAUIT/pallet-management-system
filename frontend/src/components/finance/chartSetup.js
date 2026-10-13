import {
  ArcElement, BarElement, CategoryScale, Chart as ChartJS, Filler, Legend,
  LineElement, LinearScale, PointElement, Tooltip,
} from "chart.js";

// Đăng ký 1 lần cho mọi biểu đồ tài chính
ChartJS.register(ArcElement, BarElement, CategoryScale, Filler, Legend, LineElement, LinearScale, PointElement, Tooltip);