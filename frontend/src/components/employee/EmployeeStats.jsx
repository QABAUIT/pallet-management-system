import React from 'react';
import StatCard from '../common/StatCard';
import { Users, UserCog, Database, Shield } from 'lucide-react';
import '../layout/style.css';

export default function EmployeeStats({ data = [] }) {
  const total = data.length;
  const salesCount = data.filter(nv => nv.tenVaiTro === 'Nhân viên bán hàng' || nv.tenVaiTro === 'Bán hàng' || nv.tenVaiTro === 'Sales').length;
  const khoCount = data.filter(nv => nv.tenVaiTro === 'Nhân viên kho' || nv.tenVaiTro === 'Quản lí kho').length;
  const adminCount = data.filter(nv => nv.tenVaiTro === 'Giám đốc' || nv.tenVaiTro === 'Phó giám đốc' || nv.tenVaiTro === 'Admin').length;

  const getPercent = (count) => total === 0 ? 0 : ((count / total) * 100).toFixed(1);

  return (
    <div style={{ display: 'flex', gap: '16px', marginBottom: '24px', flexWrap: 'wrap' }}>
      <StatCard 
        label="TỔNG NHÂN SỰ" 
        value={total.toString()} 
        icon={Users} 
        tone="blue" 
        trend={{ direction: 'up', text: '+2' }} 
        caption="so với tháng trước" 
      />
      <StatCard 
        label="BÁN HÀNG (SALES/B2B)" 
        value={salesCount.toString()} 
        icon={UserCog} 
        tone="green" 
        badge={`${getPercent(salesCount)}% tổng số`} 
        caption="100% đạt chỉ tiêu HĐ" 
      />
      <StatCard 
        label="QUẢN LÍ KHO & KỸ THUẬT" 
        value={khoCount.toString()} 
        icon={Database} 
        tone="amber" 
        badge={`${getPercent(khoCount)}% tổng số`} 
        caption="2 ca trực (Sáng - Đêm)" 
      />
      <StatCard 
        label="QUẢN TRỊ VIÊN ADMIN" 
        value={adminCount.toString()} 
        icon={Shield} 
        tone="purple" 
        badge="Phân quyền gốc" 
        caption="Bảo mật RFID 2 lớp" 
      />
    </div>
  );
}
