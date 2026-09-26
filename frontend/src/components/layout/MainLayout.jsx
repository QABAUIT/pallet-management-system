import React from 'react';
import { Layout, Menu } from 'antd';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import {
  TeamOutlined,
  ShopOutlined,
  InboxOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  SettingOutlined,
} from '@ant-design/icons';

const { Header, Content, Sider } = Layout;

// Menu chia theo 5 nhóm màn hình - khớp với cách chia người phụ trách.
// Thêm màn hình mới: thêm 1 dòng vào đúng nhóm bên dưới VÀ 1 <Route> trong App.jsx.
const menuItems = [
  {
    key: 'grp-nhansu',
    icon: <TeamOutlined />,
    label: 'Hệ thống & Nhân sự',
    children: [
      { key: '/nhan-vien', label: 'Nhân viên' },
      { key: '/vai-tro', label: 'Vai trò & Phân quyền' },
      { key: '/thong-bao', label: 'Thông báo' },
      { key: '/phe-duyet', label: 'Phê duyệt' },
      { key: '/nhat-ky-he-thong', label: 'Nhật ký hệ thống' },
      { key: '/cau-hinh-he-thong', label: 'Cấu hình hệ thống' },
    ],
  },
  {
    key: 'grp-doitac',
    icon: <ShopOutlined />,
    label: 'Đối tác & Mặt hàng',
    children: [
      { key: '/khach-hang', label: 'Khách hàng' },
      { key: '/nha-cung-cap', label: 'Nhà cung cấp' },
      { key: '/mat-hang', label: 'Mặt hàng' },
      { key: '/bang-gia', label: 'Bảng giá' },
    ],
  },
  {
    key: 'grp-khovan',
    icon: <InboxOutlined />,
    label: 'Kho vận',
    children: [
      { key: '/kho', label: 'Kho & Vị trí' },
      { key: '/ton-kho', label: 'Tồn kho' },
      { key: '/lo-hang', label: 'Lô hàng' },
      { key: '/phieu-kho', label: 'Phiếu kho' },
      { key: '/kiem-ke', label: 'Kiểm kê' },
      { key: '/sua-chua-pallet', label: 'Sửa chữa Pallet' },
      { key: '/phuong-tien', label: 'Phương tiện' },
    ],
  },
  {
    key: 'grp-kinhdoanh',
    icon: <ShoppingCartOutlined />,
    label: 'Kinh doanh',
    children: [
      { key: '/bao-gia', label: 'Báo giá' },
      { key: '/hop-dong', label: 'Hợp đồng' },
      { key: '/hoa-don', label: 'Hoá đơn' },
      { key: '/thanh-toan', label: 'Thanh toán' },
    ],
  },
  {
    key: 'grp-taichinh',
    icon: <DollarOutlined />,
    label: 'Tài chính',
    children: [
      { key: '/chi-phi-van-hanh', label: 'Chi phí vận hành' },
      { key: '/muc-tieu-doanh-thu', label: 'Mục tiêu doanh thu' },
    ],
  },
];

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleMenuClick = ({ key }) => {
    // Chỉ các item lá (key bắt đầu bằng '/') mới là route thật;
    // key nhóm (vd 'grp-nhansu') chỉ để mở/đóng submenu, không điều hướng.
    if (key.startsWith('/')) {
      navigate(key);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible width={260}>
        <div
          style={{
            height: 32,
            margin: 16,
            background: 'rgba(255, 255, 255, 0.2)',
            color: '#fff',
            textAlign: 'center',
            lineHeight: '32px',
            fontWeight: 'bold',
          }}
        >
          PALLETTRACK PRO
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={menuItems.map((g) => g.key)}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            padding: '0 24px',
            fontWeight: 'bold',
            fontSize: '18px',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <SettingOutlined style={{ marginRight: 8 }} />
          Hệ Thống Quản Lý Pallet
        </Header>
        <Content style={{ margin: '16px', padding: 24, background: '#fff', minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
