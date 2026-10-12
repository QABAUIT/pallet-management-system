import React from 'react';
import { Drawer, Button, Tag, Card, Row, Col, Flex, Typography, Divider, Badge } from 'antd';
import { EditOutlined, BoxPlotOutlined, ExpandOutlined, CloseOutlined, DeleteOutlined } from '@ant-design/icons';
import { Archive, Ruler, Weight, Scale, MapPin, Building2, Phone } from 'lucide-react';

const { Title, Text, Paragraph } = Typography;

export default function PalletDetailDrawer({ selectedPallet, onClose, onEdit, onDelete }) {
  return (
    <Drawer
      placement="right"
      width={540}
      closable={false}
      onClose={onClose}
      open={!!selectedPallet}
      styles={{ body: { padding: 24, background: '#f8fafc' } }}
    >
      {selectedPallet && (
        <Flex vertical gap="large">
          <Flex justify="space-between" align="center">
            <Flex gap="middle" align="center">
              <div style={{ width: 40, height: 40, background: '#e6f7ff', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                 <Archive size={20} color="#1890ff" />
              </div>
              <Flex vertical>
                <Title level={5} style={{ margin: 0 }}>Chi tiết pallet</Title>
                <Text type="secondary" style={{ fontSize: 13 }}>Mã hệ thống: {selectedPallet.systemCode}</Text>
              </Flex>
            </Flex>
            <Flex gap="small" align="center">
              {selectedPallet.stock > 0 ? (
                <Tag color="success" style={{ borderRadius: 16, padding: '4px 12px', border: 'none' }}>
                  <Badge status="success" /> {selectedPallet.status} ({selectedPallet.stock} cái)
                </Tag>
              ) : (
                <Tag color="warning" style={{ borderRadius: 16, padding: '4px 12px', border: 'none' }}>Không có sẵn</Tag>
              )}
              <Button type="text" icon={<EditOutlined />} onClick={onEdit} />
              <Button type="text" danger icon={<DeleteOutlined />} onClick={onDelete} />
              <Button type="text" icon={<CloseOutlined />} onClick={onClose} />
            </Flex>
          </Flex>

          <div style={{ position: 'relative', borderRadius: 12, overflow: 'hidden', height: 240, border: '1px solid #e2e8f0', background: 'white' }}>
             {selectedPallet.isCustom ? (
                <Flex align="center" justify="center" style={{ width: '100%', height: '100%', background: '#e2e8f0' }}>
                  <Text type="secondary">Hình ảnh tuỳ chỉnh</Text>
                </Flex>
             ) : (
               <>
                <img 
                  src={selectedPallet.image} 
                  alt={selectedPallet.name} 
                  style={{ width: '100%', height: '100%', objectFit: 'contain' }} 
                  onError={(e) => {
                    e.target.onerror = null; 
                    e.target.src = 'https://placehold.co/300x200?text=No+Image';
                  }}
                />
                <Button icon={<ExpandOutlined />} style={{ position: 'absolute', top: 12, right: 12, border: 'none', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }} shape="circle" />
                <Flex gap="small" style={{ position: 'absolute', bottom: 12, left: 12 }}>
                  <Tag color="rgba(0,0,0,0.7)" style={{ border: 'none', margin: 0 }}><BoxPlotOutlined style={{ marginRight: 4 }}/> RFID: {selectedPallet.rfid}</Tag>
                  <Tag color="#d97706" style={{ border: 'none', margin: 0 }}>{selectedPallet.material}</Tag>
                </Flex>
               </>
             )}
          </div>

          <Flex vertical gap="small">
            <Title level={3} style={{ margin: 0 }}>{selectedPallet.name} {selectedPallet.code ? `(${selectedPallet.code})` : ''}</Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>{selectedPallet.desc}</Paragraph>
          </Flex>
          
          <Flex vertical gap="small">
            <Text type="secondary" strong style={{ fontSize: 12, letterSpacing: 0.5 }}>THÔNG SỐ KỸ THUẬT ĐỊNH DANH</Text>
            <Row gutter={[12, 12]}>
              <Col span={12}>
                <Card styles={{ body: { padding: 12 } }} bordered={false} style={{ border: '1px solid #e2e8f0' }}>
                  <Flex align="center" gap="small" style={{ color: '#64748b', fontSize: 13, marginBottom: 8 }}><Ruler size={16} /> Kích thước (DxRxC)</Flex>
                  <Title level={5} style={{ margin: '0 0 4px 0', fontSize: 15 }}>{selectedPallet.specs.size}</Title>
                  <Text style={{ color: '#10b981', fontSize: 12 }}>{selectedPallet.specs.sizeNote}</Text>
                </Card>
              </Col>
              <Col span={12}>
                <Card styles={{ body: { padding: 12 } }} bordered={false} style={{ border: '1px solid #e2e8f0' }}>
                  <Flex align="center" gap="small" style={{ color: '#64748b', fontSize: 13, marginBottom: 8 }}><Weight size={16} /> Tải trọng tĩnh</Flex>
                  <Title level={5} style={{ margin: '0 0 4px 0', fontSize: 15 }}>{selectedPallet.specs.staticLoad}</Title>
                  <Text style={{ color: '#10b981', fontSize: 12 }}>{selectedPallet.specs.staticLoadNote}</Text>
                </Card>
              </Col>
              <Col span={12}>
                <Card styles={{ body: { padding: 12 } }} bordered={false} style={{ border: '1px solid #e2e8f0' }}>
                  <Flex align="center" gap="small" style={{ color: '#64748b', fontSize: 13, marginBottom: 8 }}><Weight size={16} color="#d97706" /> Tải trọng nâng</Flex>
                  <Title level={5} style={{ margin: '0 0 4px 0', fontSize: 15 }}>{selectedPallet.specs.dynamicLoad}</Title>
                  <Text style={{ color: '#10b981', fontSize: 12 }}>{selectedPallet.specs.dynamicLoadNote}</Text>
                </Card>
              </Col>
              <Col span={12}>
                <Card styles={{ body: { padding: 12 } }} bordered={false} style={{ border: '1px solid #e2e8f0' }}>
                  <Flex align="center" gap="small" style={{ color: '#64748b', fontSize: 13, marginBottom: 8 }}><Scale size={16} /> Khối lượng</Flex>
                  <Title level={5} style={{ margin: '0 0 4px 0', fontSize: 15 }}>{selectedPallet.specs.weight}</Title>
                  <Text style={{ color: '#10b981', fontSize: 12 }}>{selectedPallet.specs.weightNote}</Text>
                </Card>
              </Col>
            </Row>
          </Flex>

          <Card 
            title={<Flex align="center" style={{ fontSize: 13, color: '#475569' }}>QUẢN TRỊ KHO & NIÊM YẾT</Flex>} 
            extra={<Text style={{ color: '#3b82f6', fontSize: 12 }}>Cập nhật: 10 phút trước</Text>}
            styles={{ header: { padding: '12px 16px', background: '#f8fafc', borderBottom: '1px solid #e2e8f0', minHeight: 'auto' }, body: { padding: 16 } }}
            style={{ border: '1px solid #e2e8f0', borderRadius: 12, overflow: 'hidden' }}
          >
            <Row>
              <Col span={12}>
                <Text type="secondary" style={{ fontSize: 13 }}>Tồn kho khả dụng:</Text>
                <Title level={4} style={{ margin: '4px 0 0 0' }}>{selectedPallet.stock} chiếc</Title>
              </Col>
              <Col span={12} style={{ textAlign: 'right' }}>
                 <Text type="secondary" style={{ fontSize: 13 }}>Đơn giá niêm yết:</Text>
                 <Title level={4} style={{ margin: '4px 0 0 0', color: '#2563eb' }}>{selectedPallet.price > 0 ? `${selectedPallet.price.toLocaleString()} đ` : 'Liên hệ'}</Title>
              </Col>
            </Row>
            <Divider style={{ margin: '16px 0' }} />
            <Flex justify="space-between" align="center">
              <Flex gap="small" align="center" style={{ color: '#475569', fontSize: 13 }}><MapPin size={14} /> Vị trí: {selectedPallet.location}</Flex>
              <Text strong style={{ color: '#059669', fontSize: 13 }}>✓ EPAL / ISPM 15</Text>
            </Flex>
          </Card>

          <Card 
            title={<Flex align="center" gap="small" style={{ fontSize: 13, color: '#475569' }}><Building2 size={16} /> NHÀ CUNG CẤP & ĐỐI TÁC</Flex>} 
            extra={<Tag color="success" style={{ border: 'none', margin: 0 }}>Đối tác chiến lược</Tag>}
            styles={{ header: { padding: '12px 16px', background: '#f8fafc', borderBottom: '1px solid #e2e8f0', minHeight: 'auto' }, body: { padding: 16 } }}
            style={{ border: '1px solid #e2e8f0', borderRadius: 12, overflow: 'hidden' }}
          >
            <Flex justify="space-between" align="flex-start" style={{ marginBottom: 12 }}>
               <Text strong style={{ fontSize: 14 }}>{selectedPallet.supplier.name}</Text>
               <Text code>{selectedPallet.supplier.code}</Text>
            </Flex>
            <Flex gap="large">
               <Flex align="center" gap="small" type="secondary" style={{ fontSize: 13, color: '#475569' }}><BoxPlotOutlined /> MST: {selectedPallet.supplier.tax}</Flex>
               <Flex align="center" gap="small" type="secondary" style={{ fontSize: 13, color: '#475569' }}><Phone size={14} /> Hotline: {selectedPallet.supplier.phone}</Flex>
            </Flex>
          </Card>
        </Flex>
      )}
    </Drawer>
  );
}
