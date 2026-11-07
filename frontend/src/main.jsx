import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import App from './App.jsx'
import { AuthProvider } from './store/AuthContext.jsx'
import { NotificationProvider } from './store/NotificationContext.jsx'
import { ThemeProvider, useTheme } from './store/ThemeContext.jsx'
import { getAntdTheme } from './config/antdTheme.js'
import './index.css'
import './components/layout/style.css'

// Đọc chế độ sáng/tối hiện tại rồi cấu hình antd tương ứng.
function ThemedApp() {
  const { theme } = useTheme()

  return (
    <ConfigProvider theme={getAntdTheme(theme)}>
      <BrowserRouter>
        <AuthProvider>
          <NotificationProvider>
            <App />
          </NotificationProvider>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  )
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ThemeProvider>
      <ThemedApp />
    </ThemeProvider>
  </React.StrictMode>,
)
