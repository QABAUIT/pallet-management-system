import React from 'react';
import { Card, Typography, Flex, Badge } from 'antd';

const { Text, Title } = Typography;

export default function PalletCard({ pallet, isSelected, onClick }) {
  return (
    <Card 
      hoverable
      onClick={onClick}
      styles={{ 
        body: { padding: 16 }, 
        cover: { padding: 8, background: '#f1f5f9', position: 'relative' } 
      }}
      style={{ 
        borderRadius: 12, 
        border: isSelected ? '2px solid #1677ff' : '1px solid #e2e8f0',
        position: 'relative'
      }}
      cover={
        <div style={{ height: 180, display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: 8, overflow: 'hidden' }}>
          {pallet.isCustom ? (
            <div style={{ background: '#e2e8f0', width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Text type="secondary">Tùy chỉnh</Text>
            </div>
          ) : (
            <img 
              src={pallet.image} 
              alt={pallet.name} 
              style={{ width: '100%', height: '100%', objectFit: 'contain' }} 
              onError={(e) => {
                e.target.onerror = null; 
                e.target.src = 'https://placehold.co/300x200?text=No+Image';
              }}
            />
          )}
          <div style={{ position: 'absolute', top: 16, right: 16, background: 'white', padding: '4px 12px', borderRadius: 16, fontWeight: 600, fontSize: 13, boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            {pallet.code}
          </div>
        </div>
      }
    >
      {isSelected && (
        <div style={{ position: 'absolute', top: -10, left: '50%', transform: 'translateX(-50%)', background: '#1e3a8a', color: 'white', fontSize: 11, fontWeight: 600, padding: '4px 12px', borderRadius: 16, zIndex: 10 }}>
          ĐANG CHỌN
        </div>
      )}
      
      <Flex vertical gap="small">
        <Flex justify="space-between" align="flex-start">
          <Title level={5} style={{ margin: 0, fontSize: 16 }}>{pallet.name}</Title>
          <Text strong style={{ fontSize: 16 }}>{pallet.price > 0 ? `${pallet.price.toLocaleString()}đ` : 'Liên hệ'}</Text>
        </Flex>
        <Text type="secondary" style={{ fontFamily: 'monospace' }}>{pallet.dimensions}</Text>
        
        <Flex justify="space-between" align="center" style={{ marginTop: 8 }}>
          <Badge status={pallet.stock > 0 ? 'success' : 'warning'} text={pallet.status} />
        </Flex>
      </Flex>
    </Card>
  );
}
