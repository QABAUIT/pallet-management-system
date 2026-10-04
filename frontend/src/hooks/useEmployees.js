import { useState, useEffect } from 'react';
import { Form, message } from 'antd';
import dayjs from 'dayjs';
import { nhanVienService } from '../services/nhanVienService';

export function useEmployees() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  // Modal state
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [modalMode, setModalMode] = useState('add');
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await nhanVienService.getAll();
      const list = response?.data ? response.data : Array.isArray(response) ? response : [];
      setData(list);
    } catch (error) {
      console.error('Lỗi khi tải dữ liệu nhân viên:', error);
      message.error('Không thể tải danh sách nhân viên');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setModalMode('add');
    setEditingId(null);
    form.resetFields();
    setIsModalVisible(true);
  };

  const handleEdit = (record) => {
    setModalMode('edit');
    setEditingId(record.id);
    form.setFieldsValue({
      maNv: record.maNv,
      hoTen: record.hoTen,
      vaiTroId: record.vaiTroId,
      sdt: record.sdt,
      diaChi: record.diaChi,
      cccd: record.cccd || '',
      email: record.email || '',
      tenDangNhap: record.tenDangNhap,
      chucVu: record.chucVu,
      ngayVaoLam: record.ngayVaoLam ? dayjs(record.ngayVaoLam) : null,
      ngaySinh: record.ngaySinh ? dayjs(record.ngaySinh) : null,
      trangThai: record.trangThai || 'dang_lam_viec'
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await nhanVienService.remove(id);
      message.success('Xóa nhân viên thành công');
      fetchData();
    } catch (error) {
      message.error(error.message || 'Lỗi khi xóa nhân viên');
    }
  };

  const handleModalSubmit = async (values) => {
    try {
      const payload = {
        ...values,
        ngayVaoLam: values.ngayVaoLam ? values.ngayVaoLam.format('YYYY-MM-DD') : null,
        ngaySinh: values.ngaySinh ? values.ngaySinh.format('YYYY-MM-DD') : null,
        tenDangNhap: values.tenDangNhap || form.getFieldValue('tenDangNhap') || (values.sdt ? values.sdt : `nv${Math.floor(Math.random() * 100000)}`),
        matKhau: modalMode === 'add' ? 'Password@123' : undefined,
      };

      if (modalMode === 'add') {
        await nhanVienService.create(payload);
        message.success('Thêm nhân viên thành công');
      } else {
        await nhanVienService.update(editingId, payload);
        message.success('Cập nhật nhân viên thành công');
      }

      setIsModalVisible(false);
      fetchData();
    } catch (error) {
      if (error.errorFields) {
        return; // Lỗi validate form
      }
      console.error(error);
      message.error(error.message || 'Có lỗi xảy ra khi lưu nhân viên');
    }
  };

  return {
    data,
    loading,
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
