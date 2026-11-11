import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        // Tách các thư viện lớn thành chunk riêng để trình duyệt cache lâu dài
        // và không phải tải lại toàn bộ app khi chỉ đổi code nghiệp vụ.
        // Vite 8 (Rolldown) yêu cầu manualChunks là hàm.
        manualChunks(id) {
          if (!id.includes('node_modules')) return undefined
          if (id.includes('chart.js') || id.includes('react-chartjs-2')) return 'vendor-charts'
          if (id.includes('antd') || id.includes('@ant-design') || id.includes('rc-')) return 'vendor-antd'
          if (id.includes('react') || id.includes('scheduler')) return 'vendor-react'
          return 'vendor'
        },
      },
    },
  },
})
