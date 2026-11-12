import { useTheme } from '../store/ThemeContext'

// Màu trục/lưới/chữ cho Chart.js — đổi theo chế độ sáng/tối.
export default function useChartTheme() {
  const { isDark } = useTheme()

  return {
    isDark,
    gridColor: isDark ? 'rgba(159, 176, 199, 0.16)' : '#e5e7eb',
    tickColor: isDark ? '#9fb0c7' : '#52627a',
    legendColor: isDark ? '#cbd5e1' : '#334155',
    revenueBar: isDark ? '#f5a623' : '#0f2942',
  }
}
