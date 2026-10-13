import { Pie } from "react-chartjs-2";
import "./chartSetup";
import { PALETTE } from "../../utils/financeFormat";
import { formatMoney } from "../../utils/receiptFormat";

export default function PalletPieChart({ data }) {
  if (data.length === 0) return <div className="rc-empty">Chưa có doanh thu trong kỳ này.</div>;

  const chartData = {
    labels: data.map((d) => d.ten),
    datasets: [{ data: data.map((d) => Number(d.soTien)), backgroundColor: PALETTE, borderWidth: 0 }],
  };
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { callbacks: { label: (c) => `${c.label}: ${formatMoney(c.parsed)} đ` } },
    },
  };

  return (
    <div>
      <div style={{ height: 180 }}>
        <Pie data={chartData} options={options} />
      </div>
      <div className="fn-legend">
        {data.map((d, i) => (
          <div className="fn-legend-row" key={d.ten}>
            <span className="fn-legend-dot" style={{ background: PALETTE[i % PALETTE.length] }} />
            <span>{d.ten}</span>
            <b>{d.phanTram}%</b>
            <small>({(Number(d.soTien) / 1e6).toFixed(1)}M)</small>
          </div>
        ))}
      </div>
    </div>
  );
}