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
        cover: { padding: 8, background: 'var(--bg-tint)', position: 'relative' } 
      }}
      style={{ 
        borderRadius: 12, 
        border: isSelected ? '1px solid var(--color-blue)' : '1px solid var(--border-light)',
        boxShadow: isSelected ? '0 0 0 2px var(--color-blue-bg)' : 'var(--shadow-sm)',
        position: 'relative'
      }}
      cover={
        <div style={{ height: 180, display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: 8, overflow: 'hidden' }}>
          {pallet.isCustom && !pallet.hasImage ? (
            <div style={{ background: 'var(--border-light)', width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
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
          <div style={{ position: 'absolute', top: 16, right: 16, background: 'var(--bg-card)', padding: '4px 12px', borderRadius: 16, fontWeight: 600, fontSize: 13, boxShadow: 'var(--shadow-sm)' }}>
            {pallet.code}
          </div>
        </div>
      }
    >
      {isSelected && (
        <div style={{ position: 'absolute', top: -10, left: '50%', transform: 'translateX(-50%)', background: 'var(--navy)', color: 'var(--text-inverse)', fontSize: 11, fontWeight: 600, padding: '4px 12px', borderRadius: 16, zIndex: 10 }}>
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
