import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import App from './App.jsx'
import { AuthProvider } from './store/AuthContext.jsx'
import { NotificationProvider } from './store/NotificationContext.jsx'
import './index.css'
import './components/layout/style.css'

// Design tokens dùng chung cho antd — khớp với biến CSS trong index.css
const theme = {
  token: {
    colorPrimary: '#101a33',
    colorInfo: '#1d4ed8',
    colorSuccess: '#15803d',
    colorWarning: '#b45309',
    colorError: '#b91c1c',
    colorLink: '#1d4ed8',
    colorTextBase: '#0f172a',
    colorBgBase: '#ffffff',
    colorBorder: '#d5dde7',
    colorBorderSecondary: '#e8edf3',
    fontFamily:
      "'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif",
    fontSize: 14,
    borderRadius: 8,
    controlHeight: 38,
    boxShadow: '0 4px 16px rgba(15, 23, 42, 0.08)',
  },
  components: {
    Button: {
      fontWeight: 600,
      primaryShadow: 'none',
      defaultShadow: 'none',
      dangerShadow: 'none',
    },
    Input: {
      activeBorderColor: '#101a33',
      hoverBorderColor: '#94a3b8',
      activeShadow: '0 0 0 3px rgba(16, 26, 51, 0.10)',
    },
    Select: {
      optionSelectedBg: '#eef2f8',
      optionSelectedColor: '#101a33',
    },
    Table: {
      headerBg: '#f8fafc',
      headerColor: '#52627a',
      rowHoverBg: '#f8fafc',
      borderColor: '#e8edf3',
    },
    Card: {
      borderRadiusLG: 14,
    },
    Modal: {
      borderRadiusLG: 14,
    },
    Tag: {
      borderRadiusSM: 999,
    },
  },
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ConfigProvider theme={theme}>
      <BrowserRouter>
        <AuthProvider>
          <NotificationProvider>
            <App />
          </NotificationProvider>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  </React.StrictMode>,
)
