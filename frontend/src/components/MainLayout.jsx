import React, { useMemo } from 'react'
import { Layout, Menu, Avatar, Dropdown, Typography } from 'antd'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import {
  DollarOutlined,
  AppstoreOutlined,
  TeamOutlined,
  ContactsOutlined,
  InboxOutlined,
  ShopOutlined,
  AccountBookOutlined,
  BankOutlined,
  UserOutlined,
  LogoutOutlined,
} from '@ant-design/icons'
import { useAuth } from '../store/AuthContext'

const { Header, Content, Sider } = Layout
const { Text } = Typography

// Đúng theo cấu trúc sidebar trong Figma (PalletManagementUI):
// Quản lí hóa đơn / Quản lí pallet / Quản lí nhân viên / Quản lí khách hàng /
// Quản lí kho / Quản lí nhà cung cấp / Quản lí tài chính / Thông tin chi tiết
const menuItems = [
  {
    key: '/hoa-don',
    icon: <DollarOutlined />,
    label: 'Quản lí hóa đơn',
    children: [
      { key: '/hoa-don/lich-su', label: 'Lịch sử giao dịch' },
      { key: '/hoa-don/tao-moi', label: 'Thanh toán & Tạo hóa đơn' },
    ],
  },
  {
    key: '/pallet',
    icon: <AppstoreOutlined />,
    label: 'Quản lí pallet',
    children: [
      { key: '/pallet', label: 'Danh sách pallet' },
      { key: '/pallet/them-moi', label: 'Thêm pallet mới' },
    ],
  },
  { key: '/nhan-vien', icon: <TeamOutlined />, label: 'Quản lí nhân viên' },
  { key: '/khach-hang', icon: <ContactsOutlined />, label: 'Quản lí khách hàng' },
  {
    key: '/kho',
    icon: <InboxOutlined />,
    label: 'Quản lí kho',
    children: [
      { key: '/kho/lich-su-nhap', label: 'Lịch sử nhập kho' },
      { key: '/kho/tao-phieu-nhap', label: 'Tạo phiếu nhập kho' },
      { key: '/kho/lich-su-xuat', label: 'Lịch sử xuất kho' },
      { key: '/kho/kiem-ke', label: 'Kiểm kê' },
      { key: '/kho/bao-cao-ton', label: 'Báo cáo tồn kho' },
    ],
  },
  { key: '/nha-cung-cap', icon: <ShopOutlined />, label: 'Quản lí nhà cung cấp' },
  {
    key: '/tai-chinh',
    icon: <AccountBookOutlined />,
    label: 'Quản lí tài chính',
    children: [
      { key: '/tai-chinh/doanh-thu', label: 'Doanh thu' },
      { key: '/tai-chinh/thue', label: 'Thuế' },
      { key: '/tai-chinh/loi-nhuan', label: 'Lợi nhuận' },
    ],
  },
  {
    key: '/thong-tin',
    icon: <BankOutlined />,
    label: 'Thông tin chi tiết',
    children: [
      { key: '/thong-tin/cong-ty', label: 'Thông tin doanh nghiệp' },
      { key: '/thong-tin/nhan-vien', label: 'Thông tin nhân viên' },
    ],
  },
]

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()

  const openKeys = useMemo(() => {
    const top = menuItems.find((m) => location.pathname.startsWith(m.key) && m.children)
    return top ? [top.key] : []
  }, [location.pathname])

  const userMenu = {
    items: [
      { key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất' },
    ],
    onClick: async ({ key }) => {
      if (key === 'logout') {
        await logout()
        navigate('/login')
      }
    },
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible width={250} theme="dark">
        <div
          style={{
            height: 48,
            margin: 16,
            color: '#fff',
            textAlign: 'center',
            lineHeight: '48px',
            fontWeight: 'bold',
            fontSize: 15,
            letterSpacing: 0.5,
          }}
        >
          HOÀNG PHÁT PALLET
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={openKeys}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            padding: '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
          }}
        >
          <Text strong style={{ fontSize: 16 }}>
            Hệ Thống Quản Lý Pallet
          </Text>
          <Dropdown menu={userMenu} placement="bottomRight">
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
              <Avatar icon={<UserOutlined />} />
              <span>{user?.hoTen}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ margin: '16px', padding: 24, background: '#fff', minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
