import { TrendingUp, TrendingDown, Wallet } from "lucide-react";
import PageHeader from "../../components/common/PageHeader";
import StatCard from "../../components/common/StatCard";
import {STATS} from "../../data";

export default function Revenue() {
  return (
    <div>
      <PageHeader
        title="Doanh thu"
        subtitle="Theo dõi doanh thu và dòng tiền thu về theo từng kỳ"
      />

      <div className="stat-row">
        {STATS.map((s) => (
          <StatCard key={s.label} {...s} />
        ))}
      </div>

      {/* TODO: bảng chi tiết doanh thu theo từng giao dịch/kỳ */}
    </div>
  );
}