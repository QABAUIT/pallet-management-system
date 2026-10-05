import { useState, useEffect, useMemo } from 'react';
import { Form, message } from 'antd';
import { matHangApi } from '../api/matHangApi';
import { mapMatHangToPallet } from '../utils/palletMapper';

export function usePallets(itemsPerPage = 8) {
  const [pallets, setPallets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState("");
  const [filter, setFilter] = useState({ loaiMatHang: [], chatLieu: [] });
  const [filterTab, setFilterTab] = useState("all");
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedPallet, setSelectedPallet] = useState(null);

  // Modal State
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [modalMode, setModalMode] = useState('add');
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();

  const fetchPallets = async () => {
    setLoading(true);
    try {
      const res = await matHangApi.danhSach();
      if (res.data) {
        setPallets(res.data.map(mapMatHangToPallet));
      }
    } catch (err) {
      console.error("Fetch pallets error", err);
      message.error("Lỗi khi tải danh sách pallet");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPallets();
  }, []);

  const handleAdd = () => {
    setModalMode('add');
    setEditingId(null);
    form.resetFields();
    form.setFieldsValue({ 
      loaiMatHang: 'pallet', 
      chatLieu: 'go',
      soLuongBanDau: null,
      khoId: 1
    });
    setIsModalVisible(true);
  };

  const handleEdit = (pallet) => {
    setModalMode('edit');
    setEditingId(pallet.id);
    form.setFieldsValue({
      maMatHang: pallet.code !== 'PL-000' ? pallet.code : '',
      tenMatHang: pallet.name,
      chatLieu: pallet.rawChatLieu || (pallet.material === 'Gỗ' ? 'go' : pallet.material === 'Nhựa' ? 'nhua' : 'sat'),
      loaiMatHang: pallet.rawLoaiMatHang || (pallet.isCustom ? 'linh_kien' : 'pallet'),
      donGiaBan: pallet.price,
      moTa: pallet.desc,
      tieuChuan: pallet.specs?.sizeNote || '',
      hinhAnh: pallet.rawHinhAnh || '',
      // Parse numeric from specs? A bit hacky but works for demo
      kichThuocDai: parseInt(pallet.specs.size.split(' x ')[0]) || 0,
      kichThuocRong: parseInt(pallet.specs.size.split(' x ')[1]) || 0,
      kichThuocCao: parseInt(pallet.specs.size.split(' x ')[2]) || 0,
      taiTrongTinh: parseInt(pallet.specs.staticLoad) || 0,
      taiTrongDong: parseInt(pallet.specs.dynamicLoad) || 0,
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await matHangApi.ngungKinhDoanh(id);
      message.success("Đã ngừng kinh doanh pallet này");
      fetchPallets();
      if (selectedPallet?.id === id) setSelectedPallet(null);
    } catch (error) {
      console.error(error);
      message.error("Lỗi khi xoá/ngừng kinh doanh pallet");
    }
  };

  const handleModalSubmit = async (values) => {
    try {
      if (modalMode === 'add') {
        await matHangApi.taoMoi({ ...values, trangThai: 'dang_kinh_doanh' });
        message.success("Thêm pallet thành công");
      } else {
        await matHangApi.capNhat(editingId, values);
        message.success("Cập nhật pallet thành công");
      }
      setIsModalVisible(false);
      fetchPallets();
      if (selectedPallet && selectedPallet.id === editingId) {
        setSelectedPallet(null); // Clear selected to refresh drawer
      }
    } catch (error) {
      console.error(error);
      message.error("Có lỗi xảy ra, vui lòng thử lại");
    }
  };

  const filteredPallets = useMemo(() => {
    return pallets.filter(p => {
      // 1. Text Search
      const matchSearch = p.name.toLowerCase().includes(searchText.toLowerCase()) || 
                          (p.code || '').toLowerCase().includes(searchText.toLowerCase());
      
      // 2. Tab Filter (Radio)
      let matchTab = true;
      if (filterTab === 'standard') matchTab = (p.code || '').includes('TC') || p.name.toLowerCase().includes('tiêu chuẩn');
      else if (filterTab === 'euro') matchTab = (p.code || '').includes('EPAL') || p.name.toLowerCase().includes('euro');
      else if (filterTab === 'heavy') matchTab = (p.code || '').includes('TN') || p.name.toLowerCase().includes('nặng');
      else if (filterTab === 'plastic') matchTab = p.material === 'Nhựa' || (p.code || '').includes('NH');
      
      // 3. Checkbox Filter (loaiMatHang & chatLieu) mapped from DB
      let matchLoai = true;
      if (filter.loaiMatHang && filter.loaiMatHang.length > 0) {
        matchLoai = filter.loaiMatHang.includes(p.rawLoaiMatHang);
      }

      let matchChatLieu = true;
      if (filter.chatLieu && filter.chatLieu.length > 0) {
        matchChatLieu = filter.chatLieu.includes(p.rawChatLieu);
      }

      return matchSearch && matchTab && matchLoai && matchChatLieu;
    });
  }, [pallets, searchText, filterTab, filter]);

  const paginatedPallets = filteredPallets.slice(
    (currentPage - 1) * itemsPerPage, 
    currentPage * itemsPerPage
  );

  useEffect(() => {
    setCurrentPage(1);
  }, [searchText, filter, filterTab]);

  return {
    loading,
    searchText,
    setSearchText,
    filter,
    setFilter,
    filterTab,
    setFilterTab,
    currentPage,
    setCurrentPage,
    paginatedPallets,
    totalItems: filteredPallets.length,
    selectedPallet,
    setSelectedPallet,
    // Modal returns
    isModalVisible,
    setIsModalVisible,
    modalMode,
    form,
    handleAdd,
    handleEdit,
    handleDelete,
    handleModalSubmit
  };
}
