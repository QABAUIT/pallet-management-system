import { theme as antdTheme } from 'antd'

// Token antd dùng chung cho cả hai chế độ sáng/tối.
const sharedToken = {
  fontFamily:
    "'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif",
  fontSize: 14,
  borderRadius: 8,
  controlHeight: 38,
}

const sharedComponents = {
  Button: {
    fontWeight: 600,
    primaryShadow: 'none',
    defaultShadow: 'none',
    dangerShadow: 'none',
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
}

const lightTheme = {
  algorithm: antdTheme.defaultAlgorithm,
  token: {
    ...sharedToken,
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
    boxShadow: '0 4px 16px rgba(15, 23, 42, 0.08)',
  },
  components: {
    ...sharedComponents,
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
  },
}

const darkTheme = {
  algorithm: antdTheme.darkAlgorithm,
  token: {
    ...sharedToken,
    colorPrimary: '#5b82c7',
    colorInfo: '#7cb3ff',
    colorSuccess: '#5bd68f',
    colorWarning: '#f7b955',
    colorError: '#ff8a8a',
    colorLink: '#9ec5ff',
    colorTextBase: '#f1f5f9',
    colorBgBase: '#0a101c',
    colorBgContainer: '#121a2b',
    colorBgElevated: '#172033',
    colorBgLayout: '#0a101c',
    colorBgSpotlight: '#1a2438',
    colorBorder: '#2b3a56',
    colorBorderSecondary: '#202d45',
    colorSplit: 'rgba(148, 163, 184, 0.18)',
    colorText: '#f1f5f9',
    colorTextSecondary: '#cbd5e1',
    colorTextTertiary: '#9fb0c7',
    colorTextQuaternary: '#8496b1',
    boxShadow: '0 6px 18px rgba(0, 0, 0, 0.45)',
    boxShadowSecondary: '0 20px 44px rgba(0, 0, 0, 0.55)',
  },
  components: {
    ...sharedComponents,
    Input: {
      activeBorderColor: '#f5a623',
      hoverBorderColor: '#5f7393',
      activeShadow: '0 0 0 3px rgba(245, 166, 35, 0.20)',
    },
    Select: {
      optionSelectedBg: '#24314a',
      optionSelectedColor: '#f1f5f9',
    },
    Table: {
      headerBg: '#172033',
      headerColor: '#9fb0c7',
      rowHoverBg: '#1a2438',
      borderColor: '#202d45',
    },
  },
}

export const getAntdTheme = (mode) => (mode === 'dark' ? darkTheme : lightTheme)
