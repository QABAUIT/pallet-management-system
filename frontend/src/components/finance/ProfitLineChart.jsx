import { Line } from "react-chartjs-2";
import "./chartSetup";
import { formatMoney } from "../../utils/receiptFormat";

export default function ProfitLineChart({ data }) {
  const chartData = {
    labels: data.map((d) => d.nhan),
    datasets: [
      {
        label: "Doanh thu (Triệu đ)",
        data: data.map((d) => Number(d.doanhThu)),
        borderColor: "#0b7bc1",
        backgroundColor: "rgba(11,123,193,0.15)",
        fill: true, tension: 0.4, pointRadius: 4, pointBackgroundColor: "#0b7bc1",
      },
      {
        label: "Chi phí (Triệu đ)",
        data: data.map((d) => Number(d.chiPhi)),
        borderColor: "#f43f5e",
        backgroundColor: "rgba(244,63,94,0.12)",
        fill: true, tension: 0.4, pointRadius: 4, pointBackgroundColor: "#f43f5e",
      },
    ],
  };
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: "index", intersect: false },
    plugins: {
      legend: { position: "bottom", labels: { usePointStyle: true } },
      tooltip: { callbacks: { label: (c) => `${c.dataset.label.split(" (")[0]}: ${formatMoney(c.parsed.y)} đ` } },
    },
    scales: {
      y: { beginAtZero: true, grid: { color: "#e5e7eb" }, ticks: { callback: (v) => `${Math.round(v / 1e6)}M` } },
      x: { grid: { display: false } },
    },
  };
  return (
    <div className="fn-chart-box">
      <Line data={chartData} options={options} />
    </div>
  );
}