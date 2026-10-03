import React from 'react';
import { Card, Typography, Row, Col, Tag } from 'antd';
import { Users, UserCog, Database, Shield } from 'lucide-react';

const { Title, Text } = Typography;

export default function EmployeeStats({ data = [] }) {
  const total = data.length;

  const salesCount = data.filter(nv => nv.tenVaiTro === 'Nhân viên bán hàng').length;
  const khoCount = data.filter(nv => nv.tenVaiTro === 'Nhân viên kho').length;
  const adminCount = data.filter(nv => nv.tenVaiTro === 'Giám đốc' || nv.tenVaiTro === 'Phó giám đốc').length;

  const getPercent = (count) => total === 0 ? 0 : ((count / total) * 100).toFixed(1);

  return (
    <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
      <Col span={6}>
        <Card bodyStyle={{ padding: '20px' }} style={{ borderRadius: '8px', borderTop: '4px solid #1890ff' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <Text type="secondary" style={{ fontSize: '12px', fontWeight: 600 }}>TỔNG NHÂN SỰ</Text>
              <Title level={2} style={{ margin: '8px 0', fontSize: '32px' }}>{total}</Title>
              <Text style={{ color: '#52c41a', fontWeight: 500 }}>↑+2</Text> <Text type="secondary" style={{ fontSize: '13px' }}>so với tháng trước</Text>
            </div>
            <div style={{ backgroundColor: '#e6f7ff', padding: '8px', borderRadius: '8px' }}>
              <Users size={24} color="#1890ff" />
            </div>
          </div>
          <div style={{ marginTop: '16px' }}>
            <Tag color="blue" style={{ borderRadius: '16px', border: 'none', background: '#e6f7ff', color: '#1890ff' }}>Toàn bộ chi nhánh</Tag>
          </div>
        </Card>
      </Col>

      <Col span={6}>
        <Card bodyStyle={{ padding: '20px' }} style={{ borderRadius: '8px', borderTop: '4px solid #52c41a' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <Text type="secondary" style={{ fontSize: '12px', fontWeight: 600 }}>BÁN HÀNG (SALES/B2B)</Text>
              <Title level={2} style={{ margin: '8px 0', fontSize: '32px' }}>{salesCount}</Title>
              <Text type="secondary" style={{ fontSize: '13px' }}>100% đạt chỉ tiêu hợp đồng</Text>
            </div>
            <div style={{ backgroundColor: '#f6ffed', padding: '8px', borderRadius: '8px' }}>
              <UserCog size={24} color="#52c41a" />
            </div>
          </div>
          <div style={{ marginTop: '16px' }}>
            <Tag color="green" style={{ borderRadius: '16px', border: 'none', background: '#f6ffed', color: '#52c41a' }}>{getPercent(salesCount)}% tổng số</Tag>
          </div>
        </Card>
      </Col>

      <Col span={6}>
        <Card bodyStyle={{ padding: '20px' }} style={{ borderRadius: '8px', borderTop: '4px solid #fa8c16' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <Text type="secondary" style={{ fontSize: '12px', fontWeight: 600 }}>QUẢN LÍ KHO & KỸ THUẬT</Text>
              <Title level={2} style={{ margin: '8px 0', fontSize: '32px' }}>{khoCount}</Title>
              <Text type="secondary" style={{ fontSize: '13px' }}>2 ca trực (Sáng - Đêm)</Text>
            </div>
            <div style={{ backgroundColor: '#fff7e6', padding: '8px', borderRadius: '8px' }}>
              <Database size={24} color="#fa8c16" />
            </div>
          </div>
          <div style={{ marginTop: '16px' }}>
            <Tag color="orange" style={{ borderRadius: '16px', border: 'none', background: '#fff7e6', color: '#fa8c16' }}>{getPercent(khoCount)}% tổng số</Tag>
          </div>
        </Card>
      </Col>

      <Col span={6}>
        <Card bodyStyle={{ padding: '20px' }} style={{ borderRadius: '8px', borderTop: '4px solid #722ed1' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <Text type="secondary" style={{ fontSize: '12px', fontWeight: 600 }}>QUẢN TRỊ VIÊN ADMIN</Text>
              <Title level={2} style={{ margin: '8px 0', fontSize: '32px' }}>{adminCount}</Title>
              <Text type="secondary" style={{ fontSize: '13px' }}>Bảo mật RFID 2 lớp</Text>
            </div>
            <div style={{ backgroundColor: '#f9f0ff', padding: '8px', borderRadius: '8px' }}>
              <Shield size={24} color="#722ed1" />
            </div>
          </div>
          <div style={{ marginTop: '16px' }}>
            <Tag color="purple" style={{ borderRadius: '16px', border: 'none', background: '#f9f0ff', color: '#722ed1' }}>Phân quyền gốc</Tag>
          </div>
        </Card>
      </Col>
    </Row>
  );
}
