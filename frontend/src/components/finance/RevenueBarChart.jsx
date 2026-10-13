import { Bar } from "react-chartjs-2";
import "./chartSetup";
import { formatMoney } from "../../utils/receiptFormat";

export default function RevenueBarChart({ data }) {
  const chartData = {
    labels: data.map((d) => d.nhan),
    datasets: [{
      label: "Doanh thu",
      data: data.map((d) => Number(d.doanhThu)),
      backgroundColor: "#0f2942",
      borderRadius: 3,
      maxBarThickness: 48,
    }],
  };
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { callbacks: { label: (c) => `${formatMoney(c.parsed.y)} đ` } },
    },
    scales: {
      y: { beginAtZero: true, grid: { color: "#e5e7eb" }, ticks: { callback: (v) => `${Math.round(v / 1e6)}M` } },
      x: { grid: { display: false } },
    },
  };
  return (
    <div className="fn-chart-box">
      <Bar data={chartData} options={options} />
    </div>
  );
}