import { Bar } from "react-chartjs-2";
import "./chartSetup";
import { formatMoney } from "../../utils/receiptFormat";
import useChartTheme from "../../hooks/useChartTheme";

export default function RevenueBarChart({ data }) {
  const { gridColor, tickColor, revenueBar } = useChartTheme();

  const chartData = {
    labels: data.map((d) => d.nhan),
    datasets: [{
      label: "Doanh thu",
      data: data.map((d) => Number(d.doanhThu)),
      backgroundColor: revenueBar,
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
      y: {
        beginAtZero: true,
        grid: { color: gridColor },
        ticks: { color: tickColor, callback: (v) => `${Math.round(v / 1e6)}M` },
      },
      x: { grid: { display: false }, ticks: { color: tickColor } },
    },
  };
  return (
    <div className="fn-chart-box">
      <Bar data={chartData} options={options} />
    </div>
  );
}
