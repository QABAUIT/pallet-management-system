import React from 'react';
import { Layout, Menu } from 'antd';
import { useNavigate, Outlet } from 'react-router-dom';
import { AppstoreOutlined, ShoppingCartOutlined, InboxOutlined } from '@ant-design/icons';

const { Header, Content, Sider } = Layout;

const MainLayout = () => {
  const navigate = useNavigate();

  // Danh sách các menu tương ứng với màn hình của từng bạn
  const menuItems = [
    { key: '/pallets', icon: <AppstoreOutlined />, label: 'Quản lý Pallet' },
    { key: '/import-export', icon: <InboxOutlined />, label: 'Nhập / Xuất Kho' },
    { key: '/orders', icon: <ShoppingCartOutlined />, label: 'Quản lý Đơn Hàng' },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible width={240}>
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', color: '#fff', textAlign: 'center', lineHeight: '32px', fontWeight: 'bold' }}>
          PALLET MANAGEMENT
        </div>
        <Menu 
          theme="dark" 
          mode="inline" 
          defaultSelectedKeys={['/pallets']}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', fontWeight: 'bold', fontSize: '18px' }}>
          Hệ Thống Quản Lý Pallet
        </Header>
        <Content style={{ margin: '16px', padding: 24, background: '#fff', minHeight: 280 }}>
          {/* Outlet là nơi chứa nội dung trang do từng bạn code */}
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;