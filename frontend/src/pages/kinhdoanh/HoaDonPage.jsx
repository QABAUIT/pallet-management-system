import React from 'react';
import PageHeader from '../../components/common/PageHeader';

/**
 * STUB - chưa code. Copy pattern từ src/pages/doitac/KhachHangPage.jsx
 * (bảng + tìm kiếm + phân trang + modal thêm/sửa + xoá có xác nhận).
 * Entity/Repository liên quan đã có sẵn: HoaDon, ChiTietHoaDon
 * Việc cần làm:
 *   1. Tạo DTO Request/Response cho module này (đừng trả thẳng Entity).
 *   2. Tạo Service + Controller ở BE theo path REST tương ứng.
 *   3. Tạo service FE: const xxxService = createCrudService('/duong-dan-cua-ban');
 *   4. Copy KhachHangPage.jsx, đổi field/column cho đúng entity trên.
 */
const HoaDonPage = () => {
  return (
    <div>
      <PageHeader title="Hoá đơn" />
      <p>Chưa code - xem hướng dẫn trong comment đầu file.</p>
    </div>
  );
};

export default HoaDonPage;
