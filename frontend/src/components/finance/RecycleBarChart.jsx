import { Bar } from "react-chartjs-2";
import "./chartSetup";
import { formatMoney } from "../../utils/receiptFormat";
import useChartTheme from "../../hooks/useChartTheme";

/** Biểu đồ cột đơn giản cho báo cáo tái chế. colors: 1 màu hoặc 1 màu mỗi cột. */
export default function RecycleBarChart({ labels, values, colors, unit = "", horizontal = false }) {
  const { gridColor, tickColor } = useChartTheme();

  const chartData = {
    labels,
    datasets: [
      {
        data: values,
        backgroundColor: colors,
        borderRadius: 3,
        maxBarThickness: 48,
      },
    ],
  };
  const valueAxis = horizontal ? "x" : "y";
  const options = {
    indexAxis: horizontal ? "y" : "x",
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { callbacks: { label: (c) => `${formatMoney(c.parsed[valueAxis])} ${unit}`.trim() } },
    },
    scales: {
      [valueAxis]: { beginAtZero: true, grid: { color: gridColor }, ticks: { color: tickColor } },
      [horizontal ? "y" : "x"]: { grid: { display: false }, ticks: { color: tickColor } },
    },
  };
  return (
    <div className="fn-chart-box">
      <Bar data={chartData} options={options} />
    </div>
  );
}
