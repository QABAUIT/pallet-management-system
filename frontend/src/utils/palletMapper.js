export const mapMatHangToPallet = (item) => {
  const hasDim = item.kichThuocDai != null;
  return {
    id: item.id,
    code: item.maMatHang || 'PL-000',
    systemCode: item.maMatHang || 'PL-SYS',
    name: item.tenMatHang || 'Không tên',
    dimensions: hasDim ? `${item.kichThuocDai} x ${item.kichThuocRong} mm` : 'Kích thước tuỳ chỉnh',
    price: item.donGiaBan || 0,
    status: item.trangThai === 'dang_kinh_doanh' ? 'Còn hàng' : 'Sắp hết hàng',
    stock: item.tongKhaDung || 0,
    image: item.hinhAnh || 'https://images.unsplash.com/photo-1605378280621-08d4b3136894?q=80&w=300&auto=format&fit=crop',
    desc: item.moTa || 'Chưa có mô tả.',
    specs: {
      size: hasDim ? `${item.kichThuocDai} x ${item.kichThuocRong} x ${item.kichThuocCao} mm` : 'Tùy chọn',
      sizeNote: item.tieuChuan || '',
      staticLoad: item.taiTrongTinh ? `${item.taiTrongTinh} kg` : 'Tùy chọn',
      staticLoadNote: item.taiTrongTinh ? 'Chịu lực sàn tối đa' : '',
      dynamicLoad: item.taiTrongDong ? `${item.taiTrongDong} kg` : 'Tùy chọn',
      dynamicLoadNote: item.taiTrongDong ? 'Khi di chuyển nâng hạ' : '',
      weight: 'N/A',
      weightNote: ''
    },
    location: 'Kho tổng',
    supplier: {
      name: item.nccMacDinhTen || 'N/A',
      code: 'N/A',
      tax: 'N/A',
      phone: 'N/A'
    },
    rfid: `RFID-${item.id}`,
    material: item.chatLieu === 'go' ? 'Gỗ' : item.chatLieu === 'nhua' ? 'Nhựa' : item.chatLieu === 'sat' ? 'Sắt' : item.chatLieu || 'N/A',
    isCustom: item.loaiMatHang === 'linh_kien' || !hasDim,
    // Add raw values for exact DB filtering
    rawChatLieu: item.chatLieu,
    rawLoaiMatHang: item.loaiMatHang,
    rawHinhAnh: item.hinhAnh
  };
};
